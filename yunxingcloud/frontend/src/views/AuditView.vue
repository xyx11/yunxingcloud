<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { NCard, NDataTable, NButton, NSpace, NTag, NInput } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'

const loading = ref(false)
const items = ref<Record<string, unknown>[]>([])
const searchKeyword = ref('')

const filtered = computed(() => {
  if (!searchKeyword.value) return items.value
  return items.value.filter(a => (a.entity as string || '').includes(searchKeyword.value) || (a.field as string || '').includes(searchKeyword.value))
})

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: '实体', key: 'entity', width: 120 },
  { title: '字段', key: 'field', width: 100 },
  { title: '旧值', key: 'oldValue', width: 150, ellipsis: { tooltip: true } },
  { title: '新值', key: 'newValue', width: 150, ellipsis: { tooltip: true } },
  { title: '操作人', key: 'operator', width: 100 },
  { title: '时间', key: 'createdAt', width: 160, render(r: Record<string, unknown>) { return (r.createdAt as string || '').substring(0, 16) } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/audit'); items.value = r.data || [] } finally { loading.value = false } }
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card title="审计日志"><template #header-extra><n-button size="small" @click="load" secondary>刷新</n-button></template>
      <n-space class="mb-12">
        <n-input v-model:value="searchKeyword" placeholder="搜索实体/字段..." size="small" clearable class="w-180" />
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r: Record<string, unknown>, i: number) => i" :pagination="{pageSize:15}" size="small" />
    </n-card>
  </div>
</template>

<style scoped>
.w-180 { width: 180px; }
</style>
