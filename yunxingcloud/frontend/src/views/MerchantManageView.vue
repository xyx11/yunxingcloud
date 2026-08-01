<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NSpace, NTag, NPopconfirm } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const loading = ref(false)
const items = ref<any[]>([])
const statusFilter = ref('')

const statusLabel: Record<string, { type: any; label: string }> = {
  '0': { type: 'warning', label: t('merchant.pending') },
  '1': { type: 'success', label: t('merchant.approved') },
  '2': { type: 'error', label: t('merchant.rejected') },
}

const columns: DataTableColumns<any> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('merchant.name'), key: 'name', width: 120 },
  { title: t('merchant.phone'), key: 'phone', width: 120 },
  { title: t('merchant.businessLicense'), key: 'businessLicense', width: 120 },
  { title: t('merchant.brandName'), key: 'brandName', width: 120 },
  { title: t('merchant.category'), key: 'category', width: 100 },
  { title: t('common.status'), key: 'status', width: 80, render(r: any) {
    const s = statusLabel[r.status] || { type: 'default', label: r.status }; return h(NTag, { size: 'small', type: s.type }, { default: () => s.label })
  }},
  { title: t('common.createdAt'), key: 'createdAt', width: 140, render(r: any) { return r.createdAt?.substring(0, 10) } },
  { title: t('common.actions'), key: 'act', width: 160, render(r: any) {
    return h(NSpace, { size: 'small' }, { default: () => [
      r.status === '0' ? h(NButton, { size: 'tiny', type: 'success', onClick: () => approve(r.id) }, { default: () => t('merchant.approve') }) : null,
      r.status === '0' ? h(NPopconfirm, { onPositiveClick: () => reject_(r.id) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('merchant.reject') }), default: () => t('common.confirm') }) : null,
    ]})
  }}
]

async function load() {
  loading.value = true
  try { const r = await request.get('/api/merchant/list' + (statusFilter.value ? '?status=' + statusFilter.value : '')); items.value = r.data?.merchants || [] }
  finally { loading.value = false }
}
async function approve(id: number) { try { await request.put(`/api/merchant/${id}/approve`); notify.success(t('common.saveSuccess')); load() } catch { notify.error(t('common.updateFailed')) } }
async function reject_(id: number) { try { await request.put(`/api/merchant/${id}/reject`); notify.success(t('common.updated')); load() } catch { notify.error(t('common.updateFailed')) } }
onMounted(load)
</script>
<template>
  <n-card :title="t('nav.merchant')">
    <n-space vertical>
      <n-dataTable :columns="columns" :data="items" :loading="loading" :pagination="{ pageSize: 10 }" />
    </n-space>
  </n-card>
</template>
