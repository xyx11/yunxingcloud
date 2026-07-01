<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NTag, NInputNumber } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchOrders, createOrder, payOrder, refundOrder, fetchRecords, type PaymentOrder } from '@/api/payment'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()

const loading = ref(false)
const items = ref<PaymentOrder[]>([])
const records = ref<any[]>([])
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
        btns.push(h(NButton, { size: 'small', type: 'primary', onClick: () => doPay(row) }, { default: () => t('payment.pay') }))
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
  await createOrder(form.value)
  showModal.value = false
  notify.success(t('common.createSuccess'))
  load()
}

async function doPay(row: PaymentOrder) {
  await payOrder(row.id)
  notify.success(t('payment.paySuccess'))
  load()
}

async function openRefund(row: PaymentOrder) {
  targetOrder.value = row
  refundForm.value = { refundAmount: row.amount - (row.refundAmount || 0), reason: '' }
  showRefund.value = true
}

async function doRefund() {
  if (!targetOrder.value) return
  await refundOrder(targetOrder.value.id, refundForm.value.refundAmount, refundForm.value.reason)
  showRefund.value = false
  notify.success(t('payment.refundSuccess'))
  load()
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
  { title: t('payment.time'), key: 'createdAt', width: 140, render(r: any) { return r.createdAt?.substring(0, 16) } },
])

onMounted(load)
</script>

<template>
  <n-card :title="t('nav.payments')">
    <n-space vertical>
      <n-space justify="space-between">
        <n-input v-model:value="searchKeyword" :placeholder="t('payment.searchPlaceholder')" clearable style="width: 240px" />
        <n-button type="primary" @click="showModal = true">{{ t('payment.createOrder') }}</n-button>
      </n-space>
      <n-dataTable :columns="columns" :data="filteredItems" :loading="loading" :row-key="(r: PaymentOrder) => r.id" :pagination="{ pageSize: 10 }" />
    </n-space>

    <n-modal v-model:show="showModal" :title="t('payment.createOrder')" preset="card" style="max-width:500px">
      <n-form :model="form">
        <n-form-item :label="t('payment.title')">
          <n-input v-model:value="form.title" />
        </n-form-item>
        <n-form-item :label="t('payment.amount') + '(¥)'">
          <n-input-number v-model:value="form.amount" :min="0.01" :step="1" />
        </n-form-item>
        <n-form-item :label="t('payment.channel')">
          <n-select v-model:value="form.channel" :options="channelOptions" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
          <n-button type="primary" @click="saveOrder">{{ t('common.submit') }}</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="showRefund" :title="t('payment.refund')" preset="card" style="max-width:400px">
      <n-form :model="refundForm">
        <n-form-item :label="t('payment.refundAmount')">
          <n-input-number v-model:value="refundForm.refundAmount" :min="1" />
        </n-form-item>
        <n-form-item :label="t('payment.reason')">
          <n-input v-model:value="refundForm.reason" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showRefund = false">{{ t('common.cancel') }}</n-button>
          <n-button type="primary" @click="doRefund">{{ t('payment.confirmRefund') }}</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="showRecords" :title="t('payment.payRecords')" preset="card" style="max-width:700px">
      <n-dataTable :columns="recordColumns" :data="records" :pagination="{ pageSize: 5 }" size="small" />
      <template #footer><n-button @click="showRecords = false">{{ t('common.close') }}</n-button></template>
    </n-modal>
  </n-card>
</template>