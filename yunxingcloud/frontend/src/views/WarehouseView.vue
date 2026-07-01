<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSpace } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchWarehouses, createWarehouse } from '@/api/inventory'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()
const items = ref<any[]>([])
const showModal = ref(false)
const form = ref({ name: '', address: '' })

const columns = computed<DataTableColumns<any>>(() => [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('warehouse.name'), key: 'name', width: 150 },
  { title: t('warehouse.address'), key: 'address', width: 250 },
])

async function load() { const r = await fetchWarehouses(); items.value = r.data }
async function save() { await createWarehouse(form.value); showModal.value = false; notify.success(t('common.createSuccess')); load() }
onMounted(load)
</script>
<template>
  <n-card :title="t('nav.warehouses')">
    <n-space vertical>
      <n-button type="primary" @click="showModal = true" style="align-self:flex-start">{{ t('warehouse.add') }}</n-button>
      <n-dataTable :columns="columns" :data="items" :row-key="(r: any) => r.id" :pagination="false" />
    </n-space>
    <n-modal v-model:show="showModal" :title="t('warehouse.addTitle')" preset="card" style="max-width:400px">
      <n-form :model="form">
        <n-form-item :label="t('warehouse.name')"><n-input v-model:value="form.name" /></n-form-item>
        <n-form-item :label="t('warehouse.address')"><n-input v-model:value="form.address" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" @click="save">{{ t('common.save') }}</n-button></n-space></template>
    </n-modal>
  </n-card>
</template>