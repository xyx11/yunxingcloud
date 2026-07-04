<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { fetchLoginInfos, fetchLoginStats, deleteLoginInfo, cleanLoginInfos } from '@/api/loginlog'
import { useNotify } from '@/composables/useNotify'

import {
  NCard, NDataTable, NButton, NSelect, NSpace, NPopconfirm, NTag, NInput, NStatistic, NGrid, NGridItem, NDatePicker,
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

const items = ref<LoginInfo[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const filterUser = ref('')
const filterStatus = ref('')
const dateRange = ref<[number, number] | null>(null)
const stats = ref({ todaySuccess: 0, todayFail: 0, todayTotal: 0 })
const hourOption = ref({ tooltip:{trigger:'axis'}, grid:{left:40,right:10,top:10,bottom:20}, xAxis:{type:'category',data:Array.from({length:24},(_,i)=>i + t('loginlog.hourSuffix'))}, yAxis:{type:'value'}, series:[{data:Array(24).fill(0),type:'bar',itemStyle:{color:'#667eea',borderRadius:[3,3,0,0]}}] })

async function loadStats() {
  try { const res = await fetchLoginStats(); stats.value = res.data } catch { notify.error(t('common.error')); }
  try {
    const res = await fetchLoginInfos({ page: 1, pageSize: 200 })
    const hours = new Array(24).fill(0)
    res.data.items?.forEach((l:any) => { if (l.loginTime) { const h = new Date(l.loginTime).getHours(); hours[h]++ } })
    hourOption.value.series[0].data = hours
    hourOption.value = { ...hourOption.value }
  } catch { notify.error(t('common.error')); }
}

const statusOptions = [
  { label: t('common.all'), value: '' },
  { label: t('loginlog.success'), value: '0' },
  { label: t('loginlog.fail'), value: '1' },
]

const columns: DataTableColumns<LoginInfo> = [
  { title: t('loginlog.id'), key: 'id', width: 60 },
  { title: t('loginlog.username'), key: 'userName', width: 100 },
  { title: t('loginlog.ip'), key: 'ipaddr', width: 130 },
  { title: t('loginlog.location'), key: 'loginLocation', width: 120 },
  { title: t('loginlog.browser'), key: 'browser', width: 80 },
  { title: t('loginlog.os'), key: 'os', width: 80 },
  {
    title: t('loginlog.status'), key: 'status', width: 70,
    render: (row) => h(NTag, { type: row.status === '0' ? 'success' : 'error', size: 'small' },
      { default: () => row.status === '0' ? t('loginlog.success') : t('loginlog.fail') })
  },
  { title: t('loginlog.msg'), key: 'msg', width: 160, ellipsis: { tooltip: true } },
  { title: t('loginlog.time'), key: 'loginTime', width: 160 },
  {
    title: t('loginlog.actions'), key: 'actions', width: 80,
    render: (row) => h(NPopconfirm, { onPositiveClick: () => delItem(row.id) }, {
      trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }),
      default: () => t('common.confirmDelete')
    })
  },
]

async function loadItems() {
  loading.value = true
  try {
    const params: any = { page: page.value, pageSize: pageSize.value }
    if (filterUser.value) params.userName = filterUser.value
    if (filterStatus.value) params.status = filterStatus.value
    if (dateRange.value) { params.startTime = new Date(dateRange.value[0]).toISOString().substring(0,19); params.endTime = new Date(dateRange.value[1]).toISOString().substring(0,19) }
    const res = await fetchLoginInfos(params as any)
    items.value = res.data.items
    total.value = res.data.total
  } catch { notify.error(t('common.error')); }
  loading.value = false
}

async function delItem(id: number) {
  await deleteLoginInfo(id)
  await loadItems()
}
const checkedRowKeys = ref<number[]>([])
async function batchDelete() { if(!checkedRowKeys.value.length)return; try{for(const id of checkedRowKeys.value)await deleteLoginInfo(id);checkedRowKeys.value=[];loadItems();notify.success(t('common.deleted'))}catch{notify.error(t('common.saveFailed'))} }

async function cleanAll() {
  try {
    await cleanLoginInfos()
    notify.success(t('loginlog.cleanSuccess'))
    await loadItems()
  } catch { notify.error(t('loginlog.cleanFailed')) }
}

function onPageChange(p: number) { page.value = p; loadItems() }
function onPageSizeChange(s: number) { pageSize.value = s; page.value = 1; loadItems() }

function exportCSV() {
  const head = ['用户名','IP','状态','浏览器','OS','时间']
  const rows = items.value.map(i => [i.userName||'',i.ipaddr||'',i.status||'',i.browser||'',i.os||'',i.loginTime||''])
  const csv = [head,...rows].map(r => r.map(c => '"'+String(c).replace(/"/g,'""')+'"').join(',')).join('\n')
  const blob = new Blob(['﻿'+csv],{type:'text/csv'}); const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = 'login_logs.csv'; a.click()
}
onMounted(() => { loadItems(); loadStats() })
</script>

<template>
  <div style="padding:20px">
    <n-card :title="t('nav.loginlog')">
      <n-grid cols="3" x-gap="12" style="margin-bottom:16px">
        <n-grid-item><n-card size="small"><n-statistic :label="t('loginlog.totalLogins')" :value="stats.todayTotal" /></n-card></n-grid-item>
        <n-grid-item><n-card size="small"><n-statistic :label="t('loginlog.todayLogins')"><template #prefix>✅</template>{{ stats.todaySuccess }}</n-statistic></n-card></n-grid-item>
        <n-grid-item><n-card size="small"><n-statistic :label="t('loginlog.todayFailures')"><template #prefix>⚠️</template>{{ stats.todayFail }}</n-statistic></n-card></n-grid-item>
      </n-grid>
      <v-chart :option="hourOption" style="height:160px;margin-bottom:12px" autoresize />
      <template #header-extra>
        <n-space>
          <n-popconfirm @positive-click="cleanAll">
            <template #trigger><n-button size="small" type="error">{{ t('loginlog.clean') }}</n-button></template>
            {{ t('loginlog.cleanConfirm') }}
          </n-popconfirm>
        </n-space>
      </template>
      <n-space style="margin-bottom:12px" justify="space-between">
        <n-space>
          <n-input v-model:value="filterUser" :placeholder="t('loginlog.username')" size="small" clearable style="width:120px" />
          <n-select v-model:value="filterStatus" :options="statusOptions" size="small" style="width:80px" />
          <n-date-picker v-model:value="dateRange" type="datetimerange" size="small" style="width:240px" clearable @update:value="loadItems" />
          <n-button type="primary" size="small" @click="() => { page = 1; loadItems() }">{{ t('common.search') }}</n-button>
          <n-button size="small" @click="filterUser = ''; filterStatus = ''; page = 1; loadItems()">{{ t('common.reset') }}</n-button>
        </n-space>
        <n-space><n-button v-if="checkedRowKeys.length" type="error" size="small" @click="batchDelete">{{ t('common.batchDelete') }} ({{checkedRowKeys.length}})</n-button><n-button size="small" @click="exportCSV">{{ t('operlog.exportCsv') }}</n-button><n-button size="small" @click="loadItems" secondary>{{ t('common.refresh') }}</n-button></n-space>
      </n-space>
      <n-dataTable v-model:checked-row-keys="checkedRowKeys"
        :columns="columns" :data="items" :loading="loading" size="small"
        :bordered="false" :pagination="{ page: page, pageSize: pageSize, itemCount: total, pageSizes: [10,20,50,100], onChange: onPageChange, onUpdatePageSize: onPageSizeChange }"
        :row-key="(row: LoginInfo) => row.id"
      />
    </n-card>
  </div>
</template>
