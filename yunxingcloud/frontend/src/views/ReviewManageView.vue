<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NSpace, NTag, NPopconfirm, NModal, NForm, NFormItem, NInput, NRate, NGrid, NGridItem, NStatistic, NProgress } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()
const loading = ref(false)
const items = ref<any[]>([])
const showDetail = ref(false)
const detail = ref<any>(null)
const searchKeyword = ref('')
const filterRating = ref('')
const reviewStats = ref<any>({ total: 0, avgRating: 0, distribution: {} })

const filtered = computed(() => {
  let list = items.value
  if (filterRating.value) list = list.filter((r: any) => r.rating === Number(filterRating.value))
  if (searchKeyword.value) { const kw = searchKeyword.value.toLowerCase(); list = list.filter((r: any) => r.content?.toLowerCase().includes(kw) || r.username?.toLowerCase().includes(kw)) }
  return list
})

const columns: DataTableColumns<any> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('product.name'), key: 'productName', width: 120, ellipsis: { tooltip: true } },
  { title: '用户', key: 'username', width: 80 },
  { title: '评分', key: 'rating', width: 80, render(r: any) { return h(NRate, { readonly: true, size: 'small', value: r.rating }) } },
  { title: '内容', key: 'content', width: 200, ellipsis: { tooltip: true } },
  { title: t('common.createdAt'), key: 'createdAt', width: 140, render(r: any) { return r.createdAt?.substring(0, 16) } },
  { title: t('common.actions'), key: 'actions', width: 120, render(r: any) { return h(NSpace, { size: 'small' }, { default: () => [h(NButton, { size: 'tiny', onClick: () => { detail.value = r; showDetail.value = true } }, { default: () => '查看' }), h(NPopconfirm, { onPositiveClick: () => delReview(r.id) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }), default: () => t('common.confirmDelete') }) ] }) } },
]

async function load() { loading.value = true; try { const r = await request.get('/products/reviews/all'); items.value = r.data.content || r.data || [] } finally { loading.value = false } }
async function loadStats() { try { const r = await request.get('/api/reviews/stats'); reviewStats.value = r.data || {} } catch {} }
async function delReview(id: number) { try { await request.delete(`/products/reviews/${id}`); notify.success(t('common.deleted')); load() } catch { notify.error(t('common.saveFailed')) } }
onMounted(() => { load(); loadStats() })
</script>
<template>
  <div style="padding:20px">
    <n-card title="评价管理">
      <n-grid v-if="reviewStats.total" cols="4" x-gap="12" style="margin-bottom:16px">
        <n-grid-item><n-card size="small"><n-statistic label="总评价数" :value="reviewStats.total || 0" /></n-card></n-grid-item>
        <n-grid-item><n-card size="small"><n-statistic label="平均评分" :value="((reviewStats.avgRating || 0)).toFixed(1)"><template #suffix>/5</template></n-statistic></n-card></n-grid-item>
        <n-grid-item><n-card size="small"><n-statistic label="好评率" :value="reviewStats.goodRate ? (reviewStats.goodRate*100).toFixed(0) : 0"><template #suffix>%</template></n-statistic></n-card></n-grid-item>
      </n-grid>
      <n-space vertical>
        <n-space>
          <n-input v-model:value="searchKeyword" placeholder="搜索评价..." clearable style="width:180px" size="small" />
          <n-select v-model:value="filterRating" :options="[{label:'全部评分',value:''},{label:'5星',value:'5'},{label:'4星',value:'4'},{label:'3星',value:'3'},{label:'2星',value:'2'},{label:'1星',value:'1'}]" style="width:110px" size="small" />
          <n-button size="small" @click="load" secondary>刷新</n-button>
        </n-space>
        <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r:any)=>r.id" :pagination="{pageSize:10}" size="small" />
      </n-space>
    </n-card>
    <n-modal v-model:show="showDetail" title="评价详情" preset="card" style="max-width:500px">
      <template v-if="detail">
        <p><b>商品:</b> {{ detail.productName }}</p>
        <p><b>用户:</b> {{ detail.username }}</p>
        <p><b>评分:</b> <n-rate :value="detail.rating" readonly size="small" /></p>
        <p><b>内容:</b> {{ detail.content }}</p>
        <p><b>时间:</b> {{ detail.createdAt?.substring(0, 16) }}</p>
      </template>
    </n-modal>
  </div>
</template>
