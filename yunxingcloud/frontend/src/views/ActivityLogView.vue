<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NSpace, NTag, NInput, NSelect } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'

const loading = ref(false)
const items = ref<any[]>([])
const searchKeyword = ref(''); const filterType = ref('')

const filtered = computed(() => {
  let list = items.value
  if (filterType.value) list = list.filter(a => a.type === filterType.value)
  if (searchKeyword.value) { const kw = searchKeyword.value.toLowerCase(); list = list.filter(a => (a.userId||'').toLowerCase().includes(kw) || (a.action||'').toLowerCase().includes(kw)) }
  return list
})

const columns: DataTableColumns<any> = [
  { title: '用户', key: 'userId', width: 100 },
  { title: '操作类型', key: 'type', width: 100, render(r: any) { return h(NTag, { size: 'small' }, { default: () => r.type || '-' }) } },
  { title: t('common.description'), key: 'action', width: 250, ellipsis: { tooltip: true } },
  { title: 'IP', key: 'ip', width: 130 },
  { title: '时间', key: 'createdAt', width: 160, render(r: any) { return r.createdAt?.substring(0, 16) } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/activity'); items.value = r.data || [] } finally { loading.value = false } }
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card title="活动日志"><template #header-extra><n-button size="small" @click="load" secondary>刷新</n-button></template>
      <n-space class="mb-12">
        <n-input v-model:value="searchKeyword" placeholder="搜索用户/操作..." size="small" clearable class="w-180" />
        <n-button size="small" @click="load" secondary>刷新</n-button>
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r:any,i:number)=>i" :pagination="{pageSize:15}" size="small" />
    </n-card>
  </div>
</template>

<style scoped>
.w-180 { width: 180px; }
</style>
