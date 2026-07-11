<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatPrice } from '@/utils/format'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NInputNumber, NSelect, NSpace, NTag, NPopconfirm, NTimeline, NTimelineItem } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchCampaigns, createCampaign, type Campaign } from '@/api/campaign'
import { useNotify } from '@/composables/useNotify'
import request from '@/api/request'

const notify = useNotify()
const items = ref<Campaign[]>([])
const showModal = ref(false); const editingId = ref<number|null>(null)
const form = ref<Campaign>({ name:'', type:'full_reduction', threshold:0, discount:0 })
const searchKeyword = ref('')

const typeOpts = [{label:'满减',value:'full_reduction'},{label:'折扣',value:'discount'},{label:'赠品',value:'gift'}]
const statusLabel: Record<string,string> = {'0':'未开始','1':'进行中','2':'已结束'}

const columns: DataTableColumns<Campaign> = [
  { title: t('common.name'), key: 'name', width: 160 }, { title: '类型', key: 'type', width: 70, render(r: Campaign){ return typeOpts.find(o=>o.value===r.type)?.label } },
  { title: '门槛', key: 'threshold', width: 90, render(r: Campaign){ return r.threshold?formatPrice(r.threshold/100, 2):'-' } },
  { title: '优惠', key: 'discount', width: 90, render(r: Campaign){ return r.type==='discount'?r.discount+'%':formatPrice(r.discount/100, 2) } },
  { title: '状态', key: 'status', width: 80, render(r: Campaign){ return h(NTag,{size:'small',type:r.status==='1'?'success':r.status==='2'?'default':'warning'},{default:()=>statusLabel[r.status]}) } },
  { title: t('common.actions'), key:'act', width:120, render(r: Campaign){ return h(NSpace,{size:'small'},{default:()=>[
    h(NButton,{size:'tiny',onClick:()=>{editingId.value=r.id;form.value={...r};showModal.value=true}},{default:()=>'编辑'}),
    h(NPopconfirm,{onPositiveClick:()=>del(r.id!)},{trigger:()=>h(NButton,{size:'tiny',type:'error'},{default:()=>'删除'}),default:()=>t('common.confirmDelete')})
  ]})}}
]

async function load() { const r = await fetchCampaigns(); items.value = r.data }
async function save() {
  if (editingId.value) { await request.put(`/api/campaigns/${editingId.value}`, form.value) }
  else { await createCampaign(form.value) }
  showModal.value=false; editingId.value=null; notify.success(t('common.saveSuccess')); load()
}
async function del(id:number) { try{await request.delete(`/api/campaigns/${id}`);notify.success(t('common.deleted'));load()}catch{notify.error(t('common.deleteFailed'))} }
function add() { editingId.value=null; form.value={name:'',type:'full_reduction',threshold:0,discount:0}; showModal.value=true }
onMounted(load)
</script>
<template>
  <n-card title="营销活动">
    <n-space vertical>
      <n-space><n-button type="primary" @click="add">新建活动</n-button><n-input v-model:value="searchKeyword" placeholder="搜索..." size="small" clearable class="w-160" /></n-space>
      <n-dataTable :columns="columns" :data="items" :pagination="{pageSize:10}" />
    </n-space>
    <n-drawer v-model:show="showModal" :width="400" placement="right">
      <n-drawer-content :title="editingId?'编辑活动':'新建活动'" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item :label="t('common.name')"><n-input v-model:value="form.name" /></n-form-item>
          <n-form-item label="类型"><n-select v-model:value="form.type" :options="typeOpts" /></n-form-item>
          <n-form-item label="门槛(分)"><n-input-number v-model:value="form.threshold" :min="0" /></n-form-item>
          <n-form-item label="优惠(分/%)"><n-input-number v-model:value="form.discount" :min="1" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>

<style scoped>
.w-160 { width: 160px; }
</style>
</template>
