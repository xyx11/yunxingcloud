<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NButton, NSpace, NTag } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import * as XLSX from 'xlsx'

const notify = useNotify()
const exports = ref([
  { key: 'products', label: '商品数据', desc: '导出所有商品信息，包含价格/库存/分类' },
  { key: 'orders', label: '订单数据', desc: '导出订单列表，包含金额/状态/用户' },
  { key: 'users', label: '用户数据', desc: '导出所有用户信息' },
  { key: 'inventory', label: '库存数据', desc: '导出库存列表' },
])
const loading = ref('')

async function doExport(key: string) {
  loading.value = key
  try {
    let data: any[] = []
    if (key === 'products') { const r = await request.get('/api/products?size=200'); data = (r.data.content || r.data || []).map((p:any) => ({ 名称: p.name, 价格: (p.price/100).toFixed(2), 库存: p.stock, 状态: p.status==='0'?'上架':'下架', 销量: p.sales, 创建时间: p.createdAt?.substring(0,10) })) }
    else if (key === 'orders') { const r = await request.get('/api/orders'); data = (r.data || []).map((o:any) => ({ 订单号: o.orderNo, 用户: o.username, 金额: (o.totalAmount/100).toFixed(2), 状态: ['待支付','已支付','已发货','已完成','已取消'][Number(o.status)]||o.status, 创建时间: o.createdAt?.substring(0,10) })) }
    else if (key === 'users') { const r = await request.get('/api/users?size=500'); data = (r.data.content || r.data || []).map((u:any) => ({ 用户名: u.username, 昵称: u.nickname, 邮箱: u.email, 手机: u.phone, 状态: u.enabled?'启用':'停用', 创建时间: u.createdAt?.substring(0,10) })) }
    else if (key === 'inventory') { const r = await request.get('/api/inventory'); data = (r.data || []).map((i:any) => ({ 商品ID: i.productId, 商品名: i.productName, 仓库: i.warehouseId, 数量: i.quantity, 最低库存: i.minQuantity })) }
    const ws = XLSX.utils.json_to_sheet(data); const wb = XLSX.utils.book_new(); XLSX.utils.book_append_sheet(wb, ws, 'data')
    XLSX.writeFile(wb, `${key}_${new Date().toISOString().substring(0,10)}.xlsx`)
    notify.success(`已导出 ${data.length} 条`)
  } catch { notify.error('导出失败') }
  loading.value = ''
}
</script>
<template>
  <div style="padding:20px">
    <n-card title="数据导出中心">
      <n-space vertical size="large">
        <p style="color:#999;margin:0">选择要导出的数据类型，数据将导出为 Excel (.xlsx) 格式</p>
        <div v-for="e in exports" :key="e.key" style="display:flex;align-items:center;justify-content:space-between;padding:16px;background:#f9f9f9;border-radius:8px">
          <div>
            <div style="font-weight:600;margin-bottom:4px">{{ e.label }}</div>
            <div style="font-size:12px;color:#999">{{ e.desc }}</div>
          </div>
          <n-button type="primary" :loading="loading===e.key" @click="doExport(e.key)">{{ loading===e.key?'导出中...':'导出 Excel' }}</n-button>
        </div>
      </n-space>
    </n-card>
  </div>
</template>
