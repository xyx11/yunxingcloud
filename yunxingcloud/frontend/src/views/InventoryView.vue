<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInputNumber, NInput, NSpace, NTag, NSelect, NTabs, NTabPane } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchInventory, stockIn, stockOut, fetchAlerts, fetchWarehouses, fetchLogs } from '@/api/inventory'
import { useNotify } from '@/composables/useNotify'
import request from '@/api/request'
import * as XLSX from 'xlsx'

const { t } = useI18n()
const notify = useNotify()
const loading = ref(false); const saving = ref(false)
const items = ref<any[]>([])
const alerts = ref<any[]>([])
const warehouses = ref<any[]>([])
const logs = ref<any[]>([])
const reorderSuggestions = ref<any[]>([])
const showIn = ref(false); const showOut = ref(false); const showTransfer = ref(false)
const form = ref({ productId: 0, productName: '', warehouseId: 0, quantity: 1, remark: '' })
const transferForm = ref({ productId: 0, fromWarehouseId: 0, toWarehouseId: 0, quantity: 1, remark: '' })
const whOptions = ref<any[]>([])
const activeTab = ref('inventory')

const columns = computed<DataTableColumns<any>>(() => [
  { title: t('inventory.productId'), key: 'productId', width: 80 },
  { title: t('inventory.productName'), key: 'productName', width: 150 },
  { title: t('inventory.warehouseId'), key: 'warehouseId', width: 80 },
  { title: t('inventory.quantity'), key: 'quantity', width: 80, render(r: any) { return h(NTag, { size: 'small', type: r.quantity <= r.minQuantity ? 'error' : 'success' }, { default: () => String(r.quantity) }) } },
  { title: t('inventory.minQuantity'), key: 'minQuantity', width: 80 },
  { title: t('inventory.updatedAt'), key: 'updatedAt', width: 150, render(r: any) { return r.updatedAt?.substring(0, 16) || '-' } },
])

const logColumns: DataTableColumns<any> = [
  { title: t('inventory.productId'), key: 'productId', width: 60 },
  { title: t('common.type'), key: 'type', width: 70, render(r: any) { return h(NTag, { size: 'small', type: r.type === 'IN' ? 'success' : r.type === 'OUT' ? 'error' : 'info' }, { default: () => r.type === 'IN' ? t('inventory.stockIn') : r.type === 'OUT' ? t('inventory.stockOut') : t('inventory.transfer') }) } },
  { title: t('inventory.quantity'), key: 'quantity', width: 60 },
  { title: t('inventory.fromWarehouse'), key: 'fromWarehouseId', width: 80, render(r: any) { return r.fromWarehouseId || '-' } },
  { title: t('inventory.toWarehouse'), key: 'toWarehouseId', width: 80, render(r: any) { return r.toWarehouseId || '-' } },
  { title: t('common.remark'), key: 'remark', width: 120, ellipsis: { tooltip: true } },
  { title: t('common.time'), key: 'createdAt', width: 140, render(r: any) { return r.createdAt?.substring(0, 16) } },
]

const suggestColumns: DataTableColumns<any> = [
  { title: t('inventory.product'), key: 'productName', width: 180 },
  { title: t('inventory.currentStock'), key: 'currentStock', width: 80 },
  { title: t('inventory.suggestedQty'), key: 'suggestedQty', width: 80 },
  { title: t('inventory.priority'), key: 'priority', width: 70, render(r: any) { return h(NTag, { size: 'small', type: r.priority === 'HIGH' ? 'error' : 'warning' }, { default: () => r.priority }) } },
]

async function load() { loading.value = true; try { const r = await fetchInventory(); items.value = r.data; const a = await fetchAlerts(); alerts.value = a.data } finally { loading.value = false } }
async function loadLogs() { try { const r = await fetchLogs(); logs.value = r.data || [] } catch(e) { console.error(e) } }
async function loadSuggestions() { try { const r = await request.get('/api/inventory/reorder-suggestions'); reorderSuggestions.value = r.data || [] } catch(e) { console.error(e) } }
async function loadWh() { const r = await fetchWarehouses(); warehouses.value = r.data; whOptions.value = r.data.map((w: any) => ({ label: w.name, value: w.id })) }

async function doIn() { saving.value = true; try { await stockIn(form.value); showIn.value = false; notify.success(t('inventory.stockInSuccess')); load() } catch { notify.error(t('common.saveFailed')) } finally { saving.value = false } }
async function doOut() { saving.value = true; try { await stockOut(form.value); showOut.value = false; notify.success(t('inventory.stockOutSuccess')); load() } catch { notify.error(t('common.saveFailed')) } finally { saving.value = false } }
async function doTransfer() {
  saving.value = true
  try {
    await request.post('/api/inventory/transfer', {
      productId: transferForm.value.productId,
      fromWarehouseId: transferForm.value.fromWarehouseId,
      toWarehouseId: transferForm.value.toWarehouseId,
      quantity: transferForm.value.quantity,
      remark: transferForm.value.remark,
    })
    showTransfer.value = false; notify.success(t('inventory.transferSuccess')); load()
  } catch { notify.error(t('inventory.operationFailed')) } finally { saving.value = false }
}

function exportExcel() {
  const data = items.value.map((i: any) => ({ '商品ID': i.productId, '商品名': i.productName, '仓库ID': i.warehouseId, '库存': i.quantity, '最低库存': i.minQuantity, '更新时间': i.updatedAt }))
  const ws = XLSX.utils.json_to_sheet(data); const wb = XLSX.utils.book_new(); XLSX.utils.book_append_sheet(wb, ws, '库存')
  XLSX.writeFile(wb, 'inventory.xlsx')
}

function exportCSV() { const h = ['商品ID', '商品名', '仓库ID', '库存', '最低库存', '更新时间']; const r = items.value.map((i: any) => [i.productId || '', i.productName || '', i.warehouseId || '', i.quantity || '', i.minQuantity || '', i.updatedAt || '']); const csv = [h, ...r].map(r => r.map(c => '"' + String(c).replace(/"/g, '""') + '"').join(',')).join('\n'); const b = new Blob(['﻿' + csv], { type: 'text/csv' }); const a = document.createElement('a'); a.href = URL.createObjectURL(b); a.download = 'inventory.csv'; a.click() }

onMounted(() => { load(); loadWh() })

// SSE
let sse: EventSource | null = null
function connectSSE() {
  sse = new EventSource('/api/inventory/alerts/stream')
  sse.addEventListener('alerts', (e: MessageEvent) => { try { const data = JSON.parse(e.data); if (Array.isArray(data)) alerts.value = data } catch(e) { console.error(e) } })
  sse.onerror = () => { sse?.close(); setTimeout(connectSSE, 10000) }
}
connectSSE()
onUnmounted(() => { sse?.close() })
</script>
<template>
  <n-card :title="t('nav.inventory')">
    <n-space vertical>
      <n-space>
        <n-button type="primary" @click="showIn = true">{{ t('inventory.stockIn') }}</n-button>
        <n-button type="warning" @click="showOut = true">{{ t('inventory.stockOut') }}</n-button>
        <n-button type="info" @click="showTransfer = true">仓库调拨</n-button>
        <n-button size="small" @click="exportExcel">导出Excel</n-button>
        <n-tag v-if="alerts.length" type="error">⚠ {{ t('inventory.lowStockAlert', { n: alerts.length }) }}</n-tag>
      </n-space>
      <n-tabs v-model:value="activeTab" type="line" @update:value="(v: string) => { if (v === 'logs') loadLogs(); if (v === 'suggest') loadSuggestions() }">
        <n-tab-pane name="inventory" :tab="t('inventory.list')">
          <n-dataTable :columns="columns" :data="items" :loading="loading" :row-key="(r: any) => r.id" :pagination="{ pageSize: 10 }" />
        </n-tab-pane>
        <n-tab-pane name="logs" tab="库存流水">
          <n-dataTable :columns="logColumns" :data="logs" :loading="loading" :row-key="(r: any) => r.id" :pagination="{ pageSize: 10 }" size="small" />
        </n-tab-pane>
        <n-tab-pane name="suggest" :tab="t('inventory.suggestions')">
          <n-dataTable :columns="suggestColumns" :data="reorderSuggestions" :loading="loading" :row-key="(r: any, i: number) => i" :pagination="false" size="small" />
        </n-tab-pane>
      </n-tabs>
    </n-space>
    <n-drawer v-model:show="showIn" :width="380" placement="right">
      <n-drawer-content :title="t('inventory.stockIn')" closable>
        <template #footer><n-space justify="end"><n-button @click="showIn = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="doIn">{{ t('common.confirm') }}</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item :label="t('inventory.productId')"><n-input-number v-model:value="form.productId" :min="1" /></n-form-item>
          <n-form-item :label="t('inventory.productName')"><n-input v-model:value="form.productName" /></n-form-item>
          <n-form-item :label="t('inventory.warehouse')"><n-select v-model:value="form.warehouseId" :options="whOptions" /></n-form-item>
          <n-form-item :label="t('inventory.quantity')"><n-input-number v-model:value="form.quantity" :min="1" /></n-form-item>
          <n-form-item :label="t('inventory.remark')"><n-input v-model:value="form.remark" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
    <n-drawer v-model:show="showOut" :width="380" placement="right">
      <n-drawer-content :title="t('inventory.stockOut')" closable>
        <template #footer><n-space justify="end"><n-button @click="showOut = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="doOut">{{ t('common.confirm') }}</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item :label="t('inventory.productId')"><n-input-number v-model:value="form.productId" :min="1" /></n-form-item>
          <n-form-item :label="t('inventory.warehouse')"><n-select v-model:value="form.warehouseId" :options="whOptions" /></n-form-item>
          <n-form-item :label="t('inventory.quantity')"><n-input-number v-model:value="form.quantity" :min="1" /></n-form-item>
          <n-form-item :label="t('inventory.remark')"><n-input v-model:value="form.remark" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
    <n-drawer v-model:show="showTransfer" :width="400" placement="right">
      <n-drawer-content title="仓库调拨" closable>
        <template #footer><n-space justify="end"><n-button @click="showTransfer = false">取消</n-button><n-button type="primary" :loading="saving" @click="doTransfer">确认调拨</n-button></n-space></template>
        <n-form :model="transferForm" label-placement="left" label-width="80" size="small">
          <n-form-item label="商品ID"><n-input-number v-model:value="transferForm.productId" :min="1" /></n-form-item>
          <n-form-item label="调出仓库"><n-select v-model:value="transferForm.fromWarehouseId" :options="whOptions" /></n-form-item>
          <n-form-item label="调入仓库"><n-select v-model:value="transferForm.toWarehouseId" :options="whOptions" /></n-form-item>
          <n-form-item label="数量"><n-input-number v-model:value="transferForm.quantity" :min="1" /></n-form-item>
          <n-form-item :label="t('common.remark')"><n-input v-model:value="transferForm.remark" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>
