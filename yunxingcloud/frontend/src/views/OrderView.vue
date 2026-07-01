<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NModal, NSpace, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchOrders, getOrder, payOrder, cancelOrder, type OrderHead } from '@/api/order'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()
const loading = ref(false)
const items = ref<OrderHead[]>([])
const detail = ref<any>(null)
const showDetail = ref(false)
const searchKeyword = ref('')

const statusLabels = computed<Record<string, string>>(() => ({
  '0': t('order.statuses.0'), '1': t('order.statuses.1'), '2': t('order.statuses.2'), '3': t('order.statuses.3'), '4': t('order.statuses.4'),
}))
const statusColors: Record<string, string> = { '0': 'warning', '1': 'info', '2': 'info', '3': 'success', '4': 'default' }

const filtered = computed(() => {
  if (!searchKeyword.value) return items.value
  const kw = searchKeyword.value.toLowerCase()
  return items.value.filter(o => o.orderNo.toLowerCase().includes(kw) || o.username.includes(kw))
})

const columns = computed<DataTableColumns<OrderHead>>(() => [
  { title: t('order.orderNo'), key: 'orderNo', width: 180 },
  { title: t('order.username'), key: 'username', width: 100 },
  { title: t('order.totalAmount'), key: 'totalAmount', width: 100, render(r: OrderHead) { return `¥${(r.totalAmount / 100).toFixed(2)}` } },
  { title: t('order.status'), key: 'status', width: 80, render(r: OrderHead) { return h(NTag, { size: 'small', type: statusColors[r.status] as any }, { default: () => statusLabels.value[r.status] }) } },
  { title: t('common.createdAt'), key: 'createdAt', width: 150, render(r: OrderHead) { return r.createdAt?.substring(0, 16) } },
  {
    title: t('common.actions'), key: 'act', width: 220,
    render(r: OrderHead) {
      const btns = [h(NButton, { size: 'small', onClick: () => openDetail(r.id) }, { default: () => t('order.detail') })]
      if (r.status === '0') btns.push(h(NButton, { size: 'small', type: 'primary', onClick: () => doPay(r.id) }, { default: () => t('order.pay') }))
      if (r.status === '0' || r.status === '1') btns.push(h(NButton, { size: 'small', type: 'warning', onClick: () => doCancel(r.id) }, { default: () => t('order.cancel') }))
      return h(NSpace, { size: 'small' }, { default: () => btns })
    },
  },
])

const detailColumns = computed(() => [
  { title: t('order.product'), key: 'productName' },
  { title: t('order.unitPrice'), key: 'price', render(r: any) { return '¥' + (r.price / 100).toFixed(2) } },
  { title: t('order.quantity'), key: 'quantity' },
])

async function load() { loading.value = true; try { const r = await fetchOrders(); items.value = r.data } finally { loading.value = false } }
async function openDetail(id: number) { const r = await getOrder(id); detail.value = r.data; showDetail.value = true }
async function doPay(id: number) { await payOrder(id); notify.success(t('payment.paySuccess')); load() }
async function doCancel(id: number) { await cancelOrder(id); notify.success(t('common.success')); load() }
onMounted(load)
</script>
<template>
  <n-card :title="t('nav.orders')">
    <n-space vertical>
      <n-input v-model:value="searchKeyword" :placeholder="t('order.searchPlaceholder')" clearable style="width:240px" />
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r: OrderHead) => r.id" :pagination="{ pageSize: 10 }" />
    </n-space>
    <n-modal v-model:show="showDetail" :title="t('order.detail')" preset="card" style="max-width:600px">
      <template v-if="detail">
        <p><b>{{ t('order.orderNo') }}:</b> {{ detail.order?.orderNo }}</p>
        <p><b>{{ t('order.username') }}:</b> {{ detail.order?.username }}</p>
        <p><b>{{ t('order.totalAmount') }}:</b> ¥{{ (detail.order?.totalAmount / 100).toFixed(2) }}</p>
        <p><b>{{ t('order.status') }}:</b> <n-tag :type="statusColors[detail.order?.status] as any" size="small">{{ statusLabels[detail.order?.status] }}</n-tag></p>
        <p><b>{{ t('order.receiver') }}:</b> {{ detail.order?.receiverName }} {{ detail.order?.receiverPhone }}</p>
        <p><b>{{ t('order.address') }}:</b> {{ detail.order?.receiverAddress }}</p>
        <hr style="margin:12px 0" />
        <n-dataTable size="small" :columns="detailColumns" :data="detail.lines || []" :pagination="false" />
      </template>
    </n-modal>
  </n-card>
</template>