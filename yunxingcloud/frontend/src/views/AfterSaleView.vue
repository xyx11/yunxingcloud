<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSpace, NTag, NDivider } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchAfterSales, approveAfterSale, rejectAfterSale, type AfterSale } from '@/api/aftersale'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const loading = ref(false)
const items = ref<AfterSale[]>([])
const rejectForm = ref({ id: 0, remark: '' })
const showReject = ref(false); const showDetail = ref(false)
const detailAs = ref<any>(null)

const typeLabel: Record<string,{l:string;c:string}> = { refund:{l:'退款',c:'#d03050'}, return:{l:'退货',c:'#f0a020'}, exchange:{l:'换货',c:'#2080f0'} }
const statusLabel: Record<string,{l:string;t:string}> = {
  '0':{l:'待审核',t:'warning'}, '1':{l:'已通过',t:'success'}, '2':{l:'已拒绝',t:'error'},
  '3':{l:'退款中',t:'info'}, '4':{l:'已完成',t:'success'}
}
const statusSteps = ['待审核','已通过','退款中','已完成']

const columns: DataTableColumns<AfterSale> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '订单号', key: 'orderNo', width: 160 },
  { title: '用户', key: 'username', width: 90 },
  { title: '类型', key: 'type', width: 70, render(r:AfterSale){ const tl=typeLabel[r.type]; return h(NTag,{size:'small',color:{color:tl.c,textColor:'#fff'}},{default:()=>tl.l}) } },
  { title: '退款金额', key: 'refundAmount', width: 90, render(r:any){ return r.refundAmount?'¥'+(r.refundAmount/100).toFixed(2):'-' } },
  { title: '状态', key: 'status', width: 80, render(r:AfterSale){ const s=statusLabel[r.status||'0']; return h(NTag,{size:'small',type:s.t as any},{default:()=>s.l}) } },
  { title: '原因', key: 'reason', width: 180, ellipsis:{tooltip:true} },
  { title: '操作', key:'act', width:180, render(r:AfterSale){
    const btns = [h(NButton,{size:'small',onClick:()=>{detailAs.value=r;showDetail.value=true}},{default:()=>'详情'})]
    if(r.status==='0') btns.push(h(NButton,{size:'small',type:'success',onClick:()=>doApprove(r.id!)},{default:()=>'通过'}), h(NButton,{size:'small',type:'error',onClick:()=>openReject(r.id!)},{default:()=>'拒绝'}))
    return h(NSpace,{size:'small'},{default:()=>btns})
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
    <n-drawer v-model:show="showReject" :width="360" placement="right">
      <n-drawer-content title="拒绝原因" closable>
        <template #footer><n-space justify="end"><n-button @click="showReject=false">取消</n-button><n-button type="error" @click="doReject">确认拒绝</n-button></n-space></template>
        <n-form label-placement="left" label-width="60" size="small"><n-form-item label="原因"><n-input v-model:value="rejectForm.remark" placeholder="填写拒绝原因" /></n-form-item></n-form>
      </n-drawer-content>
    </n-drawer>
    <n-drawer v-model:show="showDetail" :width="450" placement="right">
      <n-drawer-content title="售后详情" closable>
        <template v-if="detailAs">
          <n-form label-placement="left" label-width="70" size="small">
            <n-form-item label="订单号"><span>{{ detailAs.orderNo }}</span></n-form-item>
            <n-form-item label="用户"><span>{{ detailAs.username }}</span></n-form-item>
            <n-form-item label="类型"><n-tag size="small" :color="{color:typeLabel[detailAs.type]?.c,textColor:'#fff'}">{{ typeLabel[detailAs.type]?.l }}</n-tag></n-form-item>
            <n-form-item label="金额"><b>¥{{ ((detailAs.refundAmount||0)/100).toFixed(2) }}</b></n-form-item>
          </n-form>
          <n-divider />
          <div style="display:flex;justify-content:space-between;margin:12px 0">
            <div v-for="(s,i) in statusSteps" :key="i" style="text-align:center;flex:1;position:relative">
              <div :style="{width:22,height:22,borderRadius:'50%',display:'inline-flex',alignItems:'center',justifyContent:'center',fontSize:11,background:i<=['待审核','已通过','退款中','已完成'].indexOf(statusLabel[detailAs?.status||'0']?.l)+1&&Number(detailAs?.status)>=i?'#18a058':'#e8e8e8',color:i<=['待审核','已通过','退款中','已完成'].indexOf(statusLabel[detailAs?.status||'0']?.l)+1?'#fff':'#999'}">{{ i<=Number(detailAs?.status) ? '✓' : i+1 }}</div>
              <div style="font-size:10px;margin-top:3px;color:Number(detailAs?.status)>=i&&detailAs?.status!=='2'?'#18a058':'#999'">{{ s }}</div>
            </div>
          </div>
          <n-divider />
          <p style="color:#666;font-size:13px"><b>原因:</b> {{ detailAs.reason }}</p>
        </template>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>