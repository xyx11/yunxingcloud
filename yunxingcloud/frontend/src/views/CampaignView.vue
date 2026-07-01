<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NInputNumber, NSelect, NSpace, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchCampaigns, createCampaign, type Campaign } from '@/api/campaign'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<Campaign[]>([])
const showModal = ref(false)
const form = ref<Campaign>({ name:'', type:'full_reduction', threshold:0, discount:0 })

const typeOpts = [{label:'满减',value:'full_reduction'},{label:'折扣',value:'discount'},{label:'赠品',value:'gift'}]
const statusLabel: Record<string,string> = {'0':'未开始','1':'进行中','2':'已结束'}

const columns: DataTableColumns<Campaign> = [
  { title: '名称', key: 'name', width: 160 }, { title: '类型', key: 'type', width: 70, render(r:any){ return typeOpts.find(o=>o.value===r.type)?.label } },
  { title: '门槛', key: 'threshold', width: 90, render(r:any){ return r.threshold?'¥'+(r.threshold/100).toFixed(2):'-' } },
  { title: '优惠', key: 'discount', width: 90, render(r:any){ return r.type==='discount'?r.discount+'%':'¥'+(r.discount/100).toFixed(2) } },
  { title: '状态', key: 'status', width: 80, render(r:any){ return h(NTag,{size:'small',type:r.status==='1'?'success':r.status==='2'?'default':'warning'},{default:()=>statusLabel[r.status]}) } },
]

async function load() { const r = await fetchCampaigns(); items.value = r.data }
async function save() { await createCampaign(form.value); showModal.value=false; notify.success('创建成功'); load() }
onMounted(load)
</script>
<template>
  <n-card title="营销活动">
    <n-space vertical><n-button type="primary" @click="showModal=true">新建活动</n-button>
      <n-dataTable :columns="columns" :data="items" :pagination="{pageSize:10}" />
    </n-space>
    <n-modal v-model:show="showModal" title="新建活动" preset="card" style="max-width:400px">
      <n-form :model="form">
        <n-form-item label="名称"><n-input v-model:value="form.name" /></n-form-item>
        <n-form-item label="类型"><n-select v-model:value="form.type" :options="typeOpts" /></n-form-item>
        <n-form-item label="门槛(分)"><n-input-number v-model:value="form.threshold" :min="0" /></n-form-item>
        <n-form-item label="优惠(分/%)"><n-input-number v-model:value="form.discount" :min="1" /></n-form-item>
      </n-form>
      <template #footer><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></template>
    </n-modal>
  </n-card>
</template>