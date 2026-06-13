<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NConfigProvider, NCard, NDataTable, NButton, NSpace, NTag, NPopconfirm, darkTheme, lightTheme } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

interface OnlineSession {
  token: string; username: string; createdAt: string; expiresAt: string; lastAccessTime: string
}

const notify = useNotify()
const currentTheme = computed(() => localStorage.getItem("theme") === "dark" ? darkTheme : lightTheme)
const sessions = ref<OnlineSession[]>([])
const loading = ref(false)
let timer: ReturnType<typeof setInterval>

const columns: DataTableColumns<OnlineSession> = [
  { title: '会话ID', key: 'token', width: 140, ellipsis: { tooltip: true } },
  { title: '用户名', key: 'username', width: 100 },
  { title: '登录时间', key: 'createdAt', width: 160, render: (row) => row.createdAt ? row.createdAt.substring(0,19).replace('T',' ') : '-' },
  { title: '过期时间', key: 'expiresAt', width: 160, render: (row) => row.expiresAt ? row.expiresAt.substring(0,19).replace('T',' ') : '-' },
  { title: '最后活跃', key: 'lastAccessTime', width: 160, render: (row) => row.lastAccessTime ? row.lastAccessTime.substring(0,19).replace('T',' ') : '-' },
  { title: '状态', key: 'status', width: 70, render: (row) => {
    const expired = row.expiresAt ? new Date(row.expiresAt).getTime() < Date.now() : false
    return h(NTag, { type: expired ? 'default' : 'success', size: 'small' }, { default: () => expired ? '离线' : '在线' })
  }},
  {
    title: '操作', key: 'actions', width: 100,
    render: (row) => h(NPopconfirm, { onPositiveClick: () => kickUser(row.username) }, {
      trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '强退' }),
      default: () => `确认强退 ${row.username}？`
    })
  },
]

async function loadSessions() {
  loading.value = true
  try {
    const res = await request.get('/api/system/sessions')
    sessions.value = res.data.sessions || []
  } catch {}
  loading.value = false
}

async function kickUser(username: string) {
  try {
    await request.post('/api/system/sessions/revoke', { username })
    notify.success(`已强退 ${username}`)
    await loadSessions()
  } catch {}
}

onMounted(() => { loadSessions(); timer = setInterval(loadSessions, 30000) })
onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card title="在线用户">
        <template #header-extra>
          <n-space>
            <n-tag type="info" size="small">共 {{ sessions.length }} 人在线</n-tag>
            <n-button size="small" @click="loadSessions" secondary>刷新</n-button>
          </n-space>
        </template>
        <n-dataTable
          :columns="columns" :data="sessions" :loading="loading" size="small"
          :bordered="false" :pagination="{ pageSize: 10 }"
          :row-key="(row: OnlineSession) => row.token"
        />
      </n-card>
    </div>
  </n-config-provider>
</template>
