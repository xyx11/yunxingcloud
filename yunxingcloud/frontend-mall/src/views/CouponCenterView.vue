<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import JdButton from '@/components/JdButton.vue'

const toast = useToast()
const { t } = useI18n()
const availableCoupons = ref<any[]>([])
const myCoupons = ref<any[]>([])
const activeTab = ref<'available' | 'mine'>('available')
const loading = ref(true)
const claiming = ref<Set<number>>(new Set())

async function load() {
  loading.value = true
  try { const r = await request.get('/coupons/available'); availableCoupons.value = r.data || [] } catch {}
  try { const r = await request.get('/coupons/my'); myCoupons.value = r.data || [] } catch {}
  finally { loading.value = false }
}

async function claim(couponId: number) {
  claiming.value.add(couponId)
  try { await request.post(`/coupons/${couponId}/claim`); toast.success(t('toast.couponClaimed')) } catch { toast.error('领取失败') }
  finally { claiming.value.delete(couponId) }
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
      <div v-for="i in 4" :key="i" class="sk-card"><div class="sk-line" style="width:60%;height:16px" /></div>
    </div>

    <!-- Available -->
    <div v-else-if="activeTab === 'available'" class="cp-grid">
      <div v-for="c in availableCoupons" :key="c.id" class="coupon-card">
        <div class="coupon-left">
          <span class="coupon-amount">¥{{ (c.amount / 100).toFixed(0) }}</span>
          <span class="coupon-type">{{ c.type === 'full_reduction' ? '满减' : c.discount + '折' }}</span>
        </div>
        <div class="coupon-right">
          <div>
            <div class="coupon-name">{{ c.name }}</div>
            <div class="coupon-meta">满{{ ((c.threshold || 0) / 100).toFixed(0) }}可用 · {{ c.startTime?.substring(0, 10) }} ~ {{ c.endTime?.substring(0, 10) }}</div>
          </div>
          <JdButton type="outline" size="sm" :disabled="claiming.has(c.id)" @click="claim(c.id)">
            {{ claiming.has(c.id) ? '领取中...' : t('coupon.claim') }}
          </JdButton>
        </div>
      </div>
      <div v-if="!availableCoupons.length" class="empty-full"><p class="empty-icon">🎫</p><p>{{ t('coupon.noAvailable') }}</p></div>
    </div>

    <!-- My Coupons -->
    <div v-else class="cp-grid">
      <div v-for="c in myCoupons" :key="c.id" class="coupon-card" :class="{ used: c.status === '1' }">
        <div class="coupon-left" :class="{ used: c.status !== '0' }">
          <span class="coupon-amount">¥{{ (c.amount / 100).toFixed(0) }}</span>
          <span class="coupon-status">{{ c.status === '1' ? '已使用' : c.status === '0' ? '未使用' : '已过期' }}</span>
        </div>
        <div class="coupon-right">
          <div class="coupon-name">{{ c.name || '优惠券' }}</div>
          <div class="coupon-meta">有效期至 {{ c.endTime?.substring(0, 10) || '-' }}</div>
        </div>
      </div>
      <div v-if="!myCoupons.length" class="empty-full">
        <p class="empty-icon">📭</p><p>{{ t('coupon.noMyCoupons') }}</p>
        <JdButton size="sm" @click="activeTab = 'available'">{{ t('coupon.goClaim') }}</JdButton>
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

.coupon-card { background: linear-gradient(135deg, var(--bg-white), var(--jd-red-light)); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); display: flex; }
.coupon-card.used { opacity: .5; }
.coupon-left { width: 120px; background: linear-gradient(135deg, var(--jd-red), #ff6b6b); color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: var(--space-xl); text-align: center; flex-shrink: 0; }
.coupon-left.used { background: linear-gradient(135deg, #999, #bbb); }
.coupon-amount { font-size: var(--font-h1); font-weight: 800; }
.coupon-type { font-size: var(--font-xs); opacity: .8; margin-top: var(--space-xs); }
.coupon-status { font-size: 10px; opacity: .8; margin-top: var(--space-xs); }
.coupon-right { flex: 1; padding: var(--space-lg); display: flex; flex-direction: column; justify-content: space-between; }
.coupon-name { font-weight: 600; font-size: var(--font-md); margin-bottom: var(--space-xs); color: var(--text-primary); }
.coupon-meta { color: var(--text-tertiary); font-size: var(--font-xs); }

.empty-full { grid-column: 1 / -1; text-align: center; padding: 60px; color: var(--text-tertiary); background: var(--bg-white); border-radius: var(--radius-lg); }
.empty-icon { font-size: 48px; margin-bottom: var(--space-md); }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

@media (max-width: 768px) { .cp-grid { grid-template-columns: 1fr; } }
</style>
