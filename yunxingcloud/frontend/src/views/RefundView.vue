<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatPrice } from '@/utils/format'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NTag, NSelect, NSpace, NPopconfirm, NDivider, NStatistic, NGrid, NGridItem } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<Record<string, unknown>[]>([])
const filterStatus = ref('')
const loading = ref(false)

const typeLabels: Record<string, string> = { refund: t('refund.typeRefund'), return: t('refund.typeReturn'), exchange: t('refund.typeExchange') }
const statusLabels: Record<string, { l: string; t: string }> = {
  '0': { l: t('refund.statusPending'), t: 'warning' },
  '1': { l: t('refund.statusApproved'), t: 'success' },
  '2': { l: t('refund.statusRejected'), t: 'error' },
  '3': { l: t('refund.statusRefunding'), t: 'info' },
  '4': { l: t('refund.statusCompleted'), t: 'success' },
}

const stats = computed(() => ({
  total: items.value.length,
  pending: items.value.filter(i => i.status === '0').length,
  approved: items.value.filter(i => i.status === '1').length,
  totalAmount: items.value.filter(i => i.status === '1' || i.status === '3').reduce((s, i) => s + (i.refundAmount as number || 0), 0),
}))

const filtered = computed(() => {
  if (!filterStatus.value) return items.value
  return items.value.filter(i => i.status === filterStatus.value)
})

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('refund.username'), key: 'username', width: 90 },
  { title: t('refund.orderNo'), key: 'orderNo', width: 170 },
  { title: t('refund.type'), key: 'type', width: 70, render(r) { return typeLabels[r.type as string] || r.type as string } },
  { title: t('refund.amount'), key: 'refundAmount', width: 100, render(r) { return (r.refundAmount as number) ? formatPrice((r.refundAmount as number) / 100, 2) : '-' } },
  { title: t('refund.reason'), key: 'reason', width: 150, ellipsis: { tooltip: true } },
  { title: t('refund.status'), key: 'status', width: 80, render(r) { const s = statusLabels[r.status as string] || { l: r.status as string, t: 'default' }; return h(NTag, { size: 'small', type: s.t as any }, { default: () => s.l }) } },
  { title: t('refund.time'), key: 'createdAt', width: 140, render(r) { return (r.createdAt as string)?.substring(0, 16) } },
  { title: t('common.actions'), key: 'act', width: 160, render(r) {
    if (r.status !== '0') return h('span', '-')
    return h(NSpace, { size: 'small' }, { default: () => [
      h(NButton, { size: 'tiny', type: 'success', onClick: () => approve(r.id as number) }, { default: () => t('afterSale.approve') }),
      h(NPopconfirm, { onPositiveClick: () => reject(r.id as number) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('afterSale.reject') }), default: () => '确认拒绝？' }),
    ]})
  }},
]

async function load() { loading.value = true; try { const r = await request.get('/after-sale'); items.value = r.data } finally { loading.value = false } }
async function approve(id: number) { try { await request.put(`/after-sale/${id}/approve`); notify.success(t('afterSale.approveSuccess')); load() } catch { notify.error(t('common.error')) } }
async function reject(id: number) { try { await request.put(`/after-sale/${id}/reject`); notify.success(t('afterSale.rejectSuccess')); load() } catch { notify.error(t('common.error')) } }
onMounted(load)
</script>
<template>
  <n-card :title="t('refund.title')">
    <!-- Stats -->
    <n-grid cols="4" x-gap="12" class="mb-16">
      <n-grid-item><n-statistic :label="t('refund.totalApps')" :value="stats.total" /></n-grid-item>
      <n-grid-item><n-statistic :label="t('refund.pendingApps')" :value="stats.pending" /></n-grid-item>
      <n-grid-item><n-statistic :label="t('refund.approvedApps')" :value="stats.approved" /></n-grid-item>
      <n-grid-item><n-statistic :label="t('refund.totalAmount')" :value="formatPrice(stats.totalAmount / 100, 2)" /></n-grid-item>
    </n-grid>
    <n-divider />
    <n-space class="mb-12">
      <n-select v-model:value="filterStatus" :options="[{label:t('common.allStatus'),value:''},{label:t('refund.statusPending'),value:'0'},{label:t('refund.statusApproved'),value:'1'},{label:t('refund.statusRejected'),value:'2'},{label:t('refund.statusRefunding'),value:'3'},{label:t('refund.statusCompleted'),value:'4'}]" size="small" class="w-120" />
      <n-button size="small" @click="load" :loading="loading">{{ t('common.refresh') }}</n-button>
    </n-space>
    <n-dataTable :columns="columns" :data="filtered" :pagination="{ pageSize: 10 }" />
  </n-card>
</template>
<style scoped>
.mb-16 { margin-bottom: 16px; }
.mb-12 { margin-bottom: 12px; }
.w-120 { width: 120px; }
</style>
