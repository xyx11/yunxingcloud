<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NInputNumber, NSpace, NTag, NPopconfirm } from 'naive-ui'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import type { DataTableColumns } from 'naive-ui'
import { fetchGroupBuys, createGroupBuy, expireGroupBuys, type GroupBuy } from '@/api/groupbuy'
import { useNotify } from '@/composables/useNotify'
import request from '@/api/request'

const notify = useNotify()
const loading = ref(false)
const items = ref<GroupBuy[]>([])
const showModal = ref(false); const editingId = ref<number|null>(null)
const form = ref<GroupBuy>({ productId: 0, minMembers: 2, groupPrice: 0 })

const columns: DataTableColumns<GroupBuy> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '商品ID', key: 'productId', width: 80 },
  { title: '拼团价(分)', key: 'groupPrice', width: 100 },
  { title: '成团人数', key: 'minMembers', width: 80 },
  { title: '状态', key: 'status', width: 80, render(r: GroupBuy) { return h(NTag, { size:'small', type: r.status==='0'?'success':'default' }, { default: () => r.status==='0'?'进行中':'已结束' }) } },
  { title: '开始', key: 'startTime', width: 140, render(r:any){ return r.startTime?.substring(0,16) } },
  { title: '结束', key: 'endTime', width: 140, render(r:any){ return r.endTime?.substring(0,16) } },
  { title: '操作', key:'act', width:160, render(r:any){ return h(NSpace,{size:'small'},{default:()=>[
    h(NButton,{size:'tiny',onClick:()=>{editingId.value=r.id;form.value={...r};showModal.value=true}},{default:()=>'编辑'}),
    h(NPopconfirm,{onPositiveClick:()=>del(r.id!)},{trigger:()=>h(NButton,{size:'tiny',type:'error'},{default:()=>'删除'}),default:()=>'确认删除？'})
  ]})}}
]

async function load() { loading.value = true; try { const r = await fetchGroupBuys(); items.value = r.data } finally { loading.value = false } }
async function save() {
  if (editingId.value) { await request.put(`/api/groupbuys/${editingId.value}`, form.value) }
  else { await createGroupBuy(form.value) }
  showModal.value=false; editingId.value=null; notify.success('保存成功'); load()
}
async function del(id:number) { try{await request.delete(`/api/groupbuys/${id}`);notify.success('已删除');load()}catch{notify.error('删除失败')} }
async function expire() { await expireGroupBuys(); notify.success('已处理超时团'); load() }
function add() { editingId.value=null; form.value={productId:0,minMembers:2,groupPrice:0}; showModal.value=true }
onMounted(load)
</script>
<template>
  <n-card title="拼团管理">
    <n-space vertical>
      <n-space><n-button type="primary" @click="add">新增拼团</n-button><n-button @click="expire">处理超时团</n-button></n-space>
      <n-dataTable :columns="columns" :data="items" :loading="loading" :pagination="{pageSize:10}" />
    </n-space>
    <n-drawer v-model:show="showModal" :width="380" placement="right">
      <n-drawer-content :title="editingId?'编辑拼团':'新增拼团'" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item label="商品ID"><n-input-number v-model:value="form.productId" :min="1" /></n-form-item>
          <n-form-item label="拼团价(分)"><n-input-number v-model:value="form.groupPrice" :min="1" /></n-form-item>
          <n-form-item label="成团人数"><n-input-number v-model:value="form.minMembers" :min="2" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>
