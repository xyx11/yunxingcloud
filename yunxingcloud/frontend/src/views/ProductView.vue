<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NInputNumber, NSpace, NTag, NPopconfirm, NSelect } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchProducts, createProduct, updateProduct, deleteProduct, type Product } from '@/api/product'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()
const loading = ref(false)
const items = ref<Product[]>([])
const showModal = ref(false)
const editingId = ref<number | null>(null)
const form = ref({ name: '', description: '', price: 1, stock: 0, imageUrl: '', status: '0' })
const searchKeyword = ref('')

const statusOpts = computed(() => [
  { label: t('product.statuses.0'), value: '0' },
  { label: t('product.statuses.1'), value: '1' },
])

const filtered = computed(() => {
  if (!searchKeyword.value) return items.value
  const kw = searchKeyword.value.toLowerCase()
  return items.value.filter(p => p.name.toLowerCase().includes(kw))
})

const columns = computed<DataTableColumns<Product>>(() => [
  { title: t('product.name'), key: 'name', width: 150 },
  { title: t('product.description'), key: 'description', width: 200, ellipsis: { tooltip: true } },
  { title: t('product.price'), key: 'price', width: 100, render(r: Product) { return `¥${(r.price / 100).toFixed(2)}` } },
  { title: t('product.stock'), key: 'stock', width: 80 },
  { title: t('product.status'), key: 'status', width: 70, render(r: Product) { return h(NTag, { size: 'small', type: r.status === '0' ? 'success' : 'default' }, { default: () => r.status === '0' ? t('product.statuses.0') : t('product.statuses.1') }) } },
  {
    title: t('common.actions'), key: 'act', width: 150,
    render(r: Product) {
      return h(NSpace, { size: 'small' }, { default: () => [
        h(NButton, { size: 'small', onClick: () => edit(r) }, { default: () => t('common.edit') }),
        h(NPopconfirm, { onPositiveClick: () => del(r.id) }, { trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => t('common.delete') }), default: () => t('common.confirmDelete') }),
      ]})
    },
  },
])

async function load() { loading.value = true; try { const r = await fetchProducts(); items.value = r.data } finally { loading.value = false } }
function add() { editingId.value = null; form.value = { name: '', description: '', price: 1, stock: 0, imageUrl: '', status: '0' }; showModal.value = true }
function edit(r: Product) { editingId.value = r.id; form.value = { name: r.name, description: r.description || '', price: r.price, stock: r.stock, imageUrl: r.imageUrl || '', status: r.status }; showModal.value = true }
async function save() { editingId.value ? await updateProduct(editingId.value, form.value) : await createProduct(form.value); showModal.value = false; notify.success(t('common.save')); load() }
async function del(id: number) { await deleteProduct(id); load() }
onMounted(load)
</script>
<template>
  <n-card :title="t('nav.products')">
    <n-space vertical>
      <n-space justify="space-between">
        <n-input v-model:value="searchKeyword" :placeholder="t('product.searchPlaceholder')" clearable style="width:200px" />
        <n-button type="primary" @click="add">{{ t('product.add') }}</n-button>
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r: Product) => r.id" :pagination="{ pageSize: 10 }" />
    </n-space>
    <n-modal v-model:show="showModal" :title="editingId ? t('product.edit') : t('product.add')" preset="card" style="max-width:500px">
      <n-form :model="form">
        <n-form-item :label="t('product.name')"><n-input v-model:value="form.name" /></n-form-item>
        <n-form-item :label="t('product.description')"><n-input v-model:value="form.description" type="textarea" /></n-form-item>
        <n-form-item :label="t('product.price') + '(¥)'"><n-input-number v-model:value="form.price" :min="0.01" :step="1" /></n-form-item>
        <n-form-item :label="t('product.stock')"><n-input-number v-model:value="form.stock" :min="0" /></n-form-item>
        <n-form-item :label="t('product.imageUrl')"><n-input v-model:value="form.imageUrl" /></n-form-item>
        <n-form-item :label="t('product.status')"><n-select v-model:value="form.status" :options="statusOpts" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" @click="save">{{ t('common.save') }}</n-button></n-space></template>
    </n-modal>
  </n-card>
</template>