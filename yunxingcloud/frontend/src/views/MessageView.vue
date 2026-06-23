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
  { title: t('message.title'), key: 'title', width: 180, ellipsis: { tooltip: true } },
  { title: t('message.toOrFrom'), key: tab.value === 'sent' ? 'toUser' : 'fromUser', width: 100 },
  {
    title: t('message.status'), key: 'isRead', width: 60,
    render: (row) => h(NTag, { type: row.isRead ? 'default' : 'info', size: 'small' }, { default: () => row.isRead ? t('message.read') : t('message.unread') })
  },
  { title: t('message.time'), key: 'createdAt', width: 160, render: (row: any) => row.createdAt?.substring(0, 19).replace('T', ' ') || '-' },
  {
    title: t('message.actions'), key: 'actions', width: 140,
    render: (row) => h(NSpace, null, {
      default: () => [
        tab.value === 'inbox' && !row.isRead
          ? h(NButton, { size: 'tiny', onClick: () => readMsg(row.id) }, { default: () => t('message.markRead') })
          : null,
        h(NPopconfirm, { onPositiveClick: () => delMsg(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }),
          default: () => t('common.confirmDelete')
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
  try { await request.post('/api/messages', form.value); showModal.value = false; form.value = { toUser: '', title: '', content: '' }; notify.success(t('message.sendSuccess')); loadMsgs() }
  catch { notify.error(t('message.sendFailed')) }
  finally { saving.value = false }
}

onMounted(loadMsgs)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card :title="t('nav.messages')">
        <template #header-extra>
          <n-space>
            <n-button :type="tab === 'inbox' ? 'primary' : 'default'" size="small" @click="switchTab('inbox')">{{ t('message.inbox') }}</n-button>
            <n-button :type="tab === 'sent' ? 'primary' : 'default'" size="small" @click="switchTab('sent')">{{ t('message.sent') }}</n-button>
            <n-button type="primary" size="small" @click="showModal = true"><template #icon>＋</template>{{ t('message.compose') }}</n-button>
          </n-space>
        </template>
        <n-dataTable :columns="columns" :data="messages" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10 }" :row-key="(row: Msg) => row.id" />

        <n-modal v-model:show="showModal" :title="t('message.send')" preset="card" display-directive="show" style="width:480px">
          <n-form label-placement="left" label-width="60">
            <n-form-item :label="t('message.toUser')"><n-input v-model:value="form.toUser" /></n-form-item>
            <n-form-item :label="t('message.title')"><n-input v-model:value="form.title" /></n-form-item>
            <n-form-item :label="t('message.content')"><n-input v-model:value="form.content" type="textarea" :rows="4" /></n-form-item>
          </n-form>
          <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="sendMsg">{{ t('message.send') }}</n-button></n-space></template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
