<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, onBeforeUnmount, h } from 'vue'
import { fetchSessions, revokeSession } from '@/api/system'
import { useNotify } from '@/composables/useNotify'

import { NCard, NDataTable, NButton, NSpace, NTag, NPopconfirm } from 'naive-ui'

const { t } = useI18n()
import type { DataTableColumns } from 'naive-ui'


interface OnlineSession {
  token: string; username: string; createdAt: string; expiresAt: string; lastAccessTime: string
}

const notify = useNotify()

const sessions = ref<OnlineSession[]>([])
const loading = ref(false)
let timer: ReturnType<typeof setInterval>

const columns: DataTableColumns<OnlineSession> = [
  { title: t('monitor.sessionId'), key: 'token', width: 140, ellipsis: { tooltip: true } },
  { title: t('monitor.user'), key: 'username', width: 100 },
  { title: t('monitor.loginTime'), key: 'createdAt', width: 160, render: (row) => row.createdAt ? row.createdAt.substring(0,19).replace('T',' ') : '-' },
  { title: t('monitor.expiresAt'), key: 'expiresAt', width: 160, render: (row) => row.expiresAt ? row.expiresAt.substring(0,19).replace('T',' ') : '-' },
  { title: t('monitor.lastActive'), key: 'lastAccessTime', width: 160, render: (row) => row.lastAccessTime ? row.lastAccessTime.substring(0,19).replace('T',' ') : '-' },
  { title: t('monitor.status'), key: 'status', width: 70, render: (row) => {
    const expired = row.expiresAt ? new Date(row.expiresAt).getTime() < Date.now() : false
    return h(NTag, { type: expired ? 'default' : 'success', size: 'small' }, { default: () => expired ? t('monitor.offline') : t('monitor.online') })
  }},
  {
    title: t('monitor.actions'), key: 'actions', width: 100,
    render: (row) => h(NPopconfirm, { onPositiveClick: () => kickUser(row.username) }, {
      trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('monitor.kickOut') }),
      default: () => t('monitor.revokeConfirm', { username: row.username })
    })
  },
]

async function loadSessions() {
  loading.value = true
  try {
    const res = await fetchSessions()
    sessions.value = res.data.sessions || []
  } catch { notify.error(t('common.error')); }
  loading.value = false
}

async function kickUser(username: string) {
  try {
    await revokeSession(username)
    notify.success(t('monitor.kickSuccess', { username }))
    await loadSessions()
  } catch { notify.error(t('common.error')); }
}

onMounted(() => { loadSessions(); timer = setInterval(loadSessions, 30000) })
onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <div class="view-pad">
    <n-card :title="t('monitor.onlineTitle')">
      <template #header-extra>
        <n-space>
          <n-tag type="info" size="small">{{ t('monitor.onlineCount', { n: sessions.length }) }}</n-tag>
          <n-button size="small" @click="loadSessions" secondary>{{ t('monitor.refresh') }}</n-button>
        </n-space>
      </template>
      <n-dataTable
        :columns="columns" :data="sessions" :loading="loading" size="small"
        :bordered="false" :pagination="{ pageSize: 10 }"
        :row-key="(row: OnlineSession) => row.token"
      />
    </n-card>
  </div>
</template>
