<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NInputNumber, NSelect, NSpace, NTag, NPopconfirm, NDatePicker } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { formatPrice } from '@/utils/format'
import { fetchCoupons, createCoupon, updateCoupon, deleteCoupon, type Coupon } from '@/api/coupon'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const { t } = useI18n()
const loading = ref(false); const saving = ref(false)
const items = ref<Coupon[]>([])
const showModal = ref(false); const editingId = ref<number | null>(null)
const form = ref<Coupon>({ name: '', type: 'full_reduction', threshold: 0, amount: 0, totalQty: 100, status: '0' })
const searchKeyword = ref('')

const typeOpts = [
  { label: '满减券', value: 'full_reduction' },
  { label: '折扣券', value: 'discount' },
  { label: '现金券', value: 'cash' },
]

const filtered = computed(() => {
  if (!searchKeyword.value) return items.value
  const kw = searchKeyword.value.toLowerCase()
  return items.value.filter(c => c.name?.toLowerCase().includes(kw))
})

const columns: DataTableColumns<Coupon> = [
  { title: t('common.name'), key: 'name', width: 140 },
  {
    title: '类型', key: 'type', width: 80,
    render(r: any) { const t = typeOpts.find(o => o.value === r.type); return t?.label || r.type }
  },
  { title: t('coupon.threshold'), key: 'threshold', width: 100, render(r: any) { return r.threshold ? '¥' + (r.threshold / 100).toFixed(2) : '无门槛' } },
  {
    title: '优惠', key: 'amount', width: 90,
    render(r: any) { return r.type === 'discount' ? r.amount + '%' : '¥' + (r.amount / 100).toFixed(2) }
  },
  { title: '已领/总量', key: 'qty', width: 80, render(r: any) { return `${r.usedQty || 0}/${r.totalQty || 0}` } },
  {
    title: '状态', key: 'status', width: 70,
    render(r: any) { return h(NTag, { size: 'small', type: r.status === '0' ? 'success' : 'default' }, { default: () => r.status === '0' ? '启用' : '停用' }) }
  },
  {
    title: t('common.actions'), key: 'act', width: 120,
    render(r: any) {
      return h(NSpace, { size: 'small' }, { default: () => [
          h(NButton, { size: 'tiny', onClick: () => edit(r) }, { default: () => t('common.edit') }),
          h(NPopconfirm, { onPositiveClick: () => del(r.id!) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }), default: () => t('common.confirmDelete') }),
      ]})
    },
  },
]

function edit(r: any) { editingId.value = r.id; form.value = { ...r }; showModal.value = true }
function add() { editingId.value = null; form.value = { name: '', type: 'full_reduction', threshold: 0, amount: 0, totalQty: 100, status: '0' }; showModal.value = true }

async function load() { loading.value = true; try { const r = await fetchCoupons(); items.value = r.data || [] } finally { loading.value = false } }
async function save() {
  saving.value = true
  try {
    if (editingId.value) { await updateCoupon(editingId.value, form.value) }
    else { await createCoupon(form.value) }
    showModal.value = false; editingId.value = null; notify.success(t('common.saveSuccess')); load()
  } catch { notify.error(t('common.saveFailed')) } finally { saving.value = false }
}
async function del(id: number) { try { await deleteCoupon(id); notify.success(t('common.deleted')); load() } catch { notify.error(t('common.deleteFailed')) } }
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card title="优惠券管理"><template #header-extra><n-button type="primary" size="small" @click="add">+ 新增优惠券</n-button></template>
      <n-space class="mb-12">
        <n-input v-model:value="searchKeyword" placeholder="搜索优惠券..." size="small" clearable class="w-180" />
        <n-button size="small" @click="load" secondary>刷新</n-button>
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r:any)=>r.id" :pagination="{pageSize:10}" size="small" />
    </n-card>
    <n-drawer v-model:show="showModal" :width="420" placement="right">
      <n-drawer-content :title="editingId ? '编辑优惠券' : '新增优惠券'" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" :loading="saving" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="90" size="small">
          <n-form-item :label="t('common.name')" required><n-input v-model:value="form.name" placeholder="如: 618满199减50" /></n-form-item>
          <n-form-item :label="t('common.type')"><n-select v-model:value="form.type" :options="typeOpts" /></n-form-item>
          <n-form-item label="使用门槛(元)"><n-input-number v-model:value="form.threshold" :min="0" :step="10" placeholder="0=无门槛" /></n-form-item>
          <n-form-item label="优惠(元/%)"><n-input-number v-model:value="form.amount" :min="1" /></n-form-item>
          <n-form-item label="发放总量"><n-input-number v-model:value="form.totalQty" :min="1" /></n-form-item>
          <n-form-item :label="t('order.status')"><n-select v-model:value="form.status" :options="[{label:'启用',value:'0'},{label:'停用',value:'1'}]" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<style scoped>
.w-180 { width: 180px; }
</style>
