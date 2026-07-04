<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NSpace, NImage, NPopconfirm, NInput, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'

const message = useMessage()
const loading = ref(false)
const files = ref<any[]>([])
const checkedKeys = ref<string[]>([])
const searchKeyword = ref('')

const filtered = computed(() => {
  if (!searchKeyword.value) return files.value
  return files.value.filter(f => f.filename.toLowerCase().includes(searchKeyword.value.toLowerCase()))
})

function formatSize(bytes: number) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const columns: DataTableColumns<any> = [
  { type: 'selection' },
  { title: '预览', key: 'preview', width: 80, render(r: any) { return h(NImage, { src: r.url, width: 50, height: 50, style: 'object-fit:cover;border-radius:4px' }) } },
  { title: '文件名', key: 'filename', width: 220, ellipsis: { tooltip: true } },
  { title: '大小', key: 'size', width: 90, render(r: any) { return formatSize(r.size) } },
  { title: '修改时间', key: 'modified', width: 180, render(r: any) { return r.modified?.substring(0, 19).replace('T', ' ') } },
  { title: 'URL', key: 'url', width: 200, ellipsis: { tooltip: true }, render(r: any) { return h(NButton, { size: 'tiny', text: true, onClick: () => { navigator.clipboard.writeText(r.url); message.success('已复制') } }, { default: () => r.url }) } },
  { title: '操作', key: 'act', width: 80, render(r: any) { return h(NPopconfirm, { onPositiveClick: () => delFile(r.filename) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '删除' }), default: () => '确认删除？' }) } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/files/list'); files.value = r.data || [] } finally { loading.value = false } }
async function delFile(name: string) { try { await request.delete(`/api/files/${name}`); message.success('已删除'); load() } catch { message.error('删除失败') } }
async function batchDel() {
  if (!checkedKeys.value.length) return
  for (const name of checkedKeys.value) {
    try { await request.delete(`/api/files/${name}`) } catch {}
  }
  checkedKeys.value = []; message.success(`已删除`); load()
}

onMounted(load)
import { computed } from 'vue'
</script>
<template>
  <div style="padding:20px">
    <n-card title="媒体库">
      <template #header-extra><n-button size="small" @click="load" secondary>刷新</n-button></template>
      <n-space style="margin-bottom:12px" justify="space-between">
        <n-space>
          <n-input v-model:value="searchKeyword" placeholder="搜索文件..." size="small" clearable style="width:200px" />
          <n-button v-if="checkedKeys.length" type="error" size="small" @click="batchDel">批量删除 ({{ checkedKeys.length }})</n-button>
        </n-space>
        <span style="color:#999;font-size:12px">{{ files.length }} 个文件</span>
      </n-space>
      <n-dataTable v-model:checked-row-keys="checkedKeys" :columns="columns" :data="filtered" :loading="loading" :row-key="(r:any)=>r.filename" :pagination="{pageSize:12}" size="small" />
    </n-card>
  </div>
</template>
