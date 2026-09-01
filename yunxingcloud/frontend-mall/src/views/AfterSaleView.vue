<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getAfterSales, createAfterSale, cancelAfterSale } from '@/api/aftersale'
import { formatPrice, formatRelativeTime } from '@/utils/format'
import { useI18n } from '@/locales'
import JdButton from '@/components/JdButton.vue'
import JdBadge from '@/components/JdBadge.vue'
import JdEmpty from '@/components/JdEmpty.vue'

const { t } = useI18n()

interface AfterSale {
  id: number; orderId: number; orderNo: string; type: string; reason?: string
  refundAmount?: number; evidenceUrls?: string; status: string; remark?: string
  createdAt?: string; updatedAt?: string
}

const tab = ref<'list' | 'apply'>('list')
const list = ref<AfterSale[]>([])
const loading = ref(true)
const loadError = ref(false)
const activeStatus = ref('all')

const form = ref({ orderId: '', type: 'refund', reason: '', refundAmount: '' })
const submitting = ref(false)
const submitMsg = ref('')
const submitSuccess = ref(false)

const canceling = ref<Set<number>>(new Set())

const typeLabels: Record<string, string> = {
  refund: t('afterSale.refundOnly'),
  return: t('afterSale.returnRefund'),
  exchange: t('afterSale.exchange'),
}

const statusBadge: Record<string, { label: string; type: 'orange' | 'green' | 'gray' | 'blue' }> = {
  '0': { label: t('afterSale.statusPending'), type: 'orange' },
  '1': { label: t('afterSale.statusApproved'), type: 'green' },
  '2': { label: t('afterSale.statusRejected'), type: 'gray' },
  '3': { label: t('afterSale.statusRefunding'), type: 'blue' },
  '4': { label: t('afterSale.statusCompleted'), type: 'green' },
}

const statusTabs = [
  { key: 'all', label: t('common.all') },
  { key: '0', label: t('afterSale.statusPending') },
  { key: '1', label: t('afterSale.statusApproved') },
  { key: '3', label: t('afterSale.statusRefunding') },
  { key: '4', label: t('afterSale.statusCompleted') },
]

const filteredList = computed(() => {
  if (activeStatus.value === 'all') return list.value
  return list.value.filter(a => a.status === activeStatus.value)
})

async function loadList() {
  loading.value = true; loadError.value = false
  try {
    const r = await getAfterSales()
    list.value = r.data || []
  } catch { loadError.value = true }
  finally { loading.value = false }
}

async function doSubmit() {
  const oid = form.value.orderId.trim()
  if (!oid) { submitMsg.value = t('afterSale.fillComplete'); submitSuccess.value = false; return }
  submitting.value = true; submitMsg.value = ''; submitSuccess.value = false
  try {
    await createAfterSale({
      orderId: Number(oid),
      type: form.value.type,
      reason: form.value.reason,
      refundAmount: form.value.refundAmount ? Number(form.value.refundAmount) * 100 : undefined,
    })
    submitMsg.value = t('afterSale.submitSuccess'); submitSuccess.value = true
    form.value = { orderId: '', type: 'refund', reason: '', refundAmount: '' }
    tab.value = 'list'; loadList()
  } catch (e: any) {
    submitMsg.value = e.response?.data?.message || t('afterSale.submitFail')
    submitting.value = false; submitSuccess.value = false
  }
}

async function doCancel(id: number) {
  if (canceling.value.has(id)) return
  canceling.value.add(id)
  try { await cancelAfterSale(String(id)); loadList() }
  catch { /* ignore */ }
  finally { canceling.value.delete(id) }
}

onMounted(loadList)
</script>

<template>
  <div class="as-page">
    <div class="as-tabs">
      <button class="as-tab" :class="{ active: tab === 'list' }" @click="tab = 'list'">
        {{ t('afterSale.title') }}
      </button>
      <button class="as-tab" :class="{ active: tab === 'apply' }" @click="tab = 'apply'">
        {{ t('afterSale.newRequest') }}
      </button>
    </div>

    <!-- List tab -->
    <template v-if="tab === 'list'">
      <div v-if="loading" class="as-sk-list">
        <div v-for="i in 3" :key="i" class="as-sk-card" />
      </div>

      <template v-else-if="list.length">
        <div class="as-status-bar">
          <span
            v-for="st in statusTabs" :key="st.key"
            class="as-status-tab"
            :class="{ active: activeStatus === st.key }"
            @click="activeStatus = st.key"
          >{{ st.label }}</span>
        </div>

        <div v-if="filteredList.length" class="as-list">
          <div v-for="a in filteredList" :key="a.id" class="as-card">
            <div class="as-card-header">
              <span class="as-order-no">{{ t('afterSale.orderPrefix') }}: {{ a.orderNo }}</span>
              <JdBadge :type="statusBadge[a.status]?.type || 'gray'">
                {{ statusBadge[a.status]?.label || a.status }}
              </JdBadge>
            </div>
            <div class="as-card-body">
              <div class="as-info-row">
                <span class="as-label">{{ t('afterSale.type') }}</span>
                <span>{{ typeLabels[a.type] || a.type }}</span>
              </div>
              <div v-if="a.reason" class="as-info-row">
                <span class="as-label">{{ t('afterSale.reason') }}</span>
                <span>{{ a.reason }}</span>
              </div>
              <div v-if="a.refundAmount" class="as-info-row">
                <span class="as-label">{{ t('afterSale.refundAmount') }}</span>
                <span class="as-amount">{{ formatPrice(a.refundAmount / 100, 2) }}</span>
              </div>
              <div v-if="a.remark" class="as-info-row">
                <span class="as-label">{{ t('afterSale.remark') || '备注' }}</span>
                <span class="as-remark">{{ a.remark }}</span>
              </div>
            </div>
            <div class="as-card-footer">
              <span class="as-time">{{ formatRelativeTime(a.createdAt || '') }}</span>
              <JdButton
                v-if="a.status === '0'"
                size="sm" type="ghost"
                :disabled="canceling.has(a.id)"
                @click="doCancel(a.id)"
              >{{ canceling.has(a.id) ? '...' : t('afterSale.cancelRequest') }}</JdButton>
            </div>
          </div>
        </div>

        <JdEmpty v-else icon="🔍" :title="t('afterSale.noRecords')" />
      </template>

      <div v-else-if="loadError" class="as-error">
        <JdEmpty icon="🔌" :title="t('afterSale.loadFail')">
          <JdButton @click="loadList">{{ t('common.retry') }}</JdButton>
        </JdEmpty>
      </div>

      <JdEmpty v-else icon="📋" :title="t('afterSale.noRecords')">
        <JdButton @click="tab = 'apply'">{{ t('afterSale.newRequest') }}</JdButton>
      </JdEmpty>
    </template>

    <!-- Apply tab -->
    <template v-else>
      <div class="as-form">
        <div class="as-type-group">
          <label
            v-for="opt in [
              { key: 'refund', icon: '💰', label: t('afterSale.refundOnly'), desc: t('afterSale.refundDesc') },
              { key: 'return', icon: '📦', label: t('afterSale.returnRefund'), desc: t('afterSale.returnDesc') },
              { key: 'exchange', icon: '🔄', label: t('afterSale.exchange'), desc: t('afterSale.exchangeDesc') },
            ]" :key="opt.key"
            class="as-type-card"
            :class="{ active: form.type === opt.key }"
            @click="form.type = opt.key"
          >
            <span class="as-type-icon">{{ opt.icon }}</span>
            <span class="as-type-label">{{ opt.label }}</span>
            <span class="as-type-desc">{{ opt.desc }}</span>
          </label>
        </div>

        <div class="as-field">
          <label class="as-field-label">{{ t('afterSale.orderNo') }}</label>
          <input v-model="form.orderId" type="number" :placeholder="t('afterSale.orderNo')" class="as-input" />
        </div>

        <div class="as-field">
          <label class="as-field-label">{{ t('afterSale.reason') }}</label>
          <textarea v-model="form.reason" :placeholder="t('afterSale.reason')" class="as-textarea" rows="4" />
        </div>

        <div v-if="form.type !== 'exchange'" class="as-field">
          <label class="as-field-label">{{ t('afterSale.amount') }}</label>
          <input v-model="form.refundAmount" type="number" :placeholder="t('afterSale.amount')" class="as-input" step="0.01" />
        </div>

        <JdButton class="as-submit" :loading="submitting" @click="doSubmit">
          {{ submitting ? t('afterSale.submitting') : t('afterSale.submitRequest') }}
        </JdButton>
        <p v-if="submitMsg" class="as-msg" :class="{ success: submitSuccess }">{{ submitMsg }}</p>

        <div class="as-help">
          <p>{{ t('afterSale.needHelp') }}</p>
          <p class="as-help-contact">{{ t('afterSale.supportContact') }}</p>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.as-page { max-width: 800px; margin: 30px auto; }

.as-tabs { display: flex; margin-bottom: var(--space-xl); background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.as-tab { flex: 1; padding: 14px; border: none; background: var(--bg-white); cursor: pointer; font-size: var(--font-md); font-weight: 600; color: var(--text-secondary); transition: all var(--transition-fast); }
.as-tab.active { background: var(--jd-red); color: #fff; }
.as-tab:not(.active):hover { background: var(--bg-hover); }

.as-sk-list { display: flex; flex-direction: column; gap: var(--space-md); }
.as-sk-card { height: 140px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-lg); }

.as-status-bar { display: flex; gap: var(--space-xs); margin-bottom: var(--space-lg); overflow-x: auto; padding-bottom: 4px; -webkit-overflow-scrolling: touch; }
.as-status-tab { padding: 6px 16px; border-radius: var(--radius-round); font-size: var(--font-sm); cursor: pointer; white-space: nowrap; background: var(--bg-white); color: var(--text-secondary); border: 1px solid var(--border); transition: all var(--transition-fast); }
.as-status-tab.active { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }

.as-list { display: flex; flex-direction: column; gap: var(--space-md); }
.as-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); box-shadow: var(--shadow-sm); border-left: 4px solid var(--border); }
.as-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-md); }
.as-order-no { font-weight: 600; font-size: var(--font-md); }
.as-card-body { display: flex; flex-direction: column; gap: var(--space-xs); }
.as-info-row { display: flex; gap: var(--space-sm); font-size: var(--font-sm); }
.as-label { color: var(--text-tertiary); min-width: 70px; flex-shrink: 0; }
.as-amount { color: var(--jd-red); font-weight: 600; }
.as-remark { color: var(--text-secondary); font-style: italic; }
.as-card-footer { display: flex; justify-content: space-between; align-items: center; margin-top: var(--space-md); }
.as-time { font-size: var(--font-xs); color: var(--text-placeholder); }
.as-error { padding: 60px var(--space-xl); }

.as-form { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-sm); }
.as-type-group { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-md); margin-bottom: var(--space-xl); }
.as-type-card { padding: var(--space-lg); border: 2px solid var(--border); border-radius: var(--radius-lg); cursor: pointer; text-align: center; transition: all var(--transition-fast); display: flex; flex-direction: column; align-items: center; gap: 4px; }
.as-type-card:hover { border-color: var(--jd-red-light); }
.as-type-card.active { border-color: var(--jd-red); background: var(--jd-red-light); box-shadow: 0 0 0 3px rgba(228,57,60,.15); }
.as-type-icon { font-size: 28px; }
.as-type-label { font-weight: 600; font-size: var(--font-md); }
.as-type-desc { font-size: var(--font-xs); color: var(--text-tertiary); }
.as-field { margin-bottom: var(--space-lg); }
.as-field-label { display: block; font-weight: 600; margin-bottom: var(--space-xs); font-size: var(--font-sm); color: var(--text-secondary); }
.as-input { width: 100%; padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md); font-size: var(--font-base); box-sizing: border-box; background: var(--bg-white); color: var(--text-primary); outline: none; transition: border-color var(--transition-fast); }
.as-input:focus { border-color: var(--jd-red); box-shadow: 0 0 0 2px var(--jd-red-light); }
.as-textarea { width: 100%; padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md); font-size: var(--font-base); box-sizing: border-box; background: var(--bg-white); color: var(--text-primary); outline: none; resize: vertical; font-family: inherit; transition: border-color var(--transition-fast); }
.as-textarea:focus { border-color: var(--jd-red); box-shadow: 0 0 0 2px var(--jd-red-light); }
.as-submit { width: 100%; margin-top: var(--space-md); }
.as-msg { text-align: center; margin-top: var(--space-md); font-size: var(--font-base); color: var(--jd-red); }
.as-msg.success { color: var(--green); }
.as-help { margin-top: var(--space-xxl); padding-top: var(--space-xl); border-top: 1px solid var(--border-light); text-align: center; font-size: var(--font-sm); color: var(--text-tertiary); }
.as-help-contact { color: var(--jd-red); margin-top: 4px; }


@media (max-width: 768px) {
  .as-page { padding: 0 var(--space-md) 80px; }
  .as-type-group { grid-template-columns: 1fr; }
  .as-form { padding: var(--space-lg); }
  .as-card { padding: var(--space-md); }
}
</style>
