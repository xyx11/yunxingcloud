<script setup lang="ts">
import { ref } from 'vue'
import request from '@/api/request'
import { useI18n } from '@/locales'
import JdButton from '@/components/JdButton.vue'
import JdBadge from '@/components/JdBadge.vue'

const { t } = useI18n()
const cardNo = ref(''); const card = ref<any>(null); const msg = ref('')
const loading = ref(false)

const statusMap: Record<string, string> = { '0': t('giftCard.statusInactive'), '1': t('giftCard.statusActive'), '2': t('giftCard.statusUsed'), '3': t('giftCard.statusExpired') }
const statusType: Record<string, 'orange' | 'green' | 'gray'> = { '0': 'orange', '1': 'green', '2': 'gray', '3': 'gray' }

async function query() { if(!cardNo.value.trim())return; loading.value=true; try{const r=await request.get('/gift-cards/'+cardNo.value.trim());card.value=r.data;msg.value=''}catch{card.value=null;msg.value=t('giftCard.notFound')}finally{loading.value=false} }
async function activate() { try{loading.value=true;const r=await request.post('/gift-cards/'+cardNo.value.trim()+'/activate');card.value=r.data;msg.value=t('giftCard.activateSuccess')}catch(e:any){msg.value=e.response?.data?.message||t('giftCard.activateFail')}finally{loading.value=false} }
</script>

<template>
  <div class="gc-page">
    <div v-if="card" class="gc-card">
      <div class="gc-card-bg" /><div class="gc-card-bg gc-card-bg--small" />
      <div class="gc-card-content">
        <p class="gc-card-type">GIFT CARD</p>
        <p class="gc-card-amount">¥{{ (card.amount / 100).toFixed(2) }}</p>
        <div class="gc-card-footer">
          <div>
            <p class="gc-card-balance">{{ t('giftCard.balance') }}: ¥{{ ((card.balance || 0) / 100).toFixed(2) }}</p>
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
      <input v-model="cardNo" :placeholder="t('giftCard.placeholder')" class="gc-input" />
      <div class="gc-btns">
        <JdButton type="outline" class="flex-1" :disabled="loading" @click="query">{{ t('giftCard.query') }}</JdButton>
        <JdButton class="flex-1" :loading="loading" @click="activate">{{ loading ? t('giftCard.activating') : t('giftCard.activate') }}</JdButton>
      </div>
      <p v-if="msg" class="gc-msg" :class="{ success: msg.includes('成功') }">{{ msg }}</p>
    </div>
  </div>
</template>

<style scoped>
.gc-page { max-width: 420px; margin: 30px auto; }
.gc-card { background: linear-gradient(135deg, var(--jd-red) 0%, var(--jd-red-dark) 50%, #ff6b6b 100%); color: #fff; border-radius: var(--radius-xl); padding: 28px; margin-bottom: var(--space-xxl); box-shadow: 0 8px 32px rgba(228,57,60,.3); position: relative; overflow: hidden; }
.gc-card-bg { position: absolute; top: -20px; right: -20px; width: 100px; height: 100px; border-radius: 50%; background: rgba(255,255,255,.1); }
.gc-card-bg--small { top: auto; bottom: -30px; right: auto; left: -30px; width: 120px; height: 120px; background: rgba(255,255,255,.08); }
.gc-card-content { position: relative; z-index: 1; }
.gc-card-type { font-size: var(--font-sm); opacity: .8; margin-bottom: var(--space-xs); }
.gc-card-amount { font-size: 32px; font-weight: 800; margin-bottom: var(--space-lg); }
.gc-card-footer { display: flex; justify-content: space-between; align-items: end; }
.gc-card-balance, .gc-card-no { font-size: var(--font-xs); opacity: .7; }

.gc-skeleton { background: linear-gradient(135deg, #e8e8e8, #d0d0d0); border-radius: var(--radius-xl); padding: 28px; margin-bottom: var(--space-xxl); height: 180px; position: relative; overflow: hidden; animation: pulse 1.5s ease-in-out infinite; display: flex; flex-direction: column; gap: 12px; }
.gc-skel-line { background: rgba(255,255,255,.3); border-radius: var(--radius-sm); }
.sk-w40h10 { width: 40%; height: 10px; }
.sk-w60h28 { width: 60%; height: 28px; }
.sk-w30h10 { width: 30%; height: 10px; }
.sk-w18h18 { width: 18%; height: 18px; }
.gc-skel-row { display: flex; justify-content: space-between; }

.gc-form { background: var(--bg-white); border-radius: var(--radius-lg); padding: 28px; box-shadow: var(--shadow-sm); }
.gc-title { font-size: var(--font-lg); font-weight: 700; margin-bottom: var(--space-lg); text-align: center; }
.gc-input { width: 100%; padding: var(--space-md); border: 2px solid var(--jd-red); border-radius: var(--radius-md); font-size: 15px; text-align: center; box-sizing: border-box; margin-bottom: var(--space-md); background: var(--bg-white); color: var(--text-primary); }
.gc-btns { display: flex; gap: var(--space-sm); }
.gc-msg { text-align: center; margin-top: var(--space-md); font-size: var(--font-base); color: var(--jd-red); }
.gc-msg.success { color: var(--green); }

.flex-1 { flex: 1; }

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .6; } }
</style>
