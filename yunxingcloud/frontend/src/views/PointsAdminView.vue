<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NSpace, NTag, NInput, NTabs, NTabPane } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'

const loading = ref(false)
const records = ref<any[]>([])
const searchKeyword = ref('')
const activeTab = ref('accounts')

const filtered = computed(() => {
  if (!searchKeyword.value) return records.value
  return records.value.filter(r => (r.username || '').toLowerCase().includes(searchKeyword.value.toLowerCase()))
})

const accountColumns: DataTableColumns<any> = [
  { title: t('common.name'), key: 'username', width: 120 },
  { title: t('points.available'), key: 'balance', width: 100, render(r: any) { return h(NTag, { size: 'small', type: (r.balance || 0) > 1000 ? 'success' : 'info' }, { default: () => String(r.balance || 0) }) } },
  { title: t('points.totalEarned'), key: 'totalEarned', width: 100, render(r: any) { return r.totalEarned || 0 } },
  { title: t('points.totalSpent'), key: 'totalSpent', width: 100, render(r: any) { return r.totalSpent || 0 } },
  { title: t('common.updatedAt'), key: 'updatedAt', width: 160, render(r: any) { return r.updatedAt?.substring(0, 16) || '-' } },
]

const exchangeColumns: DataTableColumns<any> = [
  { title: t('common.name'), key: 'name', width: 150 },
  { title: t('points.exchangeTitle'), key: 'points', width: 80 },
  { title: t('common.stock'), key: 'stock', width: 60, render(r:any){ return h(NTag,{size:'small',type:r.stock>0?'success':'error'},{default:()=>String(r.stock)}) } },
  { title: t('points.valueLabel'), key: 'value', width: 80, render(r:any){ return r.value ? '¥'+(r.value/100).toFixed(2) : '-' } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/points/admin/accounts'); records.value = r.data || [] } catch { records.value = [] } finally { loading.value = false } }
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card :title="t('nav.pointsAdmin')"><template #header-extra><n-button size="small" @click="load" secondary>{{ t('common.refresh') }}</n-button></template>
      <n-tabs v-model:value="activeTab" type="line" size="small">
        <n-tab-pane name="accounts" :tab="t('nav.users')">
          <n-space class="mb-12"><n-input v-model:value="searchKeyword" :placeholder="t('common.search') + '...'" size="small" clearable class="w-180" /></n-space>
          <n-dataTable :columns="accountColumns" :data="filtered" :loading="loading" :row-key="(r:any,i:number)=>i" :pagination="{pageSize:15}" size="small" />
        </n-tab-pane>
        <n-tab-pane name="exchange" :tab="t('points.exchangeTitle')">
          <n-dataTable :columns="exchangeColumns" :data="[]" size="small" />
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </div>
</template>

<style scoped>
.w-180 { width: 180px; }
</style>
