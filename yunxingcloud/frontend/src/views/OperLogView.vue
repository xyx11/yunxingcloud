<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, onBeforeUnmount, h } from 'vue'
import { fetchOperLogs, deleteOperLog, batchDeleteOperLogs, cleanOperLogs, exportOperLogs } from '@/api/operlog'
import { fetchDashboard } from '@/api/stats'
import { useNotify } from '@/composables/useNotify'

import {
  NCard, NDataTable, NButton, NTag, NPopconfirm, NSpace, NInput, NSelect, NDatePicker,
  
} from 'naive-ui'

const { t } = useI18n()
import type { DataTableColumns } from 'naive-ui'

import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
use([BarChart, GridComponent, TooltipComponent, CanvasRenderer])

interface OperLog {
  id: number; title: string; businessType: string; method: string
  operName: string; operIp: string; operUrl: string; operParam: string
  jsonResult: string; status: number; errorMsg: string; costTime: number; operTime: string
}

const notify = useNotify()

const logs = ref<OperLog[]>([])
const loading = ref(false)
const checkedKeys = ref<(string | number)[]>([])
const filterType = ref<string | undefined>(undefined)
const filterUser = ref('')
const filterMethod = ref('')
const dateRange = ref<[number, number] | null>(null)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const autoRefresh = ref(false)
const chartOption = ref({ tooltip:{trigger:'axis'}, grid:{left:40,right:20,top:10,bottom:20}, xAxis:{type:'category',data:[]}, yAxis:{type:'value'}, series:[{data:[],type:'bar',itemStyle:{color:'#667eea',borderRadius:[4,4,0,0]}}] })
const showChart = ref(false)
let refreshTimer: ReturnType<typeof setInterval> | null = null

const typeOptions = [
  { label: t('operlog.allTypes'), value: undefined },
  { label: t('operlog.insert'), value: 'INSERT' },
  { label: t('operlog.update'), value: 'UPDATE' },
  { label: t('operlog.deleteOp'), value: 'DELETE' },
]

const businessTypeMap: Record<string, string> = {
  INSERT: t('operlog.insert'), UPDATE: t('operlog.update'), DELETE: t('operlog.deleteOp'), OTHER: t('operlog.other')
}

const columns: DataTableColumns<OperLog> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('operlog.title'), key: 'title', width: 120 },
  {
    title: t('operlog.bizType'), key: 'businessType', width: 80,
    render: (row) => h(NTag, { type: row.businessType === 'DELETE' ? 'error' : row.businessType === 'INSERT' ? 'success' : 'info', size: 'small' },
      { default: () => businessTypeMap[row.businessType] || row.businessType })
  },
  { title: t('operlog.method'), key: 'method', width: 100 },
  { title: t('operlog.operator'), key: 'operName', width: 100 },
  { title: t('operlog.ip'), key: 'operIp', width: 120 },
  { title: t('operlog.url'), key: 'operUrl', width: 180, ellipsis: { tooltip: true } },
  {
    title: t('operlog.status'), key: 'status', width: 60,
    render: (row) => h(NTag, { type: row.status === 0 ? 'success' : 'error', size: 'small' },
      { default: () => row.status === 0 ? t('operlog.success') : t('operlog.fail') })
  },
  { title: t('operlog.cost'), key: 'costTime', width: 80, sorter: true },
  { title: t('operlog.time'), key: 'operTime', width: 160, sorter: true },
  {
    title: t('operlog.actions'), key: 'actions', width: 120,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => showDetail(row) }, { default: () => t('operlog.detail') }),
        h(NPopconfirm, { onPositiveClick: () => delLog(row.id) }, {
          trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => t('common.delete') }),
          default: () => t('common.confirmDelete')
        })
      ]
    })
  },
]

const detailVisible = ref(false)
const detailLog = ref<OperLog | null>(null)

function showDetail(log: OperLog) {
  detailLog.value = log
  detailVisible.value = true
}

async function loadLogs() {
  loading.value = true
  try {
    const params: Record<string, string | number | undefined> = { page: page.value, pageSize: pageSize.value }
    if (filterType.value) params.type = filterType.value
    if (filterUser.value) params.user = filterUser.value
    if (filterMethod.value) params.method = filterMethod.value
    if (dateRange.value) {
      params.startTime = new Date(dateRange.value[0]).toISOString().substring(0,19)
      params.endTime = new Date(dateRange.value[1]).toISOString().substring(0,19)
    }
    const res = await fetchOperLogs(params)
    logs.value = res.data.items
    // 加载图表数据
    try {
      const statsRes = await fetchDashboard()
      if (statsRes.data.bizTypeDist?.length) {
        chartOption.value.xAxis.data = statsRes.data.bizTypeDist.map((d: Record<string, unknown>)=>d.name as string)
        chartOption.value.series[0].data = statsRes.data.bizTypeDist.map((d: Record<string, unknown>)=>d.value as number)
        showChart.value = true
      }
    } catch { /* ignore */ }
    total.value = res.data.total || 0
  } catch { /* ignore */ }
  loading.value = false
}

function handlePageChange(p: number) {
  page.value = p
  loadLogs()
}

function searchLogs() {
  page.value = 1
  loadLogs()
}

function handlePageSizeChange(ps: number) {
  pageSize.value = ps
  page.value = 1
  loadLogs()
}

function toggleAutoRefresh() {
  autoRefresh.value = !autoRefresh.value
  if (autoRefresh.value) { refreshTimer = setInterval(loadLogs, 30000) }
  else if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null }
}

async function delLog(id: number) {
  await deleteOperLog(id)
  await loadLogs()
}

async function cleanLogs() {
  await cleanOperLogs()
  checkedKeys.value = []
  await loadLogs()
}

async function batchDelete() {
  if (checkedKeys.value.length === 0) { notify.warning(t('operlog.selectBeforeDelete')); return }
  await batchDeleteOperLogs(checkedKeys.value.map(Number))
  checkedKeys.value = []
  await loadLogs()
  notify.success(t('operlog.deleteBatchSuccess'))
}

const exporting = ref(false)
async function exportLogs() {
  exporting.value = true
  try {
    const params = new URLSearchParams()
    if (filterType.value) params.set('type', filterType.value)
    if (filterUser.value) params.set('user', filterUser.value)
    if (filterMethod.value) params.set('method', filterMethod.value)
    if (dateRange.value) { params.set('startTime', new Date(dateRange.value[0]).toISOString().substring(0,19)); params.set('endTime', new Date(dateRange.value[1]).toISOString().substring(0,19)) }
    const res = await exportOperLogs(Object.fromEntries(params))
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a'); a.href = url; a.download = `${t('nav.operlog')}.csv`; a.click()
    URL.revokeObjectURL(url)
    notify.success(t('operlog.exportSuccess'))
  } catch { notify.error(t('operlog.exportFailed')) }
  exporting.value = false
}

function handleCheck(rowKeys: (string | number)[]) {
  checkedKeys.value = rowKeys
}

onMounted(loadLogs)
onBeforeUnmount(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<template>
  <div class="view-pad">
    <n-card :title="t('nav.operlog')">
      <template #header-extra>
        <n-select v-model:value="filterType" :options="typeOptions" size="small" class="op-filter-type" @update:value="searchLogs" />
        <n-input v-model:value="filterUser" :placeholder="t('operlog.operator')" size="small" class="op-filter-input" clearable @keyup:enter="searchLogs" @clear="searchLogs" />
        <n-select v-model:value="filterMethod" :options="[{label:t('operlog.methodLabel'),value:''},{label:'GET',value:'GET'},{label:'POST',value:'POST'},{label:'PUT',value:'PUT'},{label:'DELETE',value:'DELETE'}]" size="small" class="op-filter-method" @update:value="searchLogs" />
        <n-date-picker v-model:value="dateRange" type="datetimerange" size="small" class="op-filter-date" clearable @update:value="searchLogs" />
        <n-button size="small" @click="searchLogs" class="op-mr-8">{{ t('common.search') }}</n-button>
        <n-button size="small" @click="toggleAutoRefresh" :type="autoRefresh ? 'success' : 'default'" class="op-mr-8">{{ autoRefresh ? t('operlog.autoRefreshing') : t('operlog.autoRefresh') }}</n-button>
        <n-button size="small" :loading="exporting" @click="exportLogs" class="op-mr-8">{{ t('operlog.exportCsv') }}</n-button>
        <n-popconfirm @positive-click="batchDelete" v-if="checkedKeys.length > 0">
          <template #trigger><n-button type="warning" size="small" class="op-mr-8">{{ t('operlog.batchDelete') }}({{ checkedKeys.length }})</n-button></template>
          {{ t('operlog.confirmDeleteSelected', { n: checkedKeys.length }) }}
        </n-popconfirm>
        <n-popconfirm @positive-click="cleanLogs">
          <template #trigger><n-button type="error" size="small" secondary>{{ t('operlog.clean') }}</n-button></template>
          {{ t('operlog.confirmCleanAll') }}
        </n-popconfirm>
      </template>
      <v-chart v-if="showChart" :option="chartOption" class="op-chart" autoresize />
      <n-data-table
        :columns="columns" :data="logs" :loading="loading" size="small" :bordered="false"
        :pagination="{ page: page, pageSize: pageSize, itemCount: total, pageSizes: [10,20,50,100], prefix: ({ itemCount }) => t('operlog.totalItems', { n: itemCount }) }"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        remote
        :row-key="(row: OperLog) => row.id" :max-height="600"
        :checked-row-keys="checkedKeys" @update:checked-row-keys="handleCheck"
      />

      <n-modal v-model:show="detailVisible" :title="t('operlog.detailTitle')" class="crud-modal">
        <div v-if="detailLog" class="op-detail-scroll">
          <p><strong>{{ t('operlog.title') }}：</strong>{{ detailLog.title }}</p>
          <p><strong>{{ t('operlog.bizType') }}：</strong>{{ businessTypeMap[detailLog.businessType] || detailLog.businessType }}</p>
          <p><strong>{{ t('operlog.method') }}：</strong>{{ detailLog.method }}</p>
          <p><strong>{{ t('operlog.operator') }}：</strong>{{ detailLog.operName }}</p>
          <p><strong>{{ t('operlog.ip') }}：</strong>{{ detailLog.operIp }}</p>
          <p><strong>{{ t('operlog.url') }}：</strong>{{ detailLog.operUrl }}</p>
          <p><strong>{{ t('operlog.status') }}：</strong>{{ detailLog.status === 0 ? t('operlog.success') : t('operlog.fail') }}</p>
          <p><strong>{{ t('operlog.cost') }}：</strong>{{ detailLog.costTime }}ms</p>
          <p><strong>{{ t('operlog.time') }}：</strong>{{ detailLog.operTime }}</p>
          <p v-if="detailLog.errorMsg"><strong>{{ t('operlog.errorMsg') }}：</strong>{{ detailLog.errorMsg }}</p>
          <details>
            <summary><strong>{{ t('operlog.requestParam') }}</strong></summary>
            <pre class="op-code-block">{{ detailLog.operParam }}</pre>
          </details>
          <details class="mt-8">
            <summary><strong>{{ t('operlog.responseResult') }}</strong></summary>
            <pre class="op-code-block">{{ detailLog.jsonResult }}</pre>
          </details>
        </div>
      </n-modal>
    </n-card>
  </div>
</template>

<style scoped>
.op-filter-type { width: 120px; margin-right: 8px; }
.op-filter-input { width: 100px; margin-right: 4px; }
.op-filter-method { width: 80px; margin-right: 4px; }
.op-filter-date { width: 240px; margin-right: 4px; }
.op-mr-8 { margin-right: 8px; }
.op-chart { height: 180px; margin-bottom: 12px; }
.op-detail-scroll { max-height: 500px; overflow-y: auto; }
.op-code-block { white-space: pre-wrap; word-break: break-all; background: var(--n-color-modal, #f5f5f5); padding: 8px; border-radius: 4px; max-height: 200px; overflow: auto; }
.mt-8 { margin-top: 8px; }
</style>
