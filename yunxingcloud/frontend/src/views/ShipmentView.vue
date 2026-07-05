<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NTag, NTimeline, NTimelineItem, NSpin } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()
const loading = ref(false); const saving = ref(false); const tracking = ref(false)
const items = ref<any[]>([])
const showModal = ref(false); const showTrack = ref(false)
const form = ref({ orderId:'', carrier:'', trackingNo:'', status:'0' })
const searchKeyword = ref('')
const trackNo = ref(''); const trackResult = ref<any>(null); const trackSteps = ref<any[]>([])

const filtered = computed(() => { if(!searchKeyword.value)return items.value;const kw=searchKeyword.value.toLowerCase();return items.value.filter((s:any)=>s.carrier?.toLowerCase().includes(kw)||s.trackingNo?.toLowerCase().includes(kw)) })

const columns: DataTableColumns<any> = [
  { title:'ID',key:'id',width:50 }, { title:'订单ID',key:'orderId',width:70 },
  { title:'快递公司',key:'carrier',width:100 }, { title:'快递单号',key:'trackingNo',width:160 },
  { title:'状态',key:'status',width:80,render(r:any){return h(NTag,{size:'small',type:r.status==='2'?'success':'info'},{default:()=>r.status==='2'?'已签收':'运输中'})} },
  { title:'时间',key:'createdAt',width:140,render(r:any){return r.createdAt?.substring(0,16)}},
  { title:'操作',key:'act',width:140,render(r:any){return h(NSpace,{size:'small'},{default:()=>[h(NButton,{size:'tiny',onClick:()=>{trackNo.value=r.trackingNo;trackResult.value=null;trackSteps.value=[];showTrack.value=true;doTrack(r.trackingNo)}},{default:()=>'追踪'})]})}},
]

async function load() { loading.value=true; try{const r=await request.get('/api/shipments/all');items.value=r.data.content||r.data||[]}finally{loading.value=false} }
async function save() { saving.value=true; try{await request.post('/api/shipments',form.value);showModal.value=false;notify.success(t('common.save'));load()}catch{notify.error(t('common.saveFailed'))}finally{saving.value=false} }
function add() { form.value={orderId:'',carrier:'顺丰速运',trackingNo:'',status:'1'}; showModal.value=true }

async function doTrack(no: string) {
  tracking.value = true; trackSteps.value = []
  try {
    const r = await request.get(`/api/logistics/track/${no}`)
    trackResult.value = r.data
    if (r.data?.steps) trackSteps.value = r.data.steps
    else trackSteps.value = [{ time: new Date().toISOString(), status: '已揽件', desc: '快递单号: ' + no }]
  } catch {
    trackSteps.value = [{ time: new Date().toISOString(), status: '查询中', desc: '物流信息更新中...' }]
  } finally { tracking.value = false }
}

onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card title="物流发货管理"><template #header-extra><n-space><n-button size="small" @click="showTrack=true">单号追踪</n-button><n-button type="primary" size="small" @click="add">+ 录入发货</n-button></n-space></template>
      <n-space class="mb-12">
        <n-input v-model:value="searchKeyword" placeholder="搜索快递..." size="small" clearable style="width:180px"/><n-button size="small" @click="load" secondary>刷新</n-button>
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r:any)=>r.id" :pagination="{pageSize:10}" size="small"/>
    </n-card>
    <n-drawer v-model:show="showModal" :width="420" placement="right">
      <n-drawer-content title="录入发货" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" :loading="saving" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item label="订单ID"><n-input v-model:value="form.orderId"/></n-form-item>
          <n-form-item :label="t('order.carrier')"><n-select v-model:value="form.carrier" :options="[{label:'顺丰速运',value:'顺丰速运'},{label:'中通快递',value:'中通快递'},{label:'圆通速递',value:'圆通速递'},{label:'韵达快递',value:'韵达快递'},{label:'EMS',value:'EMS'}]"/></n-form-item>
          <n-form-item label="快递单号"><n-input v-model:value="form.trackingNo"/></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
    <n-modal v-model:show="showTrack" title="物流追踪" preset="card" style="max-width:450px">
      <n-space vertical>
        <n-space><n-input v-model:value="trackNo" placeholder="输入快递单号" style="width:200px" size="small" /><n-button type="primary" size="small" :loading="tracking" @click="doTrack(trackNo)">查询</n-button></n-space>
        <n-spin :show="tracking" />
        <n-timeline v-if="trackSteps.length">
          <n-timeline-item v-for="(s,i) in trackSteps" :key="i" :type="i===0?'success':i===trackSteps.length-1?'info':'default'" :title="s.status" :time="s.time?.substring(0,16)" :content="s.desc" />
        </n-timeline>
        <div v-if="!trackSteps.length && !tracking" style="text-align:center;color:#999;padding:20px">输入快递单号点击查询</div>
      </n-space>
    </n-modal>
  </div>
</template>
