<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatPrice } from '@/utils/format'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInputNumber, NSpace, NTag, NPopconfirm } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchFlashSales, createFlashSale, preheatFlashSale, getFlashSale, type FlashSale } from '@/api/flashsale'
import { useNotify } from '@/composables/useNotify'
import request from '@/api/request'

const notify = useNotify()
const loading = ref(false)
const items = ref<FlashSale[]>([])
const showModal = ref(false); const editingId = ref<number|null>(null)
const form = ref<FlashSale>({ productId:0, flashPrice:0, stock:0, limitPerUser:1, startTime:'', endTime:'' })

const statusLabel: Record<string,string> = { '0':t('common.notStarted'), '1':t('common.inProgress'), '2':t('common.ended2') }

const columns: DataTableColumns<FlashSale> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('common.productId'), key: 'productId', width: 80 },
  { title: t('flashSale.flashPrice'), key: 'flashPrice', width: 90, render(r:any){ return formatPrice(r.flashPrice/100, 2) } },
  { title: t('common.stock') + '/' + t('common.sold'), key: 'stock', width: 100, render(r:any){ return `${r.sold||0}/${r.stock}` } },
  { title: t('common.limitPerUser'), key: 'limitPerUser', width: 60 },
  { title: t('common.status'), key: 'status', width: 80, render(r:FlashSale){ return h(NTag, {size:'small', type:r.status==='1'?'error':'default'},{default:()=>statusLabel[r.status||'0']}) } },
  { title: t('common.startTime'), key: 'startTime', width: 140, render(r:any){ return r.startTime?.substring(0,16) } },
  { title: t('common.endTime'), key: 'endTime', width: 140, render(r:any){ return r.endTime?.substring(0,16) } },
  { title: t('common.actions'), key:'act', width:160, render(r:FlashSale){ return h(NSpace,{size:'small'},{default:()=>[
    r.status==='1'?h(NButton,{size:'small',onClick:()=>preheat(r.id!)},{default:()=>t('flashSale.preheat')}):null,
    h(NButton,{size:'tiny',onClick:()=>{editingId.value=r.id ?? null;form.value={...r};showModal.value=true}},{default:()=>t('common.edit')}),
    h(NPopconfirm,{onPositiveClick:()=>del(r.id!)},{trigger:()=>h(NButton,{size:'tiny',type:'error'},{default:()=>t('common.delete')}),default:()=>t('common.confirmDelete')})
  ]})}}
]

async function load() { loading.value=true; try{const r=await fetchFlashSales();items.value=r.data}finally{loading.value=false} }
async function save() {
  const data:any = {...form.value}
  if (editingId.value) { await request.put(`/api/flashsales/${editingId.value}`, data) }
  else { await createFlashSale(data) }
  showModal.value=false; editingId.value=null; notify.success(t('common.saveSuccess')); load()
}
async function del(id:number) { try{await request.delete(`/api/flashsales/${id}`);notify.success(t('common.deleted'));load()}catch{notify.error(t('common.deleteFailed'))} }
async function preheat(id:number) { await preheatFlashSale(id); const r=await getFlashSale(id); notify.success(t('flashSale.preheatDone') + r.data.remainingStock) }
function add() { editingId.value=null; form.value={productId:0,flashPrice:0,stock:0,limitPerUser:1,startTime:'',endTime:''}; showModal.value=true }
onMounted(load)
</script>
<template>
  <n-card :title="t('nav.flashsale')">
    <n-space vertical>
      <n-button type="primary" @click="add" class="align-start">{{ t('common.add') }} {{ t('flashSale.title') }}</n-button>
      <n-dataTable :columns="columns" :data="items" :loading="loading" :pagination="{pageSize:10}" />
    </n-space>
    <n-drawer v-model:show="showModal" :width="380" placement="right">
      <n-drawer-content :title="editingId ? t('common.edit') + ' ' + t('flashSale.title') : t('common.add') + ' ' + t('flashSale.title')" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">{{ t('common.cancel') }}</n-button><n-button type="primary" @click="save">{{ t('common.save') }}</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item :label="t('common.productId')"><n-input-number v-model:value="form.productId" :min="1" /></n-form-item>
          <n-form-item :label="t('flashSale.flashPrice') + ' (' + t('common.cent') + ')'"><n-input-number v-model:value="form.flashPrice" :min="1" /></n-form-item>
          <n-form-item :label="t('product.stock')"><n-input-number v-model:value="form.stock" :min="1" /></n-form-item>
          <n-form-item :label="t('common.limitPerUser')"><n-input-number v-model:value="form.limitPerUser" :min="1" :max="10" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>

<style scoped>
.align-start { align-self: flex-start; }
</style>
