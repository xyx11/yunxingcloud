<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { fetchNotices, createNotice, updateNotice, deleteNotice } from '@/api/notice'
import { useNotify } from '@/composables/useNotify'

import {
  NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem,
  NInput, NSelect, NSpace, NPopconfirm, NTag,
  
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import type { DataTableColumns, FormRules, FormInst } from 'naive-ui'

interface Notice {
  id: number; noticeTitle: string; noticeType: string; noticeContent: string
  status: string; remark: string; createdAt: string; updatedAt: string
}

const { t } = useI18n()
const notify = useNotify()

const notices = ref<Notice[]>([])
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editing = ref<Notice | null>(null)
const formRef = ref<FormInst>()
const rules: FormRules = { noticeTitle: [{ required: true, message: t('validate.required'), trigger: 'blur' }] }
const form = ref({ noticeTitle: '', noticeType: '1', noticeContent: '', status: '0', remark: '' })
const searchKeyword = ref('')

const typeOptions = [
  { label: t('notice.types.notice'), value: '1' },
  { label: t('notice.types.announcement'), value: '2' },
]

const statusOptions = [
  { label: t('user.enabledLabel'), value: '0' },
  { label: t('notice.statusClosed'), value: '1' },
]

const filteredNotices = computed(() => {
  const kw = searchKeyword.value.toLowerCase()
  if (!kw) return notices.value
  return notices.value.filter(n => n.noticeTitle.toLowerCase().includes(kw))
})

const columns: DataTableColumns<Notice> = [
  { title: t('notice.id'), key: 'id', width: 50 },
  { title: t('notice.title'), key: 'noticeTitle', width: 180, ellipsis: { tooltip: true } },
  {
    title: t('notice.type'), key: 'noticeType', width: 60,
    render: (row) => h(NTag, { type: row.noticeType === '1' ? 'info' : 'warning', size: 'small' },
      { default: () => row.noticeType === '1' ? t('notice.types.notice') : t('notice.types.announcement') })
  },
  { title: t('notice.content'), key: 'noticeContent', width: 200, ellipsis: { tooltip: true }, render: (row: any) => h('div', { innerHTML: (row.noticeContent || '').replace(/<[^>]+>/g,'').substring(0, 50) }) },
  {
    title: t('notice.status'), key: 'status', width: 60,
    render: (row) => h(NTag, { type: row.status === '0' ? 'success' : 'default', size: 'small' },
      { default: () => row.status === '0' ? t('user.enabledLabel') : t('notice.statusClosed') })
  },
  { title: t('common.createdAt'), key: 'createdAt', width: 150 },
  {
    title: t('notice.actions'), key: 'actions', width: 120,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'tiny', onClick: () => editNotice(row) }, { default: () => t('common.edit') }),
        h(NPopconfirm, { onPositiveClick: () => delNotice(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }),
          default: () => t('common.confirmDelete')
        })
      ]
    })
  },
]

async function loadNotices() {
  loading.value = true
  try { const res = await fetchNotices(); notices.value = res.data } catch { notify.error(t('common.error')); }
  loading.value = false
}

function doSearch() { /* reactive search */ }

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
  if (formRef.value) { try { await formRef.value.validate() } catch { return } }
  saving.value = true
  try {
    if (editing.value) await updateNotice(editing.value.id, form.value)
    else await createNotice(form.value)
    showModal.value = false
    notify.success(editing.value ? t('notice.updateSuccess') : t('notice.createSuccess'))
    await loadNotices()
  } catch (e: any) { notify.error(e.response?.data?.message || t('common.saveFailed')) } finally { saving.value = false }
}

async function delNotice(id: number) {
  await deleteNotice(id)
  await loadNotices()
}

const checkedRowKeys = ref<number[]>([])
async function batchDelete() { if(!checkedRowKeys.value.length)return; try{for(const id of checkedRowKeys.value)await deleteNotice(id);checkedRowKeys.value=[];loadNotices();notify.success(t('common.deleted'))}catch{notify.error(t('common.saveFailed'))} }
onMounted(loadNotices)
</script>

<template>
  <div class="view-pad">
    <n-card :title="t('nav.notice')">
      <template #header-extra>
        <n-space><n-button v-if="checkedRowKeys.length" type="error" size="small" @click="batchDelete">{{ t('common.batchDelete') }} ({{ checkedRowKeys.length }})</n-button><n-button type="primary" size="small" @click="addNotice"><template #icon>＋</template>{{ t('common.add') }}</n-button></n-space>
      </template>
      <n-space class="mb-12" justify="space-between">
        <n-space>
          <n-input v-model:value="searchKeyword" :placeholder="t('notice.searchPlaceholder')" size="small" clearable style="width:180px" />
          <n-button type="primary" size="small" @click="doSearch">{{ t('common.search') }}</n-button>
          <n-button size="small" @click="searchKeyword = ''">{{ t('common.reset') }}</n-button>
        </n-space>
        <n-space><n-button size="small" @click="loadNotices" secondary>{{ t('common.refresh') }}</n-button></n-space>
      </n-space>
      <n-dataTable
        :columns="columns" :data="filteredNotices" :loading="loading" size="small" v-model:checked-row-keys="checkedRowKeys"
        :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }"
        :row-key="(row: Notice) => row.id"
      />

      <n-drawer v-model:show="showModal" :width="480" placement="right">
        <n-drawer-content :title="editing ? t('notice.edit') : t('notice.add')" closable>
          <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveNotice">{{ t('common.save') }}</n-button></n-space></template>
          <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80" size="small">
            <n-form-item :label="t('notice.title')"><n-input v-model:value="form.noticeTitle" /></n-form-item>
            <n-form-item :label="t('notice.type')"><n-select v-model:value="form.noticeType" :options="typeOptions" /></n-form-item>
            <n-form-item :label="t('notice.content')"><n-input v-model:value="form.noticeContent" type="textarea" :rows="5" :placeholder="t('notice.contentPlaceholder')" /></n-form-item>
            <n-form-item :label="t('notice.status')"><n-select v-model:value="form.status" :options="statusOptions" /></n-form-item>
            <n-form-item :label="t('common.remark')"><n-input v-model:value="form.remark" /></n-form-item>
          </n-form>
        </n-drawer-content>
      </n-drawer>
    </n-card>
  </div>
</template>
