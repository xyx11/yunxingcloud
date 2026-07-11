<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatPrice } from '@/utils/format'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSelect, NSpace, NTag, NInputNumber } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchOrders, createOrder, payOrder, refundOrder, fetchRecords, type PaymentOrder } from '@/api/payment'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()

const loading = ref(false)
const saving = ref(false)
const paying = ref<Set<number>>(new Set())
const refunding = ref(false)
const items = ref<PaymentOrder[]>([])
const records = ref<Record<string, unknown>[]>([])
const showModal = ref(false)
const showRecords = ref(false)
const showRefund = ref(false)
const searchKeyword = ref('')
const form = ref({ title: '', amount: 1, channel: 'wechat' })
const targetOrder = ref<PaymentOrder | null>(null)
const refundForm = ref({ refundAmount: 0, reason: '' })

const channelOptions = computed(() => [
  { label: t('payment.wechat'), value: 'wechat' },
  { label: t('payment.alipay'), value: 'alipay' },
])

const statusLabel = computed<Record<string, string>>(() => ({
  '0': t('payment.statuses.0'), '1': t('payment.statuses.1'), '2': t('payment.statuses.2'), '3': t('payment.statuses.3'), '4': t('payment.statuses.4'),
}))
const statusColor: Record<string, string> = { '0': 'warning', '1': 'info', '2': 'success', '3': 'default', '4': 'error' }
const channelLabel = computed<Record<string, string>>(() => ({ wechat: t('payment.wechat'), alipay: t('payment.alipay') }))

const filteredItems = computed(() => {
  if (!searchKeyword.value) return items.value
  const kw = searchKeyword.value.toLowerCase()
  return items.value.filter(o => o.title.toLowerCase().includes(kw) || o.orderNo.toLowerCase().includes(kw))
})

const columns = computed<DataTableColumns<PaymentOrder>>(() => [
  { title: t('payment.orderNo'), key: 'orderNo', width: 180 },
  { title: t('payment.title'), key: 'title', width: 180, ellipsis: { tooltip: true } },
  { title: t('payment.amount'), key: 'amount', width: 100, render(row: PaymentOrder) { return `¥${(row.amount / 100).toFixed(2)}` } },
  { title: t('payment.channel'), key: 'channel', width: 80, render(row: PaymentOrder) { return h(NTag, { size: 'small' }, { default: () => channelLabel.value[row.channel] }) } },
  { title: t('payment.status'), key: 'status', width: 80, render(row: PaymentOrder) { return h(NTag, { size: 'small', type: statusColor[row.status] as any }, { default: () => statusLabel.value[row.status] }) } },
  { title: t('payment.tradeNo'), key: 'tradeNo', width: 160, ellipsis: { tooltip: true } },
  { title: t('common.createdAt'), key: 'createdAt', width: 150, render(row: PaymentOrder) { return row.createdAt?.substring(0, 16) } },
  {
    title: t('common.actions'), key: 'actions', width: 240,
    render(row: PaymentOrder) {
      const btns = [
        h(NButton, { size: 'small', onClick: () => viewRecords(row) }, { default: () => t('payment.records') }),
      ]
      if (row.status === '0' || row.status === '1') {
        btns.push(h(NButton, { size: 'small', type: 'primary', loading: paying.value.has(row.id), disabled: paying.value.has(row.id), onClick: () => doPay(row) }, { default: () => t('payment.pay') }))
      }
      if (row.status === '2') {
        btns.push(h(NButton, { size: 'small', type: 'warning', onClick: () => openRefund(row) }, { default: () => t('payment.refund') }))
      }
      return h(NSpace, { size: 'small' }, { default: () => btns })
    },
  },
])

async function load() {
  loading.value = true
  try { const res = await fetchOrders(); items.value = res.data || [] } finally { loading.value = false }
}

async function saveOrder() {
  if (!form.value.title) { notify.error(t('validate.required')); return }
  saving.value = true
  try { await createOrder(form.value); showModal.value = false; notify.success(t('common.createSuccess')); load() }
  catch { notify.error(t('common.saveFailed')) }
  finally { saving.value = false }
}

async function doPay(row: PaymentOrder) {
  if (paying.value.has(row.id)) return
  paying.value.add(row.id)
  try { await payOrder(row.id); notify.success(t('payment.paySuccess')); load() }
  catch { notify.error(t('payment.payFailed')) }
  finally { paying.value.delete(row.id) }
}

async function openRefund(row: PaymentOrder) {
  targetOrder.value = row
  refundForm.value = { refundAmount: row.amount - (row.refundAmount || 0), reason: '' }
  showRefund.value = true
}

async function doRefund() {
  if (!targetOrder.value || !refundForm.value.refundAmount) return
  refunding.value = true
  try { await refundOrder(targetOrder.value.id, refundForm.value.refundAmount, refundForm.value.reason); showRefund.value = false; notify.success(t('payment.refundSuccess')); load() }
  catch { notify.error(t('payment.refundFailed')) }
  finally { refunding.value = false }
}

async function viewRecords(row: PaymentOrder) {
  targetOrder.value = row
  const res = await fetchRecords(row.id)
  records.value = res.data || []
  showRecords.value = true
}

const recordColumns = computed(() => [
  { title: t('common.type'), key: 'type', width: 60 },
  { title: t('payment.channel'), key: 'channel', width: 60 },
  { title: t('payment.request'), key: 'request', width: 200, ellipsis: { tooltip: true } },
  { title: t('payment.response'), key: 'response', width: 200, ellipsis: { tooltip: true } },
  { title: t('payment.time'), key: 'createdAt', width: 140, render(r: Record<string, unknown>) { return (r.createdAt as string || '').substring(0, 16) } },
])

function exportCSV() { const h=['订单号','标题','金额','渠道','状态','交易号','时间']; const r=items.value.map((i: PaymentOrder)=>[i.orderNo||'',i.title||'',formatPrice(i.amount/100, 2),i.channel||'',i.status||'',i.tradeNo||'',i.createdAt||'']); const csv=[h,...r].map(r=>r.map(c=>'"'+String(c).replace(/"/g,'""')+'"').join(',')).join('\n'); const b=new Blob(['﻿'+csv],{type:'text/csv'}); const a=document.createElement('a'); a.href=URL.createObjectURL(b); a.download='payments.csv'; a.click() }
onMounted(load)
</script>

<template>
  <n-card :title="t('nav.payments')">
    <n-space vertical>
      <n-space justify="space-between">
        <n-input v-model:value="searchKeyword" :placeholder="t('payment.searchPlaceholder')" clearable class="w-240" />
        <n-space><n-button size="small" @click="exportCSV">{{ t('operlog.exportCsv') }}</n-button><n-button type="primary" @click="showModal = true">{{ t('payment.createOrder') }}</n-button></n-space>
      </n-space>
      <n-dataTable :columns="columns" :data="filteredItems" :loading="loading" :row-key="(r: PaymentOrder) => r.id" :pagination="{ pageSize: 10 }" />
    </n-space>

    <n-drawer v-model:show="showModal" :width="400" placement="right">
      <n-drawer-content :title="t('payment.createOrder')" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveOrder">{{ t('common.submit') }}</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item :label="t('payment.title')"><n-input v-model:value="form.title" /></n-form-item>
          <n-form-item :label="t('payment.amount') + '(¥)'"><n-input-number v-model:value="form.amount" :min="0.01" :step="1" /></n-form-item>
          <n-form-item :label="t('payment.channel')"><n-select v-model:value="form.channel" :options="channelOptions" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>

    <n-drawer v-model:show="showRefund" :width="380" placement="right">
      <n-drawer-content :title="t('payment.refund')" closable>
        <template #footer><n-space justify="end"><n-button @click="showRefund = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="refunding" @click="doRefund">{{ t('payment.confirmRefund') }}</n-button></n-space></template>
        <n-form :model="refundForm" label-placement="left" label-width="80" size="small">
          <n-form-item :label="t('payment.refundAmount')"><n-input-number v-model:value="refundForm.refundAmount" :min="1" /></n-form-item>
          <n-form-item :label="t('payment.reason')"><n-input v-model:value="refundForm.reason" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>

    <n-modal v-model:show="showRecords" :title="t('payment.payRecords')" preset="card" class="max-w-700">
      <n-dataTable :columns="recordColumns" :data="records" :pagination="{ pageSize: 5 }" size="small" />
      <template #footer><n-button @click="showRecords = false">{{ t('common.close') }}</n-button></template>
    </n-modal>
  </n-card>
</template>

<style scoped>
.w-240 { width: 240px; }
.max-w-700 { max-width: 700px; }
</style>