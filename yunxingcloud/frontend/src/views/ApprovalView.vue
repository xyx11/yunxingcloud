<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, h } from 'vue'
import { fetchMyApprovals, fetchPendingApprovals, createApproval, approveRequest, rejectRequest } from '@/api/approval'
import { useNotify } from '@/composables/useNotify'

import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSelect, NSpace, NTag } from 'naive-ui'

const { t } = useI18n()
import type { DataTableColumns } from 'naive-ui'


interface Approval { id:number; applicant:string; approver:string; type:string; title:string; content:string; status:string; remark:string; createdAt:string }

const notify = useNotify()

const items = ref<Approval[]>([])
const pending = ref<Approval[]>([])
const loading = ref(false)
const tab = ref<'my'|'pending'>('my')
const showModal = ref(false)
const form = ref({ approver:'admin', type: t('approval.typeLeave'), title:'', content:'' })
const saving = ref(false)

const statusMap: Record<string, {label:string,type:'info'|'success'|'error'}> = { '0':{label:t('approval.statuses.pending'),type:'info'}, '1':{label:t('approval.statuses.approved'),type:'success'}, '2':{label:t('approval.statuses.rejected'),type:'error'} }

const columns: DataTableColumns<Approval> = [
  { title:t('approval.title'), key:'title', width:150 },
  { title:t('approval.applicantOrApprover'), key:tab.value==='pending'?'applicant':'approver', width:80 },
  { title:t('approval.type'), key:'type', width:60 },
  { title:t('approval.status'), key:'status', width:70, render:(row:any)=>h(NTag,{type:statusMap[row.status]?.type||'default',size:'small'},{default:()=>statusMap[row.status]?.label||row.status}) },
  { title:t('approval.time'), key:'createdAt', width:150, render:(row:any)=>row.createdAt?.substring(0,19).replace('T',' ')||'-' },
  { title:t('approval.actions'), key:'actions', width:120, render:(row:any)=>h(NSpace,null,{default:()=>[
    tab.value==='pending'&&row.status==='0'?h(NButton,{size:'tiny',type:'success',onClick:()=>approve(row.id)},{default:()=>t('approval.approve')}):null,
    tab.value==='pending'&&row.status==='0'?h(NButton,{size:'tiny',type:'error',onClick:()=>reject(row.id)},{default:()=>t('approval.reject')}):null,
  ]})},
]

async function loadItems() { loading.value=true;try{const r=await (tab.value==='my'?fetchMyApprovals():fetchPendingApprovals());items.value=r.data;pending.value=r.data}catch { /* ignore */ }loading.value=false }
async function approve(id:number){await approveRequest(id);loadItems();notify.success(t('approval.approveSuccess'))}
async function reject(id:number){await rejectRequest(id);loadItems();notify.success(t('approval.rejectSuccess'))}
async function submit(){saving.value=true;try{await createApproval(form.value);showModal.value=false;form.value={approver:'admin',type:t('approval.typeLeave'),title:'',content:''};loadItems();notify.success(t('approval.createSuccess'))}catch { /* ignore */ }finally{saving.value=false}}
const checkedRowKeys = ref<number[]>([])
async function batchApprove(){if(!checkedRowKeys.value.length)return;try{for(const id of checkedRowKeys.value)await approveRequest(id);checkedRowKeys.value=[];loadItems();notify.success(t('approval.approveSuccess'))}catch{notify.error(t('common.saveFailed'))}}
async function batchReject(){if(!checkedRowKeys.value.length)return;try{for(const id of checkedRowKeys.value)await rejectRequest(id);checkedRowKeys.value=[];loadItems();notify.success(t('approval.rejectSuccess'))}catch{notify.error(t('common.saveFailed'))}}

onMounted(loadItems)
</script>

<template>
  <div class="view-pad">
    <n-card :title="t('nav.approval')">
      <template #header-extra>
        <n-space>
          <n-button :type="tab==='my'?'primary':'default'" size="small" @click="tab='my';loadItems()">{{ t('approval.myApprovals') }}</n-button>
          <n-button :type="tab==='pending'?'primary':'default'" size="small" @click="tab='pending';loadItems()">{{ t('approval.pending') }}</n-button>
          <n-button type="primary" size="small" @click="showModal=true"><template #icon>＋</template>{{ t('approval.create') }}</n-button>
          <n-button v-if="tab==='pending'&&checkedRowKeys.length" type="success" size="small" @click="batchApprove">✓ {{ t('approval.approve') }} ({{checkedRowKeys.length}})</n-button>
          <n-button v-if="tab==='pending'&&checkedRowKeys.length" type="error" size="small" @click="batchReject">✕ {{ t('approval.reject') }} ({{checkedRowKeys.length}})</n-button>
        </n-space>
      </template>
      <n-dataTable :columns="columns" :data="items" :loading="loading" size="small" :bordered="false" :pagination="{pageSize:10}" :row-key="(r:Approval)=>r.id" v-model:checked-row-keys="checkedRowKeys" />

      <n-drawer v-model:show="showModal" :width="420" placement="right">
        <n-drawer-content :title="t('approval.create')" closable>
          <template #footer><n-space justify="end"><n-button @click="showModal=false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="submit">{{ t('common.submit') }}</n-button></n-space></template>
          <n-form label-placement="left" label-width="60" size="small">
            <n-form-item :label="t('approval.approver')"><n-input v-model:value="form.approver" /></n-form-item>
            <n-form-item :label="t('approval.type')"><n-select v-model:value="form.type" :options="[{label:t('approval.typeLeave'),value:t('approval.typeLeave')},{label:t('approval.typeExpense'),value:t('approval.typeExpense')},{label:t('approval.typeTravel'),value:t('approval.typeTravel')}]" /></n-form-item>
            <n-form-item :label="t('approval.title')"><n-input v-model:value="form.title" /></n-form-item>
            <n-form-item :label="t('approval.content')"><n-input v-model:value="form.content" type="textarea" :rows="3" /></n-form-item>
          </n-form>
        </n-drawer-content>
      </n-drawer>
    </n-card>
  </div>
</template>
