<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchArticles, createArticle, updateArticle, type Article } from '@/api/article'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<Article[]>([])
const showModal = ref(false); const editingId = ref<number|null>(null)
const form = ref<Article>({ title:'', content:'', category:'news', status:'0' })

const columns: DataTableColumns<Article> = [
  { title: '标题', key: 'title', width: 200 }, { title: '分类', key: 'category', width: 80 },
  { title: '状态', key: 'status', width: 70, render(r:any){ return h(NTag,{size:'small',type:r.status==='1'?'success':'default'},{default:()=>r.status==='1'?'已发布':'草稿'}) } },
  { title: '浏览', key: 'viewCount', width: 60 },
  { title: '操作', key:'act', width:100, render(r:Article){ return h(NButton,{size:'small',onClick:()=>edit(r)},{default:()=>'编辑'}) } },
]

async function load() { const r = await fetchArticles(); items.value = r.data }
async function save() {
  editingId.value ? await updateArticle(editingId.value, form.value) : await createArticle(form.value)
  showModal.value = false; notify.success('保存成功'); load()
}
function add() { editingId.value=null; form.value={title:'',content:'',category:'news',status:'0'}; showModal.value=true }
function edit(r:Article) { editingId.value=r.id!; form.value={...r}; showModal.value=true }
onMounted(load)
</script>
<template>
  <n-card title="内容管理">
    <n-space vertical><n-button type="primary" @click="add">新建文章</n-button>
      <n-dataTable :columns="columns" :data="items" :pagination="{pageSize:10}" />
    </n-space>
    <n-modal v-model:show="showModal" title="编辑文章" preset="card" style="max-width:600px">
      <n-form :model="form">
        <n-form-item label="标题"><n-input v-model:value="form.title" /></n-form-item>
        <n-form-item label="分类"><n-select v-model:value="form.category" :options="[{label:'新闻',value:'news'},{label:'帮助',value:'help'}]" /></n-form-item>
        <n-form-item label="内容"><n-input v-model:value="form.content" type="textarea" :autosize="{minRows:4,maxRows:12}" /></n-form-item>
        <n-form-item label="状态"><n-select v-model:value="form.status" :options="[{label:'草稿',value:'0'},{label:'发布',value:'1'}]" /></n-form-item>
      </n-form>
      <template #footer><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></template>
    </n-modal>
  </n-card>
</template>