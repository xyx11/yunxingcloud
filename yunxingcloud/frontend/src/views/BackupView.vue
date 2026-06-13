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
  { title: '文件名', key: 'filename', width: 220 },
  { title: '大小', key: 'size', width: 80, render: (row) => (row.size / 1024).toFixed(1) + ' KB' },
  { title: '修改时间', key: 'modified', width: 160 },
  {
    title: '操作', key: 'actions', width: 180,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NPopconfirm, { onPositiveClick: () => restoreBackup(row.filename) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'warning' }, { default: () => '恢复' }),
          default: () => `确认恢复 ${row.filename}？当前数据将被覆盖！`
        }),
        h(NPopconfirm, { onPositiveClick: () => delBackup(row.filename) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '删除' }),
          default: () => '确认删除?'
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
    notify.success(res.data.message || '备份完成')
    await loadFiles()
  } catch { notify.error('备份失败') }
  backing.value = false
}

async function restoreBackup(filename: string) {
  restoringFile.value = filename
  try {
    const res = await request.post(`/api/maintenance/restore/${filename}`)
    notify.success(res.data.message || '恢复完成')
  } catch { notify.error('恢复失败') }
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
      <n-card title="数据备份恢复">
        <template #header-extra>
          <n-button type="primary" size="small" :loading="backing" @click="doBackup">
            <template #icon>＋</template>立即备份
          </n-button>
        </template>
        <n-dataTable
          :columns="columns" :data="files" :loading="loading" size="small"
          :bordered="false" :pagination="{ pageSize: 10 }"
          :row-key="(row: BackupFile) => row.filename"
        />
        <n-empty v-if="!files.length && !loading" description="暂无备份文件" style="margin-top:40px" />
      </n-card>
    </div>
  </n-config-provider>
</template>
