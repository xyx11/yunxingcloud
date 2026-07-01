<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInputNumber, NInput, NSpace, NTag, NSelect } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchInventory, stockIn, stockOut, fetchAlerts, fetchWarehouses } from '@/api/inventory'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()
const loading = ref(false)
const items = ref<any[]>([])
const alerts = ref<any[]>([])
const warehouses = ref<any[]>([])
const showIn = ref(false); const showOut = ref(false)
const form = ref({ productId: 0, productName: '', warehouseId: 0, quantity: 1, remark: '' })
const whOptions = ref<any[]>([])

const columns = computed<DataTableColumns<any>>(() => [
  { title: t('inventory.productId'), key: 'productId', width: 80 },
  { title: t('inventory.productName'), key: 'productName', width: 150 },
  { title: t('inventory.warehouseId'), key: 'warehouseId', width: 80 },
  { title: t('inventory.quantity'), key: 'quantity', width: 80, render(r: any) { return h(NTag, { size: 'small', type: r.quantity <= r.minQuantity ? 'error' : 'success' }, { default: () => String(r.quantity) }) } },
  { title: t('inventory.minQuantity'), key: 'minQuantity', width: 80 },
  { title: t('inventory.updatedAt'), key: 'updatedAt', width: 150, render(r: any) { return r.updatedAt?.substring(0, 16) || '-' } },
])

async function load() { loading.value = true; try { const r = await fetchInventory(); items.value = r.data; const a = await fetchAlerts(); alerts.value = a.data } finally { loading.value = false } }
async function loadWh() { const r = await fetchWarehouses(); warehouses.value = r.data; whOptions.value = r.data.map((w: any) => ({ label: w.name, value: w.id })) }
async function doIn() { await stockIn(form.value); showIn.value = false; notify.success(t('inventory.stockInSuccess')); load() }
async function doOut() { await stockOut(form.value); showOut.value = false; notify.success(t('inventory.stockOutSuccess')); load() }
onMounted(() => { load(); loadWh() })

// SSE real-time alerts
let sse: EventSource | null = null
function connectSSE() {
  sse = new EventSource('/api/inventory/alerts/stream')
  sse.addEventListener('alerts', (e: MessageEvent) => {
    try {
      const data = JSON.parse(e.data)
      if (Array.isArray(data)) alerts.value = data
    } catch {}
  })
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
        <n-tag v-if="alerts.length" type="error">⚠ {{ t('inventory.lowStockAlert', { n: alerts.length }) }}</n-tag>
      </n-space>
      <n-dataTable :columns="columns" :data="items" :loading="loading" :row-key="(r: any) => r.id" :pagination="{ pageSize: 10 }" />
    </n-space>
    <n-modal v-model:show="showIn" :title="t('inventory.stockIn')" preset="card" style="max-width:400px">
      <n-form :model="form">
        <n-form-item :label="t('inventory.productId')"><n-input-number v-model:value="form.productId" :min="1" /></n-form-item>
        <n-form-item :label="t('inventory.productName')"><n-input v-model:value="form.productName" /></n-form-item>
        <n-form-item :label="t('inventory.warehouse')"><n-select v-model:value="form.warehouseId" :options="whOptions" /></n-form-item>
        <n-form-item :label="t('inventory.quantity')"><n-input-number v-model:value="form.quantity" :min="1" /></n-form-item>
        <n-form-item :label="t('inventory.remark')"><n-input v-model:value="form.remark" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showIn = false">{{ t('common.cancel') }}</n-button><n-button type="primary" @click="doIn">{{ t('common.confirm') }}</n-button></n-space></template>
    </n-modal>
    <n-modal v-model:show="showOut" :title="t('inventory.stockOut')" preset="card" style="max-width:400px">
      <n-form :model="form">
        <n-form-item :label="t('inventory.productId')"><n-input-number v-model:value="form.productId" :min="1" /></n-form-item>
        <n-form-item :label="t('inventory.warehouse')"><n-select v-model:value="form.warehouseId" :options="whOptions" /></n-form-item>
        <n-form-item :label="t('inventory.quantity')"><n-input-number v-model:value="form.quantity" :min="1" /></n-form-item>
        <n-form-item :label="t('inventory.remark')"><n-input v-model:value="form.remark" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showOut = false">{{ t('common.cancel') }}</n-button><n-button type="primary" @click="doOut">{{ t('common.confirm') }}</n-button></n-space></template>
    </n-modal>
  </n-card>
</template>