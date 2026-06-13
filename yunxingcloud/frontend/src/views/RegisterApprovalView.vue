<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NConfigProvider, NCard, NDataTable, NButton, NSpace, NTag, NPopconfirm, darkTheme, lightTheme } from 'naive-ui'

const { t } = useI18n()
import type { DataTableColumns } from 'naive-ui'


interface PendingUser { id: number; username: string; email: string; registerSource: string }

const notify = useNotify()
const currentTheme = computed(() => localStorage.getItem('theme') === 'dark' ? darkTheme : lightTheme)
const items = ref<PendingUser[]>([])
const loading = ref(false)

const columns: DataTableColumns<PendingUser> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '用户名', key: 'username', width: 130 },
  { title: '邮箱', key: 'email', width: 200, ellipsis: { tooltip: true } },
  { title: '来源', key: 'registerSource', width: 70 },
  {
    title: '操作', key: 'actions', width: 160,
    render: (row) => h(NSpace, null, { default: () => [
      h(NPopconfirm, { onPositiveClick: () => approve(row.id) }, {
        trigger: () => h(NButton, { size: 'small', type: 'success' }, { default: () => '通过' }),
        default: () => '确认通过？'
      }),
      h(NPopconfirm, { onPositiveClick: () => reject(row.id) }, {
        trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '拒绝' }),
        default: () => '确认拒绝并删除？'
      }),
    ]}),
  },
]

async function loadPending() {
  loading.value = true
  try {
    const res = await request.get('/api/users/pending-approval')
    items.value = res.data
  } catch {}
  loading.value = false
}

async function approve(id: number) {
  await request.put(`/api/users/${id}/approve`)
  notify.success('已通过')
  await loadPending()
}

async function reject(id: number) {
  await request.delete(`/api/users/${id}/reject`)
  notify.success('已拒绝')
  await loadPending()
}

onMounted(loadPending)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:24px">
      <n-card title="注册审核">
        <template #header-extra>
          <n-button size="small" @click="loadPending" secondary>刷新</n-button>
        </template>
        <n-data-table
          :columns="columns" :data="items" :loading="loading" size="small" :bordered="false"
          :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }" :row-key="(row: PendingUser) => row.id"
        />
        <n-space v-if="!loading && items.length === 0" justify="center" style="padding:40px">
          <span style="color:var(--n-text-color-3)">暂无待审核用户</span>
        </n-space>
      </n-card>
    </div>
  </n-config-provider>
</template>
