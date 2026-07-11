<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatPrice } from '@/utils/format'
import { NCard, NDataTable, NButton, NModal, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NInputNumber, NSpace, NTag, NPopconfirm, NSelect, NUpload, NImage, NDropdown, NTabs, NTabPane, NDivider } from 'naive-ui'
import request from '@/api/request'
import type { DataTableColumns } from 'naive-ui'
import { fetchProducts, createProduct, updateProduct, deleteProduct, type Product } from '@/api/product'
import { fetchCategories } from '@/api/product'
import { useNotify } from '@/composables/useNotify'
import RichEditor from '@/components/RichEditor.vue'

const { t } = useI18n()
const notify = useNotify()
const loading = ref(false); const batchLoading = ref(false)
const items = ref<Product[]>([])
const categories = ref<Record<string, unknown>[]>([])
const brands = ref<Record<string, unknown>[]>([])
const categoryOpts = computed(() => categories.value.map((c) => ({ label: c.name as string, value: c.id as number })))
const brandOpts = computed(() => brands.value.map((b) => ({ label: b.name as string, value: b.id as number })))
const showModal = ref(false); const showBatchModal = ref(false)
const editingId = ref<number | null>(null)
const form = ref({ name: '', description: '', price: 1, stock: 0, imageUrl: '', images: [] as string[], status: '0', categoryId: null as number | null, brandId: null as number | null, metaTitle: '', metaDescription: '' })
const batchForm = ref({ price: null as number | null, status: '', categoryId: null as number | null })
const saving = ref(false); const uploading = ref(false)
const imagePreview = ref('')
const searchKeyword = ref('')
const checkedRowKeys = ref<number[]>([])
const filterCategoryId = ref<number | null>(null)
const filterBrandId = ref<number | null>(null)
const filterStatus = ref('')
const filterPriceMin = ref<number|null>(null)
const filterPriceMax = ref<number|null>(null)

async function handleUpload(options: { file: { file: File } }) {
  const fd = new FormData(); fd.append('file', options.file.file)
  uploading.value = true
  try { const r = await request.post('/api/files/upload', fd); imagePreview.value = r.data.url || ''; form.value.imageUrl = r.data.url || ''; options.onFinish() } catch { options.onError() } finally { uploading.value = false }
}

async function handleMultiUpload(options: { file: { file: File } }) {
  const fd = new FormData(); fd.append('file', options.file.file)
  try { const r = await request.post('/api/files/upload', fd); const url = r.data.url || ''; if (url) { form.value.images.push(url) }; options.onFinish() } catch { options.onError() }
}

function removeImage(idx: number) { form.value.images.splice(idx, 1) }

const statusOpts = computed(() => [
  { label: t('product.statuses.0'), value: '0' },
  { label: t('product.statuses.1'), value: '1' },
])

const filtered = computed(() => {
  let list = items.value
  if (searchKeyword.value) { const kw = searchKeyword.value.toLowerCase(); list = list.filter(p => p.name.toLowerCase().includes(kw)) }
  if (filterCategoryId.value) list = list.filter(p => p.categoryId === filterCategoryId.value)
  if (filterBrandId.value) list = list.filter(p => p.brandId === filterBrandId.value)
  if (filterStatus.value) list = list.filter(p => p.status === filterStatus.value)
  if (filterPriceMin.value !== null) list = list.filter(p => p.price >= filterPriceMin.value! * 100)
  if (filterPriceMax.value !== null) list = list.filter(p => p.price <= filterPriceMax.value! * 100)
  return list
})

const columns = computed<DataTableColumns<Product>>(() => [
  { type: 'selection' },
  { title: t('product.name'), key: 'name', width: 150 },
  {
    title: t('product.imageUrl'), key: 'imageUrl', width: 70,
    render(r: Product) { return r.imageUrl ? h(NImage, { src: r.imageUrl, width: 40, height: 40, style: 'border-radius:4px;object-fit:cover' }) : '-' }
  },
  { title: t('product.price'), key: 'price', width: 100, render(r: Product) { return `¥${(r.price / 100).toFixed(2)}` } },
  {
    title: t('product.stock'), key: 'stock', width: 90,
    render(r: Product) { return h(NSpace, { size: 4, align: 'center' }, { default: () => [h('span', { style: { color: r.stock <= 10 ? '#d03050' : r.stock <= 50 ? '#f0a020' : '#18a058', fontWeight: '600' } }, String(r.stock)), r.stock <= 10 ? h(NTag, { size: 'tiny', type: 'error', bordered: false }, { default: () => '低库存' }) : null] }) }
  },
  {
    title: '标签', key: 'tags', width: 100,
    render(r: Product) { return h(NSpace, { size: 4 }, { default: () => [r.isHot ? h(NTag, { size: 'tiny', type: 'error', bordered: false }, { default: () => '热门' }) : null, r.isNew ? h(NTag, { size: 'tiny', type: 'info', bordered: false }, { default: () => '新品' }) : null, !r.isHot && !r.isNew ? h('span', { style: 'color:#ccc' }, '-') : null] }) }
  },
  {
    title: '销量', key: 'sales', width: 70,
    render(r: Product) { const s = r.sales || 0; return h('span', { style: { fontWeight: '600', color: s > 1000 ? '#d03050' : '#333' } }, s > 9999 ? (s/10000).toFixed(1) + '万' : String(s)) }
  },
  { title: t('product.status'), key: 'status', width: 70, render(r: Product) { return h(NTag, { size: 'small', type: r.status === '0' ? 'success' : 'default' }, { default: () => r.status === '0' ? t('product.statuses.0') : t('product.statuses.1') }) } },
  {
    title: t('common.actions'), key: 'act', width: 180,
    render(r: Product) {
      return h(NSpace, { size: 'small' }, { default: () => [
        h(NButton, { size: 'small', onClick: () => edit(r) }, { default: () => t('common.edit') }),
        h(NButton, { size: 'small', type: r.status==='0'?'warning':'success', onClick: async () => { try { await updateProduct(r.id, { status: r.status==='0'?'1':'0' }); load() } catch { notify.error(t('common.saveFailed')) } } }, { default: () => r.status==='0'?'下架':'上架' }),
        h(NPopconfirm, { onPositiveClick: () => del(r.id) }, { trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => t('common.delete') }), default: () => t('common.confirmDelete') }),
      ]})
    },
  },
])

async function load() { loading.value = true; try { const r = await fetchProducts(); items.value = r.data.content || r.data || [] } finally { loading.value = false } }
function edit(r: Product) {
  editingId.value = r.id
  let images: string[] = []
  if (r.images) { try { images = typeof r.images === 'string' ? JSON.parse(r.images) : r.images } catch { images = [] } }
  form.value = { name: r.name, description: r.description || '', price: r.price / 100, stock: r.stock, imageUrl: r.imageUrl || '', images, status: r.status, categoryId: r.categoryId, brandId: r.brandId, metaTitle: r.metaTitle || '', metaDescription: r.metaDescription || '' }
  imagePreview.value = r.imageUrl || ''; showModal.value = true
}
function add() { editingId.value = null; form.value = { name: '', description: '', price: 0.01, stock: 0, imageUrl: '', images: [], status: '0', categoryId: null, brandId: null, metaTitle: '', metaDescription: '' }; imagePreview.value = ''; showModal.value = true }
async function save() {
  if (!form.value.name.trim()) { notify.error(t('validate.required')); return }
  saving.value = true
  const data: Record<string, unknown> = { ...form.value, price: Math.round(form.value.price * 100) }
  data.images = JSON.stringify(data.images)
  if (!data.categoryId) delete data.categoryId
  if (!data.brandId) delete data.brandId
  try { editingId.value ? await updateProduct(editingId.value, data) : await createProduct(data); showModal.value = false; notify.success(t('common.saveSuccess')); load() }
  catch (e: unknown) { const err = e as { response?: { data?: { message?: string } } }; notify.error(err.response?.data?.message || t('common.saveFailed')) }
  finally { saving.value = false }
}
async function del(id: number) { try { await deleteProduct(id); notify.success(t('common.deleted')); load() } catch { notify.error(t('common.saveFailed')) } }
function exportExcel() { const data = items.value.map((p: Product) => ({ 名称:p.name, 价格:formatPrice(p.price/100, 2), 库存:p.stock, 销量:p.sales||0, 状态:p.status==='0'?'上架':'下架', 创建时间:p.createdAt?.substring(0,10) })); import('xlsx').then(X => { const ws = X.utils.json_to_sheet(data); const wb = X.utils.book_new(); X.utils.book_append_sheet(wb, ws, '商品'); X.writeFile(wb, 'products.xlsx') }) }

// 批量操作
const batchOpts = [
  { label: '批量改价', key: 'price' },
  { label: '批量改状态', key: 'status' },
  { label: '批量改分类', key: 'category' },
]
function openBatch() {
  if (!checkedRowKeys.value.length) { notify.error('请先选择商品'); return }
  batchForm.value = { price: null, status: '', categoryId: null }; showBatchModal.value = true
}
async function doBatch() {
  if (!checkedRowKeys.value.length) return
  batchLoading.value = true
  try {
    for (const id of checkedRowKeys.value) {
      const data: Record<string, unknown> = {}
      if (batchForm.value.price !== null) data.price = Math.round(batchForm.value.price * 100)
      if (batchForm.value.status) data.status = batchForm.value.status
      if (batchForm.value.categoryId) data.categoryId = batchForm.value.categoryId
      if (Object.keys(data).length) await updateProduct(id, data)
    }
    notify.success(`已更新 ${checkedRowKeys.value.length} 件商品`)
    checkedRowKeys.value = []; showBatchModal.value = false; load()
  } catch { notify.error('批量操作失败') } finally { batchLoading.value = false }
}

onMounted(() => {
  load()
  fetchCategories().then(r => categories.value = r.data || []).catch(() => {})
  request.get('/api/brands').then(r => brands.value = r.data || []).catch(() => {})
})
</script>
<template>
  <n-card :title="t('nav.products')">
    <n-space vertical>
      <n-space justify="space-between">
        <n-space>
          <n-input v-model:value="searchKeyword" :placeholder="t('product.searchPlaceholder')" clearable class="w-160" size="small" />
          <n-select v-model:value="filterCategoryId" :options="categoryOpts" :placeholder="t('product.category')" clearable size="small" class="w-100" />
          <n-select v-model:value="filterBrandId" :options="brandOpts" :placeholder="t('product.brand')" clearable size="small" class="w-100" />
          <n-select v-model:value="filterStatus" :options="[{label:'全部状态',value:''},{label:t('product.statuses.0'),value:'0'},{label:t('product.statuses.1'),value:'1'}]" size="small" class="w-90" />
          <n-input-number v-model:value="filterPriceMin" :placeholder="t('order.minAmount')" :min="0" size="small" class="w-80" />
          <n-input-number v-model:value="filterPriceMax" placeholder="最高¥" :min="0" size="small" class="w-80" />
          <n-dropdown trigger="click" :options="batchOpts" @select="openBatch">
            <n-button :disabled="!checkedRowKeys.length" secondary size="small">批量 ({{ checkedRowKeys.length }})</n-button>
          </n-dropdown>
        </n-space>
        <n-space><n-button size="small" @click="exportExcel">导出Excel</n-button><n-button type="primary" @click="add" size="small">{{ t('product.add') }}</n-button></n-space>
      </n-space>
      <n-dataTable v-model:checked-row-keys="checkedRowKeys" :columns="columns" :data="filtered" :loading="loading" :row-key="(r: Product) => r.id" :pagination="{ pageSize: 10 }" />
    </n-space>
    <n-drawer v-model:show="showModal" :width="560" placement="right">
      <n-drawer-content :title="editingId ? t('product.edit') : t('product.add')" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="save">{{ t('common.save') }}</n-button></n-space></template>
        <n-tabs type="line" size="small" :default-value="'basic'">
          <n-tab-pane name="basic" tab="基本信息">
            <n-form :model="form" label-placement="left" label-width="80" size="small">
              <n-form-item :label="t('product.name')"><n-input v-model:value="form.name" /></n-form-item>
              <n-form-item :label="t('product.price') + '(¥)'"><n-input-number v-model:value="form.price" :min="0.01" :step="0.01" /></n-form-item>
              <n-form-item :label="t('product.stock')"><n-input-number v-model:value="form.stock" :min="0" /></n-form-item>
              <n-form-item :label="t('product.category')"><n-select v-model:value="form.categoryId" :options="categoryOpts" placeholder="选择分类" clearable /></n-form-item>
              <n-form-item :label="t('product.brand')"><n-select v-model:value="form.brandId" :options="brandOpts" placeholder="选择品牌" clearable /></n-form-item>
              <n-form-item :label="t('product.status')"><n-select v-model:value="form.status" :options="statusOpts" /></n-form-item>
              <n-divider />
              <n-form-item :label="t('product.description')">
                <RichEditor v-model="form.description" />
              </n-form-item>
            </n-form>
          </n-tab-pane>
          <n-tab-pane name="media" tab="图片媒体">
            <n-form :model="form" label-placement="left" label-width="80" size="small">
              <n-form-item :label="t('product.imageUrl')"><n-input v-model:value="form.imageUrl" placeholder="https://..." /></n-form-item>
              <n-form-item label="上传主图">
                <n-upload :custom-request="handleUpload" :show-file-list="false" accept="image/*">
                  <n-button :loading="uploading" size="small">选择图片</n-button>
                </n-upload>
                <n-image v-if="imagePreview || form.imageUrl" :src="imagePreview || form.imageUrl" width="120" height="120" class="preview-image" />
              </n-form-item>
              <n-divider />
              <n-form-item label="多图">
                <n-space>
                  <div v-for="(img, i) in form.images" :key="i" class="image-wrapper">
                    <n-image :src="img" width="80" height="80" class="thumb-image" />
                    <n-button size="tiny" circle type="error" class="remove-btn" @click="removeImage(i)">×</n-button>
                  </div>
                  <n-upload :custom-request="handleMultiUpload" :show-file-list="false" accept="image/*">
                    <n-button size="small" secondary>+ 添加</n-button>
                  </n-upload>
                </n-space>
              </n-form-item>
            </n-form>
          </n-tab-pane>
          <n-tab-pane name="seo" tab="SEO">
            <n-form :model="form" label-placement="left" label-width="80" size="small">
              <n-form-item label="SEO标题"><n-input v-model:value="form.metaTitle" placeholder="留空使用商品名称" maxlength="200" /></n-form-item>
              <n-form-item label="SEO描述"><n-input v-model:value="form.metaDescription" type="textarea" placeholder="留空使用商品描述前段" maxlength="500" :autosize="{minRows:3,maxRows:6}" /></n-form-item>
            </n-form>
          </n-tab-pane>
        </n-tabs>
      </n-drawer-content>
    </n-drawer>
    <n-modal v-model:show="showBatchModal" title="批量编辑" preset="card" class="max-w-400">
      <n-form :model="batchForm">
        <n-form-item label="新价格(¥)"><n-input-number v-model:value="batchForm.price" :min="0.01" :step="0.01" placeholder="留空不修改" /></n-form-item>
        <n-form-item label="新状态"><n-select v-model:value="batchForm.status" :options="[{label:'不修改',value:''},...statusOpts]" /></n-form-item>
        <n-form-item label="新分类"><n-select v-model:value="batchForm.categoryId" :options="categoryOpts" placeholder="不修改" clearable /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showBatchModal=false">取消</n-button><n-button type="primary" :loading="batchLoading" @click="doBatch">确认更新 ({{checkedRowKeys.length}}件)</n-button></n-space></template>
    </n-modal>
  </n-card>
</template>

<style scoped>
.preview-image { margin-top: 8px; border-radius: 8px; object-fit: cover; }
.image-wrapper { position: relative; display: inline-block; }
.thumb-image { border-radius: 6px; object-fit: cover; }
.remove-btn { position: absolute; top: -6px; right: -6px; }
.max-w-400 { max-width: 400px; }
</style>
