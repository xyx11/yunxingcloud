<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NInputNumber, NSpace, NSwitch, NPopconfirm } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<Record<string, unknown>[]>([])
const showModal = ref(false); const editingId = ref<number|null>(null)
const form = ref({ name:'', minPoints:0, discountRate:100, freeShipping:false, birthdayGift:false, prioritySupport:false, sortOrder:0 })

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: t('memberTier.name'), key: 'name', width: 80 },
  { title: t('memberTier.minPoints'), key: 'minPoints', width: 100 },
  { title: t('memberTier.discountRate'), key: 'discountRate', width: 80, render(r: Record<string, unknown>){ return (r.discountRate as number)+'%' } },
  { title: t('memberTier.freeShipping'), key: 'freeShipping', width: 60, render(r: Record<string, unknown>){ return Boolean(r.freeShipping)?'✓':'' } },
  { title: t('memberTier.birthdayGift'), key: 'birthdayGift', width: 60, render(r: Record<string, unknown>){ return Boolean(r.birthdayGift)?'✓':'' } },
  { title: t('common.actions'), key:'act', width:120, render(r: Record<string, unknown>){ return h(NSpace,{size:'small'},{default:()=>[
    h(NButton,{size:'tiny',onClick:()=>{editingId.value=r.id as number;form.value={...r as Record<string, unknown>};showModal.value=true}},{default:()=>t('common.edit')}),
    h(NPopconfirm,{onPositiveClick:()=>del(r.id as number)},{trigger:()=>h(NButton,{size:'tiny',type:'error'},{default:()=>t('common.delete')}),default:()=>t('common.confirmDelete')})
  ]})}}
]

async function load() { const r = await request.get('/member/tiers'); items.value = r.data }
async function save() {
  if (editingId.value) { await request.put(`/member/tiers/${editingId.value}`, form.value) }
  else { await request.post('/member/tiers', form.value) }
  showModal.value=false; editingId.value=null; notify.success(t('common.saveSuccess')); load()
}
async function del(id:number) { try{await request.delete(`/member/tiers/${id}`);notify.success(t('common.deleted'));load()}catch{notify.error(t('common.deleteFailed'))} }
function add() { editingId.value=null; form.value={name:'',minPoints:0,discountRate:100,freeShipping:false,birthdayGift:false,prioritySupport:false,sortOrder:0}; showModal.value=true }
onMounted(load)
</script>
<template>
  <n-card :title="t('memberTier.title')">
    <n-space vertical><n-button type="primary" @click="add">{{ t('memberTier.add') }}</n-button>
      <n-dataTable :columns="columns" :data="items" :pagination="false" />
    </n-space>
    <n-drawer v-model:show="showModal" :width="400" placement="right">
      <n-drawer-content :title="editingId ? t('memberTier.editTitle') : t('memberTier.newTitle')" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">{{ t('memberTier.cancel') }}</n-button><n-button type="primary" @click="save">{{ t('memberTier.save') }}</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item :label="t('memberTier.name')"><n-input v-model:value="form.name" /></n-form-item>
          <n-form-item :label="t('memberTier.minPoints')"><n-input-number v-model:value="form.minPoints" :min="0" /></n-form-item>
          <n-form-item :label="t('memberTier.discountRate')"><n-input-number v-model:value="form.discountRate" :min="1" :max="100" /></n-form-item>
          <n-form-item :label="t('memberTier.freeShipping')"><n-switch v-model:value="form.freeShipping" /></n-form-item>
          <n-form-item :label="t('memberTier.birthdayGift')"><n-switch v-model:value="form.birthdayGift" /></n-form-item>
          <n-form-item :label="t('memberTier.prioritySupport')"><n-switch v-model:value="form.prioritySupport" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>
