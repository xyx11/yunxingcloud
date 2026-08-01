<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { formatPrice } from '@/utils/format'

const loading = ref(false)
const items = ref<any[]>([])

const columns: DataTableColumns<any> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('common.username'), key: 'username', width: 100 },
  { title: t('common.productId'), key: 'productId', width: 80 },
  { title: t('priceAlert.targetPrice'), key: 'targetPrice', width: 100, render(r: any) { return formatPrice((r.targetPrice || 0) / 100, 2) } },
  { title: t('priceAlert.notified'), key: 'notified', width: 80, render(r: any) {
    return h(NTag, { size: 'small', type: r.notified ? 'success' : 'warning' }, { default: () => r.notified ? t('common.yes') : t('common.no') })
  }},
  { title: t('common.createdAt'), key: 'createdAt', width: 140, render(r: any) { return r.createdAt?.substring(0, 10) } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/price-alert/admin/list'); items.value = r.data || [] } finally { loading.value = false } }
onMounted(load)
</script>
<template>
  <n-card :title="t('nav.priceAlert')">
    <n-dataTable :columns="columns" :data="items" :loading="loading" :pagination="{ pageSize: 10 }" />
  </n-card>
</template>
