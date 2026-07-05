<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NModal, NSpace, NTag, NInput, NSelect } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'

const loading = ref(false)
const items = ref<any[]>([])
const showDetail = ref(false); const detail = ref<any>(null)
const searchKeyword = ref(''); const filterStatus = ref('')
const filterType = ref('')

const statusOpts = [
  { label: '全部', value: '' }, { label: '待开票', value: '0' },
  { label: '已开票', value: '1' }, { label: '已作废', value: '2' },
]
const typeOpts = [
  { label: '全部', value: '' }, { label: '个人', value: 'personal' }, { label: '企业', value: 'company' },
]

const filtered = computed(() => {
  let list = items.value
  if (filterStatus.value) list = list.filter(i => i.status === filterStatus.value)
  if (filterType.value) list = list.filter(i => i.type === filterType.value)
  if (searchKeyword.value) { const kw = searchKeyword.value.toLowerCase(); list = list.filter(i => (i.invoiceNo || '').includes(kw) || (i.orderNo || '').includes(kw) || (i.username || '').toLowerCase().includes(kw)) }
  return list
})

const columns: DataTableColumns<any> = [
  { title: t('invoice.invoiceNo'), key: 'invoiceNo', width: 180 },
  { title: t('order.orderNo'), key: 'orderNo', width: 180 },
  { title: '用户', key: 'username', width: 100 },
  { title: t('invoice.amount'), key: 'amount', width: 100, render(r: any) { return '¥' + ((r.amount || 0) / 100).toFixed(2) } },
  { title: t('invoice.type'), key: 'type', width: 70, render(r: any) { return r.type === 'company' ? '企业' : '个人' } },
  { title: t('invoice.companyTitle'), key: 'title', width: 160, ellipsis: { tooltip: true } },
  { title: t('order.status'), key: 'status', width: 80, render(r: any) { return h(NTag, { size: 'small', type: r.status === '1' ? 'success' : r.status === '2' ? 'error' : 'warning' }, { default: () => r.status === '0' ? '待开票' : r.status === '1' ? '已开票' : '已作废' }) } },
  { title: t('common.actions'), key: 'act', width: 100, render(r: any) { return h(NButton, { size: 'tiny', onClick: () => { detail.value = r; showDetail.value = true } }, { default: () => '详情' }) } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/invoices'); items.value = r.data || [] } finally { loading.value = false } }
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card :title="t('invoice.title')"><template #header-extra><n-button size="small" @click="load" secondary>刷新</n-button></template>
      <n-space class="mb-12">
        <n-input v-model:value="searchKeyword" placeholder="搜索发票号/订单号/用户..." size="small" clearable style="width:220px" />
        <n-select v-model:value="filterStatus" :options="statusOpts" size="small" style="width:90px" />
        <n-select v-model:value="filterType" :options="typeOpts" size="small" style="width:80px" />
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r:any)=>r.id" :pagination="{pageSize:10}" size="small" />
    </n-card>
    <n-modal v-model:show="showDetail" title="发票详情" preset="card" style="max-width:500px">
      <template v-if="detail">
        <p><b>发票号:</b> {{ detail.invoiceNo }}</p>
        <p><b>订单号:</b> {{ detail.orderNo }}</p>
        <p><b>用户:</b> {{ detail.username }}</p>
        <p><b>金额:</b> ¥{{ ((detail.amount || 0) / 100).toFixed(2) }}</p>
        <p><b>类型:</b> {{ detail.type === 'company' ? '企业' : '个人' }}</p>
        <p><b>抬头:</b> {{ detail.title }}</p>
        <p v-if="detail.taxNo"><b>税号:</b> {{ detail.taxNo }}</p>
        <p><b>状态:</b> <n-tag size="small" :type="detail.status==='1'?'success':detail.status==='2'?'error':'warning'">{{ detail.status==='0'?'待开票':detail.status==='1'?'已开票':'已作废' }}</n-tag></p>
        <p><b>创建时间:</b> {{ detail.createdAt?.substring(0, 16) }}</p>
      </template>
    </n-modal>
  </div>
</template>
