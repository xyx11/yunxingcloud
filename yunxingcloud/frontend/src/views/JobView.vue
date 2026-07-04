<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { listJobs, createJob, updateJob, deleteJob, runJob, pauseJob, resumeJob, getJobLogs } from '@/api/job'
import { useNotify } from '@/composables/useNotify'
import { useI18n } from 'vue-i18n'

import {
  NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem,
  NInput, NSelect, NSpace, NPopconfirm, NTag,
} from 'naive-ui'
import type { DataTableColumns, FormRules, FormInst } from 'naive-ui'

interface SysJob {
  id: number; jobName: string; jobGroup: string; invokeTarget: string
  cronExpression: string; misfirePolicy: string; concurrent: string
  status: string; remark: string; createdAt: string; updatedAt: string
}

const { t } = useI18n()
const notify = useNotify()

const jobs = ref<SysJob[]>([])
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editing = ref<SysJob | null>(null)
const formRef = ref<FormInst>()
const rules: FormRules = {
  jobName: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
  invokeTarget: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
  cronExpression: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
}
const form = ref({
  jobName: '', jobGroup: 'DEFAULT', invokeTarget: '', cronExpression: '',
  misfirePolicy: '3', concurrent: '1', status: '0', remark: ''
})

const statusOptions = [
  { label: t('job.statusRunning'), value: '0' },
  { label: t('job.statusPaused'), value: '1' },
]

const misfireOptions = [
  { label: t('job.misfireImmediate'), value: '1' },
  { label: t('job.misfireOnce'), value: '2' },
  { label: t('job.misfireDiscard'), value: '3' },
]

const concurrentOptions = [
  { label: t('job.concurrentAllowed'), value: '1' },
  { label: t('job.concurrentForbidden'), value: '0' },
]

const columns: DataTableColumns<SysJob> = [
  { title: 'ID', key: 'id', width: 50 },
  { title: t('job.name'), key: 'jobName', width: 130, sorter: true },
  { title: t('job.group'), key: 'jobGroup', width: 90 },
  { title: t('job.target'), key: 'invokeTarget', width: 160, ellipsis: { tooltip: true } },
  { title: t('job.cron'), key: 'cronExpression', width: 130 },
  {
    title: t('job.status'), key: 'status', width: 60,
    render: (row) => h(NTag, { type: row.status === '0' ? 'success' : 'default', size: 'small' },
      { default: () => row.status === '0' ? t('job.statusRunning') : t('job.statusPaused') })
  },
  {
    title: t('job.concurrent'), key: 'concurrent', width: 60,
    render: (row) => h(NTag, { type: row.concurrent === '1' ? 'info' : 'warning', size: 'small' },
      { default: () => row.concurrent === '1' ? t('job.concurrentAllowed') : t('job.concurrentForbidden') })
  },
  { title: t('job.remark'), key: 'remark', width: 120, ellipsis: { tooltip: true } },
  {
    title: t('job.actions'), key: 'actions', width: 280,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'tiny', onClick: () => runJobById(row.id) }, { default: () => t('job.run') }),
        row.status === '0'
          ? h(NButton, { size: 'tiny', onClick: () => pauseJobById(row.id) }, { default: () => t('job.pause') })
          : h(NButton, { size: 'tiny', type: 'success', onClick: () => resumeJobById(row.id) }, { default: () => t('job.resume') }),
        h(NButton, { size: 'tiny', onClick: () => viewLogs(row.id, row.jobName) }, { default: () => t('job.logs') }),
        h(NButton, { size: 'tiny', onClick: () => editJob(row) }, { default: () => t('common.edit') }),
        h(NPopconfirm, { onPositiveClick: () => delJob(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }),
          default: () => t('common.confirmDelete')
        })
      ]
    })
  },
]

async function loadJobs() {
  loading.value = true
  try {
    const res = await listJobs()
    jobs.value = res.data
  } catch { notify.error(t('common.error')); }
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
  if (formRef.value) { try { await formRef.value.validate() } catch { return } }
  saving.value = true
  try {
    if (editing.value) await updateJob(editing.value.id, form.value)
    else await createJob(form.value)
    showModal.value = false
    notify.success(editing.value ? t('job.updateSuccess') : t('job.createSuccess'))
    await loadJobs()
  } catch (e: any) { notify.error(e.response?.data?.message || t('common.saveFailed')) } finally { saving.value = false }
}

async function delJob(id: number) {
  await deleteJob(id)
  await loadJobs()
}

const showLogModal = ref(false)
const jobLogs = ref<any[]>([])
const logJobName = ref('')

async function pauseJobById(id: number) { await pauseJob(id); await loadJobs() }
async function resumeJobById(id: number) { await resumeJob(id); await loadJobs() }
async function viewLogs(id: number, name: string) {
  logJobName.value = name
  try { const res = await getJobLogs(id); jobLogs.value = res.data } catch { notify.error(t('common.error')); }
  showLogModal.value = true
}

async function runJobById(id: number) {
  await runJob(id)
  notify.success(t('job.triggerSuccess'))
}

onMounted(loadJobs)
</script>

<template>
  <div style="padding:20px">
    <n-card :title="t('nav.jobs')">
      <template #header-extra>
        <n-button type="primary" size="small" @click="addJob"><template #icon>＋</template>{{ t('common.add') }}</n-button>
      </template>
      <n-space style="margin-bottom:12px"><n-button size="small" @click="loadJobs" secondary>{{ t('common.refresh') }}</n-button></n-space>
      <n-data-table
        :columns="columns" :data="jobs" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }"
        :row-key="(row: SysJob) => row.id"
      />

      <n-drawer v-model:show="showModal" :width="460" placement="right">
        <n-drawer-content :title="editing ? t('job.edit') : t('job.add')" closable>
          <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveJob">{{ t('common.save') }}</n-button></n-space></template>
          <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80" size="small">
            <n-form-item :label="t('job.name')"><n-input v-model:value="form.jobName" /></n-form-item>
            <n-form-item :label="t('job.group')"><n-input v-model:value="form.jobGroup" /></n-form-item>
            <n-form-item :label="t('job.target')"><n-input v-model:value="form.invokeTarget" placeholder="com.example.Task.method" /></n-form-item>
            <n-form-item :label="t('job.cron')">
              <n-input v-model:value="form.cronExpression" placeholder="0/10 * * * * ?" />
              <n-space style="margin-top:4px">
                <n-button size="tiny" @click="form.cronExpression='0/10 * * * * ?'">{{ t('job.cron10s') }}</n-button>
                <n-button size="tiny" @click="form.cronExpression='0/30 * * * * ?'">{{ t('job.cron30s') }}</n-button>
                <n-button size="tiny" @click="form.cronExpression='0 * * * * ?'">{{ t('job.cron1m') }}</n-button>
                <n-button size="tiny" @click="form.cronExpression='0 0/30 * * * ?'">{{ t('job.cron30m') }}</n-button>
                <n-button size="tiny" @click="form.cronExpression='0 0 8 * * ?'">{{ t('job.cron8h') }}</n-button>
              </n-space>
            </n-form-item>
            <n-form-item :label="t('job.status')"><n-select v-model:value="form.status" :options="statusOptions" /></n-form-item>
            <n-form-item :label="t('job.concurrentLabel')"><n-select v-model:value="form.concurrent" :options="concurrentOptions" /></n-form-item>
            <n-form-item :label="t('job.misfireLabel')"><n-select v-model:value="form.misfirePolicy" :options="misfireOptions" /></n-form-item>
            <n-form-item :label="t('job.remark')"><n-input v-model:value="form.remark" type="textarea" /></n-form-item>
          </n-form>
        </n-drawer-content>
      </n-drawer>

      <!-- 执行日志弹窗 -->
      <n-modal v-model:show="showLogModal" :title="`${t('job.logsTitle')}: ${logJobName}`" preset="card" display-directive="show" style="max-width:700px;width:95%">
        <n-dataTable
          v-if="jobLogs.length" :columns="[
            {title:t('job.startTime'),key:'startTime',width:150,render:(r:any)=>r.startTime?.substring(0,19)||'-'},
            {title:t('job.endTime'),key:'endTime',width:150,render:(r:any)=>r.endTime?.substring(0,19)||'-'},
            {title:t('job.status'),key:'status',width:60,render:(r:any)=>h(NTag,{type:r.status==='0'?'success':'error',size:'small'},{default:()=>r.status==='0'?t('common.success'):t('common.error')})},
            {title:t('job.result'),key:'result',width:200,ellipsis:{tooltip:true}},
          ]" :data="jobLogs" size="small" :row-key="(r:any)=>r.id" :max-height="400"
        />
        <span v-else style="color:#999">{{ t('job.noLogs') }}</span>
      </n-modal>
    </n-card>
  </div>
</template>