<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NModal, NForm, NFormItem, NInput, NInputNumber, NSpace, NTag, NPopconfirm } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const { t } = useI18n()
const loading = ref(false); const saving = ref(false)
const items = ref<any[]>([])
const showModal = ref(false)
const form = ref({ name: '', productIds: [] as number[], discountRate: 100, status: '0' })
const searchKeyword = ref('')
const productInput = ref('')

const filtered = computed(() => {
  if (!searchKeyword.value) return items.value
  return items.value.filter(b => b.name?.toLowerCase().includes(searchKeyword.value.toLowerCase()))
})

const columns: DataTableColumns<any> = [
  { title: t('common.name'), key: 'name', width: 180 },
  { title: '商品数', key: 'cnt', width: 70, render(r: any) { return r.productIds?.length || 0 } },
  { title: '折扣率', key: 'discountRate', width: 80, render(r: any) { return (r.discountRate || 100) + '%' } },
  { title: '状态', key: 'status', width: 70, render(r: any) { return h(NTag, { size: 'small', type: r.status === '0' ? 'success' : 'default' }, { default: () => r.status === '0' ? '启用' : '停用' }) } },
  { title: t('common.actions'), key: 'act', width: 100, render(r: any) { return h(NSpace, { size: 'small' }, { default: () => [
    h(NButton, { size: 'tiny', onClick: () => viewDetail(r) }, { default: () => '详情' }),
    h(NPopconfirm, { onPositiveClick: () => del(r.id!) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }), default: () => t('common.confirmDelete') })
  ]})}}
]

const showDetail = ref(false); const detail = ref<any>(null)
function viewDetail(r: any) { detail.value = r; showDetail.value = true }

async function load() { loading.value = true; try { const r = await request.get('/api/bundles'); items.value = r.data || [] } finally { loading.value = false } }
async function save() {
  saving.value = true
  const ids = productInput.value.split(',').map(s => Number(s.trim())).filter(n => n > 0)
  try {
    await request.post('/api/bundles', { ...form.value, productIds: ids })
    showModal.value = false; notify.success(t('common.saveSuccess')); load()
  } catch { notify.error(t('common.saveFailed')) } finally { saving.value = false }
}
async function del(id: number) { try { await request.delete(`/api/bundles/${id}`); notify.success(t('common.deleted')); load() } catch { notify.error(t('common.deleteFailed')) } }
function add() { form.value = { name: '', productIds: [], discountRate: 100, status: '0' }; productInput.value = ''; showModal.value = true }
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card title="捆绑套餐"><template #header-extra><n-button type="primary" size="small" @click="add">+ 新增套餐</n-button></template>
      <n-space class="mb-12">
        <n-input v-model:value="searchKeyword" placeholder="搜索套餐..." size="small" clearable style="width:180px" />
        <n-button size="small" @click="load" secondary>刷新</n-button>
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r:any)=>r.id" :pagination="{pageSize:10}" size="small" />
    </n-card>
    <n-drawer v-model:show="showModal" :width="380" placement="right">
      <n-drawer-content title="新增套餐" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" :loading="saving" @click="save">保存</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="100" size="small">
          <n-form-item label="套餐名称" required><n-input v-model:value="form.name" placeholder="如: 手机配件套装" /></n-form-item>
          <n-form-item label="商品ID"><n-input v-model:value="productInput" placeholder="1,2,3" /></n-form-item>
          <n-form-item label="折扣率(%)"><n-input-number v-model:value="form.discountRate" :min="1" :max="100" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
    <n-modal v-model:show="showDetail" title="套餐详情" preset="card" style="max-width:450px">
      <template v-if="detail">
        <p><b>名称:</b> {{ detail.name }}</p>
        <p><b>折扣率:</b> {{ detail.discountRate || 100 }}%</p>
        <p><b>商品ID:</b> {{ detail.productIds?.join(', ') }}</p>
        <p><b>状态:</b> <n-tag size="small" :type="detail.status==='0'?'success':'default'">{{ detail.status==='0'?'启用':'停用' }}</n-tag></p>
      </template>
    </n-modal>
  </div>
</template>
