<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NInputNumber, NSelect, NSpace, NPopconfirm, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const { t } = useI18n()
const loading = ref(false); const saving = ref(false)
const items = ref<Record<string, unknown>[]>([])
const showModal = ref(false); const editingId = ref<number | null>(null)
const form = ref({ name: '', icon: '', sortOrder: 0, status: '0', parentId: null as number | null })
const searchKeyword = ref('')

const filtered = computed(() => {
  if (!searchKeyword.value) return items.value
  const kw = searchKeyword.value.toLowerCase()
  return items.value.filter((c) => (c.name as string || '').toLowerCase().includes(kw))
})

const parentOpts = computed(() => [
  { label: '顶级分类', value: null },
  ...items.value.filter((c) => c.id !== editingId.value).map((c) => ({ label: c.name as string, value: c.id as number }))
])

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '图标', key: 'icon', width: 60, render: (r) => (r.icon as string) || '-' },
  { title: '分类名称', key: 'name', width: 160 },
  {
    title: '父级', key: 'parentId', width: 100,
    render: (r) => {
      if (!r.parentId) return '-'
      const p = items.value.find((c) => c.id === r.parentId)
      return (p?.name as string) || (r.parentId as string)
    }
  },
  { title: t('common.sort'), key: 'sortOrder', width: 60 },
  {
    title: '状态', key: 'status', width: 70,
    render: (r) => h(NTag, { type: r.status === '0' ? 'success' : 'default', size: 'small' }, { default: () => r.status === '0' ? '启用' : '禁用' })
  },
  {
    title: t('common.actions'), key: 'act', width: 120,
    render: (r) => h(NSpace, { size: 'small' }, { default: () => [
      h(NButton, { size: 'tiny', onClick: () => { editingId.value = r.id as number; form.value = { name: r.name as string, icon: (r.icon as string) || '', sortOrder: (r.sortOrder as number) || 0, status: r.status as string, parentId: (r.parentId as number | null) || null }; showModal.value = true } }, { default: () => t('common.edit') }),
      h(NPopconfirm, { onPositiveClick: () => del(r.id as number) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }), default: () => t('common.confirmDelete') })
    ]})}
]

async function load() { loading.value = true; try { const r = await request.get('/api/categories'); items.value = r.data || [] } finally { loading.value = false } }

async function save() {
  saving.value = true
  try {
    const data: Record<string, unknown> = { name: form.value.name, icon: form.value.icon, sortOrder: form.value.sortOrder, status: form.value.status, parentId: form.value.parentId || 0 }
    if (editingId.value) {
      await request.put(`/api/categories/${editingId.value}`, data)
    } else {
      await request.post('/api/categories', data)
    }
    showModal.value = false; editingId.value = null; notify.success(t('common.saveSuccess')); load()
  } catch { notify.error(t('common.saveFailed')) } finally { saving.value = false }
}

async function del(id: number) { try { await request.delete(`/api/categories/${id}`); notify.success(t('common.deleted')); load() } catch { notify.error(t('common.deleteFailed')) } }

function add() { editingId.value = null; form.value = { name: '', icon: '', sortOrder: 0, status: '0', parentId: null }; showModal.value = true }

onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card title="分类管理"><template #header-extra><n-button type="primary" size="small" @click="add">+ 新增分类</n-button></template>
      <n-space class="mb-12">
        <n-input v-model:value="searchKeyword" placeholder="搜索分类..." size="small" clearable class="w-180" />
        <n-button size="small" @click="load" secondary>刷新</n-button>
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r: Record<string, unknown>)=>r.id" :pagination="{pageSize:20}" size="small" />
    </n-card>
    <n-drawer v-model:show="showModal" :width="420" placement="right">
      <n-drawer-content :title="editingId ? '编辑分类' : '新增分类'" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" :loading="saving" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item label="分类名称" required><n-input v-model:value="form.name" placeholder="如: 手机数码"/></n-form-item>
          <n-form-item label="图标"><n-input v-model:value="form.icon" placeholder="图标(emoji或icon类名)" /></n-form-item>
          <n-form-item label="父级分类"><n-select v-model:value="form.parentId" :options="parentOpts" placeholder="顶级分类" clearable /></n-form-item>
          <n-form-item :label="t('common.sort')"><n-input-number v-model:value="form.sortOrder" :min="0" /></n-form-item>
          <n-form-item label="状态"><n-select v-model:value="form.status" :options="[{label:'启用',value:'0'},{label:'禁用',value:'1'}]" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<style scoped>
.w-180 { width: 180px; }
</style>
