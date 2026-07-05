<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, h } from 'vue'

import { fetchPendingApprovals, approveUser, rejectUser } from '@/api/user'
import { useNotify } from '@/composables/useNotify'
import { NCard, NDataTable, NButton, NSpace, NPopconfirm } from 'naive-ui'

const { t } = useI18n()
import type { DataTableColumns } from 'naive-ui'


interface PendingUser { id: number; username: string; email: string; registerSource: string }

const notify = useNotify()

const items = ref<PendingUser[]>([])
const loading = ref(false)

const columns: DataTableColumns<PendingUser> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('user.username'), key: 'username', width: 130 },
  { title: t('user.email'), key: 'email', width: 200, ellipsis: { tooltip: true } },
  { title: t('user.source'), key: 'registerSource', width: 70 },
  {
    title: t('user.actions'), key: 'actions', width: 160,
    render: (row) => h(NSpace, null, { default: () => [
      h(NPopconfirm, { onPositiveClick: () => approve(row.id) }, {
        trigger: () => h(NButton, { size: 'small', type: 'success' }, { default: () => t('common.approve') }),
        default: () => t('user.approveConfirm')
      }),
      h(NPopconfirm, { onPositiveClick: () => reject(row.id) }, {
        trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => t('common.reject') }),
        default: () => t('user.rejectConfirm')
      }),
    ]}),
  },
]

async function loadPending() {
  loading.value = true
  try {
    const res = await fetchPendingApprovals()
    items.value = res.data
  } catch { /* ignore */ }
  loading.value = false
}

async function approve(id: number) {
  await approveUser(id)
  notify.success(t('user.approveSuccess'))
  await loadPending()
}

async function reject(id: number) {
  await rejectUser(id)
  notify.success(t('user.rejectSuccess'))
  await loadPending()
}

onMounted(loadPending)
</script>

<template>
  <div class="view-pad-lg">
    <n-card :title="t('nav.registerApproval')">
      <template #header-extra>
        <n-button size="small" @click="loadPending" secondary>{{ t('common.refresh') }}</n-button>
      </template>
      <n-data-table
        :columns="columns" :data="items" :loading="loading" size="small" :bordered="false"
        :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }" :row-key="(row: PendingUser) => row.id"
      />
      <n-space v-if="!loading && items.length === 0" justify="center" style="padding:40px">
        <span style="color:var(--n-text-color-3)">{{ t('common.noData') }}</span>
      </n-space>
    </n-card>
  </div>
</template>
