<script setup lang="ts">
import { ref, onMounted } from 'vue'
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
const showForm = ref(false)
const submitting = ref(false)
const form = ref({ orderNo: '', type: 'personal', title: '', taxNo: '', email: '' })

onMounted(async () => {
  try { const r = await request.get('/invoices'); invoices.value = r.data || [] } catch {}
  finally { loading.value = false }
})

async function submit() {
  if (!form.value.orderNo) { toast.error(t('invoice.fillOrderNo')); return }
  if (form.value.type === 'company' && !form.value.title) { toast.error(t('invoice.fillTitle')); return }
  submitting.value = true
  try { await request.post('/invoices', form.value); toast.success(t('invoice.submitSuccess')); showForm.value = false; load() } catch { toast.error(t('invoice.submitFail')) }
  finally { submitting.value = false }
}

async function load() {
  try { const r = await request.get('/invoices'); invoices.value = r.data || [] } catch {}
}

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

    <div v-if="loading" class="invoice-skeleton">
      <div v-for="i in 2" :key="i" class="skeleton-card">
        <div class="skeleton-line" />
      </div>
    </div>
    <div v-else-if="invoices.length">
      <div v-for="inv in invoices" :key="inv.id" class="invoice-card">
        <div>
          <div class="invoice-card-title">{{ (inv.type==='company' ? t('invoice.company') : t('invoice.personal')) + ' ' + t('invoice.invoicePrefix') }} · {{ inv.title || t('invoice.personal') }}</div>
          <div class="invoice-card-meta">{{ t('afterSale.orderPrefix', '订单') }} {{ inv.orderNo }} · {{ inv.createdAt?.substring(0,10) }}</div>
        </div>
        <JdBadge :type="badgeTypeMap[inv.status] || 'gray'">{{ statusMap[inv.status]?.label }}</JdBadge>
      </div>
    </div>
    <JdEmpty v-else icon="🧾" :title="t('invoice.noRecords')" />
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
</style>
