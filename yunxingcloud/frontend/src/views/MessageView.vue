<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NTag, NPopconfirm, darkTheme, lightTheme } from 'naive-ui'

const { t } = useI18n()
import type { DataTableColumns } from 'naive-ui'


interface Msg { id: number; fromUser: string; toUser: string; title: string; content: string; isRead: boolean; createdAt: string }

const notify = useNotify()
const currentTheme = computed(() => localStorage.getItem("theme") === "dark" ? darkTheme : lightTheme)
const messages = ref<Msg[]>([])
const loading = ref(false)
const tab = ref<'inbox' | 'sent'>('inbox')
const showModal = ref(false)
const form = ref({ toUser: '', title: '', content: '' })
const saving = ref(false)

const columns: DataTableColumns<Msg> = [
  { title: '标题', key: 'title', width: 180, ellipsis: { tooltip: true } },
  { title: tab.value === 'sent' ? '收件人' : '发件人', key: tab.value === 'sent' ? 'toUser' : 'fromUser', width: 100 },
  {
    title: '状态', key: 'isRead', width: 60,
    render: (row) => h(NTag, { type: row.isRead ? 'default' : 'info', size: 'small' }, { default: () => row.isRead ? '已读' : '未读' })
  },
  { title: '时间', key: 'createdAt', width: 160, render: (row: any) => row.createdAt?.substring(0, 19).replace('T', ' ') || '-' },
  {
    title: '操作', key: 'actions', width: 140,
    render: (row) => h(NSpace, null, {
      default: () => [
        tab.value === 'inbox' && !row.isRead
          ? h(NButton, { size: 'tiny', onClick: () => readMsg(row.id) }, { default: () => '标为已读' })
          : null,
        h(NPopconfirm, { onPositiveClick: () => delMsg(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '删除' }),
          default: () => '确认删除?'
        })
      ]
    })
  },
]

async function loadMsgs() {
  loading.value = true
  try {
    const res = await request.get(tab.value === 'inbox' ? '/api/messages/inbox' : '/api/messages/sent')
    messages.value = res.data
  } catch {}
  loading.value = false
}

function switchTab(t: 'inbox' | 'sent') { tab.value = t; loadMsgs() }
async function readMsg(id: number) { await request.put(`/api/messages/${id}/read`); loadMsgs() }
async function delMsg(id: number) { await request.delete(`/api/messages/${id}`); loadMsgs() }
async function sendMsg() {
  saving.value = true
  try { await request.post('/api/messages', form.value); showModal.value = false; form.value = { toUser: '', title: '', content: '' }; notify.success('发送成功'); loadMsgs() }
  catch { notify.error('发送失败') }
  finally { saving.value = false }
}

onMounted(loadMsgs)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card title="站内信">
        <template #header-extra>
          <n-space>
            <n-button :type="tab === 'inbox' ? 'primary' : 'default'" size="small" @click="switchTab('inbox')">收件箱</n-button>
            <n-button :type="tab === 'sent' ? 'primary' : 'default'" size="small" @click="switchTab('sent')">已发送</n-button>
            <n-button type="primary" size="small" @click="showModal = true"><template #icon>＋</template>发信</n-button>
          </n-space>
        </template>
        <n-dataTable :columns="columns" :data="messages" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10 }" :row-key="(row: Msg) => row.id" />

        <n-modal v-model:show="showModal" title="发送站内信" preset="card" display-directive="show" style="width:480px">
          <n-form label-placement="left" label-width="60">
            <n-form-item label="收件人"><n-input v-model:value="form.toUser" /></n-form-item>
            <n-form-item label="标题"><n-input v-model:value="form.title" /></n-form-item>
            <n-form-item label="内容"><n-input v-model:value="form.content" type="textarea" :rows="4" /></n-form-item>
          </n-form>
          <template #footer><n-space justify="end"><n-button @click="showModal = false">取消</n-button><n-button type="primary" :loading="saving" @click="sendMsg">发送</n-button></n-space></template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
