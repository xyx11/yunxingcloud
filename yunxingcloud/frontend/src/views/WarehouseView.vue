<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSpace, NPopconfirm } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchWarehouses, createWarehouse } from '@/api/inventory'
import { useNotify } from '@/composables/useNotify'
import request from '@/api/request'

const { t } = useI18n()
const notify = useNotify()
const items = ref<any[]>([])
const showModal = ref(false); const editingId = ref<number|null>(null)
const form = ref({ name: '', address: '' })

const columns = computed<DataTableColumns<any>>(() => [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('warehouse.name'), key: 'name', width: 150 },
  { title: t('warehouse.address'), key: 'address', width: 250 },
  { title: t('common.actions'), key:'act', width:120, render(r:any){ return h(NSpace,{size:'small'},{default:()=>[
    h(NButton,{size:'tiny',onClick:()=>{editingId.value=r.id;form.value={name:r.name,address:r.address||''};showModal.value=true}},{default:()=>t('common.edit')}),
    h(NPopconfirm,{onPositiveClick:()=>del(r.id)},{trigger:()=>h(NButton,{size:'tiny',type:'error'},{default:()=>t('common.delete')}),default:()=>t('common.confirmDelete')})
  ]})}}
])

async function load() { const r = await fetchWarehouses(); items.value = r.data }
async function save() {
  if (editingId.value) { await request.put(`/api/warehouses/${editingId.value}`, form.value) }
  else { await createWarehouse(form.value) }
  showModal.value = false; editingId.value = null; notify.success(t('common.saveSuccess')); load()
}
async function del(id:number) { try{await request.delete(`/api/warehouses/${id}`);notify.success(t('common.deleted'));load()}catch{notify.error(t('common.saveFailed'))} }
function add() { editingId.value=null; form.value={name:'',address:''}; showModal.value=true }
onMounted(load)
</script>
<template>
  <n-card :title="t('nav.warehouses')">
    <n-space vertical>
      <n-button type="primary" @click="add" class="align-start">{{ t('warehouse.add') }}</n-button>
      <n-dataTable :columns="columns" :data="items" :row-key="(r: any) => r.id" :pagination="false" />
    </n-space>
    <n-drawer v-model:show="showModal" :width="380" placement="right">
      <n-drawer-content :title="editingId?t('common.edit'):t('warehouse.add')" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" @click="save">{{ t('common.save') }}</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item :label="t('warehouse.name')"><n-input v-model:value="form.name" /></n-form-item>
          <n-form-item :label="t('warehouse.address')"><n-input v-model:value="form.address" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>

<style scoped>
.align-start { align-self: flex-start; }
</style>
