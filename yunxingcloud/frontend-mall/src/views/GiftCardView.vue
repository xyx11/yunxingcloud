<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyGiftCards, queryGiftCard, activateGiftCard, getGiftCardHistory, purchaseGiftCard } from '@/api/giftcard'
import { formatPrice } from '@/utils/format'
import { useI18n } from '@/locales'
import JdButton from '@/components/JdButton.vue'
import JdModal from '@/components/JdModal.vue'
import JdBadge from '@/components/JdBadge.vue'
import JdEmpty from '@/components/JdEmpty.vue'

const { t } = useI18n()

interface GiftCard { id: number; cardNo: string; amount: number; balance: number; status: string; activatedAt?: string; expiredAt?: string }
interface GiftCardHistory { id: number; amount: number; type: string; remark: string; createdAt: string }

const tab = ref<'my' | 'activate'>('my')

// My cards
const myCards = ref<GiftCard[]>([])
const myCardsLoading = ref(true)

// Activate/query
const cardNo = ref('')
const card = ref<{ amount: number; balance: number; status: string; cardNo?: string } | null>(null)
const msg = ref('')
const msgSuccess = ref(false)
const loading = ref(false)

// History
const history = ref<GiftCardHistory[]>([])
const historyLoading = ref(false)

const statusMap: Record<string, string> = {
  '0': t('giftCard.statusInactive'), '1': t('giftCard.statusActive'),
  '2': t('giftCard.statusUsed'), '3': t('giftCard.statusExpired'),
}
const statusType: Record<string, 'orange' | 'green' | 'gray'> = {
  '0': 'orange', '1': 'green', '2': 'gray', '3': 'gray',
}

// Purchase modal
const showPurchase = ref(false)
const purchaseAmount = ref(20000) // 默认200元
const purchaseLoading = ref(false)
const purchaseMsg = ref('')
const purchaseSuccess = ref(false)
const amountOptions = [5000, 10000, 20000, 50000, 100000] // 分

async function doPurchase() {
  purchaseLoading.value = true; purchaseMsg.value = ''; purchaseSuccess.value = false
  try {
    await purchaseGiftCard(purchaseAmount.value)
    purchaseSuccess.value = true
    purchaseMsg.value = t('giftCard.purchaseSuccess') || '购买成功'
    loadMyCards()
    setTimeout(() => { showPurchase.value = false; purchaseSuccess.value = false; purchaseMsg.value = '' }, 2000)
  } catch (e: any) {
    purchaseMsg.value = e.response?.data?.message || t('giftCard.purchaseFail') || '购买失败'
  }
  finally { purchaseLoading.value = false }
}

onMounted(() => { loadMyCards() })

async function loadMyCards() {
  myCardsLoading.value = true
  try { const r = await getMyGiftCards(); myCards.value = r.data || [] }
  catch { /* silent */ }
  finally { myCardsLoading.value = false }
}

async function loadHistory(cardId: number) {
  historyLoading.value = true
  try { const r = await getGiftCardHistory(cardId); history.value = r.data || [] }
  catch { history.value = [] }
  finally { historyLoading.value = false }
}

async function query() {
  if (!cardNo.value.trim()) return
  loading.value = true; msgSuccess.value = false
  try { const r = await queryGiftCard(cardNo.value.trim()); card.value = r.data; msg.value = ''; loading.value = false;
    } catch { card.value = null; msg.value = t('giftCard.notFound') }
}

async function activate() {
  if (!cardNo.value.trim()) return
  loading.value = true
  try {
    const r = await activateGiftCard(cardNo.value.trim())
    card.value = r.data; msg.value = t('giftCard.activateSuccess'); msgSuccess.value = true
    loadMyCards()
  } catch (e: any) {
    msg.value = e.response?.data?.message || t('giftCard.activateFail')
    msgSuccess.value = false
    loading.value = false; 
    }
}

function selectCard(c: GiftCard) {
  card.value = { amount: c.amount, balance: c.balance, status: c.status, cardNo: c.cardNo }
  cardNo.value = c.cardNo
  loadHistory(c.id)
}
</script>

<template>
  <div class="gc-page">
    <!-- Tabs -->
    <div class="gc-tabs">
      <button class="gc-tab" :class="{ active: tab === 'my' }" @click="tab = 'my'">
        💳 {{ t('giftCard.myCards') }}
      </button>
      <button class="gc-tab" :class="{ active: tab === 'activate' }" @click="tab = 'activate'">
        🎁 {{ t('giftCard.activateQuery') }}
      </button>
    </div>

    <!-- My Cards tab -->
    <template v-if="tab === 'my'">
      <div v-if="myCardsLoading" class="gc-skel-list">
        <div v-for="i in 2" :key="i" class="gc-skel-card" />
      </div>

      <div v-else-if="myCards.length" class="gc-my-list">
        <div
          v-for="c in myCards" :key="c.id"
          class="gc-card gc-card--clickable"
          :class="{ 'gc-card--selected': cardNo === c.cardNo }"
          @click="selectCard(c)"
        >
          <div class="gc-card-bg" /><div class="gc-card-bg gc-card-bg--small" />
          <div class="gc-card-content">
            <p class="gc-card-type">GIFT CARD</p>
            <p class="gc-card-amount">{{ formatPrice(c.amount / 100, 2) }}</p>
            <div class="gc-card-footer">
              <div>
                <p class="gc-card-balance">{{ t('giftCard.balance') }}: {{ formatPrice((c.balance || 0) / 100, 2) }}</p>
                <p class="gc-card-no">****{{ c.cardNo.slice(-4) }}</p>
              </div>
              <JdBadge :type="statusType[c.status] || 'gray'">{{ statusMap[c.status] || '-' }}</JdBadge>
            </div>
          </div>
        </div>

        <!-- History for selected card -->
        <div v-if="history.length" class="gc-history">
          <h4 class="gc-history-title">{{ t('giftCard.usageHistory') }}</h4>
          <div v-for="h in history" :key="h.id" class="gc-history-row">
            <span class="gc-history-desc">{{ h.remark || h.type }}</span>
            <span class="gc-history-amount" :class="{ plus: h.type === 'RECHARGE' }">
              {{ h.type === 'RECHARGE' ? '+' : '-' }}{{ formatPrice(h.amount / 100, 2) }}
            </span>
            <span class="gc-history-time">{{ String(h.createdAt || '').substring(0, 10) }}</span>
          </div>
        </div>
      </div>

      <JdEmpty v-else icon="💳" :title="t('giftCard.noCards')">
        <JdButton @click="tab = 'activate'">{{ t('giftCard.activateQuery') }}</JdButton>
      </JdEmpty>
    </template>

    <!-- Activate/Query tab -->
    <template v-else>
      <!-- Card display (when queried/activated) -->
      <div v-if="card" class="gc-card">
        <div class="gc-card-bg" /><div class="gc-card-bg gc-card-bg--small" />
        <div class="gc-card-content">
          <p class="gc-card-type">GIFT CARD</p>
          <p class="gc-card-amount">{{ formatPrice(card.amount / 100, 2) }}</p>
          <div class="gc-card-footer">
            <div>
              <p class="gc-card-balance">{{ t('giftCard.balance') }}: {{ formatPrice((card.balance || 0) / 100, 2) }}</p>
              <p class="gc-card-no">{{ t('giftCard.cardNo') }}: {{ cardNo }}</p>
            </div>
            <JdBadge :type="statusType[card.status] || 'gray'">{{ statusMap[card.status] || '-' }}</JdBadge>
          </div>
        </div>
      </div>

      <div v-else-if="loading" class="gc-skeleton">
        <div class="gc-skel-line sk-w40h10" />
        <div class="gc-skel-line sk-w60h28" />
        <div class="gc-skel-row">
          <div class="gc-skel-line sk-w30h10" />
          <div class="gc-skel-line sk-w18h18" />
        </div>
      </div>

      <div class="gc-form">
        <h2 class="gc-title">💳 {{ t('giftCard.title') }}</h2>
        <input
          v-model="cardNo"
          :placeholder="t('giftCard.placeholder')"
          class="gc-input"
          @keyup.enter="query"
        />
        <div class="gc-btns">
          <JdButton type="outline" class="flex-1" :disabled="loading" @click="query">
            {{ t('giftCard.query') }}
          </JdButton>
          <JdButton class="flex-1" :loading="loading" @click="activate">
            {{ loading ? t('giftCard.activating') : t('giftCard.activate') }}
          </JdButton>
        </div>
        <p v-if="msg" class="gc-msg" :class="{ success: msgSuccess }">{{ msg }}</p>
      </div>

      <!-- Card designs -->
      <div class="gc-designs">
        <h4 class="gc-designs-title">{{ t('giftCard.chooseDesign') }}</h4>
        <div class="gc-designs-grid">
          <div class="gc-design-card active" style="background:linear-gradient(135deg,#f10215,#ff6b6b)"><span>🧧</span><span>{{ t('giftCard.designClassic') }}</span></div>
          <div class="gc-design-card" style="background:linear-gradient(135deg,#667eea,#764ba2)"><span>💎</span><span>{{ t('giftCard.designPurple') }}</span></div>
          <div class="gc-design-card" style="background:linear-gradient(135deg,#f093fb,#f5576c)"><span>🌸</span><span>{{ t('giftCard.designPink') }}</span></div>
          <div class="gc-design-card" style="background:linear-gradient(135deg,#4facfe,#00f2fe)"><span>🌊</span><span>{{ t('giftCard.designBlue') }}</span></div>
        </div>
      </div>

      <!-- Buy gift card hint -->
      <div class="gc-buy-hint">
        <p>🎁 {{ t('giftCard.buyHint') }}</p>
        <JdButton size="sm" type="outline" @click="showPurchase = true">{{ t('giftCard.buyNow') }}</JdButton>
      </div>

      <!-- Purchase Modal -->
      <JdModal v-model:visible="showPurchase" :title="'🎁 ' + t('giftCard.buyNow')" width="380px">
        <div class="gc-amount-grid">
          <button
            v-for="a in amountOptions" :key="a"
            class="gc-amount-btn"
            :class="{ active: purchaseAmount === a }"
            @click="purchaseAmount = a"
          >{{ formatPrice(a / 100, 0) }}</button>
        </div>
        <p v-if="purchaseMsg" class="gc-msg" :class="{ success: purchaseSuccess }">{{ purchaseMsg }}</p>
        <template #footer>
          <JdButton type="ghost" @click="showPurchase = false">{{ t('common.cancel') }}</JdButton>
          <JdButton :loading="purchaseLoading" @click="doPurchase">{{ purchaseLoading ? '...' : t('giftCard.buyNow') }}</JdButton>
        </template>
      </JdModal>
    </template>
  </div>
</template>

<style scoped>
.gc-page { max-width: 420px; margin: 30px auto; }

/* Tabs */
.gc-tabs { display: flex; margin-bottom: var(--space-xl); background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.gc-tab { flex: 1; padding: 12px; border: none; background: var(--bg-white); cursor: pointer; font-size: var(--font-md); font-weight: 600; color: var(--text-secondary); transition: all var(--transition-fast); }
.gc-tab.active { background: var(--jd-red); color: #fff; }
.gc-tab:not(.active):hover { background: var(--bg-hover); }

/* My cards */
.gc-skel-list { display: flex; flex-direction: column; gap: var(--space-md); }
.gc-skel-card { height: 130px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-xl); }

.gc-my-list { display: flex; flex-direction: column; gap: var(--space-md); }

/* Card design */
.gc-card {
  background: linear-gradient(135deg, var(--jd-red) 0%, var(--jd-red-dark) 50%, #ff6b6b 100%);
  color: #fff; border-radius: var(--radius-xl); padding: 28px;
  box-shadow: 0 8px 32px rgba(228,57,60,.3); position: relative; overflow: hidden;
}
.gc-card--clickable { cursor: pointer; transition: transform var(--transition), box-shadow var(--transition); }
.gc-card--clickable:hover { transform: translateY(-2px); box-shadow: 0 12px 36px rgba(228,57,60,.4); }
.gc-card--selected { outline: 3px solid #ffc107; outline-offset: 2px; }
.gc-card-bg { position: absolute; top: -20px; right: -20px; width: 100px; height: 100px; border-radius: 50%; background: rgba(255,255,255,.1); }
.gc-card-bg--small { top: auto; bottom: -30px; right: auto; left: -30px; width: 120px; height: 120px; background: rgba(255,255,255,.08); }
.gc-card-content { position: relative; z-index: 1; }
.gc-card-type { font-size: var(--font-sm); opacity: .8; margin-bottom: var(--space-xs); }
.gc-card-amount { font-size: 32px; font-weight: 800; margin-bottom: var(--space-lg); }
.gc-card-footer { display: flex; justify-content: space-between; align-items: end; }
.gc-card-balance, .gc-card-no { font-size: var(--font-xs); opacity: .7; }

/* History */
.gc-history { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-lg); box-shadow: var(--shadow-sm); }
.gc-history-title { font-size: var(--font-md); font-weight: 600; margin-bottom: var(--space-md); }
.gc-history-row { display: flex; align-items: center; gap: var(--space-sm); padding: var(--space-sm) 0; border-bottom: 1px solid var(--border-light); font-size: var(--font-sm); }
.gc-history-desc { flex: 1; color: var(--text-secondary); }
.gc-history-amount { font-weight: 600; color: var(--jd-red); }
.gc-history-amount.plus { color: var(--green); }
.gc-history-time { color: var(--text-placeholder); font-size: var(--font-xs); width: 80px; text-align: right; }

/* Skeleton */
.gc-skeleton {
  background: linear-gradient(135deg, #e8e8e8, #d0d0d0); border-radius: var(--radius-xl);
  padding: 28px; margin-bottom: var(--space-xxl); height: 180px; position: relative;
  overflow: hidden; animation: pulse 1.5s ease-in-out infinite;
  display: flex; flex-direction: column; gap: 12px;
}
.gc-skel-line { background: rgba(255,255,255,.3); border-radius: var(--radius-sm); }
.sk-w40h10 { width: 40%; height: 10px; }
.sk-w60h28 { width: 60%; height: 28px; }
.sk-w30h10 { width: 30%; height: 10px; }
.sk-w18h18 { width: 18%; height: 18px; }
.gc-skel-row { display: flex; justify-content: space-between; }

/* Form */
.gc-form { background: var(--bg-white); border-radius: var(--radius-lg); padding: 28px; box-shadow: var(--shadow-sm); }
.gc-title { font-size: var(--font-lg); font-weight: 700; margin-bottom: var(--space-lg); text-align: center; }
.gc-input { width: 100%; padding: var(--space-md); border: 2px solid var(--jd-red); border-radius: var(--radius-md); font-size: 15px; text-align: center; box-sizing: border-box; margin-bottom: var(--space-md); background: var(--bg-white); color: var(--text-primary); outline: none; transition: box-shadow var(--transition-fast); }
.gc-input:focus { box-shadow: 0 0 0 3px var(--jd-red-light); }
.gc-btns { display: flex; gap: var(--space-sm); }
.gc-msg { text-align: center; margin-top: var(--space-md); font-size: var(--font-base); color: var(--jd-red); }
.gc-msg.success { color: var(--green); }

/* Card designs */
.gc-designs { margin-bottom: var(--space-lg); }
.gc-designs-title { font-size: var(--font-md); font-weight: 600; margin-bottom: var(--space-md); }
.gc-designs-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-sm); }
.gc-design-card { border-radius: var(--radius-md); padding: var(--space-lg); color: #fff; text-align: center; cursor: pointer; display: flex; flex-direction: column; align-items: center; gap: 6px; font-weight: 600; font-size: var(--font-sm); transition: transform var(--transition-fast), box-shadow var(--transition-fast); border: 2px solid transparent; }
.gc-design-card.active { border-color: #ffc107; box-shadow: 0 0 0 3px rgba(255,193,7,.3); transform: scale(1.05); }
.gc-design-card span:first-child { font-size: 24px; }

.gc-buy-hint {
  background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-lg);
  margin-top: var(--space-lg); text-align: center; box-shadow: var(--shadow-sm);
  display: flex; align-items: center; justify-content: space-between; gap: var(--space-md);
}
.gc-buy-hint p { font-size: var(--font-md); color: var(--text-secondary); }

.flex-1 { flex: 1; }

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .6; } }

/* Purchase Modal (hosted inside JdModal) */
.gc-amount-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-sm); margin-bottom: var(--space-lg); }
.gc-amount-btn { padding: var(--space-md); border: 2px solid var(--border); border-radius: var(--radius-md); background: var(--bg-white); font-size: var(--font-lg); font-weight: 700; cursor: pointer; color: var(--text-primary); transition: all var(--transition-fast); }
.gc-amount-btn.active { border-color: var(--jd-red); background: var(--jd-red-light); color: var(--jd-red); }

@media (max-width: 768px) {
  .gc-page { padding: 0 var(--space-md) 80px; }
  .gc-card { padding: 20px; }
  .gc-card-amount { font-size: 24px; }
  .gc-form { padding: 20px; }
  .gc-btns { flex-direction: column; }
  .gc-buy-hint { flex-direction: column; text-align: center; }
}
</style>