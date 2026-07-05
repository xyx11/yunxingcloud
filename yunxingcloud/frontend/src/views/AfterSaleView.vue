<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatPrice } from '@/utils/format'
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

const typeLabel = (k: string) => ({ refund:{l:t('afterSale.refundOnly'),c:'#d03050'}, return:{l:t('afterSale.returnRefund'),c:'#f0a020'}, exchange:{l:t('afterSale.exchange'),c:'#2080f0'} }[k] || {l:k,c:'#999'})
const statusLabel = (k: string) => ({ '0':{l:t('afterSale.statusPending'),t:'warning'}, '1':{l:t('afterSale.statusApproved'),t:'success'}, '2':{l:t('afterSale.statusRejected'),t:'error'}, '3':{l:t('afterSale.statusRefunding'),t:'info'}, '4':{l:t('afterSale.statusCompleted'),t:'success'} }[k] || {l:k,t:'default'})
const statusSteps = computed(() => [t('afterSale.statusPending'),t('afterSale.statusApproved'),t('afterSale.statusRefunding'),t('afterSale.statusCompleted')])

const columns: DataTableColumns<AfterSale> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('order.orderNo'), key: 'orderNo', width: 160 },
  { title: t('user.username'), key: 'username', width: 90 },
  { title: t('afterSale.type'), key: 'type', width: 70, render(r:AfterSale){ const tl=typeLabel(r.type); return h(NTag,{size:'small',color:{color:tl.c,textColor:'#fff'}},{default:()=>tl.l}) } },
  { title: t('afterSale.amount'), key: 'refundAmount', width: 90, render(r:any){ return r.refundAmount?formatPrice(r.refundAmount/100, 2):'-' } },
  { title: t('order.status'), key: 'status', width: 80, render(r:AfterSale){ const s=statusLabel(r.status||'0'); return h(NTag,{size:'small',type:s.t as any},{default:()=>s.l}) } },
  { title: t('afterSale.reason'), key: 'reason', width: 180, ellipsis:{tooltip:true} },
  { title: t('common.actions'), key:'act', width:180, render(r:AfterSale){
    const btns = [h(NButton,{size:'small',onClick:()=>{detailAs.value=r;showDetail.value=true}},{default:()=>t('common.viewDetail')})]
    if(r.status==='0') btns.push(h(NButton,{size:'small',type:'success',onClick:()=>doApprove(r.id!)},{default:()=>t('afterSale.statusApproved')}), h(NButton,{size:'small',type:'error',onClick:()=>openReject(r.id!)},{default:()=>t('afterSale.statusRejected')}))
    return h(NSpace,{size:'small'},{default:()=>btns})
  }},
]

async function load() { loading.value=true; try{const r=await fetchAfterSales();items.value=r.data}finally{loading.value=false} }
async function doApprove(id:number) { await approveAfterSale(id); notify.success(t('afterSale.statusApproved')); load() }
function openReject(id:number) { rejectForm.value={id,remark:''}; showReject.value=true }
async function doReject() { await rejectAfterSale(rejectForm.value.id, rejectForm.value.remark); showReject.value=false; notify.success(t('afterSale.statusRejected')); load() }
onMounted(load)
</script>
<template>
  <n-card :title="t('afterSale.title')">
    <n-dataTable :columns="columns" :data="items" :loading="loading" :pagination="{pageSize:10}" />
    <n-drawer v-model:show="showReject" :width="360" placement="right">
      <n-drawer-content :title="t('afterSale.rejectReason')" closable>
        <template #footer><n-space justify="end"><n-button @click="showReject=false">取消</n-button><n-button type="error" @click="doReject">确认拒绝</n-button></n-space></template>
        <n-form label-placement="left" label-width="60" size="small"><n-form-item :label="t('afterSale.reason')"><n-input v-model:value="rejectForm.remark" :placeholder="t('afterSale.rejectPlaceholder')" /></n-form-item></n-form>
      </n-drawer-content>
    </n-drawer>
    <n-drawer v-model:show="showDetail" :width="450" placement="right">
      <n-drawer-content :title="t('afterSale.detail')" closable>
        <template v-if="detailAs">
          <n-form label-placement="left" label-width="70" size="small">
            <n-form-item :label="t('order.orderNo')"><span>{{ detailAs.orderNo }}</span></n-form-item>
            <n-form-item :label="t('user.username')"><span>{{ detailAs.username }}</span></n-form-item>
            <n-form-item :label="t('afterSale.type')"><n-tag size="small" :color="{color:typeLabel(detailAs.type)?.c,textColor:'#fff'}">{{ typeLabel(detailAs.type)?.l }}</n-tag></n-form-item>
            <n-form-item :label="t('afterSale.amount')"><b>{{ formatPrice((detailAs.refundAmount||0)/100, 2) }}</b></n-form-item>
          </n-form>
          <n-divider />
          <div style="display:flex;justify-content:space-between;margin:12px 0">
            <div v-for="(s,i) in statusSteps" :key="i" style="text-align:center;flex:1;position:relative">
              <div :style="{width:22,height:22,borderRadius:'50%',display:'inline-flex',alignItems:'center',justifyContent:'center',fontSize:11,background:i<=['待审核','已通过','退款中','已完成'].indexOf(statusLabel(detailAs?.status||'0')?.l)+1&&Number(detailAs?.status)>=i?'#18a058':'#e8e8e8',color:i<=['待审核','已通过','退款中','已完成'].indexOf(statusLabel(detailAs?.status||'0')?.l)+1?'#fff':'#999'}">{{ i<=Number(detailAs?.status) ? '✓' : i+1 }}</div>
              <div style="font-size:10px;margin-top:3px;color:Number(detailAs?.status)>=i&&detailAs?.status!=='2'?'#18a058':'#999'">{{ s }}</div>
            </div>
          </div>
          <n-divider />
          <p style="color:#666;font-size:13px"><b>{{ t('afterSale.reason') }}:</b> {{ detailAs.reason }}</p>
        </template>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>