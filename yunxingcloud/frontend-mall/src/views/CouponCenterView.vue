<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '@/api/request'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdButton from '@/components/JdButton.vue'

const toast = useToast()
const { t } = useI18n()
const availableCoupons = ref<any[]>([])
const myCoupons = ref<any[]>([])
const activeTab = ref<'available' | 'mine'>('available')
const loading = ref(true)
const loadError = ref(false)
const claiming = ref<Set<number>>(new Set())
const claimed = ref<Set<number>>(new Set())
const claimingAll = ref(false)
const showExpired = ref(false)

function daysLeft(endTime: string): number {
  return (new Date(endTime).getTime() - Date.now()) / 86400000
}
function expiryLabel(endTime: string): string {
  const d = daysLeft(endTime)
  if (d <= 0) return t('coupon.expired')
  if (d <= 1) return t('coupon.expiringToday')
  if (d <= 3) return t('coupon.expiringSoon')
  return ''
}
function couponStatus(c: any): string {
  if (c.status === '1') return t('coupon.statusUsed')
  if (daysLeft(c.endTime) <= 0) return t('coupon.statusExpired')
  return t('coupon.statusUnused')
}
const sortedMyCoupons = computed(() =>
  [...myCoupons.value].sort((a, b) => {
    const aExp = daysLeft(a.endTime) <= 3 ? 0 : a.status === '0' ? 1 : 2
    const bExp = daysLeft(b.endTime) <= 3 ? 0 : b.status === '0' ? 1 : 2
    if (aExp !== bExp) return aExp - bExp
    return new Date(a.endTime).getTime() - new Date(b.endTime).getTime()
  })
)
const filteredMyCoupons = computed(() =>
  showExpired.value ? sortedMyCoupons.value : sortedMyCoupons.value.filter(c => c.status === '0' || daysLeft(c.endTime) > 0)
)

async function load() {
  loadError.value = false
  loading.value = true
  try { const r = await request.get('/coupons/available'); availableCoupons.value = r.data || []
    } catch { toast.error(t('coupon.loadFail')); loadError.value = true }
  try { const r = await request.get('/coupons/my'); myCoupons.value = r.data || []
    } catch { toast.error(t('coupon.myLoadFail')) }
  loading.value = false
}

async function claim(couponId: number) {
  claiming.value.add(couponId)
  try {
    await request.post(`/coupons/${couponId}/claim`)
    claimed.value.add(couponId)
    toast.success(t('toast.couponClaimed'))
    setTimeout(() => load(), 600)
  } catch { toast.error(t('coupon.claimFail')) }
  finally { claiming.value.delete(couponId) }
}

async function claimAll() {
  claimingAll.value = true
  let success = 0
  for (const c of availableCoupons.value) {
    try { await request.post(`/coupons/${c.id}/claim`); success++ } catch {}
  }
  if (success > 0) { toast.success(t('coupon.claimSuccess', { n: String(success) })); load() }
  else { toast.error(t('coupon.claimAllFail')) }
  claimingAll.value = false
}

onMounted(load)
</script>

<template>
  <div class="cp-page">
    <h2 class="page-title">{{ t('coupon.center') }}</h2>

    <div class="tab-bar">
      <span class="tab" :class="{ active: activeTab === 'available' }" @click="activeTab = 'available'">{{ t('coupon.available') }}</span>
      <span class="tab" :class="{ active: activeTab === 'mine' }" @click="activeTab = 'mine'">{{ t('coupon.myCoupons') }} ({{ myCoupons.length }})</span>
    </div>

    <div v-if="loading" class="cp-grid">
      <SkeletonBox variant="text" :count="4" height="120px" />
    </div>

    <div v-else-if="loadError" class="error-state">
      <p class="error-icon">🔌</p>
      <p class="error-text">{{ t('coupon.loadFail') }}</p>
      <JdButton size="sm" @click="load()">{{ t('common.retry') }}</JdButton>
    </div>

    <!-- Available -->
    <div v-else-if="activeTab === 'available'">
      <div v-if="availableCoupons.length" class="claim-all-bar">
        <JdButton size="sm" type="outline" :loading="claimingAll" @click="claimAll">{{ t('coupon.claimAll', { n: String(availableCoupons.length) }) }}</JdButton>
      </div>
      <div class="cp-grid">
      <div v-for="c in availableCoupons" :key="c.id" class="coupon-card">
        <div class="coupon-left">
          <span class="coupon-amount">¥{{ (c.amount / 100).toFixed(0) }}</span>
          <span class="coupon-type">{{ c.type === 'full_reduction' ? t('coupon.fullReduction') : (c.discount || '') + t('common.discount') }}</span>
        </div>
        <div class="coupon-right">
          <div>
            <div class="coupon-name">{{ c.name }}</div>
            <div class="coupon-meta">{{ t('coupon.minAmount', { '0': String(((c.threshold || 0) / 100).toFixed(0)) }) }} · {{ c.startTime?.substring(0, 10) }} ~ {{ c.endTime?.substring(0, 10) }}</div>
          </div>
          <JdButton type="outline" size="sm" :disabled="claiming.has(c.id) || claimed.has(c.id)" @click="claim(c.id)">
            <template v-if="claimed.has(c.id)">✓ {{ t('coupon.claimed') }}</template>
            <template v-else-if="claiming.has(c.id)">{{ t('coupon.claiming') }}</template>
            <template v-else>{{ t('coupon.claim') }}</template>
          </JdButton>
        </div>
      </div>
      <div v-if="!availableCoupons.length" class="empty-full"><p class="empty-icon">🎫</p><p>{{ t('coupon.noAvailable') }}</p></div>
      </div>
    </div>

    <!-- My Coupons -->
    <div v-else>
      <div class="expired-toggle">
        <label><input type="checkbox" v-model="showExpired" /> {{ t('coupon.showExpired') }} ({{ myCoupons.filter(c => c.status !== '0' || daysLeft(c.endTime) <= 0).length }})</label>
      </div>
      <div class="cp-grid">
      <div v-for="c in filteredMyCoupons" :key="c.id" class="coupon-card" :class="{ used: c.status !== '0' || daysLeft(c.endTime) <= 0 }">
        <div class="coupon-left" :class="{ used: c.status !== '0' || daysLeft(c.endTime) <= 0 }">
          <span class="coupon-amount">¥{{ (c.amount / 100).toFixed(0) }}</span>
          <span v-if="expiryLabel(c.endTime)" class="coupon-expiry-warn">{{ expiryLabel(c.endTime) }}</span>
          <span class="coupon-status">{{ couponStatus(c) }}</span>
        </div>
        <div class="coupon-right">
          <div class="coupon-name">{{ c.name || t('coupon.name') }}</div>
          <div class="coupon-meta">{{ t('coupon.validUntil') }} {{ c.endTime?.substring(0, 10) || '-' }}</div>
          <div v-if="c.status === '0' && daysLeft(c.endTime) <= 3 && daysLeft(c.endTime) > 0" class="coupon-countdown">
            ⏱ {{ daysLeft(c.endTime) < 1 ? t('coupon.expiringToday') : t('coupon.expiringIn', { n: String(Math.ceil(daysLeft(c.endTime))) }) }}
          </div>
          <div v-if="c.scope" class="coupon-scope">{{ c.scope }}</div>
        </div>
      </div>
      <div v-if="!myCoupons.length" class="empty-full">
        <p class="empty-icon">📭</p><p>{{ t('coupon.noMyCoupons') }}</p>
        <JdButton size="sm" @click="activeTab = 'available'">{{ t('coupon.goClaim') }}</JdButton>
      </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cp-page { max-width: 900px; margin: 0 auto; }
.page-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-lg); }
.tab-bar { display: flex; margin-bottom: var(--space-lg); background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.tab { flex: 1; text-align: center; padding: var(--space-md); cursor: pointer; font-size: var(--font-md); transition: all var(--transition-fast); background: var(--bg-white); color: var(--text-secondary); }
.tab.active { background: var(--jd-red); color: #fff; }

.cp-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; }

.sk-card { background: var(--bg-white); border-radius: var(--radius-md); padding: var(--space-xl); box-shadow: var(--shadow-sm); height: 120px; display: flex; align-items: center; }
.sk-line { background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-sm); }
.sk-line-s { width: 60%; height: 16px; }

.coupon-card { background: linear-gradient(135deg, var(--bg-white), var(--jd-red-light)); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); display: flex; }
.claim-all-bar { text-align: right; margin-bottom: var(--space-md); }

.coupon-card.used { opacity: .5; }
.coupon-left { width: 120px; background: linear-gradient(135deg, var(--jd-red), #ff6b6b); color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: var(--space-xl); text-align: center; flex-shrink: 0; }
.coupon-left.used { background: linear-gradient(135deg, #999, #bbb); }
.coupon-amount { font-size: var(--font-h1); font-weight: 800; }
.coupon-type { font-size: var(--font-xs); opacity: .8; margin-top: var(--space-xs); }
.coupon-status { font-size: 10px; opacity: .8; margin-top: var(--space-xs); }
.coupon-expiry-warn { font-size: 10px; margin-top: 4px; padding: 2px 6px; background: var(--jd-red); color: #fff; border-radius: var(--radius-sm); font-weight: 700; animation: pulse-warn 1.5s ease-in-out infinite; }
@keyframes pulse-warn { 0%, 100% { opacity: 1; } 50% { opacity: .6; } }
.expired-toggle { margin-bottom: var(--space-md); font-size: var(--font-sm); color: var(--text-secondary); }
.expired-toggle input { margin-right: 6px; }
.coupon-right { flex: 1; padding: var(--space-lg); display: flex; flex-direction: column; justify-content: space-between; }
.coupon-name { font-weight: 600; font-size: var(--font-md); margin-bottom: var(--space-xs); color: var(--text-primary); }
.coupon-meta { color: var(--text-tertiary); font-size: var(--font-xs); }
.coupon-countdown { margin-top: 4px; font-size: 11px; color: var(--jd-red); font-weight: 600; animation: pulse-red 1.5s ease-in-out infinite; }
@keyframes pulse-red { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }
.coupon-scope { margin-top: 2px; font-size: 10px; color: var(--text-placeholder); }

.empty-full { grid-column: 1 / -1; text-align: center; padding: 60px; color: var(--text-tertiary); background: var(--bg-white); border-radius: var(--radius-lg); }
.empty-icon { font-size: 48px; margin-bottom: var(--space-md); }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

@media (max-width: 768px) { .cp-grid { grid-template-columns: 1fr; } .cp-page { padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px)); } }
.error-state { text-align: center; padding: 60px var(--space-md); }
.error-icon { font-size: 48px; margin-bottom: var(--space-md); }
.error-text { color: var(--text-secondary); margin-bottom: var(--space-lg); }
</style>
