<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSpace, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchAfterSales, approveAfterSale, rejectAfterSale, type AfterSale } from '@/api/aftersale'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const loading = ref(false)
const items = ref<AfterSale[]>([])
const rejectForm = ref({ id: 0, remark: '' })
const showReject = ref(false)

const typeLabel: Record<string,string> = { refund:'退款', return:'退货', exchange:'换货' }
const statusLabel: Record<string,{l:string;t:string}> = {
  '0':{l:'待审核',t:'warning'}, '1':{l:'已通过',t:'success'}, '2':{l:'已拒绝',t:'error'},
  '3':{l:'退款中',t:'info'}, '4':{l:'已完成',t:'success'}
}

const columns: DataTableColumns<AfterSale> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '订单号', key: 'orderNo', width: 160 },
  { title: '用户', key: 'username', width: 90 },
  { title: '类型', key: 'type', width: 70, render(r:AfterSale){ return typeLabel[r.type] } },
  { title: '退款金额', key: 'refundAmount', width: 90, render(r:any){ return r.refundAmount?'¥'+(r.refundAmount/100).toFixed(2):'-' } },
  { title: '状态', key: 'status', width: 80, render(r:AfterSale){ const s=statusLabel[r.status||'0']; return h(NTag,{size:'small',type:s.t as any},{default:()=>s.l}) } },
  { title: '原因', key: 'reason', width: 200, ellipsis:{tooltip:true} },
  { title: '操作', key:'act', width:140, render(r:AfterSale){
    if(r.status!=='0') return null
    return h(NSpace,{size:'small'},{default:()=>[
      h(NButton,{size:'small',type:'success',onClick:()=>doApprove(r.id!)},{default:()=>'通过'}),
      h(NButton,{size:'small',type:'error',onClick:()=>openReject(r.id!)},{default:()=>'拒绝'})
    ]})
  }},
]

async function load() { loading.value=true; try{const r=await fetchAfterSales();items.value=r.data}finally{loading.value=false} }
async function doApprove(id:number) { await approveAfterSale(id); notify.success('已通过'); load() }
function openReject(id:number) { rejectForm.value={id,remark:''}; showReject.value=true }
async function doReject() { await rejectAfterSale(rejectForm.value.id, rejectForm.value.remark); showReject.value=false; notify.success('已拒绝'); load() }
onMounted(load)
</script>
<template>
  <n-card title="售后管理">
    <n-dataTable :columns="columns" :data="items" :loading="loading" :pagination="{pageSize:10}" />
    <n-modal v-model:show="showReject" title="拒绝原因" preset="card" style="max-width:400px">
      <n-form><n-form-item><n-input v-model:value="rejectForm.remark" placeholder="填写拒绝原因" /></n-form-item></n-form>
      <template #footer><n-button @click="showReject=false">取消</n-button><n-button type="error" @click="doReject">确认拒绝</n-button></template>
    </n-modal>
  </n-card>
</template>