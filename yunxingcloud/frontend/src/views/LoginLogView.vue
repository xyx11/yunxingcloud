<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import {
  NConfigProvider, NCard, NDataTable, NButton, NSelect, NSpace, NPopconfirm, NTag, NInput, NStatistic, NGrid, NGridItem,
  darkTheme, lightTheme
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import type { DataTableColumns } from 'naive-ui'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
use([BarChart, GridComponent, TooltipComponent, CanvasRenderer])

interface LoginInfo {
  id: number; userName: string; ipaddr: string; loginLocation: string
  browser: string; os: string; status: string; msg: string; loginTime: string
}

const { t } = useI18n()
const notify = useNotify()
const currentTheme = computed(() => localStorage.getItem("theme") === "dark" ? darkTheme : lightTheme)
const items = ref<LoginInfo[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const filterUser = ref('')
const filterStatus = ref('')
const stats = ref({ todaySuccess: 0, todayFail: 0, todayTotal: 0 })
const hourOption = ref({ tooltip:{trigger:'axis'}, grid:{left:40,right:10,top:10,bottom:20}, xAxis:{type:'category',data:Array.from({length:24},(_,i)=>i+'时')}, yAxis:{type:'value'}, series:[{data:Array(24).fill(0),type:'bar',itemStyle:{color:'#667eea',borderRadius:[3,3,0,0]}}] })

async function loadStats() {
  try { const res = await request.get('/api/logininfor/stats'); stats.value = res.data } catch {}
  try {
    const res = await request.get('/api/logininfor?pageSize=200')
    const hours = new Array(24).fill(0)
    res.data.items?.forEach((l:any) => { if (l.loginTime) { const h = new Date(l.loginTime).getHours(); hours[h]++ } })
    hourOption.value.series[0].data = hours
    hourOption.value = { ...hourOption.value }
  } catch {}
}

const statusOptions = [
  { label: '全部', value: '' },
  { label: '成功', value: '0' },
  { label: '失败', value: '1' },
]

const columns: DataTableColumns<LoginInfo> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '用户名', key: 'userName', width: 100 },
  { title: 'IP地址', key: 'ipaddr', width: 130 },
  { title: '登录地点', key: 'loginLocation', width: 120 },
  { title: '浏览器', key: 'browser', width: 80 },
  { title: '操作系统', key: 'os', width: 80 },
  {
    title: '状态', key: 'status', width: 70,
    render: (row) => h(NTag, { type: row.status === '0' ? 'success' : 'error', size: 'small' },
      { default: () => row.status === '0' ? '成功' : '失败' })
  },
  { title: '消息', key: 'msg', width: 160, ellipsis: { tooltip: true } },
  { title: '登录时间', key: 'loginTime', width: 160 },
  {
    title: '操作', key: 'actions', width: 80,
    render: (row) => h(NPopconfirm, { onPositiveClick: () => delItem(row.id) }, {
      trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '删除' }),
      default: () => '确认删除?'
    })
  },
]

async function loadItems() {
  loading.value = true
  try {
    const params: any = { page: page.value, pageSize: pageSize.value }
    if (filterUser.value) params.userName = filterUser.value
    if (filterStatus.value) params.status = filterStatus.value
    const res = await request.get('/api/logininfor', { params })
    items.value = res.data.items
    total.value = res.data.total
  } catch {}
  loading.value = false
}

async function delItem(id: number) {
  await request.delete(`/api/logininfor/${id}`)
  await loadItems()
}

async function cleanAll() {
  try {
    await request.delete('/api/logininfor/clean')
    notify.success('登录日志已清空')
    await loadItems()
  } catch (e: any) { notify.error('清空失败') }
}

function onPageChange(p: number) { page.value = p; loadItems() }
function onPageSizeChange(s: number) { pageSize.value = s; page.value = 1; loadItems() }

onMounted(() => { loadItems(); loadStats() })
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card :title="t('nav.loginlog')">
        <n-grid cols="3" x-gap="12" style="margin-bottom:16px">
          <n-grid-item><n-card size="small"><n-statistic label="总登录次数" :value="stats.todayTotal" /></n-card></n-grid-item>
          <n-grid-item><n-card size="small"><n-statistic label="今日成功"><template #prefix>✅</template>{{ stats.todaySuccess }}</n-statistic></n-card></n-grid-item>
          <n-grid-item><n-card size="small"><n-statistic label="今日失败"><template #prefix>⚠️</template>{{ stats.todayFail }}</n-statistic></n-card></n-grid-item>
        </n-grid>
        <v-chart :option="hourOption" style="height:160px;margin-bottom:12px" autoresize />
        <template #header-extra>
          <n-space>
            <n-popconfirm @positive-click="cleanAll">
              <template #trigger><n-button size="small" type="error">清空日志</n-button></template>
              确认清空所有登录日志?
            </n-popconfirm>
          </n-space>
        </template>
        <n-space style="margin-bottom:12px" justify="space-between">
          <n-space>
            <n-input v-model:value="filterUser" placeholder="用户名" size="small" clearable style="width:120px" />
            <n-select v-model:value="filterStatus" :options="statusOptions" size="small" style="width:80px" />
            <n-button type="primary" size="small" @click="() => { page = 1; loadItems() }">搜索</n-button>
            <n-button size="small" @click="filterUser = ''; filterStatus = ''; page = 1; loadItems()">重置</n-button>
          </n-space>
          <n-space><n-button size="small" @click="loadItems" secondary>刷新</n-button></n-space>
        </n-space>
        <n-dataTable
          :columns="columns" :data="items" :loading="loading" size="small"
          :bordered="false" :pagination="{ page: page, pageSize: pageSize, itemCount: total, pageSizes: [10,20,50,100], onChange: onPageChange, onUpdatePageSize: onPageSizeChange }"
          :row-key="(row: LoginInfo) => row.id"
        />
      </n-card>
    </div>
  </n-config-provider>
</template>
