<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NButton, NSpace, NTag, NDataTable } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import * as XLSX from 'xlsx'

const { t } = useI18n()
const notify = useNotify()
const exports = ref([
  { key: 'products', label: t('export.products'), desc: t('export.productsDesc'), icon: '📦' },
  { key: 'orders', label: t('export.orders'), desc: t('export.ordersDesc'), icon: '📋' },
  { key: 'users', label: t('export.users'), desc: t('export.usersDesc'), icon: '👥' },
  { key: 'inventory', label: t('export.inventory'), desc: t('export.inventoryDesc'), icon: '🏭' },
])
const loading = ref('')
const history = ref<{key:string, label:string, time:string, count:number}[]>([])

async function doExport(key: string) {
  loading.value = key
  try {
    let data: Record<string, unknown>[] = []
    if (key === 'products') { const r = await request.get('/api/products?size=200'); data = (r.data.content || r.data || []).map((p: Record<string, unknown>) => ({ 名称: p.name, 价格: ((p.price as number)/100).toFixed(2), 库存: p.stock, 状态: p.status==='0'?'上架':'下架', 销量: p.sales, 创建时间: (p.createdAt as string || '').substring(0,10) })) }
    else if (key === 'orders') { const r = await request.get('/api/orders'); data = (r.data || []).map((o: Record<string, unknown>) => ({ 订单号: o.orderNo, 用户: o.username, 金额: ((o.totalAmount as number)/100).toFixed(2), 状态: ['待支付','已支付','已发货','已完成','已取消'][Number(o.status)]||o.status, 创建时间: (o.createdAt as string || '').substring(0,10) })) }
    else if (key === 'users') { const r = await request.get('/api/users?size=500'); data = (r.data.content || r.data || []).map((u: Record<string, unknown>) => ({ 用户名: u.username, 昵称: u.nickname, 邮箱: u.email, 手机: u.phone, 状态: u.enabled?'启用':'停用', 创建时间: (u.createdAt as string || '').substring(0,10) })) }
    else if (key === 'inventory') { const r = await request.get('/api/inventory'); data = (r.data || []).map((i: Record<string, unknown>) => ({ 商品ID: i.productId, 商品名: i.productName, 仓库: i.warehouseId, 数量: i.quantity, 最低库存: i.minQuantity })) }
    const ws = XLSX.utils.json_to_sheet(data); const wb = XLSX.utils.book_new(); XLSX.utils.book_append_sheet(wb, ws, 'data')
    XLSX.writeFile(wb, `${key}_${new Date().toISOString().substring(0,10)}.xlsx`)
    history.value.unshift({ key, label: exports.value.find(e=>e.key===key)?.label||key, time: new Date().toLocaleString(), count: data.length })
    if (history.value.length > 10) history.value.pop()
    notify.success(t('export.exported', { n: data.length }))
  } catch { notify.error(t('export.fail')) }
  loading.value = ''
}
</script>
<template>
  <div class="view-pad">
    <n-card :title="t('export.title')">
      <n-space vertical size="large">
        <p class="export-desc">{{ t('export.desc') }}</p>
        <div v-for="e in exports" :key="e.key" class="export-card">
          <div>
            <div class="export-card-title">{{ e.icon }} {{ e.label }}</div>
            <div class="text-xs-muted">{{ e.desc }}</div>
          </div>
          <n-button type="primary" :loading="loading===e.key" @click="doExport(e.key)">{{ loading===e.key ? t('export.exporting') : '导出 Excel' }}</n-button>
        </div>
        <!-- Export history -->
        <div v-if="history.length" class="export-history">
          <div class="history-title">📋 最近导出</div>
          <div v-for="h in history" :key="h.key+h.time" class="history-row">
            <n-tag size="small" type="info">{{ h.label }}</n-tag>
            <span class="history-count">{{ h.count }} 条</span>
            <span class="history-time">{{ h.time }}</span>
          </div>
        </div>
      </n-space>
    </n-card>
  </div>
</template>

<style scoped>
.export-history { margin-top: 20px; padding-top: 16px; border-top: 1px solid var(--n-border-color, #eee); }
.history-title { font-size: 13px; font-weight: 600; color: var(--n-text-color-2, #666); margin-bottom: 8px; }
.history-row { display: flex; align-items: center; gap: 12px; padding: 4px 0; font-size: 12px; }
.history-count { color: var(--n-text-color-3, #999); }
.history-time { color: var(--n-text-color-3, #999); margin-left: auto; }
</style>
