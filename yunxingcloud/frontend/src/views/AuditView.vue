<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { NCard, NDataTable, NButton, NSpace, NTag, NInput, NSelect } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'

const loading = ref(false)
const items = ref<Record<string, unknown>[]>([])
const searchKeyword = ref('')
const typeFilter = ref('')

const typeOpts = [
  {label:'全部',value:''},{label:'CREATE',value:'CREATE'},{label:'UPDATE',value:'UPDATE'},
  {label:'DELETE',value:'DELETE'},{label:'LOGIN',value:'LOGIN'},
]

const typeColors: Record<string,string> = {CREATE:'success',UPDATE:'info',DELETE:'error',LOGIN:'warning'}

const filtered = computed(() => {
  let list = items.value
  if (searchKeyword.value) list = list.filter(a => (a.entity as string||'').includes(searchKeyword.value))
  if (typeFilter.value) list = list.filter(a => a.action === typeFilter.value)
  return list
})

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: '操作', key: 'action', width: 80, render(r:any){ return h(NTag,{size:'small',type:typeColors[r.action]||'default'},{default:()=>r.action||'-'}) } },
  { title: '实体', key: 'entity', width: 110 },
  { title: '字段', key: 'field', width: 90 },
  { title: '旧值', key: 'oldValue', width: 130, ellipsis: { tooltip: true } },
  { title: '新值', key: 'newValue', width: 130, ellipsis: { tooltip: true } },
  { title: '操作人', key: 'operator', width: 90 },
  { title: '时间', key: 'createdAt', width: 150, render(r: Record<string, unknown>) { return (r.createdAt as string || '').substring(0, 16) } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/audit'); items.value = r.data || [] } finally { loading.value = false } }
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card title="审计日志"><template #header-extra><n-button size="small" @click="load" secondary>刷新</n-button></template>
      <n-space class="mb-12">
        <n-input v-model:value="searchKeyword" placeholder="搜索实体..." size="small" clearable class="w-160" />
        <n-select v-model:value="typeFilter" :options="typeOpts" size="small" class="w-100" />
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r: Record<string, unknown>, i: number) => i" :pagination="{pageSize:15}" size="small" />
    </n-card>
  </div>
</template>

<style scoped>
.w-160 { width: 160px; }
.w-100 { width: 100px; }
</style>
