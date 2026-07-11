<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSpace, NPopconfirm, NUpload } from 'naive-ui'
import type { DataTableColumns, UploadCustomRequestOptions } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const { t } = useI18n()
const loading = ref(false); const saving = ref(false)
const items = ref<Record<string, unknown>[]>([])
const showModal = ref(false); const editingId = ref<number | null>(null)
const form = ref({ name: '', logo: '', description: '' })
const searchKeyword = ref('')

const filtered = computed(() => {
  if (!searchKeyword.value) return items.value
  const kw = searchKeyword.value.toLowerCase()
  return items.value.filter((b) => (b.name as string || '').toLowerCase().includes(kw))
})

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: 'ID', key: 'id', width: 60 },
  {
    title: 'Logo', key: 'logo', width: 80,
    render: (r) => r.logo ? h('img', { src: r.logo as string, style: 'width:40px;height:40px;object-fit:contain;border-radius:4px' }) : '-'
  },
  { title: '品牌名称', key: 'name', width: 150 },
  { title: t('common.description'), key: 'description', width: 200, ellipsis: { tooltip: true } },
  {
    title: t('common.actions'), key: 'act', width: 120,
    render: (r) => h(NSpace, { size: 'small' }, { default: () => [
      h(NButton, { size: 'tiny', onClick: () => { editingId.value = r.id as number; form.value = { name: r.name as string, logo: (r.logo as string) || '', description: (r.description as string) || '' }; showModal.value = true } }, { default: () => t('common.edit') }),
      h(NPopconfirm, { onPositiveClick: () => delBrand(r.id as number) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }), default: () => t('common.confirmDelete') })
    ]})}
]

async function load() { loading.value = true; try { const r = await request.get('/api/brands'); items.value = r.data || [] } finally { loading.value = false } }

async function save() {
  saving.value = true
  try {
    const data = { name: form.value.name, logo: form.value.logo, description: form.value.description }
    if (editingId.value) {
      await request.put(`/api/brands/${editingId.value}`, data)
    } else {
      await request.post('/api/brands', data)
    }
    showModal.value = false; editingId.value = null; notify.success(t('common.saveSuccess')); load()
  } catch { notify.error(t('common.saveFailed')) } finally { saving.value = false }
}

async function delBrand(id: number) { try { await request.delete(`/api/brands/${id}`); notify.success(t('common.deleted')); load() } catch { notify.error(t('common.deleteFailed')) } }

function add() { editingId.value = null; form.value = { name: '', logo: '', description: '' }; showModal.value = true }

function handleUpload(options: UploadCustomRequestOptions) {
  const formData = new FormData(); formData.append('file', options.file.file!)
  request.post('/api/upload', formData).then(r => {
    form.value.logo = r.data?.url || r.data || ''
    options.onFinish()
    notify.success(t('common.uploadSuccess'))
  }).catch(() => { options.onError(); notify.error(t('common.uploadFailed')) })
}

onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card title="品牌管理"><template #header-extra><n-button type="primary" size="small" @click="add">+ 新增品牌</n-button></template>
      <n-space class="mb-12">
        <n-input v-model:value="searchKeyword" placeholder="搜索品牌..." size="small" clearable class="w-180" />
        <n-button size="small" @click="load" secondary>刷新</n-button>
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r: Record<string, unknown>)=>r.id" :pagination="{pageSize:10}" size="small" />
    </n-card>
    <n-drawer v-model:show="showModal" :width="420" placement="right">
      <n-drawer-content :title="editingId ? '编辑品牌' : '新增品牌'" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" :loading="saving" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item label="品牌名称" required><n-input v-model:value="form.name" placeholder="如: 华为"/></n-form-item>
          <n-form-item :label="t('common.description')"><n-input v-model:value="form.description" type="textarea" placeholder="品牌描述" /></n-form-item>
          <n-form-item label="Logo">
            <n-space vertical>
              <n-input v-model:value="form.logo" placeholder="Logo URL" />
              <n-upload :custom-request="handleUpload" :show-file-list="false" accept="image/*"><n-button size="small" secondary>本地上传</n-button></n-upload>
              <img v-if="form.logo" :src="form.logo" class="brand-logo" />
            </n-space>
          </n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<style scoped>
.w-180 { width: 180px; }
.brand-logo { width: 100px; height: 100px; object-fit: contain; border: 1px solid #eee; border-radius: 8px; }
</style>
