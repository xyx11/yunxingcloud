<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NModal, NDrawer, NDrawerContent, NSpace, NTag, NInput, NSelect, NForm, NFormItem, NTimeline, NTimelineItem, NDivider } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchOrders, getOrder, payOrder, cancelOrder, type OrderHead } from '@/api/order'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { formatPrice } from '@/utils/format'

const { t } = useI18n()
const notify = useNotify()
const loading = ref(false)
const items = ref<OrderHead[]>([])
const detail = ref<any>(null)
const showDetail = ref(false)
const searchKeyword = ref('')
const filterStatus = ref('')
const filterMinAmount = ref('')
const filterDateFrom = ref('')
const filterDateTo = ref('')
const checkedRowKeys = ref<number[]>([])
const showBatchShip = ref(false); const showBatchCancel = ref(false)
const batchCarrier = ref('SF Express'); const batchReason = ref(t('order.batchCancel'))
const batchLoading = ref(false)

const statusLabels = computed<Record<string, string>>(() => ({
  '0': t('order.statuses.0'), '1': t('order.statuses.1'), '2': t('order.statuses.2'), '3': t('order.statuses.3'), '4': t('order.statuses.4'),
}))
const statusColors: Record<string, string> = { '0': 'warning', '1': 'info', '2': 'info', '3': 'success', '4': 'default' }

const filtered = computed(() => {
  let list = items.value
  if (filterStatus.value) list = list.filter(o => o.status === filterStatus.value)
  if (filterMinAmount.value) list = list.filter(o => o.totalAmount >= Number(filterMinAmount.value) * 100)
  if (searchKeyword.value) { const kw = searchKeyword.value.toLowerCase(); list = list.filter(o => o.orderNo.toLowerCase().includes(kw) || o.username.includes(kw)) }
  if (filterDateFrom.value) { const from = new Date(filterDateFrom.value).getTime(); list = list.filter(o => new Date(o.createdAt).getTime() >= from) }
  if (filterDateTo.value) { const to = new Date(filterDateTo.value).getTime() + 86400000; list = list.filter(o => new Date(o.createdAt).getTime() < to) }
  return list
})

const columns = computed<DataTableColumns<OrderHead>>(() => [
  { type: 'selection' },
  { title: t('order.orderNo'), key: 'orderNo', width: 180 },
  { title: t('order.username'), key: 'username', width: 100 },
  { title: t('order.totalAmount'), key: 'totalAmount', width: 100, render(r: OrderHead) { return formatPrice(r.totalAmount / 100, 2) } },
  { title: t('order.status'), key: 'status', width: 80, render(r: OrderHead) { return h(NTag, { size: 'small', type: statusColors[r.status] as any }, { default: () => statusLabels.value[r.status] }) } },
  { title: t('common.createdAt'), key: 'createdAt', width: 150, render(r: OrderHead) { return r.createdAt?.substring(0, 16) } },
  {
    title: t('common.actions'), key: 'act', width: 280,
    render(r: OrderHead) {
      const btns = [h(NButton, { size: 'small', onClick: () => openDetail(r.id) }, { default: () => t('order.detail') })]
      const s = submitting.value
      if (r.status === '0') btns.push(h(NButton, { size: 'small', type: 'primary', loading: s.has(r.id), disabled: s.has(r.id), onClick: () => doPay(r.id) }, { default: () => t('order.pay') }))
      if (r.status === '1') btns.push(h(NButton, { size: 'small', type: 'info', loading: s.has(r.id), disabled: s.has(r.id), onClick: () => doStatus(r.id, '2') }, { default: () => t('order.ship') }))
      if (r.status === '2') btns.push(h(NButton, { size: 'small', type: 'success', loading: s.has(r.id), disabled: s.has(r.id), onClick: () => doStatus(r.id, '3') }, { default: () => t('order.complete') }))
      if (r.status !== '3' && r.status !== '4') btns.push(h(NButton, { size: 'small', type: 'warning', loading: s.has(r.id), disabled: s.has(r.id), onClick: () => doCancel(r.id) }, { default: () => t('order.cancel') }))
      return h(NSpace, { size: 'small' }, { default: () => btns })
    },
  },
])

const detailColumns = computed(() => [
  { title: t('order.product'), key: 'productName' },
  { title: t('order.unitPrice'), key: 'price', render(r: any) { return formatPrice(r.price / 100, 2) } },
  { title: t('order.quantity'), key: 'quantity' },
])

async function load() { loading.value = true; try { const r = await fetchOrders(); items.value = r.data } finally { loading.value = false } }
async function openDetail(id: number) { const r = await getOrder(id); detail.value = r.data; showDetail.value = true }
const submitting = ref<Set<number>>(new Set())
async function doPay(id: number) { if(submitting.value.has(id))return; submitting.value.add(id); try{await payOrder(id);notify.success(t('payment.paySuccess'));load()}catch{notify.error(t('common.saveFailed'))}finally{submitting.value.delete(id)} }
async function doCancel(id: number) { if(submitting.value.has(id))return; submitting.value.add(id); try{await cancelOrder(id);notify.success(t('common.success'));load()}catch{notify.error(t('common.saveFailed'))}finally{submitting.value.delete(id)} }
async function doStatus(id: number, status: string) { if(submitting.value.has(id))return; submitting.value.add(id); try{await request.put(`/api/orders/${id}/status`,{status});notify.success(t('common.success'));load()}catch{notify.error(t('common.saveFailed'))}finally{submitting.value.delete(id)} }

// 批量操作
async function doBatchShip() {
  if (!checkedRowKeys.value.length) return; batchLoading.value = true
  try { const r = await request.put('/api/batch/ship', { ids: checkedRowKeys.value, carrier: batchCarrier.value }); notify.success(t('order.shipResult', { shipped: r.data.shipped, total: r.data.total })); checkedRowKeys.value = []; showBatchShip.value = false; load() }
  catch { notify.error(t('order.batchShipFailed')) } finally { batchLoading.value = false }
}
async function doBatchCancel() {
  if (!checkedRowKeys.value.length) return; batchLoading.value = true
  try { const r = await request.put('/api/batch/cancel', { ids: checkedRowKeys.value, reason: batchReason.value }); notify.success(t('order.cancelResult', { canceled: r.data.canceled, total: r.data.total })); checkedRowKeys.value = []; showBatchCancel.value = false; load() }
  catch { notify.error(t('order.batchCancelFailed')) } finally { batchLoading.value = false }
}
function exportCSV() { const h=[t('order.orderNo'),t('order.username'),t('order.totalAmount'),t('order.status'),t('common.createdAt')]; const r=items.value.map((i:any)=>[i.orderNo||'',i.username||'',(i.totalAmount/100, 2),i.status||'',i.createdAt||'']); const csv=[h,...r].map(r=>r.map(c=>'"'+String(c).replace(/"/g,'""')+'"').join(',')).join('\n'); const b=new Blob(['﻿'+csv],{type:'text/csv'}); const a=document.createElement('a'); a.href=URL.createObjectURL(b); a.download='orders.csv'; a.click() }
onMounted(load)
</script>
<template>
  <n-card :title="t('nav.orders')">
    <n-space vertical>
      <n-space justify="space-between">
        <n-space>
          <n-input v-model:value="searchKeyword" :placeholder="t('order.searchPlaceholder')" clearable class="w-160" size="small" />
          <n-select v-model:value="filterStatus" :options="[{label:t('order.allStatus'),value:''},{label:t('order.statuses.0'),value:'0'},{label:t('order.statuses.1'),value:'1'},{label:t('order.statuses.2'),value:'2'},{label:t('order.statuses.3'),value:'3'},{label:t('order.statuses.4'),value:'4'}]" class="w-100" size="small" />
          <n-input v-model:value="filterMinAmount" :placeholder="t('order.minAmount')" class="w-80" size="small" />
          <span class="filter-label">{{ t('common.date') }}</span>
          <input type="date" v-model="filterDateFrom" class="date-input" />
          <span class="text-muted">-</span>
          <input type="date" v-model="filterDateTo" class="date-input" />
        </n-space>
        <n-space>
          <n-button v-if="checkedRowKeys.length" type="info" size="small" @click="showBatchShip = true">批量发货 ({{ checkedRowKeys.length }})</n-button>
          <n-button v-if="checkedRowKeys.length" type="warning" size="small" @click="showBatchCancel = true">批量取消 ({{ checkedRowKeys.length }})</n-button>
          <n-button size="small" @click="exportCSV">{{ t('operlog.exportCsv') }}</n-button>
        </n-space>
      </n-space>
      <n-dataTable v-model:checked-row-keys="checkedRowKeys" :columns="columns" :data="filtered" :loading="loading" :row-key="(r: OrderHead) => r.id" :pagination="{ pageSize: 10 }" />
    </n-space>
    <n-drawer v-model:show="showDetail" :width="480" placement="right">
      <n-drawer-content :title="t('order.detail')" closable>
        <template v-if="detail">
          <n-form label-placement="left" label-width="80" size="small">
            <n-form-item :label="t('order.orderNo')"><span>{{ detail.order?.orderNo }}</span></n-form-item>
            <n-form-item :label="t('order.username')"><span>{{ detail.order?.username }}</span></n-form-item>
            <n-form-item :label="t('order.totalAmount')"><b>{{ formatPrice(detail.order?.totalAmount / 100, 2) }}</b></n-form-item>
            <n-form-item :label="t('order.status')"><n-tag :type="statusColors[detail.order?.status] as any" size="small">{{ statusLabels[detail.order?.status] }}</n-tag></n-form-item>
            <n-form-item label="收货人"><span>{{ detail.order?.receiverName }} {{ detail.order?.receiverPhone }}</span></n-form-item>
            <n-form-item :label="t('order.address')"><span>{{ detail.order?.receiverAddress }}</span></n-form-item>
          </n-form>
          <n-divider />
          <div style="display:flex;justify-content:space-between;margin-bottom:12px">
            <div v-for="(s,i) in ['已下单','已支付','已发货','已完成']" :key="i" style="text-align:center;flex:1;position:relative">
              <div :style="{width:24,height:24,borderRadius:'50%',display:'inline-flex',alignItems:'center',justifyContent:'center',fontSize:12,background:Number(detail.order?.status)>=i?'#18a058':'#e8e8e8',color:Number(detail.order?.status)>=i?'#fff':'#999'}">{{ Number(detail.order?.status)>=i ? '✓' : i+1 }}</div>
              <div style="font-size:11px;margin-top:4px;color:Number(detail.order?.status)>=i?'#18a058':'#999'">{{ s }}</div>
              <div v-if="i<3" :style="{position:'absolute',top:12,left:'60%',width:'80%',height:2,background:Number(detail.order?.status)>i?'#18a058':'#e8e8e8',transform:'translateX(-40%)'}" />
            </div>
          </div>
          <n-divider />
          <div class="price-breakdown">
            <div class="price-row"><span>{{ t('order.productTotal') }}</span><span>{{ formatPrice((detail.order?.totalAmount||0)/100, 2) }}</span></div>
            <div class="price-row price-coupon" v-if="detail.order?.couponAmount"><span>{{ t('order.couponDeduction') }}</span><span>-{{ formatPrice((detail.order?.couponAmount||0)/100, 2) }}</span></div>
            <n-divider style="margin:8px 0" />
            <div class="price-row price-total"><span>{{ t('order.actualPay') }}</span><span class="price-total-amount">{{ formatPrice((detail.order?.actualAmount||detail.order?.totalAmount||0)/100, 2) }}</span></div>
          </div>
          <n-divider />
          <n-dataTable size="small" :columns="detailColumns" :data="detail.lines || []" :pagination="false" :max-height="300" />
        </template>
      </n-drawer-content>
    </n-drawer>
    <!-- 批量发货弹窗 -->
    <n-modal v-model:show="showBatchShip" :title="t('order.batchShip')" preset="card" style="max-width:400px">
      <n-form>
        <n-form-item :label="t('order.carrier')"><n-input v-model:value="batchCarrier" :placeholder="t('order.carrierPlaceholder')" /></n-form-item>
        <n-form-item :label="t('order.selectedOrders')">{{ checkedRowKeys.length }} {{ t('order.unit') }}</n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showBatchShip=false">取消</n-button><n-button type="primary" :loading="batchLoading" @click="doBatchShip">确认批量发货</n-button></n-space></template>
    </n-modal>
    <!-- 批量取消弹窗 -->
    <n-modal v-model:show="showBatchCancel" title="批量取消" preset="card" style="max-width:400px">
      <n-form>
        <n-form-item label="取消原因"><n-input v-model:value="batchReason" placeholder="输入取消原因" /></n-form-item>
        <n-form-item :label="t('order.selectedOrders')">{{ checkedRowKeys.length }} {{ t('order.unit') }}</n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showBatchCancel=false">取消</n-button><n-button type="primary" :loading="batchLoading" @click="doBatchCancel">确认批量取消</n-button></n-space></template>
    </n-modal>
  </n-card>
</template>