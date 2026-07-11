<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSelect, NSpace, NTag, NPopconfirm, NUpload, NDivider } from 'naive-ui'
import type { DataTableColumns, UploadCustomRequestOptions } from 'naive-ui'
import { fetchArticles, createArticle, updateArticle, type Article } from '@/api/article'
import { useNotify } from '@/composables/useNotify'
import RichEditor from '@/components/RichEditor.vue'
import request from '@/api/request'

const notify = useNotify()
const items = ref<Article[]>([])
const showModal = ref(false); const editingId = ref<number|null>(null)
const form = ref<Article & { coverImage?: string }>({ title:'', content:'', category:'news', status:'0', coverImage: '' })

const catOpts = [
  { label: '新闻', value: 'news' },
  { label: '帮助', value: 'help' },
  { label: '公告', value: 'notice' },
  { label: '活动', value: 'promotion' },
]

function handleUpload(options: UploadCustomRequestOptions) {
  const fd = new FormData(); fd.append('file', options.file.file!)
  request.post('/api/files/upload', fd).then(r => {
    form.value.coverImage = r.data?.url || r.data || ''
    options.onFinish(); notify.success(t('common.uploadSuccess'))
  }).catch(() => { options.onError(); notify.error(t('common.uploadFailed')) })
}

const columns: DataTableColumns<Article> = [
  { title: t('common.title'), key: 'title', width: 200 },
  { title: t('product.category'), key: 'category', width: 70, render(r: Article) { return catOpts.find(o => o.value === r.category)?.label || r.category } },
  { title: '状态', key: 'status', width: 70, render(r: Article) { return h(NTag, { size: 'small', type: r.status === '1' ? 'success' : 'default' }, { default: () => r.status === '1' ? '已发布' : '草稿' }) } },
  { title: '浏览', key: 'viewCount', width: 60 },
  { title: t('common.actions'), key: 'act', width: 140, render(r: Article) { return h(NSpace, { size: 'small' }, { default: () => [
    h(NButton, { size: 'small', onClick: () => edit(r) }, { default: () => t('common.edit') }),
    h(NPopconfirm, { onPositiveClick: () => del(r.id!) }, { trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => t('common.delete') }), default: () => t('common.confirmDelete') }),
  ]})}},
]

async function load() { const r = await fetchArticles(); items.value = r.data }
async function save() {
  editingId.value ? await updateArticle(editingId.value, form.value) : await createArticle(form.value)
  showModal.value = false; notify.success(t('common.saveSuccess')); load()
}
async function del(id: number) { try { await request.delete(`/api/articles/${id}`); notify.success(t('common.deleted')); load() } catch { notify.error(t('common.deleteFailed')) } }
function add() { editingId.value = null; form.value = { title: '', content: '', category: 'news', status: '0', coverImage: '' }; showModal.value = true }
function edit(r: Article) { editingId.value = r.id!; form.value = { ...r, coverImage: (r as { coverImage?: string }).coverImage || '' }; showModal.value = true }
onMounted(load)
</script>
<template>
  <n-card title="内容管理">
    <n-space vertical><n-button type="primary" @click="add">新建文章</n-button>
      <n-dataTable :columns="columns" :data="items" :pagination="{pageSize:10}" />
    </n-space>
    <n-drawer v-model:show="showModal" :width="600" placement="right">
      <n-drawer-content title="编辑文章" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="70" size="small">
          <n-form-item :label="t('common.title')"><n-input v-model:value="form.title" /></n-form-item>
          <n-form-item :label="t('product.category')"><n-select v-model:value="form.category" :options="catOpts" /></n-form-item>
          <n-form-item label="状态"><n-select v-model:value="form.status" :options="[{label:'草稿',value:'0'},{label:'发布',value:'1'}]" /></n-form-item>
          <n-form-item label="封面图">
            <n-space vertical>
              <n-input v-model:value="form.coverImage" placeholder="https://..." />
              <n-upload :custom-request="handleUpload" :show-file-list="false" accept="image/*"><n-button size="small" secondary>本地上传</n-button></n-upload>
            </n-space>
          </n-form-item>
          <n-divider />
          <n-form-item :label="t('common.content')">
            <RichEditor v-model="form.content!" />
          </n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>
