<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderById, cancelOrder } from '@/api/order'
import { updateOrderStatus } from '@/api/order'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const toast = inject<any>('toast')
const order = ref<any>(null)
const lines = ref<any[]>([])
const shipment = ref<any>(null)
const reviewForm = ref({ productId: 0, rating: 5, content: '' })
const showReview = ref(false)
const loading = ref(true)

const statusMap: Record<string, { label: string; color: string; bg: string }> = {
  '0': { label: '待支付', color: '#856404', bg: '#fff3cd' },
  '1': { label: '已支付', color: '#155724', bg: '#d4edda' },
  '2': { label: '已发货', color: '#004085', bg: '#cce5ff' },
  '3': { label: '已完成', color: '#155724', bg: '#d4edda' },
  '4': { label: '已取消', color: '#383d41', bg: '#e2e3e5' },
}

const steps = ['0', '1', '2', '3']

onMounted(async () => {
  try {
    const r = await getOrderById(Number(route.params.id))
    order.value = r.data.order
    lines.value = r.data.lines || []
  } catch {} finally { loading.value = false }
  try { const r = await request.get(`/orders/${route.params.id}/shipment`); shipment.value = r.data } catch {}
})

const receiving = ref(false)
const reviewing = ref(false)

async function pay() { if (!order.value) return; router.push(`/pay/${order.value.id}`) }
async function cancel() {
  if (!confirm('确认取消订单？')) return
  try { await cancelOrder(order.value.id); order.value.status = '4'; toast.info('已取消') } catch {}
}
async function confirmReceive() {
  if (receiving.value) return; receiving.value = true
  try { await request.put(`/orders/${order.value.id}/status`, { status: '3' }); order.value.status = '3'; toast.success('已确认收货') } catch { toast.error('操作失败') }
  finally { receiving.value = false }
}
function openReview(productId: number) {
  reviewForm.value = { productId, rating: 5, content: '' }; showReview.value = true
}
async function submitReview() {
  if (reviewing.value) return; reviewing.value = true
  try { await request.post(`/products/${reviewForm.value.productId}/reviews`, reviewForm.value); toast.success('评价成功'); showReview.value = false } catch { toast.error('评价失败') }
  finally { reviewing.value = false }
}
</script>

<template>
  <div v-if="loading" style="max-width:800px;margin:0 auto">
    <div style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06);margin-bottom:16px">
      <div v-for="i in 4" :key="i" style="height:16px;margin-bottom:12px;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:4px" :style="{width:(100-i*15)+'%'}"></div>
    </div>
  </div>
  <div v-else-if="order" style="max-width:800px;margin:0 auto">
    <div style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06);margin-bottom:16px">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <div><span style="font-size:14px;color:#999">订单号：</span><span style="font-weight:600">{{ order.orderNo }}</span></div>
        <span style="padding:4px 12px;border-radius:20px;font-size:13px;font-weight:600"
              :style="{background:statusMap[order.status]?.bg,color:statusMap[order.status]?.color}">{{ statusMap[order.status]?.label }}</span>
      </div>
      <div style="display:flex;justify-content:space-between;position:relative;padding:20px 0">
        <div v-for="(s, i) in steps" :key="s" style="flex:1;display:flex;flex-direction:column;align-items:center;position:relative;z-index:1">
          <div style="width:24px;height:24px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:11px;margin-bottom:4px"
               :style="{background:parseInt(order.status)>=parseInt(s)?'#e4393c':'#ddd',color:'#fff'}">{{ parseInt(order.status) >= parseInt(s) ? '✓' : i+1 }}</div>
          <span style="font-size:11px" :style="{color:parseInt(order.status)>=parseInt(s)?'#e4393c':'#999'}">{{ ['下单','已支付','已发货','已完成'][i] }}</span>
        </div>
        <div style="position:absolute;top:32px;left:12%;right:12%;height:2px;background:#eee;z-index:0">
          <div style="height:100%;background:#e4393c;transition:width .6s" :style="{width:((parseInt(order.status)/(steps.length-1))*100)+'%'}"></div>
        </div>
      </div>
    </div>
    <div v-if="shipment" style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06);margin-bottom:16px">
      <h3 style="font-size:16px;font-weight:600;margin-bottom:12px">物流信息</h3>
      <div style="display:flex;gap:16px;color:#666;font-size:14px">
        <span>快递公司：{{ shipment.carrier || '-' }}</span><span>物流单号：{{ shipment.trackingNo || '-' }}</span>
      </div>
    </div>
    <div style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06);margin-bottom:16px">
      <h3 style="font-size:16px;font-weight:600;margin-bottom:12px">收货信息</h3>
      <div style="color:#666;font-size:14px"><p>{{ order.receiverName }} {{ order.receiverPhone }}</p><p>{{ order.receiverAddress }}</p></div>
    </div>
    <div style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06);margin-bottom:16px">
      <h3 style="font-size:16px;font-weight:600;margin-bottom:12px">商品清单</h3>
      <div v-for="l in lines" :key="l.id" style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #f5f5f5">
        <div style="width:60px;height:60px;background:#f5f5f5;border-radius:6px;display:flex;align-items:center;justify-content:center;font-size:24px;margin-right:16px;flex-shrink:0">📦</div>
        <div style="flex:1"><div style="font-weight:600">{{ l.productName }}</div><div style="color:#999;font-size:13px">¥{{ (l.price/100).toFixed(2) }} × {{ l.quantity }}</div></div>
        <div style="color:#e4393c;font-weight:700">¥{{ (l.price*l.quantity/100).toFixed(2) }}</div>
        <button v-if="order.status==='3'" @click="openReview(l.productId)" style="margin-left:12px;padding:4px 12px;border:1px solid #e4393c;color:#e4393c;background:#fff;border-radius:4px;cursor:pointer;font-size:12px">评价</button>
      </div>
      <div style="text-align:right;padding-top:16px">
        <span style="font-size:14px;color:#666">合计：</span><span style="font-size:22px;color:#e4393c;font-weight:700">¥{{ (order.totalAmount/100).toFixed(2) }}</span>
      </div>
    </div>
    <div style="display:flex;justify-content:flex-end;gap:12px">
      <button v-if="order.status==='0'" @click="cancel" style="padding:10px 24px;border:1px solid #ddd;background:#fff;border-radius:8px;cursor:pointer;font-size:14px">取消订单</button>
      <button v-if="order.status==='0'" @click="pay" style="padding:10px 32px;background:#e4393c;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:14px;font-weight:600">去支付</button>
      <button v-if="order.status==='2'" @click="confirmReceive" :disabled="receiving" style="padding:10px 32px;background:#e4393c;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:14px;font-weight:600" :style="{opacity:receiving?'.7':'1'}">{{ receiving ? '处理中...' : '确认收货' }}</button>
    </div>
    <div v-if="showReview" style="position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,.4);display:flex;align-items:center;justify-content:center;z-index:200">
      <div style="background:#fff;border-radius:12px;padding:24px;width:400px;max-width:90vw">
        <h3 style="font-size:18px;font-weight:600;margin-bottom:16px">商品评价</h3>
        <div style="margin-bottom:12px">
          <span style="font-size:13px;color:#666;margin-right:12px">评分</span>
          <span v-for="i in 5" :key="i" @click="reviewForm.rating=i" style="cursor:pointer;font-size:24px" :style="{color:i<=reviewForm.rating?'#f90':'#ddd'}">{{ i <= reviewForm.rating ? '★' : '☆' }}</span>
        </div>
        <textarea v-model="reviewForm.content" placeholder="分享你的使用体验..." style="width:100%;height:100px;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;resize:none;box-sizing:border-box;margin-bottom:16px"></textarea>
        <div style="display:flex;justify-content:flex-end;gap:8px">
          <button @click="showReview=false" style="padding:8px 20px;border:1px solid #ddd;background:#fff;border-radius:6px;cursor:pointer;font-size:13px">取消</button>
          <button @click="submitReview" style="padding:8px 20px;background:#e4393c;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:13px;font-weight:600">提交评价</button>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
</style>