<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NInputNumber, NSelect, NSpace, NPopconfirm, NImage, NUpload } from 'naive-ui'
import type { DataTableColumns, UploadCustomRequestOptions } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()
const loading = ref(false); const saving = ref(false)
const items = ref<Record<string, unknown>[]>([])
const showModal = ref(false); const editingId = ref<number|null>(null)
const form = ref({ title:'', imageUrl:'', linkUrl:'', sortOrder:0, status:'0' })

function handleUpload(options: UploadCustomRequestOptions) {
  const fd = new FormData(); fd.append('file', options.file.file!)
  request.post('/api/files/upload', fd).then(r => {
    form.value.imageUrl = r.data?.url || r.data || ''
    options.onFinish(); notify.success(t('common.uploadSuccess'))
  }).catch(() => { options.onError(); notify.error(t('common.uploadFailed')) })
}

const columns: DataTableColumns<Record<string, unknown>> = [
  { title:'ID', key:'id', width:50 },
  { title:t('common.title'), key:'title', width:150 },
  { title:t('common.image'), key:'imageUrl', width:100, render(r: Record<string, unknown>) { return h(NImage, {src:r.imageUrl as string,width:80,height:40,style:{objectFit:'cover',borderRadius:'4px'}}) } },
  { title:t('common.url'), key:'linkUrl', width:150, ellipsis:{tooltip:true} },
  { title:t('common.sort'), key:'sortOrder', width:60 },
  { title:t('common.status'), key:'status', width:70, render(r: Record<string, unknown>) { return r.status==='0' ? t('common.enabled') : t('common.disabled') } },
  { title:t('common.actions'), key:'act', width:120,
    render(r: Record<string, unknown>) { return h(NSpace,{size:'small'},{default:()=>[
      h(NButton,{size:'tiny',onClick:()=>{editingId.value=r.id as number;form.value={title:r.title as string,imageUrl:(r.imageUrl as string)||'',linkUrl:(r.linkUrl as string)||'',sortOrder:(r.sortOrder as number)||0,status:r.status as string};showModal.value=true}},{default:()=>t('common.edit')}),
      h(NPopconfirm,{onPositiveClick:()=>del(r.id as number)},{trigger:()=>h(NButton,{size:'tiny',type:'error'},{default:()=>t('common.delete')}),default:()=>t('common.confirmDelete')})
    ]})}
  }
]

async function load() { loading.value=true; try{const r=await request.get('/banners');items.value=r.data||[]} finally{loading.value=false} }
async function save() { saving.value=true; try{editingId.value?await request.put(`/banners/${editingId.value}`,form.value):await request.post('/banners',form.value);showModal.value=false;editingId.value=null;notify.success(t('common.save'));load()}catch{notify.error(t('common.saveFailed'))}finally{saving.value=false} }
async function del(id:number) { try{await request.delete(`/banners/${id}`);notify.success(t('common.deleted'));load()}catch{notify.error(t('common.saveFailed'))} }
function add() { editingId.value=null; form.value={title:'',imageUrl:'',linkUrl:'',sortOrder:0,status:'0'}; showModal.value=true }
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card :title="t('nav.banners')">
      <template #header-extra><n-button type="primary" size="small" @click="add">{{ t('common.add') }}</n-button></template>
      <n-dataTable :columns="columns" :data="items" :loading="loading" :row-key="(r: Record<string, unknown>)=>r.id" :pagination="{pageSize:10}" size="small" />
    </n-card>
    <n-drawer v-model:show="showModal" :width="450" placement="right">
      <n-drawer-content :title="editingId ? t('common.edit') : t('common.add')" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="save">{{ t('common.save') }}</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item :label="t('common.title')"><n-input v-model:value="form.title" /></n-form-item>
          <n-form-item :label="t('common.url')"><n-input v-model:value="form.linkUrl" placeholder="/products?categoryId=1" /></n-form-item>
          <n-form-item :label="t('common.sort')"><n-input-number v-model:value="form.sortOrder" :min="0" /></n-form-item>
          <n-form-item :label="t('common.status')"><n-select v-model:value="form.status" :options="[{label:t('common.enabled'),value:'0'},{label:t('common.disabled'),value:'1'}]" /></n-form-item>
          <n-form-item :label="t('common.image')">
            <n-space vertical>
              <n-input v-model:value="form.imageUrl" placeholder="https://..." />
              <n-upload :custom-request="handleUpload" :show-file-list="false" accept="image/*"><n-button size="small" secondary>{{ t('common.upload') }}</n-button></n-upload>
              <n-image v-if="form.imageUrl" :src="form.imageUrl" width="200" height="80" class="banner-preview" />
            </n-space>
          </n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<style scoped>
.banner-preview { object-fit: cover; border-radius: 8px; }
</style>
