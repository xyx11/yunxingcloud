<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NInputNumber, NSpace, NSwitch, NTag, NPopconfirm } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<any[]>([])
const showModal = ref(false); const editingId = ref<number|null>(null)
const form = ref({ name:'', minPoints:0, discountRate:100, freeShipping:false, birthdayGift:false, prioritySupport:false, sortOrder:0 })

const columns: DataTableColumns<any> = [
  { title: '等级', key: 'name', width: 80 },
  { title: '所需积分', key: 'minPoints', width: 100 },
  { title: '折扣率', key: 'discountRate', width: 80, render(r:any){ return r.discountRate+'%' } },
  { title: '包邮', key: 'freeShipping', width: 60, render(r:any){ return r.freeShipping?'✓':'' } },
  { title: '生日礼', key: 'birthdayGift', width: 60, render(r:any){ return r.birthdayGift?'✓':'' } },
  { title: '操作', key:'act', width:120, render(r:any){ return h(NSpace,{size:'small'},{default:()=>[
    h(NButton,{size:'tiny',onClick:()=>{editingId.value=r.id;form.value={...r};showModal.value=true}},{default:()=>'编辑'}),
    h(NPopconfirm,{onPositiveClick:()=>del(r.id)},{trigger:()=>h(NButton,{size:'tiny',type:'error'},{default:()=>'删除'}),default:()=>'确认删除？'})
  ]})}}
]

async function load() { const r = await request.get('/member/tiers'); items.value = r.data }
async function save() {
  if (editingId.value) { await request.put(`/member/tiers/${editingId.value}`, form.value) }
  else { await request.post('/member/tiers', form.value) }
  showModal.value=false; editingId.value=null; notify.success('保存成功'); load()
}
async function del(id:number) { try{await request.delete(`/member/tiers/${id}`);notify.success('已删除');load()}catch{notify.error('删除失败')} }
function add() { editingId.value=null; form.value={name:'',minPoints:0,discountRate:100,freeShipping:false,birthdayGift:false,prioritySupport:false,sortOrder:0}; showModal.value=true }
onMounted(load)
</script>
<template>
  <n-card title="会员等级">
    <n-space vertical><n-button type="primary" @click="add">新建等级</n-button>
      <n-dataTable :columns="columns" :data="items" :pagination="false" />
    </n-space>
    <n-drawer v-model:show="showModal" :width="400" placement="right">
      <n-drawer-content :title="editingId?'编辑等级':'新建等级'" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item label="等级名"><n-input v-model:value="form.name" /></n-form-item>
          <n-form-item label="所需积分"><n-input-number v-model:value="form.minPoints" :min="0" /></n-form-item>
          <n-form-item label="折扣率(%)"><n-input-number v-model:value="form.discountRate" :min="1" :max="100" /></n-form-item>
          <n-form-item label="包邮"><n-switch v-model:value="form.freeShipping" /></n-form-item>
          <n-form-item label="生日礼"><n-switch v-model:value="form.birthdayGift" /></n-form-item>
          <n-form-item label="优先客服"><n-switch v-model:value="form.prioritySupport" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>
