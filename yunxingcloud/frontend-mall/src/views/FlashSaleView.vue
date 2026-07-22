<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from '@/locales'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'
import { formatPrice } from '@/utils/format'
import CountdownTimer from '@/components/CountdownTimer.vue'
import LazyImage from '@/components/LazyImage.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdButton from '@/components/JdButton.vue'
import JdEmpty from '@/components/JdEmpty.vue'

interface FlashSaleItem {
  id: number
  productId: number
  productName?: string
  imageUrl?: string
  flashPrice: number
  originalPrice: number
  startTime: string
  endTime: string
  stock: number
  sold?: number
}

const toast = useToast()
const { t } = useI18n()
const router = useRouter()
const sales = ref<FlashSaleItem[]>([])
const loading = ref(true)
const loadError = ref(false)
const activeSlot = ref<number>(0)
const remindSet = ref<Set<number>>(new Set())
const buying = ref<Set<number>>(new Set())

// Time slots
const timeSlots = [
  { key: 10, label: t('flashSale.session10'), start: 10, end: 12 },
  { key: 14, label: t('flashSale.session14'), start: 14, end: 16 },
  { key: 20, label: t('flashSale.session20'), start: 20, end: 22 },
]

const slotSales = computed(() => {
  const active = timeSlots[activeSlot.value]
  if (!active) return sales.value
  const startHour = active.start
  const list = sales.value.filter(s => {
    const h = new Date(s.startTime).getHours()
    const startH = new Date(s.startTime)
    startH.setHours(startHour, 0, 0, 0)
    const endH = new Date(s.startTime)
    endH.setHours(active.end, 0, 0, 0)
    return h >= startHour && h < active.end
  })
  return list
})

// Auto-detect current active slot
function detectSlot() {
  const h = new Date().getHours()
  for (let i = 0; i < timeSlots.length; i++) {
    if (h < timeSlots[i].start) { activeSlot.value = i; return }
    if (h >= timeSlots[i].start && h < timeSlots[i].end) { activeSlot.value = i; return }
  }
  activeSlot.value = 0
}

onMounted(async () => {
  detectSlot()
  try { const r = await request.get('/flash-sale'); sales.value = r.data || []; loadError.value = false
    } catch { toast.error(t('toast.flashFail')); loadError.value = true }
  loading.value = false
})

function goProduct(id: number) { router.push(`/product/${id}`) }
const isActive = (s: FlashSaleItem) => new Date(s.startTime).getTime() <= Date.now() && new Date(s.endTime).getTime() > Date.now()
const isUpcoming = (s: FlashSaleItem) => new Date(s.startTime).getTime() > Date.now()
const stockPct = (s: FlashSaleItem) => Math.max(0, Math.round(((s.stock - (s.sold || 0)) / Math.max(1, s.stock)) * 100))
const soldPct = (s: FlashSaleItem) => 100 - stockPct(s)

async function setRemind(s: FlashSaleItem) {
  if (remindSet.value.has(s.id)) return
  try {
    await request.post('/flash-sale/remind', { saleId: s.id, productId: s.productId })
    remindSet.value.add(s.id)
    toast.success(t('toast.flashRemindSet'))
  } catch { toast.error(t('toast.flashRemindFail')) }
}

async function buyNow(s: FlashSaleItem) {
  if (!isActive(s)) { toast.error(t('toast.flashInactive')); return }
  if (buying.value.has(s.id)) return
  buying.value.add(s.id)
  try {
    await request.post(`/flash-sale/${s.id}/buy`, null, { params: { productId: s.productId } })
    toast.success(t('toast.flashSuccess'))
    await request.post('/cart', { productId: s.productId, quantity: 1 })
    router.push('/cart')
  } catch (e: unknown) {
    const msg = (e as { response?: { data?: { message?: string } } }).response?.data?.message || t('toast.flashBuyFail')
    toast.error(msg)
  } finally { buying.value.delete(s.id) }
}
</script>

<template>
  <div class="flash-page">
    <div class="flash-hero">
      <h1 class="flash-hero-title">⚡ {{ t('flashSale.title') }}</h1>
      <p class="flash-hero-sub">{{ t('flashSale.subtitle') }}</p>
    </div>

    <!-- Time slots -->
    <div class="slot-tabs">
      <span v-for="(slot, i) in timeSlots" :key="slot.key" class="slot-tab" :class="{ active: activeSlot === i }" @click="activeSlot = i">
        {{ slot.label }}
      </span>
    </div>

    <div v-if="loading" class="flash-grid">
      <div v-for="i in 6" :key="i" class="flash-card">
        <SkeletonBox height="200px" :count="1" />
        <div class="sk-pad"><SkeletonBox height="16px" width="60%" :count="1" /></div>
      </div>
    </div>

    <div v-else-if="loadError" class="fs-error">
      <p class="fs-error-icon">🔌</p>
      <p>{{ t('toast.flashFail') }}</p>
      <JdButton size="sm" @click="loadError = false; loading = true; detectSlot()">{{ t('common.retry') }}</JdButton>
    </div>

    <div v-else-if="slotSales.length" class="flash-grid">
      <div v-for="s in slotSales" :key="s.id" class="flash-card" :class="{ expired: !isActive(s) && !isUpcoming(s) }" role="button" tabindex="0" @click="goProduct(s.productId)" @keydown.enter.prevent="goProduct(s.productId)" @keydown.space.prevent="goProduct(s.productId)">
        <div class="flash-img">
          <LazyImage :src="s.imageUrl || ''" :alt="s.productName" height="200px" bg="linear-gradient(135deg,#1a1a1a,#333)" />
          <div class="flash-countdown">
            <CountdownTimer :end-time="s.endTime" :label="isUpcoming(s) ? t('flashSale.starting') : t('flashSale.ending')" compact />
          </div>
          <span class="flash-stock" :class="{ low: stockPct(s) < 20 }">{{ stockPct(s) }}%剩余</span>
        </div>
        <div class="flash-info">
          <h3 class="flash-name">{{ s.productName || t('flashSale.productDefault') }}</h3>
          <div class="flash-prices">
            <span class="flash-price">{{ formatPrice(s.flashPrice / 100, 2) }}</span>
            <span class="flash-original">{{ formatPrice(s.originalPrice / 100, 2) }}</span>
          </div>
          <!-- Progress bar with sold count -->
          <div class="progress-bar-wrap">
            <div class="progress-bar"><div class="progress-fill" :class="{ urgent: soldPct(s) > 80 }" :style="{ width: soldPct(s) + '%' }" /></div>
            <span class="progress-text">{{ t('flashSale.progressLabel', { sold: String(s.sold || 0), stock: String(s.stock) }) }}</span>
          </div>
          <div class="flash-card-actions">
            <JdButton v-if="isActive(s)" block size="sm" :loading="buying.has(s.id)" @click.stop="buyNow(s)">
              {{ t('flashSale.buyNow') }}
            </JdButton>
            <JdButton v-else-if="isUpcoming(s)" block size="sm" type="outline"
              :disabled="remindSet.has(s.id)"
              @click.stop="setRemind(s)">
              {{ remindSet.has(s.id) ? t('flashSale.remindDone') : t('flashSale.remindBtn') }}
            </JdButton>
            <JdButton v-else block size="sm" disabled>
              {{ t('countdown.ended') }}
            </JdButton>
          </div>
        </div>
      </div>
    </div>

    <JdEmpty v-else icon="⚡" :title="t('flashSale.emptySlot')" />
  </div>
</template>

<style scoped>
.flash-page { max-width: 900px; margin: 0 auto; }
.flash-hero { background: linear-gradient(135deg, #1a1a1a, #333); color: #fff; border-radius: var(--radius-xl); padding: var(--space-xxxl); margin-bottom: var(--space-xxl); text-align: center; box-shadow: var(--shadow-lg); }
.flash-hero-title { font-size: var(--font-h1); font-weight: 800; margin-bottom: var(--space-xs); }
.flash-hero-sub { font-size: var(--font-md); opacity: .7; }

/* Time slots */
.slot-tabs { display: flex; justify-content: center; gap: var(--space-md); margin-bottom: var(--space-xxl); }
.slot-tab {
  padding: var(--space-md) var(--space-xxl); border-radius: var(--radius-round);
  cursor: pointer; font-size: var(--font-md); font-weight: 600;
  background: var(--bg-white); color: var(--text-secondary);
  border: 2px solid var(--border); transition: all var(--transition-fast);
}
.slot-tab.active { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }
.slot-tab:not(.active):hover { border-color: var(--jd-red); color: var(--jd-red); }

.flash-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-lg); }
.flash-card { background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition); }
.flash-card:hover { transform: translateY(-6px); }
.flash-card.expired { opacity: .5; cursor: default; }
.flash-card.expired:hover { transform: none; }

.flash-img { height: 200px; position: relative; }
.flash-countdown { position: absolute; top: 0; left: 0; right: 0; background: rgba(0,0,0,.6); color: #fff; text-align: center; padding: 6px; font-size: var(--font-sm); font-weight: 600; z-index: 1; }
.flash-stock { position: absolute; bottom: 10px; left: 10px; background: var(--jd-red); color: #fff; font-size: var(--font-xs); padding: 2px 8px; border-radius: var(--radius-sm); transition: background var(--transition-fast); }
.flash-stock.low { background: #ff6b00; animation: pulse-stock 1s ease-in-out infinite; }
@keyframes pulse-stock { 0%, 100% { opacity: 1; } 50% { opacity: .6; } }

.flash-info { padding: var(--space-lg); }
.flash-name { font-size: 15px; font-weight: 600; margin-bottom: var(--space-sm); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.flash-prices { display: flex; align-items: baseline; gap: var(--space-sm); margin-bottom: var(--space-xs); }
.flash-price { color: var(--jd-red); font-size: var(--font-title); font-weight: 700; }
.flash-original { color: var(--text-tertiary); font-size: var(--font-sm); text-decoration: line-through; }

.progress-bar-wrap { display: flex; align-items: center; gap: var(--space-sm); margin-bottom: var(--space-md); }
.progress-bar { flex: 1; background: var(--bg-hover); border-radius: var(--radius-sm); height: 6px; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, var(--jd-red), #ff6b6b); border-radius: var(--radius-sm); transition: width 1s; }
.progress-fill.urgent { background: linear-gradient(90deg, #ff4444, #ff0000); animation: progressPulse .6s ease-in-out infinite; }
@keyframes progressPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .7; }
}
.progress-text { font-size: var(--font-xs); color: var(--text-tertiary); white-space: nowrap; }

.flash-card-actions { margin-top: var(--space-xs); }

.sk-pad { padding: 16px; }

@media (max-width: 768px) {
  .flash-grid { grid-template-columns: repeat(2, 1fr); }
  .slot-tabs { gap: var(--space-sm); }
  .slot-tab { padding: var(--space-sm) var(--space-lg); font-size: var(--font-sm); }
  .flash-hero { padding: var(--space-xl); }
  .flash-page { padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px)); }
  .fs-error { text-align: center; padding: 60px var(--space-md); }
  .fs-error-icon { font-size: 48px; margin-bottom: var(--space-md); }
}
</style>
