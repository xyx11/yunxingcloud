<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { useI18n } from 'vue-i18n'
import {
  NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem,
  NInput, NSelect, NSpace, NPopconfirm, NTag,
  darkTheme, lightTheme
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

interface SysJob {
  id: number; jobName: string; jobGroup: string; invokeTarget: string
  cronExpression: string; misfirePolicy: string; concurrent: string
  status: string; remark: string; createdAt: string; updatedAt: string
}

const { t } = useI18n()
const notify = useNotify()
const currentTheme = computed(() => localStorage.getItem("theme") === "dark" ? darkTheme : lightTheme)
const jobs = ref<SysJob[]>([])
const loading = ref(false)
const saving = ref(false)
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
  { title: '任务名称', key: 'jobName', width: 130, sorter: true },
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
    title: '操作', key: 'actions', width: 280,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'tiny', onClick: () => runJob(row.id) }, { default: () => '执行' }),
        row.status === '0'
          ? h(NButton, { size: 'tiny', onClick: () => pauseJob(row.id) }, { default: () => '暂停' })
          : h(NButton, { size: 'tiny', type: 'success', onClick: () => resumeJob(row.id) }, { default: () => '恢复' }),
        h(NButton, { size: 'tiny', onClick: () => viewLogs(row.id, row.jobName) }, { default: () => '日志' }),
        h(NButton, { size: 'tiny', onClick: () => editJob(row) }, { default: () => '编辑' }),
        h(NPopconfirm, { onPositiveClick: () => delJob(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '删除' }),
          default: () => '确认删除?'
        })
      ]
    })
  },
]

async function loadJobs() {
  loading.value = true
  try {
    const res = await request.get('/api/job')
    jobs.value = res.data
  } catch {}
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
  saving.value = true
  try {
    if (editing.value) await request.put(`/api/job/${editing.value.id}`, form.value)
    else await request.post('/api/job', form.value)
    showModal.value = false
    notify.success(editing.value ? '更新成功' : '创建成功')
    await loadJobs()
  } catch (e: any) { notify.error(e.response?.data?.message || '保存失败') } finally { saving.value = false }
}

async function delJob(id: number) {
  await request.delete(`/api/job/${id}`)
  await loadJobs()
}

const showLogModal = ref(false)
const jobLogs = ref<any[]>([])
const logJobName = ref('')

async function pauseJob(id: number) { await request.post(`/api/job/${id}/pause`); await loadJobs() }
async function resumeJob(id: number) { await request.post(`/api/job/${id}/resume`); await loadJobs() }
async function viewLogs(id: number, name: string) {
  logJobName.value = name
  try { const res = await request.get(`/api/job/${id}/logs`); jobLogs.value = res.data } catch {}
  showLogModal.value = true
}

async function runJob(id: number) {
  await request.post(`/api/job/${id}/run`)
  notify.success('任务已触发执行')
}

onMounted(loadJobs)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card :title="t('nav.jobs')">
        <template #header-extra>
          <n-button type="primary" size="small" @click="addJob"><template #icon>＋</template>新增</n-button>
        </template>
        <n-space style="margin-bottom:12px"><n-button size="small" @click="loadJobs" secondary>刷新</n-button></n-space>
        <n-data-table
          :columns="columns" :data="jobs" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }"
          :row-key="(row: SysJob) => row.id"
        />

        <n-modal v-model:show="showModal" :title="editing ? t('common.edit') : t('common.add')" style="width:560px">
          <n-form label-placement="left" label-width="80">
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
              <n-space style="margin-top:4px">
                <n-button size="tiny" @click="form.cronExpression='0/10 * * * * ?'">每10秒</n-button>
                <n-button size="tiny" @click="form.cronExpression='0/30 * * * * ?'">每30秒</n-button>
                <n-button size="tiny" @click="form.cronExpression='0 * * * * ?'">每分钟</n-button>
                <n-button size="tiny" @click="form.cronExpression='0 0/30 * * * ?'">每30分钟</n-button>
                <n-button size="tiny" @click="form.cronExpression='0 0 8 * * ?'">每天8点</n-button>
              </n-space>
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
              <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
              <n-button type="primary" :loading="saving" @click="saveJob">{{ t('common.save') }}</n-button>
            </n-space>
          </template>
        </n-modal>

        <!-- 执行日志弹窗 -->
        <n-modal v-model:show="showLogModal" :title="`执行日志: ${logJobName}`" style="width:700px">
          <n-dataTable
            v-if="jobLogs.length" :columns="[
              {title:'开始时间',key:'startTime',width:150,render:(r:any)=>r.startTime?.substring(0,19)||'-'},
              {title:'结束时间',key:'endTime',width:150,render:(r:any)=>r.endTime?.substring(0,19)||'-'},
              {title:'状态',key:'status',width:60,render:(r:any)=>h(NTag,{type:r.status==='0'?'success':'error',size:'small'},{default:()=>r.status==='0'?'成功':'失败'})},
              {title:'结果',key:'result',width:200,ellipsis:{tooltip:true}},
            ]" :data="jobLogs" size="small" :row-key="(r:any)=>r.id" :max-height="400"
          />
          <span v-else style="color:#999">暂无执行记录</span>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>