<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import request from '@/api/request'
import {
  NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem,
  NInput, NSelect, NSpace, NPopconfirm, NTag
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

interface SysJob {
  id: number; jobName: string; jobGroup: string; invokeTarget: string
  cronExpression: string; misfirePolicy: string; concurrent: string
  status: string; remark: string; createdAt: string; updatedAt: string
}

const jobs = ref<SysJob[]>([])
const loading = ref(false)
const showModal = ref(false)
const editing = ref<SysJob | null>(null)
const form = ref({
  jobName: '', jobGroup: 'DEFAULT', invokeTarget: '', cronExpression: '',
  misfirePolicy: '3', concurrent: '1', status: '0', remark: ''
})

const statusOptions = [
  { label: '运行', value: '0' },
  { label: '暂停', value: '1' },
]

const misfireOptions = [
  { label: '立即执行', value: '1' },
  { label: '执行一次', value: '2' },
  { label: '放弃执行', value: '3' },
]

const concurrentOptions = [
  { label: '允许', value: '1' },
  { label: '禁止', value: '0' },
]

const columns: DataTableColumns<SysJob> = [
  { title: 'ID', key: 'id', width: 50 },
  { title: '任务名称', key: 'jobName', width: 130 },
  { title: '任务组', key: 'jobGroup', width: 90 },
  { title: '调用目标', key: 'invokeTarget', width: 160, ellipsis: { tooltip: true } },
  { title: 'Cron表达式', key: 'cronExpression', width: 130 },
  {
    title: '状态', key: 'status', width: 60,
    render: (row) => h(NTag, { type: row.status === '0' ? 'success' : 'default', size: 'small' },
      { default: () => row.status === '0' ? '运行' : '暂停' })
  },
  {
    title: '并发', key: 'concurrent', width: 60,
    render: (row) => h(NTag, { type: row.concurrent === '1' ? 'info' : 'warning', size: 'small' },
      { default: () => row.concurrent === '1' ? '允许' : '禁止' })
  },
  { title: '备注', key: 'remark', width: 120, ellipsis: { tooltip: true } },
  {
    title: '操作', key: 'actions', width: 200,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => runJob(row.id) }, { default: () => '执行' }),
        h(NButton, { size: 'small', onClick: () => editJob(row) }, { default: () => '编辑' }),
        h(NPopconfirm, { onPositiveClick: () => delJob(row.id) }, {
          trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
          default: () => '确认删除?'
        })
      ]
    })
  },
]

async function loadJobs() {
  loading.value = true
  const res = await request.get('/api/job')
  jobs.value = res.data
  loading.value = false
}

function addJob() {
  editing.value = null
  form.value = { jobName: '', jobGroup: 'DEFAULT', invokeTarget: '', cronExpression: '', misfirePolicy: '3', concurrent: '1', status: '0', remark: '' }
  showModal.value = true
}

function editJob(job: SysJob) {
  editing.value = job
  form.value = {
    jobName: job.jobName, jobGroup: job.jobGroup, invokeTarget: job.invokeTarget,
    cronExpression: job.cronExpression || '', misfirePolicy: job.misfirePolicy,
    concurrent: job.concurrent, status: job.status, remark: job.remark || ''
  }
  showModal.value = true
}

async function saveJob() {
  if (editing.value) {
    await request.put(`/api/job/${editing.value.id}`, form.value)
  } else {
    await request.post('/api/job', form.value)
  }
  showModal.value = false
  await loadJobs()
}

async function delJob(id: number) {
  await request.delete(`/api/job/${id}`)
  await loadJobs()
}

async function runJob(id: number) {
  await request.post(`/api/job/${id}/run`)
  alert('任务已触发执行')
}

onMounted(loadJobs)
</script>

<template>
  <n-config-provider>
    <div style="padding: 24px">
      <n-card title="定时任务">
        <template #header-extra>
          <n-button type="primary" size="small" @click="addJob">新增任务</n-button>
        </template>
        <n-data-table :columns="columns" :data="jobs" :loading="loading"
          :row-key="(row: SysJob) => row.id" />

        <n-modal v-model:show="showModal" :title="editing ? '编辑任务' : '新增任务'" style="width:560px">
          <n-form label-placement="left" label-width="90">
            <n-form-item label="任务名称">
              <n-input v-model:value="form.jobName" />
            </n-form-item>
            <n-form-item label="任务组">
              <n-input v-model:value="form.jobGroup" />
            </n-form-item>
            <n-form-item label="调用目标">
              <n-input v-model:value="form.invokeTarget" placeholder="com.example.Task.method" />
            </n-form-item>
            <n-form-item label="Cron表达式">
              <n-input v-model:value="form.cronExpression" placeholder="0/10 * * * * ?" />
            </n-form-item>
            <n-form-item label="状态">
              <n-select v-model:value="form.status" :options="statusOptions" />
            </n-form-item>
            <n-form-item label="并发执行">
              <n-select v-model:value="form.concurrent" :options="concurrentOptions" />
            </n-form-item>
            <n-form-item label="错过策略">
              <n-select v-model:value="form.misfirePolicy" :options="misfireOptions" />
            </n-form-item>
            <n-form-item label="备注">
              <n-input v-model:value="form.remark" type="textarea" />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-space justify="end">
              <n-button @click="showModal = false">取消</n-button>
              <n-button type="primary" @click="saveJob">保存</n-button>
            </n-space>
          </template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>