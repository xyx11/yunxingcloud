<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NInputNumber, NColorPicker, NSpace, NTag, NPopconfirm } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchTags, createTag, type Tag } from '@/api/tag'
import { useNotify } from '@/composables/useNotify'
import request from '@/api/request'

const notify = useNotify()
const items = ref<Tag[]>([])
const showModal = ref(false); const editingId = ref<number|null>(null)
const form = ref<Tag>({ name:'', color:'#e4393c', sortOrder:0 })

const columns: DataTableColumns<Tag> = [
  { title: t('common.name'), key: 'name', width: 120 },
  { title: '颜色', key: 'color', width: 80, render(r: Tag){ return h(NTag,{size:'small',style:{background:r.color,color:'#fff',border:'none'}},{default:()=>r.name}) } },
  { title: t('common.sort'), key: 'sortOrder', width: 60 },
  { title: t('common.actions'), key:'act', width:120, render(r: Tag){ return h(NSpace,{size:'small'},{default:()=>[
    h(NButton,{size:'tiny',onClick:()=>{editingId.value=r.id;form.value={...r};showModal.value=true}},{default:()=>'编辑'}),
    h(NPopconfirm,{onPositiveClick:()=>del(r.id!)},{trigger:()=>h(NButton,{size:'tiny',type:'error'},{default:()=>'删除'}),default:()=>t('common.confirmDelete')})
  ]})}}
]

async function load() { const r = await fetchTags(); items.value = r.data }
async function save() {
  if (editingId.value) { await request.put(`/api/tags/${editingId.value}`, form.value) }
  else { await createTag(form.value) }
  showModal.value=false; editingId.value=null; notify.success(t('common.saveSuccess')); load()
}
async function del(id:number) { try{await request.delete(`/api/tags/${id}`);notify.success(t('common.deleted'));load()}catch{notify.error(t('common.deleteFailed'))} }
function add() { editingId.value=null; form.value={name:'',color:'#e4393c',sortOrder:0}; showModal.value=true }
onMounted(load)
</script>
<template>
  <n-card title="标签管理">
    <n-space vertical><n-button type="primary" @click="add">新建标签</n-button>
      <n-dataTable :columns="columns" :data="items" :pagination="false" />
    </n-space>
    <n-drawer v-model:show="showModal" :width="340" placement="right">
      <n-drawer-content :title="editingId?'编辑标签':'新建标签'" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="60" size="small">
          <n-form-item :label="t('common.name')"><n-input v-model:value="form.name" /></n-form-item>
          <n-form-item label="颜色"><n-color-picker v-model:value="form.color" /></n-form-item>
          <n-form-item :label="t('common.sort')"><n-input-number v-model:value="form.sortOrder" :min="0" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>
