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
  { title: t('common.productId'), key: 'productId', width: 80 },
  { title: t('common.groupPrice'), key: 'groupPrice', width: 100 },
  { title: t('groupBuy.minMembers'), key: 'minMembers', width: 80 },
  { title: t('common.status'), key: 'status', width: 80, render(r: GroupBuy) { return h(NTag, { size:'small', type: r.status==='0'?'success':'default' }, { default: () => r.status==='0' ? t('common.inProgress') : t('common.ended2') }) } },
  { title: t('common.startTime'), key: 'startTime', width: 140, render(r: GroupBuy){ return r.startTime?.substring(0,16) } },
  { title: t('common.endTime'), key: 'endTime', width: 140, render(r: GroupBuy){ return r.endTime?.substring(0,16) } },
  { title: t('common.actions'), key:'act', width:160, render(r: GroupBuy){ return h(NSpace,{size:'small'},{default:()=>[
    h(NButton,{size:'tiny',onClick:()=>{editingId.value=r.id;form.value={...r};showModal.value=true}},{default:()=>t('common.edit')}),
    h(NPopconfirm,{onPositiveClick:()=>del(r.id!)},{trigger:()=>h(NButton,{size:'tiny',type:'error'},{default:()=>t('common.delete')}),default:()=>t('common.confirmDelete')})
  ]})}}
]

async function load() { loading.value = true; try { const r = await fetchGroupBuys(); items.value = r.data } finally { loading.value = false } }
async function save() {
  if (editingId.value) { await request.put(`/api/groupbuys/${editingId.value}`, form.value) }
  else { await createGroupBuy(form.value) }
  showModal.value=false; editingId.value=null; notify.success(t('common.saveSuccess')); load()
}
async function del(id:number) { try{await request.delete(`/api/groupbuys/${id}`);notify.success(t('common.deleted'));load()}catch{notify.error(t('common.deleteFailed'))} }
async function expire() { await expireGroupBuys(); notify.success(t('groupBuy.expired')); load() }
function add() { editingId.value=null; form.value={productId:0,minMembers:2,groupPrice:0}; showModal.value=true }
onMounted(load)
</script>
<template>
  <n-card :title="t('nav.groupbuy')">
    <n-space vertical>
      <n-space><n-button type="primary" @click="add">{{ t('common.add') }} {{ t('groupBuy.title') }}</n-button><n-button @click="expire">{{ t('groupBuy.expireBtn') }}</n-button></n-space>
      <n-dataTable :columns="columns" :data="items" :loading="loading" :pagination="{pageSize:10}" />
    </n-space>
    <n-drawer v-model:show="showModal" :width="380" placement="right">
      <n-drawer-content :title="editingId ? t('common.edit') + ' ' + t('groupBuy.title') : t('common.add') + ' ' + t('groupBuy.title')" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">{{ t('common.cancel') }}</n-button><n-button type="primary" @click="save">{{ t('common.save') }}</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item :label="t('common.productId')"><n-input-number v-model:value="form.productId" :min="1" /></n-form-item>
          <n-form-item :label="t('common.groupPrice')"><n-input-number v-model:value="form.groupPrice" :min="1" /></n-form-item>
          <n-form-item :label="t('groupBuy.minMembers')"><n-input-number v-model:value="form.minMembers" :min="2" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>
