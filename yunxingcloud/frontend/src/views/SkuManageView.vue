<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatPrice } from '@/utils/format'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NInputNumber, NSelect, NSpace, NPopconfirm, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()
const loading = ref(false); const saving = ref(false)
const items = ref<Record<string, unknown>[]>([]); const products = ref<Record<string, unknown>[]>([])
const showModal = ref(false); const editingId = ref<number|null>(null)
const form = ref({ productId: null as number | null, name:'', price:1, stock:0, skuCode:'', status:'0' })
const searchKeyword = ref('')

const filtered = computed(() => { if(!searchKeyword.value)return items.value; const kw=searchKeyword.value.toLowerCase(); return items.value.filter((s)=>((s.name as string)||'').toLowerCase().includes(kw)||((s.skuCode as string)||'').toLowerCase().includes(kw)) })
const productOpts = computed(() => products.value.map((p)=>({label:p.name as string,value:p.id as number})))

const columns: DataTableColumns<Record<string, unknown>> = [
  { title:'ID',key:'id',width:50 }, { title:'商品',key:'productName',width:120 }, { title:'SKU名称',key:'name',width:120 },
  { title:'价格',key:'price',width:80,render:(r)=>formatPrice(((r.price as number)||0)/100, 2) },
  { title:'库存',key:'stock',width:60 }, { title:'编码',key:'skuCode',width:100 },
  { title:'操作',key:'act',width:120,render(r){return h(NSpace,{size:'small'},{default:()=>[h(NButton,{size:'tiny',onClick:()=>{editingId.value=r.id as number;form.value={productId:r.productId as number,name:r.name as string,price:(r.price as number)/100,stock:r.stock as number,skuCode:(r.skuCode as string)||'',status:r.status as string};showModal.value=true}},{default:()=>'编辑'}),h(NPopconfirm,{onPositiveClick:()=>del(r.id as number)},{trigger:()=>h(NButton,{size:'tiny',type:'error'},{default:()=>'删除'}),default:()=>t('common.confirmDelete')})]})}}
]

async function load() { loading.value=true; try{const r=await request.get('/api/products/skus/all');items.value=r.data.content||r.data||[];const p=await request.get('/api/products?size=100');products.value=p.data.content||[]}finally{loading.value=false} }
async function save() { saving.value=true; const data={...form.value,price:Math.round(form.value.price*100)}; try{editingId.value?await request.put('/api/skus/'+editingId.value,data):await request.post('/api/products/'+form.value.productId+'/skus',data);showModal.value=false;editingId.value=null;notify.success(t('common.save'));load()}catch{notify.error(t('common.saveFailed'))}finally{saving.value=false} }
async function del(id:number) { try{await request.delete('/api/skus/'+id);notify.success(t('common.deleted'));load()}catch{notify.error(t('common.saveFailed'))} }
function add() { editingId.value=null; form.value={productId:null,name:'',price:1,stock:0,skuCode:'',status:'0'}; showModal.value=true }
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card title="SKU 规格管理"><template #header-extra><n-button type="primary" size="small" @click="add">+ 新增</n-button></template>
      <n-space class="mb-12"><n-input v-model:value="searchKeyword" placeholder="搜索SKU..." size="small" clearable class="w-180"/><n-button size="small" @click="load" secondary>刷新</n-button></n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r: Record<string, unknown>)=>r.id" :pagination="{pageSize:10}" size="small"/>
    </n-card>
    <n-drawer v-model:show="showModal" :width="400" placement="right">
      <n-drawer-content :title="editingId?'编辑SKU':'新增SKU'" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" :loading="saving" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item label="商品"><n-select v-model:value="form.productId" :options="productOpts" placeholder="选择商品" filterable /></n-form-item>
          <n-form-item label="SKU名称"><n-input v-model:value="form.name" placeholder="如: 皓月白 128GB"/></n-form-item>
          <n-form-item label="价格(¥)"><n-input-number v-model:value="form.price" :min="0.01" :step="0.01"/></n-form-item>
          <n-form-item label="库存"><n-input-number v-model:value="form.stock" :min="0"/></n-form-item>
          <n-form-item label="编码"><n-input v-model:value="form.skuCode" placeholder="SKU-001"/></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<style scoped>
.w-180 { width: 180px; }
</style>
