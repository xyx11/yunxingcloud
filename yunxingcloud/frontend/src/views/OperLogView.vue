<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import {
  NConfigProvider, NCard, NDataTable, NButton, NTag, NPopconfirm, NSpace, NInput, NSelect, NDatePicker,
  darkTheme, lightTheme
} from 'naive-ui'
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
const currentTheme = computed(() => localStorage.getItem("theme") === "dark" ? darkTheme : lightTheme)
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
  { label: '全部类型', value: undefined },
  { label: '新增', value: 'INSERT' },
  { label: '修改', value: 'UPDATE' },
  { label: '删除', value: 'DELETE' },
]

const businessTypeMap: Record<string, string> = {
  INSERT: '新增', UPDATE: '修改', DELETE: '删除', OTHER: '其他'
}

const columns: DataTableColumns<OperLog> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', width: 120 },
  {
    title: '业务类型', key: 'businessType', width: 80,
    render: (row) => h(NTag, { type: row.businessType === 'DELETE' ? 'error' : row.businessType === 'INSERT' ? 'success' : 'info', size: 'small' },
      { default: () => businessTypeMap[row.businessType] || row.businessType })
  },
  { title: '请求方法', key: 'method', width: 100 },
  { title: '操作人', key: 'operName', width: 100 },
  { title: 'IP', key: 'operIp', width: 120 },
  { title: '请求URL', key: 'operUrl', width: 180, ellipsis: { tooltip: true } },
  {
    title: '状态', key: 'status', width: 60,
    render: (row) => h(NTag, { type: row.status === 0 ? 'success' : 'error', size: 'small' },
      { default: () => row.status === 0 ? '成功' : '失败' })
  },
  { title: '耗时(ms)', key: 'costTime', width: 80, sorter: true },
  { title: '操作时间', key: 'operTime', width: 160, sorter: true },
  {
    title: '操作', key: 'actions', width: 120,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => showDetail(row) }, { default: () => '详情' }),
        h(NPopconfirm, { onPositiveClick: () => delLog(row.id) }, {
          trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
          default: () => '确认删除?'
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
    const params: any = { page: page.value, pageSize: pageSize.value }
    if (filterType.value) params.type = filterType.value
    if (filterUser.value) params.user = filterUser.value
    if (filterMethod.value) params.method = filterMethod.value
    if (dateRange.value) {
      params.startTime = new Date(dateRange.value[0]).toISOString().substring(0,19)
      params.endTime = new Date(dateRange.value[1]).toISOString().substring(0,19)
    }
    const res = await request.get('/api/operlog', { params })
    logs.value = res.data.items
    // 加载图表数据
    try {
      const statsRes = await request.get('/api/stats/dashboard')
      if (statsRes.data.bizTypeDist?.length) {
        chartOption.value.xAxis.data = statsRes.data.bizTypeDist.map((d:any)=>d.name)
        chartOption.value.series[0].data = statsRes.data.bizTypeDist.map((d:any)=>d.value)
        showChart.value = true
      }
    } catch {}
    total.value = res.data.total || 0
  } catch {}
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
  await request.delete(`/api/operlog/${id}`)
  await loadLogs()
}

async function cleanLogs() {
  await request.delete('/api/operlog/clean')
  checkedKeys.value = []
  await loadLogs()
}

async function batchDelete() {
  if (checkedKeys.value.length === 0) { notify.warning('请先选择日志'); return }
  await request.delete('/api/operlog/batch', { data: { ids: checkedKeys.value } })
  checkedKeys.value = []
  await loadLogs()
  notify.success('批量删除成功')
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
    const res = await request.get(`/api/operlog/export?${params.toString()}`, { responseType: 'blob' })
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a'); a.href = url; a.download = '操作日志.csv'; a.click()
    URL.revokeObjectURL(url)
    notify.success('导出成功')
  } catch { notify.error('导出失败') }
  exporting.value = false
}

function handleCheck(rowKeys: (string | number)[]) {
  checkedKeys.value = rowKeys
}

onMounted(loadLogs)
onBeforeUnmount(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card title="操作日志">
        <template #header-extra>
          <n-select v-model:value="filterType" :options="typeOptions" size="small" style="width:120px;margin-right:8px" @update:value="searchLogs" />
          <n-input v-model:value="filterUser" placeholder="操作人" size="small" style="width:100px;margin-right:4px" clearable @keyup:enter="searchLogs" @clear="searchLogs" />
          <n-select v-model:value="filterMethod" :options="[{label:'方法',value:''},{label:'GET',value:'GET'},{label:'POST',value:'POST'},{label:'PUT',value:'PUT'},{label:'DELETE',value:'DELETE'}]" size="small" style="width:80px;margin-right:4px" @update:value="searchLogs" />
          <n-date-picker v-model:value="dateRange" type="datetimerange" size="small" style="width:240px;margin-right:4px" clearable @update:value="searchLogs" />
          <n-button size="small" @click="searchLogs" style="margin-right:8px">搜索</n-button>
          <n-button size="small" @click="toggleAutoRefresh" :type="autoRefresh ? 'success' : 'default'" style="margin-right:8px">{{ autoRefresh ? '自动刷新中' : '自动刷新' }}</n-button>
          <n-button size="small" :loading="exporting" @click="exportLogs" style="margin-right:8px">导出CSV</n-button>
          <n-popconfirm @positive-click="batchDelete" v-if="checkedKeys.length > 0">
            <template #trigger><n-button type="warning" size="small" style="margin-right:8px">批量删除({{ checkedKeys.length }})</n-button></template>
            确认删除选中的 {{ checkedKeys.length }} 条日志?
          </n-popconfirm>
          <n-popconfirm @positive-click="cleanLogs">
            <template #trigger><n-button type="error" size="small" secondary>清空日志</n-button></template>
            确认清空所有操作日志?
          </n-popconfirm>
        </template>
        <v-chart v-if="showChart" :option="chartOption" style="height:180px;margin-bottom:12px" autoresize />
        <n-data-table
          :columns="columns" :data="logs" :loading="loading" size="small" :bordered="false"
          :pagination="{ page: page, pageSize: pageSize, itemCount: total, pageSizes: [10,20,50,100], prefix: ({ itemCount }) => `共 ${itemCount} 条` }"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
          remote
          :row-key="(row: OperLog) => row.id" :max-height="600"
          :checked-row-keys="checkedKeys" @update:checked-row-keys="handleCheck"
        />

        <n-modal v-model:show="detailVisible" title="日志详情" style="width:640px">
          <div v-if="detailLog" style="max-height:500px; overflow-y:auto">
            <p><strong>标题：</strong>{{ detailLog.title }}</p>
            <p><strong>业务类型：</strong>{{ businessTypeMap[detailLog.businessType] || detailLog.businessType }}</p>
            <p><strong>请求方法：</strong>{{ detailLog.method }}</p>
            <p><strong>操作人：</strong>{{ detailLog.operName }}</p>
            <p><strong>IP：</strong>{{ detailLog.operIp }}</p>
            <p><strong>请求URL：</strong>{{ detailLog.operUrl }}</p>
            <p><strong>状态：</strong>{{ detailLog.status === 0 ? '成功' : '失败' }}</p>
            <p><strong>耗时：</strong>{{ detailLog.costTime }}ms</p>
            <p><strong>时间：</strong>{{ detailLog.operTime }}</p>
            <p v-if="detailLog.errorMsg"><strong>错误信息：</strong>{{ detailLog.errorMsg }}</p>
            <details>
              <summary><strong>请求参数</strong></summary>
              <pre style="white-space:pre-wrap;word-break:break-all;background:var(--n-color-modal, #f5f5f5);padding:8px;border-radius:4px;max-height:200px;overflow:auto">{{ detailLog.operParam }}</pre>
            </details>
            <details style="margin-top:8px">
              <summary><strong>返回结果</strong></summary>
              <pre style="white-space:pre-wrap;word-break:break-all;background:var(--n-color-modal, #f5f5f5);padding:8px;border-radius:4px;max-height:200px;overflow:auto">{{ detailLog.jsonResult }}</pre>
            </details>
          </div>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>