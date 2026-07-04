<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/api/request'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import JdButton from '@/components/JdButton.vue'
import JdBadge from '@/components/JdBadge.vue'
import JdEmpty from '@/components/JdEmpty.vue'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const records = ref<any[]>([])
const loading = ref(true)
const showForm = ref(false)
const submitting = ref(false)
const form = ref({ orderNo: '', type: 'refund', reason: '', amount: '', description: '' })

const types = [
  { value: 'refund', label: () => t('afterSale.refundOnly'), icon: '💰', desc: () => t('afterSale.refundDesc') },
  { value: 'return', label: () => t('afterSale.returnRefund'), icon: '📦', desc: () => t('afterSale.returnDesc') },
  { value: 'exchange', label: () => t('afterSale.exchange'), icon: '🔄', desc: () => t('afterSale.exchangeDesc') },
]

const typeLabels: Record<string, () => string> = {
  refund: () => t('afterSale.refundOnly'),
  return: () => t('afterSale.returnRefund'),
  exchange: () => t('afterSale.exchange'),
}

const statusMap: Record<string, { label: string }> = {
  '0': { label: t('afterSale.statusPending') },
  '1': { label: t('afterSale.statusApproved') },
  '2': { label: t('afterSale.statusRejected') },
  '3': { label: t('afterSale.statusRefunding') },
  '4': { label: t('afterSale.statusCompleted') },
}

const badgeTypeMap: Record<string, 'orange' | 'green' | 'red' | 'blue'> = {
  '0': 'orange',
  '1': 'green',
  '2': 'red',
  '3': 'blue',
  '4': 'green',
}

onMounted(async () => {
  try { const r = await request.get('/after-sale'); records.value = r.data || [] } catch {}
  finally { loading.value = false }
})

async function submit() {
  if (!form.value.orderNo || !form.value.reason) { toast.error(t('afterSale.fillComplete')); return }
  submitting.value = true
  try { await request.post('/after-sale', form.value); toast.success(t('afterSale.submitSuccess')); showForm.value = false; records.value.unshift({ ...form.value, status: '0', createdAt: new Date().toISOString() }) }
  catch { toast.error(t('afterSale.submitFail')) }
  finally { submitting.value = false }
}
</script>

<template>
  <div class="after-sale-page">
    <div class="after-sale-header">
      <h2 class="after-sale-title">🛡️ {{ t('afterSale.title') }}</h2>
      <JdButton type="primary" @click="showForm=true">+ {{ t('afterSale.newRequest') }}</JdButton>
    </div>

    <div v-if="showForm" class="after-sale-form">
      <div class="after-sale-form-group">
        <label class="after-sale-label">{{ t('afterSale.type') }}</label>
        <div class="after-sale-type-grid">
          <div v-for="tp in types" :key="tp.value" @click="form.type=tp.value"
               class="after-sale-type-item" :class="{ active: form.type === tp.value }">
            <div class="after-sale-type-icon">{{ tp.icon }}</div>
            <div class="after-sale-type-name">{{ tp.label() }}</div>
            <div class="after-sale-type-desc">{{ tp.desc() }}</div>
          </div>
        </div>
      </div>
      <input v-model="form.orderNo" :placeholder="t('afterSale.orderNo')" class="after-sale-input" />
      <input v-model="form.amount" :placeholder="t('afterSale.amount') + ' (¥)'" type="number" class="after-sale-input" />
      <textarea v-model="form.reason" :placeholder="t('afterSale.reason')" class="after-sale-textarea"></textarea>
      <div class="after-sale-form-actions">
        <JdButton type="ghost" @click="showForm=false">{{ t('common.cancel') }}</JdButton>
        <JdButton type="primary" :loading="submitting" @click="submit">{{ submitting ? t('afterSale.submitting') : t('afterSale.submitRequest') }}</JdButton>
      </div>
    </div>

    <div v-if="loading" class="after-sale-skeleton">
      <div v-for="i in 3" :key="i" class="skeleton-card">
        <div class="skeleton-line" />
      </div>
    </div>
    <div v-else-if="records.length">
      <div v-for="r in records" :key="r.id || r.orderNo" class="after-sale-card">
        <div class="after-sale-card-top">
          <div>
            <span class="after-sale-card-order">{{ t('afterSale.orderPrefix') }} {{ r.orderNo }}</span>
            <span class="after-sale-card-type">{{ typeLabels[r.type]?.() || r.type }}</span>
          </div>
          <JdBadge :type="badgeTypeMap[r.status] || 'gray'">{{ statusMap[r.status]?.label }}</JdBadge>
        </div>
        <div class="after-sale-card-reason">{{ r.reason }}</div>
        <div v-if="r.amount" class="after-sale-card-amount">¥{{ (Number(r.amount)/100).toFixed(2) }}</div>
        <div class="after-sale-card-time">{{ r.createdAt?.substring(0,16) }}</div>
      </div>
    </div>
    <JdEmpty v-else icon="🛡️" :title="t('afterSale.noRecords')" />
  </div>
</template>

<style scoped>
.after-sale-page { max-width: 700px; margin: 0 auto; }
.after-sale-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.after-sale-title { font-size: var(--font-xl); font-weight: 700; }
.after-sale-form { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-md); margin-bottom: var(--space-lg); }
.after-sale-form-group { margin-bottom: var(--space-lg); }
.after-sale-label { font-size: var(--font-base); color: var(--text-secondary); margin-bottom: var(--space-sm); display: block; }
.after-sale-type-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-sm); }
.after-sale-type-item { padding: var(--space-md); border-radius: var(--radius-sm); cursor: pointer; text-align: center; transition: all var(--transition-fast); border: 1px solid var(--border-light); background: var(--bg-white); }
.after-sale-type-item.active { border: 2px solid var(--jd-red); background: var(--jd-red-light); }
.after-sale-type-icon { font-size: var(--font-xxl); margin-bottom: var(--space-xs); }
.after-sale-type-name { font-size: var(--font-base); font-weight: 600; margin-bottom: 2px; }
.after-sale-type-desc { font-size: var(--font-xs); color: var(--text-tertiary); }
.after-sale-input { width: 100%; padding: 10px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--font-base); box-sizing: border-box; margin-bottom: var(--space-md); }
.after-sale-textarea { width: 100%; height: 80px; padding: 10px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--font-base); resize: none; box-sizing: border-box; margin-bottom: var(--space-md); }
.after-sale-form-actions { display: flex; gap: var(--space-sm); justify-content: flex-end; }
.after-sale-skeleton { display: flex; flex-direction: column; gap: var(--space-md); }
.skeleton-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); box-shadow: var(--shadow-md); height: 80px; }
.skeleton-line { height: 16px; width: 50%; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-sm); }
.after-sale-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); margin-bottom: var(--space-md); box-shadow: var(--shadow-md); }
.after-sale-card-top { display: flex; justify-content: space-between; align-items: start; margin-bottom: 10px; }
.after-sale-card-order { font-size: var(--font-base); color: var(--text-tertiary); }
.after-sale-card-type { margin-left: var(--space-md); font-size: var(--font-base); font-weight: 600; }
.after-sale-card-reason { color: var(--text-secondary); font-size: var(--font-base); }
.after-sale-card-amount { color: var(--jd-red); font-weight: 700; margin-top: var(--space-xs); }
.after-sale-card-time { color: var(--text-placeholder); font-size: var(--font-xs); margin-top: var(--space-sm); }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
</style>
