<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NSpace, NTag, NInput } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'

const loading = ref(false)
const records = ref<any[]>([])
const searchKeyword = ref('')

const filtered = computed(() => {
  if (!searchKeyword.value) return records.value
  return records.value.filter(r => (r.username || '').toLowerCase().includes(searchKeyword.value.toLowerCase()))
})

const columns: DataTableColumns<any> = [
  { title: '用户', key: 'username', width: 120 },
  { title: '积分余额', key: 'balance', width: 100, render(r: any) { return r.balance || 0 } },
  { title: '累计获得', key: 'totalEarned', width: 100, render(r: any) { return r.totalEarned || 0 } },
  { title: '累计使用', key: 'totalSpent', width: 100, render(r: any) { return r.totalSpent || 0 } },
  { title: t('common.updatedAt'), key: 'updatedAt', width: 160, render(r: any) { return r.updatedAt?.substring(0, 16) || '-' } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/points/admin/accounts'); records.value = r.data || [] } catch { /* fallback */ try { const r2 = await request.get('/api/points/records'); records.value = r2.data || [] } catch { records.value = [] } } finally { loading.value = false } }
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card title="积分管理"><template #header-extra><n-button size="small" @click="load" secondary>刷新</n-button></template>
      <n-space class="mb-12">
        <n-input v-model:value="searchKeyword" placeholder="搜索用户..." size="small" clearable style="width:180px" />
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r:any,i:number)=>i" :pagination="{pageSize:15}" size="small" />
    </n-card>
  </div>
</template>
