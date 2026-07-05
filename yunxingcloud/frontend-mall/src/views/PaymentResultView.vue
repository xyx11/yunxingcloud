<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderById } from '@/api/order'
import { useI18n } from '@/locales'
import { formatPrice } from '@/utils/format'
import JdButton from '@/components/JdButton.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import type { OrderHead } from '@/types'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const orderNo = ref('')
const orderId = ref('')
const status = ref<'success' | 'fail'>('success')
const orderInfo = ref<OrderHead | null>(null)
const loading = ref(true)
const countdown = ref(3)
let cdTimer: ReturnType<typeof setInterval> | null = null
function startCountdown() {
  cdTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) { if (cdTimer) clearInterval(cdTimer); router.push('/order/' + orderId.value) }
  }, 1000)
}
onUnmounted(() => { if (cdTimer) clearInterval(cdTimer) })

onMounted(async () => {
  orderId.value = (route.params.id as string) || ''
  status.value = (route.query.status as string) === 'fail' ? 'fail' : 'success'
  if (orderId.value) {
    try {
      const r = await getOrderById(Number(orderId.value))
      orderInfo.value = r.data.order
      orderNo.value = orderInfo.value?.orderNo || ''
    } catch {}
  }
  loading.value = false
  if (status.value === 'success') startCountdown()
})
</script>

<template>
  <div class="result-page">
    <SkeletonBox v-if="loading" variant="card" :columns="1" height="280px" />
    <div v-else class="result-card">
      <template v-if="status === 'success'">
        <div class="result-icon success">✓</div>
        <h2 class="result-title success">{{ t('paymentResult.success') }}</h2>
        <p class="result-desc">{{ t('paymentResult.successDesc') }}</p>
        <div v-if="orderNo" class="order-info">
          <div class="info-row">
            <span class="info-label">{{ t('paymentResult.orderNo') }}</span>
            <span class="info-value">{{ orderNo }}</span>
          </div>
          <div v-if="orderInfo" class="info-row">
            <span class="info-label">{{ t('paymentResult.amount') }}</span>
            <span class="amount-value">{{ formatPrice(orderInfo.totalAmount / 100, 2) }}</span>
          </div>
        </div>
        <div class="btn-row">
          <JdButton type="outline" block @click="router.push(`/order/${orderId}`)">
            {{ t('paymentResult.viewOrder') }}
          </JdButton>
          <JdButton block @click="router.push('/')">
            {{ t('paymentResult.continueShopping') }}
          </JdButton>
        </div>
        <p class="auto-redirect">{{ countdown }}秒后自动跳转订单详情</p>
      </template>
      <template v-else>
        <div class="result-icon fail">✕</div>
        <h2 class="result-title fail">{{ t('paymentResult.failed') }}</h2>
        <p class="result-desc">{{ t('paymentResult.failedDesc') }}</p>
        <div class="btn-row">
          <JdButton type="outline" block @click="router.push('/pay/' + orderId)">
            {{ t('paymentResult.retryPayment') }}
          </JdButton>
          <JdButton block @click="router.push('/')">
            {{ t('paymentResult.backHome') }}
          </JdButton>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.result-page { max-width: 500px; margin: 40px auto; text-align: center; }
.result-card {
  background: var(--bg-white); border-radius: var(--radius-xl); padding: 48px 40px;
  box-shadow: var(--shadow-sm);
}
.result-icon {
  width: 80px; height: 80px; border-radius: 50%; display: flex;
  align-items: center; justify-content: center; font-size: 40px;
  margin: 0 auto 20px; animation: popIn .4s ease-out;
}
.result-icon.success { background: #d4edda; color: #28a745; }
.result-icon.fail { background: #f8d7da; color: #dc3545; }
.result-title { font-size: 22px; margin-bottom: 8px; }
.result-title.success { color: #155724; }
.result-title.fail { color: #721c24; }
.result-desc { color: var(--text-secondary); font-size: var(--font-md); margin-bottom: 24px; }
.order-info {
  background: var(--bg-page); border-radius: var(--radius-md); padding: var(--space-lg);
  margin-bottom: 24px; text-align: left;
}
.info-row { display: flex; justify-content: space-between; margin-bottom: var(--space-sm); }
.info-row:last-child { margin-bottom: 0; }
.info-label { color: var(--text-tertiary); font-size: var(--font-base); }
.info-value { font-weight: 600; font-size: var(--font-base); }
.amount-value { color: var(--jd-red); font-weight: 700; font-size: 18px; }
.btn-row { display: flex; gap: 12px; }
.auto-redirect { margin-top: var(--space-lg); font-size: var(--font-sm); color: var(--text-tertiary); }
@keyframes popIn { 0% { transform: scale(0); opacity: 0; } 70% { transform: scale(1.2); } 100% { transform: scale(1); opacity: 1; } }

@media (max-width: 768px) {
  .result-card { padding: 32px 24px; }
  .btn-row { flex-direction: column; }
}
</style>
