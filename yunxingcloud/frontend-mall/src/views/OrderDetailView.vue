<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderById, cancelOrder } from '@/api/order'
import { ToastInjectionKey } from '@/composables/useToast'
import request from '@/api/request'
import { useI18n } from '@/locales'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import JdBadge from '@/components/JdBadge.vue'
import type { OrderHead } from '@/types'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const toast = inject(ToastInjectionKey)!
const order = ref<OrderHead | null>(null)
const lines = ref<any[]>([])
const shipment = ref<{carrier:string,trackingNo:string} | null>(null)
const reviewForm = ref({ productId: 0, rating: 5, content: '' })
const showReview = ref(false)
const loading = ref(true)
const receiving = ref(false)
const reviewing = ref(false)

const statusMap: Record<string, { label: string; bg: string }> = {
  '0': { label: t('orderDetail.statusPending'), bg: '#fff3cd' },
  '1': { label: t('orderDetail.statusPaid'), bg: '#d4edda' },
  '2': { label: t('orderDetail.statusShipped'), bg: '#cce5ff' },
  '3': { label: t('orderDetail.statusDone'), bg: '#d4edda' },
  '4': { label: t('orderDetail.statusCanceled'), bg: '#e2e3e5' },
}

const stepLabels = ['下单', t('orderDetail.statusPaid'), t('orderDetail.statusShipped'), t('orderDetail.statusDone')]

onMounted(async () => {
  try { const r = await getOrderById(Number(route.params.id)); order.value = r.data.order; lines.value = r.data.lines || [] } catch {} finally { loading.value = false }
  try { const r = await request.get(`/orders/${route.params.id}/shipment`); shipment.value = r.data } catch {}
})

async function pay() { if (!order.value) return; router.push(`/pay/${order.value.id}`) }
async function cancel() { if (!confirm('确认取消订单？')) return; try { await cancelOrder(order.value.id); order.value.status = '4'; toast.info(t('orderDetail.statusCanceled')) } catch {} }

async function confirmReceive() {
  if (receiving.value) return; receiving.value = true
  try { await request.put(`/orders/${order.value.id}/status`, { status: '3' }); order.value.status = '3'; toast.success('已确认收货') } catch { toast.error('操作失败') }
  finally { receiving.value = false }
}

function openReview(productId: number) { reviewForm.value = { productId, rating: 5, content: '' }; showReview.value = true }

async function submitReview() {
  if (reviewing.value) return; reviewing.value = true
  try { await request.post(`/products/${reviewForm.value.productId}/reviews`, reviewForm.value); toast.success('评价成功'); showReview.value = false } catch { toast.error('评价失败') }
  finally { reviewing.value = false }
}
</script>

<template>
  <div v-if="loading" class="order-page">
    <div class="card">
      <div v-for="i in 4" :key="i" class="sk-line" :style="{ width: (100 - i * 15) + '%' }" />
    </div>
  </div>

  <div v-else-if="order" class="order-page">
    <!-- Status -->
    <div class="card">
      <div class="order-header">
        <div><span class="order-no-label">{{ t('orderDetail.orderNo') }}：</span><span class="order-no">{{ order.orderNo }}</span></div>
        <JdBadge :type="order.status === '3' || order.status === '1' ? 'green' : order.status === '4' ? 'gray' : 'orange'">
          {{ statusMap[order.status]?.label }}
        </JdBadge>
      </div>

      <!-- Steps -->
      <div class="steps">
        <div v-for="(s, i) in ['0', '1', '2', '3']" :key="s" class="step" :class="{ done: parseInt(order.status) >= parseInt(s) }">
          <div class="step-dot" :class="{ active: parseInt(order.status) >= parseInt(s) }">
            {{ parseInt(order.status) >= parseInt(s) ? '✓' : i + 1 }}
          </div>
          <span class="step-label" :class="{ active: parseInt(order.status) >= parseInt(s) }">{{ stepLabels[i] }}</span>
        </div>
        <div class="step-line">
          <div class="step-line-fill" :style="{ width: ((parseInt(order.status) / 3) * 100) + '%' }" />
        </div>
      </div>
    </div>

    <!-- Logistics -->
    <div v-if="shipment" class="card">
      <h3 class="card-title">{{ t('orderDetail.title') }}</h3>
      <div class="shipment-info">
        <span>{{ t('orderDetail.carrier') }}：{{ shipment.carrier || '-' }}</span>
        <span>{{ t('orderDetail.trackingNo') }}：{{ shipment.trackingNo || '-' }}</span>
        <span v-if="order.status==='2'" class="eta">📦 预计 {{ new Date(Date.now()+3*86400000).toLocaleDateString('zh-CN',{month:'long',day:'numeric'}) }} 送达</span>
      </div>
    </div>

    <!-- Receiver -->
    <div class="card">
      <h3 class="card-title">{{ t('orderDetail.receiver') }}</h3>
      <div class="receiver-info">
        <p>{{ order.receiverName }} {{ order.receiverPhone }}</p>
        <p>{{ order.receiverAddress }}</p>
      </div>
    </div>

    <!-- Items -->
    <div class="card">
      <h3 class="card-title">{{ t('orderDetail.items') }}</h3>
      <div v-for="l in lines" :key="l.id" class="item-row">
        <LazyImage :src="l.imageUrl || l.productImage || ''" alt="" height="60px" width="60px" rounded="6px" />
        <div class="item-info">
          <div class="item-name">{{ l.productName }}</div>
          <div class="item-meta">¥{{ (l.price / 100).toFixed(2) }} × {{ l.quantity }}</div>
        </div>
        <span class="item-total">¥{{ (l.price * l.quantity / 100).toFixed(2) }}</span>
        <JdButton v-if="order.status==='3'" type="outline" size="sm" @click="openReview(l.productId)">{{ t('orderDetail.review') }}</JdButton>
      </div>
      <div class="order-total">
        <span class="total-label">{{ t('orderDetail.total') }}：</span>
        <span class="total-price">¥{{ (order.totalAmount / 100).toFixed(2) }}</span>
      </div>
    </div>

    <!-- Actions -->
    <div class="action-row">
      <JdButton v-if="order.status==='0'" type="ghost" @click="cancel">{{ t('orderDetail.cancelOrder') }}</JdButton>
      <JdButton v-if="order.status==='0'" @click="pay">{{ t('orderDetail.toPay') }}</JdButton>
      <JdButton v-if="order.status==='2'" :loading="receiving" @click="confirmReceive">确认收货</JdButton>
      <JdButton v-if="order.status==='3'" type="outline" @click="router.push('/after-sale?orderId=' + order.id)">申请售后</JdButton>
    </div>

    <!-- Review Modal -->
    <div v-if="showReview" class="modal-overlay" @click.self="showReview=false">
      <div class="review-modal">
        <h3 class="review-title">商品评价</h3>
        <div class="rating-stars">
          <span class="rating-label">评分</span>
          <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= reviewForm.rating }" @click="reviewForm.rating = i">
            {{ i <= reviewForm.rating ? '★' : '☆' }}
          </span>
        </div>
        <textarea v-model="reviewForm.content" placeholder="分享你的使用体验..." class="review-textarea" />
        <div class="review-actions">
          <JdButton type="ghost" @click="showReview = false">取消</JdButton>
          <JdButton @click="submitReview">提交评价</JdButton>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-page { max-width: 800px; margin: 0 auto; }
.card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-sm); margin-bottom: var(--space-lg); }
.card-title { font-size: var(--font-lg); font-weight: 600; margin-bottom: var(--space-md); }

.sk-line { height: 16px; margin-bottom: var(--space-md); background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-sm); }

.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.order-no-label { font-size: var(--font-md); color: var(--text-tertiary); }
.order-no { font-weight: 600; }

/* Steps */
.steps { display: flex; justify-content: space-between; position: relative; padding: var(--space-xl) 0; }
.step { flex: 1; display: flex; flex-direction: column; align-items: center; position: relative; z-index: 1; }
.step-dot { width: 24px; height: 24px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: var(--font-xs); margin-bottom: var(--space-xs); background: var(--border); color: #fff; }
.step-dot.active { background: var(--jd-red); }
.step-label { font-size: var(--font-xs); color: var(--text-tertiary); }
.step-label.active { color: var(--jd-red); }
.step-line { position: absolute; top: 32px; left: 12%; right: 12%; height: 2px; background: var(--border-light); z-index: 0; }
.step-line-fill { height: 100%; background: var(--jd-red); transition: width .6s; }

/* Shipment */
.shipment-info { display: flex; gap: var(--space-lg); color: var(--text-secondary); font-size: var(--font-md); flex-wrap: wrap; align-items: center; }
.eta { color: var(--green); font-size: var(--font-base); font-weight: 600; margin-left: auto; }

/* Receiver */
.receiver-info { color: var(--text-secondary); font-size: var(--font-md); }
.receiver-info p { margin-bottom: 2px; }

/* Items */
.item-row { display: flex; align-items: center; padding: var(--space-md) 0; border-bottom: 1px solid var(--border-light); gap: var(--space-lg); }
.item-info { flex: 1; }
.item-name { font-weight: 600; }
.item-meta { color: var(--text-tertiary); font-size: var(--font-base); }
.item-total { color: var(--jd-red); font-weight: 700; }

.order-total { text-align: right; padding-top: var(--space-lg); }
.total-label { font-size: var(--font-md); color: var(--text-secondary); }
.total-price { font-size: var(--font-title); color: var(--jd-red); font-weight: 700; }

/* Actions */
.action-row { display: flex; justify-content: flex-end; gap: var(--space-md); }

/* Review Modal */
.review-modal { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); width: 400px; max-width: 90vw; }
.review-title { font-size: var(--font-lg); font-weight: 600; margin-bottom: var(--space-lg); }
.rating-stars { margin-bottom: var(--space-md); display: flex; align-items: center; }
.rating-label { font-size: var(--font-base); color: var(--text-secondary); margin-right: var(--space-md); }
.star { cursor: pointer; font-size: var(--font-xxl); color: var(--border); }
.star.filled { color: var(--orange); }
.review-textarea { width: 100%; height: 100px; padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md); font-size: var(--font-base); resize: none; box-sizing: border-box; margin-bottom: var(--space-lg); background: var(--bg-white); color: var(--text-primary); }
.review-actions { display: flex; justify-content: flex-end; gap: var(--space-sm); }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
</style>
