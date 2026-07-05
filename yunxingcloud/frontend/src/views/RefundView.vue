<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatPrice } from '@/utils/format'
const { t } = useI18n()
import { NCard, NDataTable, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'

const items = ref<any[]>([])

const typeLabels: Record<string, string> = { refund: t('refund.typeRefund'), return: t('refund.typeReturn'), exchange: t('refund.typeExchange') }
const statusLabels: Record<string, { l: string; t: string }> = {
  '0': { l: t('refund.statusPending'), t: 'warning' },
  '1': { l: t('refund.statusApproved'), t: 'success' },
  '2': { l: t('refund.statusRejected'), t: 'error' },
  '3': { l: t('refund.statusRefunding'), t: 'info' },
  '4': { l: t('refund.statusCompleted'), t: 'success' },
}

const columns: DataTableColumns<any> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('refund.username'), key: 'username', width: 90 },
  { title: t('refund.orderNo'), key: 'orderNo', width: 170 },
  { title: t('refund.type'), key: 'type', width: 70, render(r: any) { return typeLabels[r.type] || r.type } },
  { title: t('refund.amount'), key: 'refundAmount', width: 100, render(r: any) { return r.refundAmount ? formatPrice(r.refundAmount / 100, 2) : '-' } },
  { title: t('refund.reason'), key: 'reason', width: 150, ellipsis: { tooltip: true } },
  { title: t('refund.status'), key: 'status', width: 80, render(r: any) { const s = statusLabels[r.status] || { l: r.status, t: 'default' }; return h(NTag, { size: 'small', type: s.t as any }, { default: () => s.l }) } },
  { title: t('refund.time'), key: 'createdAt', width: 140, render(r: any) { return r.createdAt?.substring(0, 16) } },
]

async function load() { const r = await request.get('/after-sale'); items.value = r.data }
onMounted(load)
</script>
<template>
  <n-card :title="t('refund.title')">
    <n-dataTable :columns="columns" :data="items" :pagination="{ pageSize: 10 }" />
  </n-card>
</template>
