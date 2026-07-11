<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSpace, NTag, NPopconfirm } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<Record<string, unknown>[]>([])
const showModal = ref(false); const editingId = ref<number|null>(null)
const form = ref({ name:'', contact:'', phone:'', email:'', address:'' })
const searchKeyword = ref('')

const filtered = computed(() => {
  if (!searchKeyword.value) return items.value
  const kw = searchKeyword.value.toLowerCase()
  return items.value.filter((s: Record<string, unknown>) => (s.name as string)?.toLowerCase().includes(kw) || (s.contact as string)?.toLowerCase().includes(kw))
})

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: t('common.name'), key: 'name', width: 160 }, { title: '联系人', key: 'contact', width: 80 },
  { title: '电话', key: 'phone', width: 120 }, { title: '邮箱', key: 'email', width: 160 },
  { title: '地址', key: 'address', width: 200, ellipsis:{tooltip:true} },
  { title: t('common.actions'), key:'act', width:120, render(r: Record<string, unknown>){ return h(NSpace,{size:'small'},{default:()=>[
    h(NButton,{size:'tiny',onClick:()=>{editingId.value=r.id as number;form.value={name:r.name as string,contact:(r.contact as string)||'',phone:(r.phone as string)||'',email:(r.email as string)||'',address:(r.address as string)||''};showModal.value=true}},{default:()=>'编辑'}),
    h(NPopconfirm,{onPositiveClick:()=>del(r.id as number)},{trigger:()=>h(NButton,{size:'tiny',type:'error'},{default:()=>'删除'}),default:()=>t('common.confirmDelete')})
  ]})}}
]

async function load() { const r = await request.get('/suppliers'); items.value = r.data }
async function save() {
  if (editingId.value) { await request.put(`/suppliers/${editingId.value}`, form.value) }
  else { await request.post('/suppliers', form.value) }
  showModal.value=false; editingId.value=null; notify.success(t('common.saveSuccess')); load()
}
async function del(id:number) { try{await request.delete(`/suppliers/${id}`);notify.success(t('common.deleted'));load()}catch{notify.error(t('common.deleteFailed'))} }
function add() { editingId.value=null; form.value={name:'',contact:'',phone:'',email:'',address:''}; showModal.value=true }
onMounted(load)
</script>
<template>
  <n-card title="供应商管理">
    <n-space vertical>
      <n-space><n-button type="primary" @click="add">新增供应商</n-button><n-input v-model:value="searchKeyword" placeholder="搜索..." size="small" clearable class="w-160" /></n-space>
      <n-dataTable :columns="columns" :data="filtered" :pagination="{pageSize:10}" />
    </n-space>
    <n-drawer v-model:show="showModal" :width="420" placement="right">
      <n-drawer-content :title="editingId?'编辑供应商':'新增供应商'" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="70" size="small">
          <n-form-item :label="t('common.name')"><n-input v-model:value="form.name" /></n-form-item>
          <n-form-item label="联系人"><n-input v-model:value="form.contact" /></n-form-item>
          <n-form-item label="电话"><n-input v-model:value="form.phone" /></n-form-item>
          <n-form-item label="邮箱"><n-input v-model:value="form.email" /></n-form-item>
          <n-form-item label="地址"><n-input v-model:value="form.address" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>

<style scoped>
.w-160 { width: 160px; }
</style>
