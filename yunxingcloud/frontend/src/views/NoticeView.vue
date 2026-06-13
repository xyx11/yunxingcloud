<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import {
  NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem,
  NInput, NSelect, NSpace, NPopconfirm, NTag,
  darkTheme, lightTheme
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import type { DataTableColumns } from 'naive-ui'

interface Notice {
  id: number; noticeTitle: string; noticeType: string; noticeContent: string
  status: string; remark: string; createdAt: string; updatedAt: string
}

const { t } = useI18n()
const notify = useNotify()
const currentTheme = computed(() => localStorage.getItem("theme") === "dark" ? darkTheme : lightTheme)
const notices = ref<Notice[]>([])
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editing = ref<Notice | null>(null)
const form = ref({ noticeTitle: '', noticeType: '1', noticeContent: '', status: '0', remark: '' })
const searchKeyword = ref('')

const typeOptions = [
  { label: '通知', value: '1' },
  { label: '公告', value: '2' },
]

const statusOptions = [
  { label: t('user.enabledLabel'), value: '0' },
  { label: '关闭', value: '1' },
]

const filteredNotices = computed(() => {
  const kw = searchKeyword.value.toLowerCase()
  if (!kw) return notices.value
  return notices.value.filter(n => n.noticeTitle.toLowerCase().includes(kw))
})

const columns: DataTableColumns<Notice> = [
  { title: 'ID', key: 'id', width: 50 },
  { title: '标题', key: 'noticeTitle', width: 180, ellipsis: { tooltip: true } },
  {
    title: '类型', key: 'noticeType', width: 60,
    render: (row) => h(NTag, { type: row.noticeType === '1' ? 'info' : 'warning', size: 'small' },
      { default: () => row.noticeType === '1' ? '通知' : '公告' })
  },
  { title: '内容', key: 'noticeContent', width: 200, ellipsis: { tooltip: true }, render: (row: any) => h('div', { innerHTML: (row.noticeContent || '').replace(/<[^>]+>/g,'').substring(0, 50) }) },
  {
    title: '状态', key: 'status', width: 60,
    render: (row) => h(NTag, { type: row.status === '0' ? 'success' : 'default', size: 'small' },
      { default: () => row.status === '0' ? t('user.enabledLabel') : '关闭' })
  },
  { title: '创建时间', key: 'createdAt', width: 150 },
  {
    title: '操作', key: 'actions', width: 120,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'tiny', onClick: () => editNotice(row) }, { default: () => '编辑' }),
        h(NPopconfirm, { onPositiveClick: () => delNotice(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '删除' }),
          default: () => '确认删除?'
        })
      ]
    })
  },
]

async function loadNotices() {
  loading.value = true
  try { const res = await request.get('/api/notices'); notices.value = res.data } catch {}
  loading.value = false
}

function addNotice() {
  editing.value = null
  form.value = { noticeTitle: '', noticeType: '1', noticeContent: '', status: '0', remark: '' }
  showModal.value = true
}

function editNotice(notice: Notice) {
  editing.value = notice
  form.value = { noticeTitle: notice.noticeTitle, noticeType: notice.noticeType, noticeContent: notice.noticeContent || '', status: notice.status || '0', remark: notice.remark || '' }
  showModal.value = true
}

async function saveNotice() {
  saving.value = true
  try {
    if (editing.value) await request.put(`/api/notices/${editing.value.id}`, form.value)
    else await request.post('/api/notices', form.value)
    showModal.value = false
    notify.success(editing.value ? '更新成功' : '创建成功')
    await loadNotices()
  } catch (e: any) { notify.error(e.response?.data?.message || '保存失败') } finally { saving.value = false }
}

async function delNotice(id: number) {
  await request.delete(`/api/notices/${id}`)
  await loadNotices()
}

onMounted(loadNotices)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card :title="t('nav.notice')">
        <template #header-extra>
          <n-button type="primary" size="small" @click="addNotice"><template #icon>＋</template>新增</n-button>
        </template>
        <n-space style="margin-bottom:12px" justify="space-between">
          <n-space>
            <n-input v-model:value="searchKeyword" placeholder="标题" size="small" clearable style="width:180px" />
            <n-button type="primary" size="small" @click="() => {}">搜索</n-button>
            <n-button size="small" @click="searchKeyword = ''">重置</n-button>
          </n-space>
          <n-space><n-button size="small" @click="loadNotices" secondary>刷新</n-button></n-space>
        </n-space>
        <n-dataTable
          :columns="columns" :data="filteredNotices" :loading="loading" size="small"
          :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }"
          :row-key="(row: Notice) => row.id"
        />

        <n-modal v-model:show="showModal" :title="editing ? t('common.edit') : t('common.add')" style="width:560px">
          <n-form label-placement="left" label-width="80">
            <n-form-item label="标题">
              <n-input v-model:value="form.noticeTitle" />
            </n-form-item>
            <n-form-item label="类型">
              <n-select v-model:value="form.noticeType" :options="typeOptions" />
            </n-form-item>
            <n-form-item label="内容">
              <n-input v-model:value="form.noticeContent" type="textarea" :rows="5" placeholder="支持HTML标签: <b>粗体</b> <br>换行 <a href=''>链接</a>" />
            </n-form-item>
            <n-form-item label="状态">
              <n-select v-model:value="form.status" :options="statusOptions" />
            </n-form-item>
            <n-form-item label="备注">
              <n-input v-model:value="form.remark" />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-space justify="end">
              <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
              <n-button type="primary" :loading="saving" @click="saveNotice">{{ t('common.save') }}</n-button>
            </n-space>
          </template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
