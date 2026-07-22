<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '@/api/request'
import { useI18n } from '@/locales'
import { useToast } from '@/composables/useToast'
import { formatRelativeTime } from '@/utils/format'
import JdEmpty from '@/components/JdEmpty.vue'
import JdButton from '@/components/JdButton.vue'

const { t } = useI18n()
const toast = useToast()

interface PointsAccount { balance: number; totalEarned: number; totalSpent: number }
interface PointsRecord { id: number; amount: number; type: string; remark?: string; createdAt: string }
interface ExchangeItem { id: number; name: string; points: number; value: number; stock: number; type: string }

const account = ref<PointsAccount | null>(null)
const records = ref<PointsRecord[]>([])
const exchangeItems = ref<ExchangeItem[]>([])
const loading = ref(true)
const checkingIn = ref(false)
const checkedInToday = ref(false)
const exchanging = ref<number | null>(null)

// Pagination
const recordPage = ref(1)
const recordTotal = ref(0)
const loadingMore = ref(false)
const PAGE_SIZE = 10

onMounted(async () => {
  await Promise.all([loadAccount(), loadRecords(), loadExchanges()])
  loading.value = false
})

async function loadAccount() {
  try { const r = await request.get('/points/account'); account.value = r.data; checkedInToday.value = r.data?.checkedInToday || false } catch { toast.error(t('toast.pointsLoadFail')) }
}

async function loadRecords(page = 1) {
  try {
    const r = await request.get('/points/records', { params: { page, size: PAGE_SIZE } })
    const data = r.data || {}
    if (page === 1) records.value = data.list || data.records || data || []
    else records.value = [...records.value, ...(data.list || data.records || [])]
    recordTotal.value = data.total || 0
    recordPage.value = page
  } catch { if (page === 1) toast.error(t('toast.pointsLoadFail')) }
}

async function loadExchanges() {
  try { const r = await request.get('/points/exchanges'); exchangeItems.value = r.data || [] } catch { /* silent */ }
}

async function checkin() {
  if (checkingIn.value || checkedInToday.value) return
  checkingIn.value = true
  try {
    const r = await request.post('/points/checkin')
    account.value = { ...account.value!, balance: r.data?.balance ?? account.value!.balance + 5, totalEarned: (account.value?.totalEarned || 0) + 5 }
    checkedInToday.value = true
    toast.success(t('toast.checkinSuccess'))
    loadRecords(1)
  } catch (e: any) { toast.error(e.response?.data?.message || t('toast.checkinFail')) }
  finally { checkingIn.value = false }
}

async function doExchange(item: ExchangeItem) {
  exchanging.value = item.id
  try {
    const r = await request.post(`/points/exchange/${item.id}`)
    account.value!.balance = r.data?.balance ?? account.value!.balance - item.points
    toast.success(t('points.exchangeSuccess', { name: item.name }))
    loadRecords(1)
  } catch (e: any) { toast.error(e.response?.data?.message || t('points.exchangeFail')) }
  finally { exchanging.value = null }
}

function loadMore() {
  loadingMore.value = true
  loadRecords(recordPage.value + 1).finally(() => { loadingMore.value = false })
}

const hasMore = computed(() => records.value.length < recordTotal.value)

const typeLabel: Record<string, string> = {
  SHOP: t('points.typeShop'), REVIEW: t('points.typeReview'),
  CHECKIN: t('points.typeCheckin'), EXCHANGE: t('points.typeExchange'), REFUND: t('points.typeRefund'),
}
</script>

<template>
  <div class="pts-page">
    <!-- Hero -->
    <div class="pts-hero">
      <div class="pts-hero-bg" /><div class="pts-hero-bg pts-hero-bg--small" />
      <div class="pts-hero-content">
        <p class="pts-label">⭐ {{ t('points.available') }}</p>
        <p class="pts-balance">{{ account?.balance || 0 }}</p>
        <div class="pts-stats">
          <span>{{ t('points.totalEarned') }} <b>{{ account?.totalEarned || 0 }}</b></span>
          <span>{{ t('points.totalSpent') }} <b>{{ account?.totalSpent || 0 }}</b></span>
        </div>
      </div>
    </div>

    <!-- Action cards -->
    <div class="pts-actions">
      <div class="pts-action" :class="{ done: checkedInToday }" @click="checkin">
        <div class="pts-action-icon">🎁</div>
        <div class="pts-action-label">{{ checkedInToday ? t('points.checkinDone') : t('points.checkinReward') }}</div>
        <div class="pts-action-desc">{{ checkedInToday ? t('points.checkinTomorrow') : t('points.checkinDesc') }}</div>
      </div>
      <div class="pts-action">
        <div class="pts-action-icon">🛒</div>
        <div class="pts-action-label">{{ t('points.shopReward') }}</div>
        <div class="pts-action-desc">{{ t('points.shopDesc') }}</div>
      </div>
      <div class="pts-action">
        <div class="pts-action-icon">✍️</div>
        <div class="pts-action-label">{{ t('points.reviewReward') }}</div>
        <div class="pts-action-desc">{{ t('points.reviewDesc') }}</div>
      </div>
    </div>

    <!-- Points exchange section -->
    <div v-if="exchangeItems.length" class="pts-card">
      <h3 class="pts-title">🎫 {{ t('points.exchangeTitle') }}</h3>
      <div class="pts-exchange-grid">
        <div v-for="item in exchangeItems" :key="item.id" class="pts-exchange-item">
          <div class="pts-exchange-info">
            <div class="pts-exchange-name">{{ item.name }}</div>
            <div class="pts-exchange-cost">{{ item.points }}{{ t('points.pointsUnit') }}</div>
            <div v-if="item.type === 'coupon'" class="pts-exchange-value">{{ t('points.valueLabel') }}{{ (item.value / 100).toFixed(0) }}</div>
          </div>
          <JdButton
            size="sm"
            :disabled="(account?.balance || 0) < item.points || item.stock <= 0"
            :loading="exchanging === item.id"
            @click="doExchange(item)"
          >
            {{ item.stock <= 0 ? t('points.exchangeSoldOut') : t('points.exchange') }}
          </JdButton>
        </div>
      </div>
    </div>

    <!-- Earning rules -->
    <div class="pts-card">
      <h3 class="pts-title">📋 {{ t('points.ruleTitle') }}</h3>
      <div class="pts-rules-grid">
        <div class="pts-rule-item">
          <span class="pts-rule-icon">🛒</span>
          <div><strong>{{ t('points.rule1Title') }}</strong><p>{{ t('points.rule1Desc') }}</p></div>
        </div>
        <div class="pts-rule-item">
          <span class="pts-rule-icon">✍️</span>
          <div><strong>{{ t('points.rule2Title') }}</strong><p>{{ t('points.rule2Desc') }}</p></div>
        </div>
        <div class="pts-rule-item">
          <span class="pts-rule-icon">📅</span>
          <div><strong>{{ t('points.rule3Title') }}</strong><p>{{ t('points.rule3Desc') }}</p></div>
        </div>
        <div class="pts-rule-item">
          <span class="pts-rule-icon">🎉</span>
          <div><strong>{{ t('points.rule4Title') }}</strong><p>{{ t('points.rule4Desc') }}</p></div>
        </div>
      </div>
    </div>

    <!-- Redeem info -->
    <div class="pts-card pts-redeem-card">
      <h3 class="pts-title">💡 {{ t('points.redeemInfoTitle') }}</h3>
      <p class="pts-redeem-text">{{ t('points.redeemInfoText') }}</p>
      <p class="pts-redeem-rate">{{ t('points.redeemInfoRate') }}</p>
    </div>

    <!-- Points history -->
    <div class="pts-card">
      <h3 class="pts-title">{{ t('points.detail') }}</h3>
      <div v-if="loading" class="pts-skel"><div v-for="i in 4" :key="i" class="sk-line" /></div>
      <div v-else-if="records.length">
        <div v-for="r in records" :key="r.id" class="pts-row">
          <div class="pts-row-info">
            <div class="pts-row-desc">{{ r.remark || typeLabel[r.type] || r.type }}</div>
            <div class="pts-row-date">{{ formatRelativeTime(r.createdAt) }}</div>
          </div>
          <span class="pts-row-amount" :class="{ plus: r.amount > 0 }">{{ r.amount > 0 ? '+' : '' }}{{ r.amount }}</span>
        </div>
        <!-- Load more -->
        <div v-if="hasMore" class="pts-load-more">
          <JdButton type="outline" size="sm" :loading="loadingMore" @click="loadMore">
            {{ loadingMore ? t('common.loading') : t('common.loadMore') }}
          </JdButton>
        </div>
      </div>
      <JdEmpty v-else icon="📋" :title="t('points.noRecords')" />
    </div>
  </div>
</template>

<style scoped>
.pts-page { max-width: 600px; margin: 0 auto; }
.pts-hero { background: linear-gradient(135deg, var(--orange), #ffc107); color: #fff; border-radius: var(--radius-xl); padding: 36px; margin-bottom: var(--space-xxl); text-align: center; box-shadow: 0 8px 32px rgba(255,152,0,.25); position: relative; overflow: hidden; }
.pts-hero-bg { position: absolute; top: -30px; right: -30px; width: 120px; height: 120px; border-radius: 50%; background: rgba(255,255,255,.1); }
.pts-hero-bg--small { top: auto; bottom: -20px; right: auto; left: -20px; width: 80px; height: 80px; background: rgba(255,255,255,.08); }
.pts-hero-content { position: relative; z-index: 1; }
.pts-label { font-size: var(--font-base); opacity: .85; margin-bottom: var(--space-xs); }
.pts-balance { font-size: 56px; font-weight: 800; margin: var(--space-sm) 0; }
.pts-stats { display: flex; justify-content: center; gap: var(--space-xxl); margin-top: var(--space-md); font-size: var(--font-base); opacity: .8; }

.pts-actions { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-md); margin-bottom: var(--space-xxl); }
.pts-action { background: var(--bg-white); border-radius: var(--radius-md); padding: var(--space-lg); text-align: center; box-shadow: var(--shadow-sm); cursor: pointer; transition: transform var(--transition); }
.pts-action:hover { transform: translateY(-2px); }
.pts-action.done { opacity: .6; cursor: default; }
.pts-action-icon { font-size: var(--font-h1); margin-bottom: 6px; }
.pts-action-label { font-size: var(--font-base); font-weight: 600; margin-bottom: 2px; }
.pts-action-desc { font-size: var(--font-xs); color: var(--text-tertiary); }

.pts-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-sm); margin-bottom: var(--space-lg); }
.pts-title { font-size: var(--font-lg); font-weight: 600; margin-bottom: var(--space-lg); }

/* Exchange */
.pts-exchange-grid { display: flex; flex-direction: column; gap: var(--space-sm); }
.pts-exchange-item { display: flex; justify-content: space-between; align-items: center; padding: var(--space-md); border: 1px solid var(--border-light); border-radius: var(--radius-md); transition: border-color var(--transition-fast); }
.pts-exchange-item:hover { border-color: var(--orange); }
.pts-exchange-name { font-weight: 600; font-size: var(--font-md); }
.pts-exchange-cost { color: var(--orange); font-weight: 700; font-size: var(--font-sm); }
.pts-exchange-value { color: var(--text-tertiary); font-size: var(--font-xs); margin-top: 2px; }

.pts-redeem-card { margin-top: var(--space-lg); }
.pts-redeem-text { font-size: var(--font-md); color: var(--text-secondary); line-height: 1.6; margin-bottom: var(--space-sm); }
.pts-redeem-rate { font-size: var(--font-md); color: var(--jd-red); font-weight: 600; }

/* Earning rules */
.pts-rules-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-md); }
.pts-rule-item { display: flex; align-items: flex-start; gap: var(--space-md); padding: var(--space-md); background: var(--bg-hover); border-radius: var(--radius-md); }
.pts-rule-icon { font-size: 28px; flex-shrink: 0; }
.pts-rule-item strong { font-size: var(--font-sm); color: var(--text-primary); }
.pts-rule-item p { font-size: var(--font-xs); color: var(--text-tertiary); margin-top: 2px; }

.pts-skel { display: flex; flex-direction: column; gap: var(--space-md); }
.sk-line { height: 40px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-sm); }
.pts-row { display: flex; justify-content: space-between; align-items: center; padding: var(--space-md) 0; border-bottom: 1px solid var(--border-light); }
.pts-row-info { flex: 1; }
.pts-row-desc { font-size: var(--font-md); font-weight: 500; }
.pts-row-date { color: var(--text-tertiary); font-size: var(--font-sm); }
.pts-row-amount { font-size: var(--font-lg); font-weight: 700; color: var(--text-tertiary); }
.pts-row-amount.plus { color: var(--green); }

.pts-load-more { text-align: center; padding-top: var(--space-lg); }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

@media (max-width: 768px) {
  .pts-page { padding: 0 var(--space-md) calc(80px + env(safe-area-inset-bottom, 0px)); }
  .pts-hero { padding: 24px; }
  .pts-balance { font-size: 40px; }
  .pts-stats { gap: var(--space-lg); font-size: 13px; }
  .pts-actions { grid-template-columns: repeat(3, 1fr); gap: var(--space-sm); }
  .pts-action { padding: var(--space-md); }
  .pts-action-icon { font-size: var(--font-xxl); }
  .pts-action-label { font-size: 12px; }
  .pts-action-desc { font-size: 11px; }
  .pts-card { padding: var(--space-lg); }
}
</style>