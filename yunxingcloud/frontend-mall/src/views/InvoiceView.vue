<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '@/api/request'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import JdButton from '@/components/JdButton.vue'
import JdBadge from '@/components/JdBadge.vue'
import JdEmpty from '@/components/JdEmpty.vue'

const toast = useToast()
const { t } = useI18n()
const invoices = ref<any[]>([])
const loading = ref(true)
const loadError = ref(false)
const showForm = ref(false)
const submitting = ref(false)
const form = ref({ orderNo: '', type: 'personal', title: '', taxNo: '', email: '' })
const typeFilter = ref<'all' | 'personal' | 'company'>('all')
const statusFilter = ref<'all' | '0' | '1' | '2'>('all')
const detailInv = ref<any>(null)

const filteredInvoices = computed(() => {
  let list = invoices.value
  if (typeFilter.value !== 'all') list = list.filter(i => i.type === typeFilter.value)
  if (statusFilter.value !== 'all') list = list.filter(i => i.status === statusFilter.value)
  return list
})

onMounted(async () => {
  try { const r = await request.get('/invoices'); invoices.value = r.data || []
    } catch { toast.error(t('invoice.loadFail')); loadError.value = true }
  loading.value = false
})

async function submit() {
  if (!form.value.orderNo) { toast.error(t('invoice.fillOrderNo')); return }
  if (form.value.type === 'company' && !form.value.title) { toast.error(t('invoice.fillTitle')); return }
  submitting.value = true
  try { await request.post('/invoices', form.value); toast.success(t('invoice.submitSuccess')); showForm.value = false; load() } catch { toast.error(t('invoice.submitFail')) }
  finally { submitting.value = false }
}

async function load() {
  try { const r = await request.get('/invoices'); invoices.value = r.data || []; loading.value = false; 
    } catch { toast.error(t('invoice.loadFail')) }
}

function copyInvNo(no: string) {
  navigator.clipboard.writeText(no).then(() => toast.success(t('toast.copied'))).catch(() => {})
}

function showDetail(inv: any) { detailInv.value = inv }

const badgeTypeMap: Record<string, 'orange' | 'green' | 'blue'> = {
  '0': 'orange',
  '1': 'green',
  '2': 'blue',
}

const statusMap: Record<string, { label: string }> = {
  '0': { label: t('invoice.statusPending') },
  '1': { label: t('invoice.statusDone') },
  '2': { label: t('invoice.statusSent') },
}
</script>

<template>
  <div class="invoice-page">
    <div class="invoice-header">
      <h2 class="invoice-title">🧾 {{ t('invoice.title') }}</h2>
      <JdButton type="primary" @click="showForm=true">+ {{ t('invoice.newRequest') }}</JdButton>
    </div>

    <div v-if="showForm" class="invoice-form">
      <div class="invoice-form-group">
        <label class="invoice-label">{{ t('invoice.type') }}</label>
        <div class="invoice-type-row">
          <span @click="form.type='personal'"
                class="invoice-type-tag" :class="{ active: form.type === 'personal' }">{{ t('invoice.personal') }}</span>
          <span @click="form.type='company'"
                class="invoice-type-tag" :class="{ active: form.type === 'company' }">{{ t('invoice.company') }}</span>
        </div>
      </div>
      <input v-model="form.orderNo" :placeholder="t('invoice.orderNo')" class="invoice-input" />
      <template v-if="form.type==='company'">
        <input v-model="form.title" :placeholder="t('invoice.companyTitle')" class="invoice-input" />
        <input v-model="form.taxNo" :placeholder="t('invoice.taxNo')" class="invoice-input" />
      </template>
      <input v-model="form.email" :placeholder="t('invoice.email')" type="email" class="invoice-input" />
      <div class="invoice-form-actions">
        <JdButton type="ghost" @click="showForm=false">{{ t('common.cancel') }}</JdButton>
        <JdButton type="primary" :loading="submitting" @click="submit">{{ submitting ? t('invoice.submitting') : t('invoice.submit') }}</JdButton>
      </div>
    </div>

    <!-- Filter bar -->
    <div class="invoice-filters">
      <div class="filter-group">
        <span class="filter-label">{{ t('common.type') }}</span>
        <span class="filter-opt" :class="{ active: typeFilter === 'all' }" @click="typeFilter = 'all'">{{ t('common.all') }}</span>
        <span class="filter-opt" :class="{ active: typeFilter === 'personal' }" @click="typeFilter = 'personal'">{{ t('invoice.personal') }}</span>
        <span class="filter-opt" :class="{ active: typeFilter === 'company' }" @click="typeFilter = 'company'">{{ t('invoice.company') }}</span>
      </div>
      <div class="filter-group">
        <span class="filter-label">{{ t('invoice.statusLabel') || t('common.status') }}</span>
        <span class="filter-opt" :class="{ active: statusFilter === 'all' }" @click="statusFilter = 'all'">{{ t('common.all') }}</span>
        <span class="filter-opt" :class="{ active: statusFilter === '0' }" @click="statusFilter = '0'">{{ t('invoice.statusPending') }}</span>
        <span class="filter-opt" :class="{ active: statusFilter === '1' }" @click="statusFilter = '1'">{{ t('invoice.statusDone') }}</span>
        <span class="filter-opt" :class="{ active: statusFilter === '2' }" @click="statusFilter = '2'">{{ t('invoice.statusSent') }}</span>
      </div>
    </div>

    <div v-if="loading" class="invoice-skeleton">
      <div v-for="i in 2" :key="i" class="skeleton-card">
        <div class="skeleton-line" />
      </div>
    </div>
    <div v-else-if="filteredInvoices.length">
      <div v-for="inv in filteredInvoices" :key="inv.id" class="invoice-card" role="button" tabindex="0" @click="showDetail(inv)" @keydown.enter.prevent="showDetail(inv)" @keydown.space.prevent="showDetail(inv)">
        <div>
          <div class="invoice-card-title">{{ (inv.type==='company' ? t('invoice.company') : t('invoice.personal')) }} · {{ inv.title || t('invoice.personal') }}</div>
          <div class="invoice-card-meta">
            {{ t('invoice.orderPrefix') || t('order.orderNo') }} {{ inv.orderNo }} · {{ inv.createdAt?.substring(0,10) }}
            <span v-if="inv.invoiceNo" class="inv-no" @click.stop="copyInvNo(inv.invoiceNo)">{{ t('invoice.invoiceNo') || 'No.' }}: {{ inv.invoiceNo }} 📋</span>
          </div>
        </div>
        <JdBadge :type="badgeTypeMap[inv.status] || 'gray'">{{ statusMap[inv.status]?.label }}</JdBadge>
      </div>
      <div v-if="filteredInvoices.length < invoices.length" class="filter-notice">
        {{ t('common.items', { '0': String(filteredInvoices.length) }) }} / {{ invoices.length }}
      </div>
    </div>
    <JdEmpty v-else icon="🧾" :title="filteredInvoices.length === 0 && invoices.length > 0 ? '筛选结果为空' : t('invoice.noRecords')" />

    <!-- Detail modal -->
    <div v-if="detailInv" class="inv-modal-overlay" @click.self="detailInv = null">
      <div class="inv-modal">
        <h3 class="inv-modal-title">{{ t('invoice.title') }}</h3>
        <div class="inv-modal-body">
          <div class="inv-detail-row"><span>类型</span><span>{{ detailInv.type === 'company' ? t('invoice.company') : t('invoice.personal') }}</span></div>
          <div class="inv-detail-row"><span>{{ t('invoice.companyTitle') }}</span><span>{{ detailInv.title || '-' }}</span></div>
          <div class="inv-detail-row" v-if="detailInv.taxNo"><span>{{ t('invoice.taxNo') }}</span><span>{{ detailInv.taxNo }}</span></div>
          <div class="inv-detail-row"><span>{{ t('invoice.orderNo') }}</span><span>{{ detailInv.orderNo }}</span></div>
          <div class="inv-detail-row"><span>{{ t('common.status') }}</span><span>{{ statusMap[detailInv.status]?.label }}</span></div>
          <div class="inv-detail-row" v-if="detailInv.invoiceNo"><span>{{ t('invoice.invoiceNo') || 'No.' }}</span><span class="inv-no-link" @click="copyInvNo(detailInv.invoiceNo)">{{ detailInv.invoiceNo }} 📋</span></div>
          <div class="inv-detail-row"><span>{{ t('invoice.applyTime') || t('common.createdAt') }}</span><span>{{ detailInv.createdAt }}</span></div>
          <div class="inv-detail-row" v-if="detailInv.email"><span>{{ t('invoice.email') }}</span><span>{{ detailInv.email }}</span></div>
        </div>
        <JdButton block @click="detailInv = null">关闭</JdButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.invoice-page { max-width: 700px; margin: 0 auto; }
.invoice-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.invoice-title { font-size: var(--font-xl); font-weight: 700; }
.invoice-form { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-md); margin-bottom: var(--space-lg); }
.invoice-form-group { margin-bottom: var(--space-md); }
.invoice-label { font-size: var(--font-base); color: var(--text-secondary); margin-bottom: var(--space-sm); display: block; }
.invoice-type-row { display: flex; gap: var(--space-sm); }
.invoice-type-tag { cursor: pointer; padding: 6px 16px; border-radius: var(--radius-sm); font-size: var(--font-base); border: 1px solid var(--border); background: var(--bg-white); transition: all var(--transition-fast); }
.invoice-type-tag.active { border: 2px solid var(--jd-red); background: var(--jd-red-light); }
.invoice-input { width: 100%; padding: 10px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--font-base); box-sizing: border-box; margin-bottom: 10px; }
.invoice-form-actions { display: flex; gap: var(--space-sm); justify-content: flex-end; }
.invoice-skeleton { display: flex; flex-direction: column; gap: var(--space-md); }
.invoice-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); margin-bottom: 10px; box-shadow: var(--shadow-md); display: flex; justify-content: space-between; align-items: center; }
.invoice-card-title { font-weight: 600; font-size: var(--font-md); }
.invoice-card-meta { color: var(--text-tertiary); font-size: var(--font-sm); }
.skeleton-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); box-shadow: var(--shadow-md); height: 60px; }
.skeleton-line { height: 16px; width: 50%; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-sm); }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

/* Filters */
.invoice-filters { display: flex; flex-wrap: wrap; gap: var(--space-lg); margin-bottom: var(--space-lg); background: var(--bg-white); border-radius: var(--radius-md); padding: var(--space-md) var(--space-lg); box-shadow: var(--shadow-sm); }
.filter-group { display: flex; align-items: center; gap: var(--space-sm); flex-wrap: wrap; }
.filter-label { font-size: var(--font-sm); color: var(--text-tertiary); font-weight: 600; }
.filter-opt {
  padding: 3px 12px; border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-sm);
  color: var(--text-secondary); border: 1px solid var(--border); transition: all var(--transition-fast);
}
.filter-opt.active { color: var(--jd-red); border-color: var(--jd-red); background: var(--jd-red-light); }
.filter-opt:hover:not(.active) { border-color: var(--text-tertiary); }

.inv-no { cursor: pointer; color: var(--jd-red); font-size: var(--font-xs); margin-left: var(--space-md); }
.inv-no:hover { text-decoration: underline; }
.filter-notice { text-align: center; font-size: var(--font-xs); color: var(--text-placeholder); margin-top: var(--space-md); }

.inv-modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,.5); z-index: 500; display: flex; align-items: center; justify-content: center; }
.inv-modal { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); max-width: 440px; width: 90%; box-shadow: var(--shadow-xl); }
.inv-modal-title { font-size: var(--font-lg); font-weight: 700; margin-bottom: var(--space-lg); }
.inv-modal-body { margin-bottom: var(--space-lg); }
.inv-detail-row { display: flex; justify-content: space-between; padding: var(--space-sm) 0; border-bottom: 1px solid var(--border-light); font-size: var(--font-sm); }
.inv-detail-row span:first-child { color: var(--text-tertiary); }
.inv-no-link { color: var(--jd-red); cursor: pointer; }
.inv-no-link:hover { text-decoration: underline; }

@media (max-width: 768px) {
  .invoice-page { padding: 0 var(--space-md) calc(80px + env(safe-area-inset-bottom, 0px)); }
  .invoice-header { flex-direction: column; gap: var(--space-sm); align-items: flex-start; }
  .invoice-form { padding: var(--space-lg); }
  .invoice-type-row { flex-wrap: wrap; }
  .invoice-card { flex-direction: column; align-items: flex-start; gap: var(--space-sm); }
}
</style>
