<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NConfigProvider, NCard, NDataTable, NButton, NSpace, NTag, NPopconfirm, NModal, darkTheme, lightTheme } from 'naive-ui'

const { t } = useI18n()
import type { DataTableColumns } from 'naive-ui'


interface BackupFile { filename: string; size: number; modified: string }

const notify = useNotify()
const currentTheme = computed(() => localStorage.getItem("theme") === "dark" ? darkTheme : lightTheme)
const files = ref<BackupFile[]>([])
const loading = ref(false)
const backing = ref(false)
const restoringFile = ref('')

const columns: DataTableColumns<BackupFile> = [
  { title: t('maintenance.filename'), key: 'filename', width: 220 },
  { title: t('maintenance.size'), key: 'size', width: 80, render: (row) => (row.size / 1024).toFixed(1) + ' KB' },
  { title: t('backup.modified'), key: 'modified', width: 160 },
  {
    title: t('user.actions'), key: 'actions', width: 180,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NPopconfirm, { onPositiveClick: () => restoreBackup(row.filename) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'warning' }, { default: () => t('backup.restore') }),
          default: () => t('backup.restoreConfirm')
        }),
        h(NPopconfirm, { onPositiveClick: () => delBackup(row.filename) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }),
          default: () => t('common.confirmDelete')
        })
      ]
    })
  },
]

async function loadFiles() {
  loading.value = true
  try { const res = await request.get('/api/maintenance/backups'); files.value = res.data } catch {}
  loading.value = false
}

async function doBackup() {
  backing.value = true
  try {
    const res = await request.post('/api/maintenance/backup')
    notify.success(res.data.message || t('maintenance.backupSuccess'))
    await loadFiles()
  } catch { notify.error(t('backup.backupFailed')) }
  backing.value = false
}

async function restoreBackup(filename: string) {
  restoringFile.value = filename
  try {
    const res = await request.post(`/api/maintenance/restore/${filename}`)
    notify.success(res.data.message || t('backup.restoreSuccess'))
  } catch { notify.error(t('backup.restoreFailed')) }
  restoringFile.value = ''
}

async function delBackup(filename: string) {
  await request.delete(`/api/maintenance/backups/${filename}`)
  await loadFiles()
}

onMounted(loadFiles)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card :title="t('backup.title')">
        <template #header-extra>
          <n-button type="primary" size="small" :loading="backing" @click="doBackup">
            <template #icon>＋</template>{{ t('backup.createNow') }}
          </n-button>
        </template>
        <n-dataTable
          :columns="columns" :data="files" :loading="loading" size="small"
          :bordered="false" :pagination="{ pageSize: 10 }"
          :row-key="(row: BackupFile) => row.filename"
        />
        <n-empty v-if="!files.length && !loading" :description="t('backup.noBackups')" style="margin-top:40px" />
      </n-card>
    </div>
  </n-config-provider>
</template>
