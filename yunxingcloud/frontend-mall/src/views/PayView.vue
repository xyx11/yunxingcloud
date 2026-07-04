<script setup lang="ts">
import { ref, onMounted, onUnmounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderById, payOrder } from '@/api/order'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'
import JdButton from '@/components/JdButton.vue'
import type { OrderHead } from '@/types'

const route = useRoute()
const router = useRouter()
const toast = inject(ToastInjectionKey)!
const { t } = useI18n()
const order = ref<OrderHead | null>(null)
const selectedChannel = ref<'wechat' | 'alipay'>('wechat')
const paying = ref(false)
const polling = ref(false)
const pollCount = ref(0)
let pollTimer: ReturnType<typeof setInterval> | null = null

const minutes = ref(15); const seconds = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

function startCountdown(expiresAt?: string) {
  if (expiresAt) {
    const remaining = Math.max(0, new Date(expiresAt).getTime() - Date.now())
    minutes.value = Math.floor(remaining / 60000)
    seconds.value = Math.floor((remaining % 60000) / 1000)
  }
  countdownTimer = setInterval(() => {
    if (seconds.value > 0) seconds.value--
    else if (minutes.value > 0) { minutes.value--; seconds.value = 59 }
    else { if (countdownTimer) clearInterval(countdownTimer) }
  }, 1000)
}

onMounted(async () => {
  try {
    const r = await getOrderById(Number(route.params.id))
    order.value = r.data.order
    if (order.value?.status !== '0') { toast.error('订单状态异常'); router.replace('/orders') }
    else { startCountdown(order.value?.expiresAt) }
  } catch { router.replace('/orders') }
})

function startPolling() {
  polling.value = true; pollCount.value = 0
  pollTimer = setInterval(async () => {
    pollCount.value++
    try {
      const r = await getOrderById(Number(route.params.id))
      if (['1', '2', '3'].includes(r.data.order?.status)) {
        if (pollTimer) clearInterval(pollTimer); polling.value = false
        router.replace(`/order/${route.params.id}/result?status=success`)
      } else if (pollCount.value > 30) {
        if (pollTimer) clearInterval(pollTimer); polling.value = false
        router.replace(`/order/${route.params.id}/result?status=fail`)
      }
    } catch {}
  }, 2000)
}

async function confirmPay() {
  paying.value = true
  try { await payOrder(order.value.id, selectedChannel.value); startPolling() }
  catch { paying.value = false; router.replace(`/order/${route.params.id}/result?status=fail`) }
}

const pad = (n: number) => String(n).padStart(2, '0')

onUnmounted(() => { if (pollTimer) clearInterval(pollTimer); if (countdownTimer) clearInterval(countdownTimer) })
</script>

<template>
  <div class="pay-page">
    <div class="pay-card">
      <!-- Polling -->
      <template v-if="polling">
        <div class="polling">
          <div class="polling-icon">⏳</div>
          <h3 class="polling-title">{{ t('pay.polling') }}</h3>
          <p class="polling-desc">{{ t('pay.pollingDesc') }}</p>
          <div class="polling-bar"><div class="polling-fill" :style="{ width: (pollCount / 30 * 100) + '%' }" /></div>
          <p class="polling-count">{{ pollCount }}/30 s</p>
          <p v-if="pollCount > 25" class="polling-hint">
            {{ t('pay.pollingDesc') }}
            <span class="polling-link" @click="router.replace('/orders')">{{ t('pay.backToOrders') }}</span>
          </p>
        </div>
      </template>

      <!-- Payment Selection -->
      <template v-else>
        <h2 class="pay-title">{{ t('pay.title') }}</h2>
        <div v-if="order" class="pay-order-info">
          {{ order.orderNo }} · <b class="pay-amount">¥{{ (order.totalAmount / 100).toFixed(2) }}</b>
        </div>

        <div class="countdown-bar">
          ⏱ {{ t('pay.timeout', { min: pad(minutes), sec: pad(seconds) }) }}
        </div>

        <div class="channel-list">
          <div class="channel-item" :class="{ selected: selectedChannel === 'wechat' }" @click="selectedChannel = 'wechat'">
            <div class="channel-icon channel-icon--wechat">💬</div>
            <div class="channel-info">
              <div class="channel-name">{{ t('pay.wechat') }}</div>
              <div class="channel-desc">{{ t('pay.wechatDesc') }}</div>
            </div>
            <div class="channel-radio" :class="{ checked: selectedChannel === 'wechat' }">
              <span v-if="selectedChannel === 'wechat'">✓</span>
            </div>
          </div>
          <div class="channel-item" :class="{ selected: selectedChannel === 'alipay' }" @click="selectedChannel = 'alipay'">
            <div class="channel-icon channel-icon--alipay">💳</div>
            <div class="channel-info">
              <div class="channel-name">{{ t('pay.alipay') }}</div>
              <div class="channel-desc">{{ t('pay.alipayDesc') }}</div>
            </div>
            <div class="channel-radio" :class="{ checked: selectedChannel === 'alipay' }">
              <span v-if="selectedChannel === 'alipay'">✓</span>
            </div>
          </div>
        </div>

        <JdButton block size="lg" :loading="paying" @click="confirmPay">
          {{ paying ? t('pay.processing') : t('pay.confirm', { amount: (order?.totalAmount / 100).toFixed(2) }) }}
        </JdButton>
        <p class="security-note">{{ t('pay.securityNote') }}</p>
      </template>
    </div>
  </div>
</template>

<style scoped>
.pay-page { max-width: 500px; margin: 30px auto; }
.pay-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxxl); box-shadow: var(--shadow-md); }

/* Polling */
.polling { text-align: center; padding: 40px 0; }
.polling-icon { font-size: 48px; margin-bottom: var(--space-lg); animation: spin 1s linear infinite; }
.polling-title { font-size: var(--font-lg); margin-bottom: var(--space-sm); }
.polling-desc { color: var(--text-tertiary); font-size: var(--font-base); margin-bottom: var(--space-lg); }
.polling-bar { width: 200px; height: 4px; background: var(--border-light); border-radius: 2px; margin: 0 auto; overflow: hidden; }
.polling-fill { height: 100%; background: var(--jd-red); border-radius: 2px; transition: width .3s; }
.polling-count { color: var(--text-placeholder); font-size: var(--font-xs); margin-top: var(--space-sm); }
.polling-hint { color: var(--orange); font-size: var(--font-sm); margin-top: var(--space-sm); }
.polling-link { color: var(--jd-red); cursor: pointer; text-decoration: underline; }

/* Payment */
.pay-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-sm); }
.pay-order-info { color: var(--text-tertiary); font-size: var(--font-base); margin-bottom: var(--space-sm); }
.pay-amount { color: var(--jd-red); font-size: var(--font-lg); }

.countdown-bar { background: #fff3cd; border-radius: var(--radius-md); padding: var(--space-sm) var(--space-md); margin-bottom: var(--space-lg); text-align: center; font-size: var(--font-base); color: #856404; }

.channel-list { margin-bottom: var(--space-xxl); }
.channel-item { display: flex; align-items: center; padding: var(--space-lg); border-radius: var(--radius-md); margin-bottom: var(--space-md); cursor: pointer; border: 1px solid var(--border-light); background: var(--bg-white); transition: all var(--transition-fast); }
.channel-item.selected { border: 2px solid var(--jd-red); background: var(--jd-red-light); }
.channel-icon { width: 32px; height: 32px; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; font-size: var(--font-lg); margin-right: var(--space-md); }
.channel-icon--wechat { background: #07c160; }
.channel-icon--alipay { background: var(--blue); }
.channel-info { flex: 1; }
.channel-name { font-weight: 600; font-size: 15px; }
.channel-desc { color: var(--text-tertiary); font-size: var(--font-sm); }
.channel-radio { width: 18px; height: 18px; border-radius: 50%; border: 2px solid var(--jd-red); display: flex; align-items: center; justify-content: center; }
.channel-radio.checked { background: var(--jd-red); color: #fff; font-size: var(--font-xs); }
.security-note { text-align: center; margin-top: var(--space-lg); font-size: var(--font-sm); color: var(--text-tertiary); }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
