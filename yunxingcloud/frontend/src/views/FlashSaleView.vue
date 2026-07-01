<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInputNumber, NSpace, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchFlashSales, createFlashSale, preheatFlashSale, getFlashSale, type FlashSale } from '@/api/flashsale'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const loading = ref(false)
const items = ref<FlashSale[]>([])
const showModal = ref(false)
const form = ref<FlashSale>({ productId:0, flashPrice:0, stock:0, limitPerUser:1, startTime:'', endTime:'' })

const statusLabel: Record<string,string> = { '0':'未开始', '1':'进行中', '2':'已结束' }

const columns: DataTableColumns<FlashSale> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '商品ID', key: 'productId', width: 80 },
  { title: '秒杀价', key: 'flashPrice', width: 90, render(r:any){ return '¥'+(r.flashPrice/100).toFixed(2) } },
  { title: '库存/已售', key: 'stock', width: 100, render(r:any){ return `${r.sold||0}/${r.stock}` } },
  { title: '限购', key: 'limitPerUser', width: 60 },
  { title: '状态', key: 'status', width: 80, render(r:FlashSale){ return h(NTag, {size:'small', type:r.status==='1'?'error':'default'},{default:()=>statusLabel[r.status||'0']}) } },
  { title: '开始', key: 'startTime', width: 140, render(r:any){ return r.startTime?.substring(0,16) } },
  { title: '结束', key: 'endTime', width: 140, render(r:any){ return r.endTime?.substring(0,16) } },
  { title: '操作', key:'act', width:80, render(r:FlashSale){ return r.status==='1'?h(NButton,{size:'small',onClick:()=>preheat(r.id!)},{default:()=>'预热'}):null } },
]

async function load() { loading.value=true; try{const r=await fetchFlashSales();items.value=r.data}finally{loading.value=false} }
async function save() { await createFlashSale(form.value); showModal.value=false; notify.success('创建成功'); load() }
async function preheat(id:number) { await preheatFlashSale(id); const r=await getFlashSale(id); notify.success(`预热完成, 库存: ${r.data.remainingStock}`) }
onMounted(load)
</script>
<template>
  <n-card title="秒杀管理">
    <n-space vertical>
      <n-button type="primary" @click="showModal=true" style="align-self:flex-start">新增秒杀</n-button>
      <n-dataTable :columns="columns" :data="items" :loading="loading" :pagination="{pageSize:10}" />
    </n-space>
    <n-modal v-model:show="showModal" title="新增秒杀" preset="card" style="max-width:400px">
      <n-form :model="form">
        <n-form-item label="商品ID"><n-input-number v-model:value="form.productId" :min="1" /></n-form-item>
        <n-form-item label="秒杀价(分)"><n-input-number v-model:value="form.flashPrice" :min="1" /></n-form-item>
        <n-form-item label="库存"><n-input-number v-model:value="form.stock" :min="1" /></n-form-item>
        <n-form-item label="每人限购"><n-input-number v-model:value="form.limitPerUser" :min="1" :max="10" /></n-form-item>
      </n-form>
      <template #footer><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></template>
    </n-modal>
  </n-card>
</template>