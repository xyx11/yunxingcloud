<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NSpace, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const loading = ref(false)
const items = ref<any[]>([])

const columns: DataTableColumns<any> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('common.productId'), key: 'productId', width: 80 },
  { title: t('common.name'), key: 'productName', width: 140 },
  { title: t('presale.deposit'), key: 'deposit', width: 90, render(r: any) { return '¥' + ((r.deposit || 0) / 100).toFixed(2) } },
  { title: t('presale.finalPrice'), key: 'finalPrice', width: 90, render(r: any) { return '¥' + ((r.finalPrice || 0) / 100).toFixed(2) } },
  { title: t('common.status'), key: 'status', width: 80, render(r: any) {
    const m: Record<string, any> = { '0': { type: 'default', label: t('common.notStarted') }, '1': { type: 'info', label: t('common.inProgress') }, '2': { type: 'success', label: t('common.ended2') } }
    const s = m[r.status] || { type: 'default', label: r.status }; return h(NTag, { size: 'small', type: s.type }, { default: () => s.label })
  }},
  { title: t('common.startTime'), key: 'startTime', width: 140, render(r: any) { return r.startTime?.substring(0, 16) } },
  { title: t('common.endTime'), key: 'endTime', width: 140, render(r: any) { return r.endTime?.substring(0, 16) } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/presale'); items.value = r.data?.content || r.data || [] } finally { loading.value = false } }
onMounted(load)
</script>
<template>
  <n-card :title="t('nav.presale')">
    <n-dataTable :columns="columns" :data="items" :loading="loading" :pagination="{ pageSize: 10 }" />
  </n-card>
</template>
