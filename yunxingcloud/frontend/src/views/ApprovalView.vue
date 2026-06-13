<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NTag, darkTheme, lightTheme } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

interface Approval { id:number; applicant:string; approver:string; type:string; title:string; content:string; status:string; remark:string; createdAt:string }

const notify = useNotify()
const currentTheme = computed(() => localStorage.getItem("theme") === "dark" ? darkTheme : lightTheme)
const items = ref<Approval[]>([])
const pending = ref<Approval[]>([])
const loading = ref(false)
const tab = ref<'my'|'pending'>('my')
const showModal = ref(false)
const form = ref({ approver:'admin', type:'请假', title:'', content:'' })
const saving = ref(false)

const statusMap: Record<string, {label:string,type:'info'|'success'|'error'}> = { '0':{label:'审批中',type:'info'}, '1':{label:'已通过',type:'success'}, '2':{label:'已驳回',type:'error'} }

const columns: DataTableColumns<Approval> = [
  { title:'标题', key:'title', width:150 },
  { title:tab.value==='pending'?'申请人':'审批人', key:tab.value==='pending'?'applicant':'approver', width:80 },
  { title:'类型', key:'type', width:60 },
  { title:'状态', key:'status', width:70, render:(row:any)=>h(NTag,{type:statusMap[row.status]?.type||'default',size:'small'},{default:()=>statusMap[row.status]?.label||row.status}) },
  { title:'时间', key:'createdAt', width:150, render:(row:any)=>row.createdAt?.substring(0,19).replace('T',' ')||'-' },
  { title:'操作', key:'actions', width:120, render:(row:any)=>h(NSpace,null,{default:()=>[
    tab.value==='pending'&&row.status==='0'?h(NButton,{size:'tiny',type:'success',onClick:()=>approve(row.id)},{default:()=>'通过'}):null,
    tab.value==='pending'&&row.status==='0'?h(NButton,{size:'tiny',type:'error',onClick:()=>reject(row.id)},{default:()=>'驳回'}):null,
  ]})},
]

async function loadItems() { loading.value=true;try{const r=await request.get(tab.value==='my'?'/api/approval':'/api/approval/pending');items.value=r.data;pending.value=r.data}catch{}loading.value=false }
async function approve(id:number){await request.put(`/api/approval/${id}/approve`,{});loadItems();notify.success('已通过')}
async function reject(id:number){await request.put(`/api/approval/${id}/reject`,{});loadItems();notify.success('已驳回')}
async function submit(){saving.value=true;try{await request.post('/api/approval',form.value);showModal.value=false;form.value={approver:'admin',type:'请假',title:'',content:''};loadItems();notify.success('提交成功')}catch{}finally{saving.value=false}}

onMounted(loadItems)
</script>

<template>
  <n-config-provider :theme="currentTheme"><div style="padding:20px">
    <n-card title="审批流程">
      <template #header-extra>
        <n-space>
          <n-button :type="tab==='my'?'primary':'default'" size="small" @click="tab='my';loadItems()">我的申请</n-button>
          <n-button :type="tab==='pending'?'primary':'default'" size="small" @click="tab='pending';loadItems()">待审批</n-button>
          <n-button type="primary" size="small" @click="showModal=true"><template #icon>＋</template>新建申请</n-button>
        </n-space>
      </template>
      <n-dataTable :columns="columns" :data="items" :loading="loading" size="small" :bordered="false" :pagination="{pageSize:10}" :row-key="(r:Approval)=>r.id" />

      <n-modal v-model:show="showModal" title="新建申请" preset="card" display-directive="show" style="width:480px">
        <n-form label-placement="left" label-width="60">
          <n-form-item label="审批人"><n-input v-model:value="form.approver" /></n-form-item>
          <n-form-item label="类型"><n-select v-model:value="form.type" :options="[{label:'请假',value:'请假'},{label:'报销',value:'报销'},{label:'出差',value:'出差'}]" /></n-form-item>
          <n-form-item label="标题"><n-input v-model:value="form.title" /></n-form-item>
          <n-form-item label="内容"><n-input v-model:value="form.content" type="textarea" :rows="3" /></n-form-item>
        </n-form>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" :loading="saving" @click="submit">提交</n-button></n-space></template>
      </n-modal>
    </n-card>
  </div></n-config-provider>
</template>
