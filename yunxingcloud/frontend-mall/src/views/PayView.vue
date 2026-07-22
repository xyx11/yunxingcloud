<script setup lang="ts">
import { ref, onMounted, onUnmounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderById, payOrder } from '@/api/order'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'
import { formatPrice } from '@/utils/format'
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
const expired = ref(false)
const pageLoading = ref(true)
const loadError = ref(false)
const payAmount = ref(0)
const payOrderNo = ref('')
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
    else { if (countdownTimer) clearInterval(countdownTimer); expired.value = true }
  }, 1000)
}

async function loadOrder() {
  pageLoading.value = true
  loadError.value = false
  try {
    const r = await getOrderById(Number(route.params.id))
    order.value = r.data.order
    payAmount.value = order.value?.totalAmount || 0
    payOrderNo.value = order.value?.orderNo || ''
    if (order.value?.status !== '0') { toast.error(t('toast.payStatusError')); router.replace('/orders') }
    else { startCountdown(order.value?.expiresAt) }
  } catch { loadError.value = true; toast.error(t('toast.payLoadFail')) }
  finally { pageLoading.value = false }
}

onMounted(loadOrder)

function startPolling() {
  polling.value = true; pollCount.value = 0
  pollTimer = setInterval(async () => {
    pollCount.value++
    try {
      const r = await getOrderById(Number(route.params.id))
      if (['1', '2', '3'].includes(r.data.order?.status)) {
        if (pollTimer) clearInterval(pollTimer); polling.value = false
        router.replace(`/order/${route.params.id}/result?status=success`)
      } else if (pollCount.value > 120) {
        if (pollTimer) clearInterval(pollTimer); polling.value = false
        router.replace(`/order/${route.params.id}/result?status=fail`)
      }
    } catch { /* poll failure is transient, next cycle will retry */ }
  }, 2000)
}

async function confirmPay() {
  if (!order.value) return; paying.value = true
  try { await payOrder(order.value.id, selectedChannel.value); startPolling() }
  catch { paying.value = false; router.replace(`/order/${route.params.id}/result?status=fail`) }
}

const pad = (n: number) => String(n).padStart(2, '0')

onUnmounted(() => { if (pollTimer) clearInterval(pollTimer); if (countdownTimer) clearInterval(countdownTimer) })
</script>

<template>
  <div class="pay-page">
    <div class="pay-card">
      <!-- Loading -->
      <div v-if="pageLoading" class="pay-skel">
        <div class="sk-line w80" />
        <div class="sk-line w40" />
        <div class="sk-bar" />
        <div v-for="i in 2" :key="i" class="sk-channel" />
      </div>

      <!-- Error -->
      <div v-else-if="loadError" class="pay-error">
        <p class="pay-error-icon">⚠️</p>
        <p class="pay-error-text">订单信息加载失败</p>
        <JdButton @click="loadOrder()">重新加载</JdButton>
      </div>

      <!-- Polling -->
      <template v-else-if="polling">
        <div class="polling">
          <div class="polling-icon" :class="{ pulse: pollCount < 30 }">⏳</div>
          <h3 class="polling-title">{{ pollCount < 60 ? t('pay.polling') : t('pay.waiting') }}</h3>
          <p class="polling-desc">{{ t('pay.pollingDesc') }}</p>
          <div class="polling-bar"><div class="polling-fill" :class="{ slow: pollCount > 60 }" :style="{ width: (pollCount / 120 * 100) + '%' }" /></div>
          <p class="polling-count">{{ pollCount }}/120 s</p>
          <div v-if="pollCount > 25" class="polling-actions">
            <JdButton size="sm" type="outline" @click="loadOrder()">手动刷新</JdButton>
            <span class="polling-link" @click="router.replace('/orders')">{{ t('pay.backToOrders') }}</span>
          </div>
        </div>
      </template>

      <!-- Payment Selection -->
      <template v-else>
        <h2 class="pay-title">{{ t('pay.title') }}</h2>

        <!-- Order summary card -->
        <div class="pay-summary">
          <div class="pay-summary-row">
            <span>订单编号</span>
            <span class="pay-summary-val">{{ payOrderNo || '-' }}</span>
          </div>
          <div class="pay-summary-row">
            <span>支付金额</span>
            <span class="pay-amount">{{ formatPrice(payAmount / 100, 2) }}</span>
          </div>
          <div class="pay-summary-row pay-summary-channel">
            <span>支付方式</span>
            <span>{{ selectedChannel === 'wechat' ? t('pay.wechat') : t('pay.alipay') }}</span>
          </div>
        </div>

        <!-- Countdown with urgency -->
        <div class="countdown-bar" :class="{ expired: expired, urgent: !expired && minutes === 0 && seconds <= 60 }">
          <template v-if="expired">
            <span class="countdown-icon">⚠️</span> 订单已过期，请重新下单
          </template>
          <template v-else>
            <span class="countdown-icon">⏱</span>
            {{ t('pay.timeout', { min: pad(minutes), sec: pad(seconds) }) }}
            <span class="countdown-tip">超时订单将自动取消</span>
          </template>
        </div>

        <div class="channel-list">
          <div class="channel-item" :class="{ selected: selectedChannel === 'wechat' }" role="button" tabindex="0" @click="selectedChannel = 'wechat'" @keydown.enter.prevent="selectedChannel = 'wechat'" @keydown.space.prevent="selectedChannel = 'wechat'">
            <div class="channel-icon channel-icon--wechat">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="#fff"><path d="M8.5 13.5c-.83 0-1.5.67-1.5 1.5s.67 1.5 1.5 1.5S10 15.83 10 15s-.67-1.5-1.5-1.5zm5 0c-.83 0-1.5.67-1.5 1.5s.67 1.5 1.5 1.5S15 15.83 15 15s-.67-1.5-1.5-1.5zM12 2C6.48 2 2 6.04 2 11c0 2.57 1.2 4.88 3.11 6.44l-.86 2.56 2.82-1.53c.9.3 1.86.48 2.87.53l.06-.58A5 5 0 0 1 12 6a5 5 0 0 1 5 5 5 5 0 0 1-3.3 4.73l.3 1.08c.33.04.66.08.99.08 1.72 0 3.34-.44 4.75-1.2l2.86 1.46-.82-2.44C21.24 15.59 22 13.38 22 11c0-4.96-4.48-9-10-9z"/></svg>
            </div>
            <div class="channel-info">
              <div class="channel-name">{{ t('pay.wechat') }}</div>
              <div class="channel-desc">{{ t('pay.wechatDesc') }}</div>
            </div>
            <div class="channel-radio" :class="{ checked: selectedChannel === 'wechat' }">
              <span v-if="selectedChannel === 'wechat'">✓</span>
            </div>
          </div>
          <div class="channel-item" :class="{ selected: selectedChannel === 'alipay' }" role="button" tabindex="0" @click="selectedChannel = 'alipay'" @keydown.enter.prevent="selectedChannel = 'alipay'" @keydown.space.prevent="selectedChannel = 'alipay'">
            <div class="channel-icon channel-icon--alipay">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="#fff"><path d="M21.422 15.358c-3.22-1.386-6.847-2.408-10.604-2.996-1.04-.165-2.104-.284-3.18-.336-1.09-.052-2.19-.064-3.29-.006-.402.02-.805.05-1.205.108-.086.012-.22.04-.3.06-.08.02-.154.05-.194.09-.064.082-.02.258.03.382.074.19.242.368.398.508.69.62 1.51 1.07 2.35 1.41 1.69.68 3.49 1.07 5.24 1.51 1.08.27 2.15.55 3.21.88.42.13.83.28 1.23.45.87.36 1.46.86 1.47 1.48.01.56-.35 1.06-.88 1.24-.34.11-.71.14-1.07.14-1.04.02-2.05-.22-3.03-.5-1.82-.52-3.57-1.2-5.2-2.14-.14-.08-.29-.19-.44-.27-.02-.01-.06-.02-.08-.01-.01.02-.01.05-.01.08l-.01.47c0 .74.07 1.47.25 2.19.17.68.47 1.3.88 1.88.42.59.94 1.09 1.53 1.48.6.4 1.24.71 1.93.91 1.08.32 2.2.48 3.33.5 1.28.03 2.56-.09 3.8-.42.61-.16 1.2-.37 1.76-.65.55-.28 1.05-.62 1.47-1.06.32-.34.6-.72.78-1.16.2-.5.28-1.04.19-1.57-.15-.86-.68-1.58-1.54-2.16-.13-.09-.26-.16-.41-.24l-.04-.02zM3.904 8.158c.34-.11.69-.19 1.04-.24.78-.13 1.57-.12 2.35-.02.72.1 1.42.29 2.12.5.68.2 1.36.42 2.02.69 1.29.52 2.46 1.17 3.47 1.98.2.16.43.31.62.47.01 0 .06.04.07.04 0-.24-.01-.48-.01-.72 0-.86-.01-1.73-.01-2.59 0-.52-.01-1.04 0-1.56.01-.36.04-.72.13-1.07.08-.32.21-.61.4-.88.22-.31.5-.57.83-.76.33-.2.68-.35 1.05-.43.41-.1.82-.14 1.24-.12.4.02.8.07 1.2.13 0-.53-.02-1.06-.06-1.59-.02-.3-.07-.6-.15-.9-.06-.24-.16-.47-.3-.68-.2-.31-.47-.58-.79-.76-.56-.32-1.19-.47-1.83-.51-1.52-.12-3.04.12-4.46.77-.56.26-1.08.6-1.56 1-.57.47-1.05 1.02-1.43 1.65-.18.29-.23.62-.23.96.01.34.03.68.04 1.02.01.36 0 .72 0 1.08.01.78 0 1.56 0 2.34l-.01.33c-.04.03-.08.05-.12.07-1.37.93-2.55 2.09-3.46 3.46-.44.67-.79 1.39-1.05 2.15-.01.02-.02.05-.04.08-.12.29-.12.29-.24-.01-.23-.58-.53-1.14-.8-1.7-.25-.52-.57-1.01-.86-1.52-.22-.39-.46-.77-.65-1.17-.12-.26-.23-.52-.3-.8-.06-.23-.09-.47-.08-.71.02-.5.18-.96.5-1.36.21-.26.47-.48.76-.63z"/></svg>
            </div>
            <div class="channel-info">
              <div class="channel-name">{{ t('pay.alipay') }}</div>
              <div class="channel-desc">{{ t('pay.alipayDesc') }}</div>
            </div>
            <div class="channel-radio" :class="{ checked: selectedChannel === 'alipay' }">
              <span v-if="selectedChannel === 'alipay'">✓</span>
            </div>
          </div>
        </div>

        <!-- Payment instructions -->
        <div class="pay-instructions">
          <div class="inst-step">
            <span class="inst-num">1</span>
            <span>选择上方支付方式</span>
          </div>
          <div class="inst-step">
            <span class="inst-num">2</span>
            <span>点击下方按钮确认支付</span>
          </div>
          <div class="inst-step">
            <span class="inst-num">3</span>
            <span>{{ t('pay.payInApp', { app: selectedChannel === 'wechat' ? t('pay.appWechat') : t('pay.appAlipay') }) }}</span>
          </div>
        </div>

        <JdButton block size="lg" :loading="paying" :disabled="expired" @click="confirmPay">
          {{ paying ? t('pay.processing') : expired ? t('pay.expired2') : t('pay.confirm', { amount: formatPrice(payAmount / 100, 2) }) }}
        </JdButton>
        <div class="pay-footer">
          <p class="security-note">{{ t('pay.securityNote') }}</p>
          <p class="pay-cancel-link" @click="router.push('/orders')">← 返回订单列表</p>
        </div>
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
.polling-bar { width: 200px; height: 4px; background: var(--border-light); border-radius: 2px; margin: 0 auto var(--space-md); overflow: hidden; }
.polling-fill { height: 100%; background: var(--jd-red); border-radius: 2px; transition: width .3s; }
.polling-fill.slow { background: var(--orange); }
.polling-icon.pulse { animation: spin 1s linear infinite; }
.polling-actions { display: flex; align-items: center; justify-content: center; gap: var(--space-xl); margin-top: var(--space-md); }
.polling-count { color: var(--text-placeholder); font-size: var(--font-xs); margin-top: var(--space-sm); }
.polling-hint { color: var(--orange); font-size: var(--font-sm); margin-top: var(--space-sm); }
.polling-link { color: var(--jd-red); cursor: pointer; text-decoration: underline; }

/* Payment */
.pay-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-lg); }

/* Order summary */
.pay-summary { background: var(--bg-hover); border-radius: var(--radius-md); padding: var(--space-lg); margin-bottom: var(--space-lg); }
.pay-summary-row { display: flex; justify-content: space-between; padding: var(--space-xs) 0; font-size: var(--font-base); color: var(--text-secondary); }
.pay-summary-val { color: var(--text-primary); font-weight: 500; }
.pay-amount { color: var(--jd-red); font-size: var(--font-title); font-weight: 700; }
.pay-summary-channel { border-top: 1px dashed var(--border); padding-top: var(--space-sm); margin-top: var(--space-sm); }

.countdown-bar { background: #fff3cd; border-radius: var(--radius-md); padding: var(--space-md) var(--space-lg); margin-bottom: var(--space-lg); text-align: center; font-size: var(--font-md); color: #856404; font-weight: 600; display: flex; flex-direction: row; align-items: center; justify-content: center; gap: var(--space-sm); }
.countdown-bar.urgent { background: #fff0f0; color: var(--jd-red); font-size: var(--font-lg); font-weight: 700; animation: pulse-countdown .8s ease-in-out infinite; }
.countdown-bar.expired { background: #f8d7da; color: #721c24; }
.countdown-icon { font-size: 20px; }
.countdown-bar.urgent .countdown-icon { font-size: 24px; }
.countdown-tip { font-size: var(--font-xs); font-weight: 400; opacity: .8; }
@keyframes pulse-countdown { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.02); } }

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
/* Instructions */
.pay-instructions { margin: var(--space-lg) 0; padding: var(--space-lg); background: #f0f7ff; border-radius: var(--radius-md); }
.inst-step { display: flex; align-items: center; gap: var(--space-md); padding: var(--space-xs) 0; font-size: var(--font-sm); color: var(--text-secondary); }
.inst-num { width: 22px; height: 22px; border-radius: 50%; background: var(--jd-red); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; flex-shrink: 0; }

.pay-footer { text-align: center; margin-top: var(--space-lg); }
.security-note { font-size: var(--font-sm); color: var(--text-tertiary); margin-bottom: var(--space-sm); }
.pay-cancel-link { font-size: var(--font-sm); color: var(--text-placeholder); cursor: pointer; display: inline-block; }
.pay-cancel-link:hover { color: var(--jd-red); }

/* Skeleton */
.pay-skel { display: flex; flex-direction: column; gap: var(--space-lg); }
.sk-line { height: 20px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-sm); }
.sk-line.w80 { width: 80%; }
.sk-line.w40 { width: 40%; }
.sk-bar { height: 40px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-md); }
.sk-channel { height: 64px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-md); }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

/* Error */
.pay-error { text-align: center; padding: var(--space-xxxl) 0; }
.pay-error-icon { font-size: 48px; margin-bottom: var(--space-md); }
.pay-error-text { color: var(--text-secondary); margin-bottom: var(--space-lg); }

/* QR */
.qr-section { text-align: center; margin: var(--space-lg) 0; }
.qr-placeholder {
  display: flex; flex-direction: column; align-items: center; gap: var(--space-sm);
  padding: var(--space-xl); background: var(--bg-hover); border-radius: var(--radius-lg);
}
.qr-icon { font-size: 40px; }
.qr-text { font-size: var(--font-sm); color: var(--text-secondary); }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .pay-page { max-width: 100%; margin: 0; padding: var(--space-md); padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px)); }
  .pay-card { padding: var(--space-xl); border-radius: var(--radius-md); }
  .pay-title { font-size: var(--font-lg); }
  .channel-item { padding: var(--space-md); }
  .channel-name { font-size: 14px; }
  .polling { padding: 20px 0; }
  .polling-icon { font-size: 36px; }
  .polling-bar { width: 100%; }
  .security-note { font-size: 11px; }
}
</style>
