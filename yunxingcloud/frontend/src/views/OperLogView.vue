<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import {
  NConfigProvider, NCard, NDataTable, NButton, NTag, NPopconfirm, NSpace, NInput, NSelect
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

interface OperLog {
  id: number; title: string; businessType: string; method: string
  operName: string; operIp: string; operUrl: string; operParam: string
  jsonResult: string; status: number; errorMsg: string; costTime: number; operTime: string
}

const notify = useNotify()
const logs = ref<OperLog[]>([])
const loading = ref(false)
const checkedKeys = ref<number[]>([])
const filterType = ref<string | null>(null)
const filterUser = ref('')

const typeOptions = [
  { label: '全部类型', value: null },
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
  const params: any = {}
  if (filterType.value) params.type = filterType.value
  if (filterUser.value) params.user = filterUser.value
  const res = await request.get('/api/operlog', { params })
  logs.value = res.data
  loading.value = false
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

function exportLogs() {
  window.open('/api/operlog/export', '_blank')
}

function handleCheck(rowKeys: number[]) {
  checkedKeys.value = rowKeys
}

onMounted(loadLogs)
</script>

<template>
  <n-config-provider>
    <div style="padding: 24px">
      <n-card title="操作日志">
        <template #header-extra>
          <n-select v-model:value="filterType" :options="typeOptions" size="small" style="width:120px;margin-right:8px" @update:value="loadLogs" />
          <n-input v-model:value="filterUser" placeholder="操作人" size="small" style="width:120px;margin-right:8px" clearable @keyup:enter="loadLogs" @clear="loadLogs" />
          <n-button size="small" @click="loadLogs" style="margin-right:8px">搜索</n-button>
          <n-button size="small" @click="exportLogs" style="margin-right:8px">导出CSV</n-button>
          <n-popconfirm @positive-click="batchDelete" v-if="checkedKeys.length > 0">
            <template #trigger><n-button type="warning" size="small" style="margin-right:8px">批量删除({{ checkedKeys.length }})</n-button></template>
            确认删除选中的 {{ checkedKeys.length }} 条日志?
          </n-popconfirm>
          <n-popconfirm @positive-click="cleanLogs">
            <template #trigger><n-button type="error" size="small" secondary>清空日志</n-button></template>
            确认清空所有操作日志?
          </n-popconfirm>
        </template>
        <n-data-table :columns="columns" :data="logs" :loading="loading" :pagination="{ pageSize: 10 }"
          :row-key="(row: OperLog) => row.id" :max-height="600"
          :checked-row-keys="checkedKeys" @update:checked-row-keys="handleCheck" />

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
              <pre style="white-space:pre-wrap;word-break:break-all;background:#f5f5f5;padding:8px;border-radius:4px;max-height:200px;overflow:auto">{{ detailLog.operParam }}</pre>
            </details>
            <details style="margin-top:8px">
              <summary><strong>返回结果</strong></summary>
              <pre style="white-space:pre-wrap;word-break:break-all;background:#f5f5f5;padding:8px;border-radius:4px;max-height:200px;overflow:auto">{{ detailLog.jsonResult }}</pre>
            </details>
          </div>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>