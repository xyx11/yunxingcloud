<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderById } from '@/api/order'
import { useI18n } from '@/locales'
import { formatPrice } from '@/utils/format'
import JdButton from '@/components/JdButton.vue'
import LazyImage from '@/components/LazyImage.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import request from '@/api/request'
import type { OrderHead, Product } from '@/types'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const orderNo = ref('')
const orderId = ref('')
const status = ref<'success' | 'fail'>('success')
const orderInfo = ref<OrderHead | null>(null)
const loading = ref(true)
const countdown = ref(3)
const recommendations = ref<Product[]>([])
// Confetti particles
const particles = ref<{ x: number; y: number; color: string; delay: number; size: number }[]>([])
let cdTimer: ReturnType<typeof setInterval> | null = null

function startCountdown() {
  cdTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) { if (cdTimer) clearInterval(cdTimer); router.push('/order/' + orderId.value) }
  }, 1000)
}

function spawnConfetti() {
  const colors = ['#f10215','#ff6b6b','#ffc107','#4caf50','#2196f3','#9c27b0','#ff9800']
  const items: typeof particles.value = []
  for (let i = 0; i < 60; i++) {
    items.push({
      x: Math.random() * 100,
      y: -10 - Math.random() * 30,
      color: colors[Math.floor(Math.random() * colors.length)],
      delay: Math.random() * 2,
      size: 6 + Math.random() * 8,
    })
  }
  particles.value = items
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
  if (status.value === 'success') {
    spawnConfetti()
    startCountdown()
    // Load recommendations
    try { const r = await request.get('/recommend/hot'); recommendations.value = (r.data || []).slice(0, 4) } catch {}
  }
})

function shareOrder() {
  const text = `我在 YXCLOUD 商城下单成功！订单号 ${orderNo.value}，金额 ¥${orderInfo.value ? formatPrice(orderInfo.value.totalAmount / 100, 2) : '--'}`
  if (navigator.share) { navigator.share({ title: 'YXCLOUD', text }).catch(() => {}) }
  else { navigator.clipboard.writeText(text).catch(() => {}) }
}
</script>

<template>
  <div class="result-page">
    <SkeletonBox v-if="loading" variant="card" :columns="1" height="280px" />
    <div v-else class="result-card">
      <template v-if="status === 'success'">
        <!-- Confetti particles -->
        <div v-if="particles.length" class="confetti-container">
          <span
            v-for="(p, i) in particles" :key="i"
            class="confetti-particle"
            :style="{
              left: p.x + '%',
              backgroundColor: p.color,
              animationDelay: p.delay + 's',
              width: p.size + 'px',
              height: p.size + 'px',
            }"
          />
        </div>
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
            {{ t('paymentResult.viewOrder') }} ({{ countdown }}s)
          </JdButton>
          <JdButton block @click="router.push('/')">
            {{ t('paymentResult.continueShopping') }}
          </JdButton>
          <JdButton type="ghost" block @click="shareOrder">📤 分享战绩</JdButton>
        </div>
        <p class="auto-redirect">{{ t('paymentResult.autoRedirect', { n: countdown }) }}</p>

        <!-- Recommendations -->
        <div v-if="recommendations.length" class="recs-section">
          <h4 class="recs-title">🔥 猜你喜欢</h4>
          <div class="recs-grid">
            <div v-for="p in recommendations" :key="p.id" class="recs-item" @click="router.push('/product/' + p.id)">
              <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="100px" />
              <div class="recs-name">{{ p.name }}</div>
              <div class="recs-price">{{ formatPrice(p.price / 100, 2) }}</div>
            </div>
          </div>
        </div>
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
.result-icon.success { background: var(--green-bg); color: var(--green); }
.result-icon.fail { background: var(--jd-red-light); color: var(--jd-red); }
.result-title { font-size: 22px; margin-bottom: 8px; }
.result-title.success { color: var(--green); }
.result-title.fail { color: var(--jd-red); }
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

/* Confetti */
.confetti-container { position: fixed; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; z-index: 100; overflow: hidden; }
.confetti-particle { position: absolute; top: -20px; border-radius: 2px; animation: confetti-fall 3s ease-out forwards; }
@keyframes confetti-fall {
  0% { transform: translateY(0) rotate(0deg); opacity: 1; }
  100% { transform: translateY(100vh) rotate(720deg); opacity: 0; }
}

/* Recommendations */
.recs-section { margin-top: var(--space-xxl); text-align: left; }
.recs-title { font-size: var(--font-md); font-weight: 700; margin-bottom: var(--space-md); color: var(--text-primary); }
.recs-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-sm); }
.recs-item { background: var(--bg-hover); border-radius: var(--radius-md); padding: var(--space-sm); text-align: center; cursor: pointer; transition: transform var(--transition-fast); }
.recs-item:hover { transform: translateY(-3px); }
.recs-name { font-size: 12px; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 4px; }
.recs-price { font-size: var(--font-sm); color: var(--jd-red); font-weight: 700; }

@media (max-width: 768px) {
  .result-card { padding: 32px 24px; }
  .btn-row { flex-direction: column; }
  .recs-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
