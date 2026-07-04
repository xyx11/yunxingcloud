<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSpace, NPopconfirm, NUpload } from 'naive-ui'
import type { DataTableColumns, UploadCustomRequestOptions } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const loading = ref(false); const saving = ref(false)
const items = ref<any[]>([])
const showModal = ref(false); const editingId = ref<number | null>(null)
const form = ref({ name: '', logo: '', description: '' })
const searchKeyword = ref('')

const filtered = computed(() => {
  if (!searchKeyword.value) return items.value
  const kw = searchKeyword.value.toLowerCase()
  return items.value.filter((b: any) => b.name?.toLowerCase().includes(kw))
})

const columns: DataTableColumns<any> = [
  { title: 'ID', key: 'id', width: 60 },
  {
    title: 'Logo', key: 'logo', width: 80,
    render: (r: any) => r.logo ? h('img', { src: r.logo, style: 'width:40px;height:40px;object-fit:contain;border-radius:4px' }) : '-'
  },
  { title: '品牌名称', key: 'name', width: 150 },
  { title: '描述', key: 'description', width: 200, ellipsis: { tooltip: true } },
  {
    title: '操作', key: 'act', width: 120,
    render: (r: any) => h(NSpace, { size: 'small' }, { default: () => [
      h(NButton, { size: 'tiny', onClick: () => { editingId.value = r.id; form.value = { name: r.name, logo: r.logo || '', description: r.description || '' }; showModal.value = true } }, { default: () => '编辑' }),
      h(NPopconfirm, { onPositiveClick: () => delBrand(r.id) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '删除' }), default: () => '确认删除？' })
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
    showModal.value = false; editingId.value = null; notify.success('保存成功'); load()
  } catch { notify.error('保存失败') } finally { saving.value = false }
}

async function delBrand(id: number) { try { await request.delete(`/api/brands/${id}`); notify.success('已删除'); load() } catch { notify.error('删除失败') } }

function add() { editingId.value = null; form.value = { name: '', logo: '', description: '' }; showModal.value = true }

function handleUpload(options: UploadCustomRequestOptions) {
  const formData = new FormData(); formData.append('file', options.file.file!)
  request.post('/api/upload', formData).then(r => {
    form.value.logo = r.data?.url || r.data || ''
    options.onFinish()
    notify.success('上传成功')
  }).catch(() => { options.onError(); notify.error('上传失败') })
}

onMounted(load)
</script>
<template>
  <div style="padding:20px">
    <n-card title="品牌管理"><template #header-extra><n-button type="primary" size="small" @click="add">+ 新增品牌</n-button></template>
      <n-space style="margin-bottom:12px">
        <n-input v-model:value="searchKeyword" placeholder="搜索品牌..." size="small" clearable style="width:180px" />
        <n-button size="small" @click="load" secondary>刷新</n-button>
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r:any)=>r.id" :pagination="{pageSize:10}" size="small" />
    </n-card>
    <n-drawer v-model:show="showModal" :width="420" placement="right">
      <n-drawer-content :title="editingId ? '编辑品牌' : '新增品牌'" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" :loading="saving" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item label="品牌名称" required><n-input v-model:value="form.name" placeholder="如: 华为"/></n-form-item>
          <n-form-item label="描述"><n-input v-model:value="form.description" type="textarea" placeholder="品牌描述" /></n-form-item>
          <n-form-item label="Logo">
            <n-space vertical>
              <n-input v-model:value="form.logo" placeholder="Logo URL" />
              <n-upload :custom-request="handleUpload" :show-file-list="false" accept="image/*"><n-button size="small" secondary>本地上传</n-button></n-upload>
              <img v-if="form.logo" :src="form.logo" style="width:100px;height:100px;object-fit:contain;border:1px solid #eee;border-radius:8px" />
            </n-space>
          </n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>
