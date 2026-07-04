<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSpace, NTag, NPopconfirm } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<any[]>([])
const showModal = ref(false); const editingId = ref<number|null>(null)
const form = ref({ name:'', contact:'', phone:'', email:'', address:'' })
const searchKeyword = ref('')

const filtered = computed(() => {
  if (!searchKeyword.value) return items.value
  const kw = searchKeyword.value.toLowerCase()
  return items.value.filter((s:any) => s.name?.toLowerCase().includes(kw) || s.contact?.toLowerCase().includes(kw))
})

const columns: DataTableColumns<any> = [
  { title: '名称', key: 'name', width: 160 }, { title: '联系人', key: 'contact', width: 80 },
  { title: '电话', key: 'phone', width: 120 }, { title: '邮箱', key: 'email', width: 160 },
  { title: '地址', key: 'address', width: 200, ellipsis:{tooltip:true} },
  { title: '操作', key:'act', width:120, render(r:any){ return h(NSpace,{size:'small'},{default:()=>[
    h(NButton,{size:'tiny',onClick:()=>{editingId.value=r.id;form.value={name:r.name,contact:r.contact||'',phone:r.phone||'',email:r.email||'',address:r.address||''};showModal.value=true}},{default:()=>'编辑'}),
    h(NPopconfirm,{onPositiveClick:()=>del(r.id)},{trigger:()=>h(NButton,{size:'tiny',type:'error'},{default:()=>'删除'}),default:()=>'确认删除？'})
  ]})}}
]

async function load() { const r = await request.get('/suppliers'); items.value = r.data }
async function save() {
  if (editingId.value) { await request.put(`/suppliers/${editingId.value}`, form.value) }
  else { await request.post('/suppliers', form.value) }
  showModal.value=false; editingId.value=null; notify.success('保存成功'); load()
}
async function del(id:number) { try{await request.delete(`/suppliers/${id}`);notify.success('已删除');load()}catch{notify.error('删除失败')} }
function add() { editingId.value=null; form.value={name:'',contact:'',phone:'',email:'',address:''}; showModal.value=true }
onMounted(load)
</script>
<template>
  <n-card title="供应商管理">
    <n-space vertical>
      <n-space><n-button type="primary" @click="add">新增供应商</n-button><n-input v-model:value="searchKeyword" placeholder="搜索..." size="small" clearable style="width:160px" /></n-space>
      <n-dataTable :columns="columns" :data="filtered" :pagination="{pageSize:10}" />
    </n-space>
    <n-drawer v-model:show="showModal" :width="420" placement="right">
      <n-drawer-content :title="editingId?'编辑供应商':'新增供应商'" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="70" size="small">
          <n-form-item label="名称"><n-input v-model:value="form.name" /></n-form-item>
          <n-form-item label="联系人"><n-input v-model:value="form.contact" /></n-form-item>
          <n-form-item label="电话"><n-input v-model:value="form.phone" /></n-form-item>
          <n-form-item label="邮箱"><n-input v-model:value="form.email" /></n-form-item>
          <n-form-item label="地址"><n-input v-model:value="form.address" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>
