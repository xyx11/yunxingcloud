<script setup lang="ts">
import { ref } from 'vue'
import request from '@/api/request'
import { useI18n } from '@/locales'

const { t } = useI18n()
const cardNo = ref(''); const card = ref<any>(null); const msg = ref('')
const loading = ref(false)

const statusMap: Record<string, string> = { '0': t('giftCard.statusInactive'), '1': t('giftCard.statusActive'), '2': t('giftCard.statusUsed'), '3': t('giftCard.statusExpired') }
const statusColor: Record<string, string> = { '0': '#ff9800', '1': '#4caf50', '2': '#999', '3': '#f44336' }

async function query() { if(!cardNo.value.trim())return; loading.value=true; try{const r=await request.get('/gift-cards/'+cardNo.value.trim());card.value=r.data;msg.value=''}catch{card.value=null;msg.value=t('giftCard.notFound')}finally{loading.value=false} }
async function activate() { try{loading.value=true;const r=await request.post('/gift-cards/'+cardNo.value.trim()+'/activate');card.value=r.data;msg.value=t('giftCard.activateSuccess')}catch(e:any){msg.value=e.response?.data?.message||t('giftCard.activateFail')}finally{loading.value=false} }
</script>
<template>
  <div style="max-width:420px;margin:30px auto">
    <div v-if="card" style="background:linear-gradient(135deg,#f10215 0%,#d4000f 50%,#ff6b6b 100%);color:#fff;border-radius:16px;padding:28px;margin-bottom:24px;box-shadow:0 8px 32px rgba(228,57,60,.3);position:relative;overflow:hidden">
      <div style="position:absolute;top:-20px;right:-20px;width:100px;height:100px;border-radius:50%;background:rgba(255,255,255,.1)"></div>
      <div style="position:absolute;bottom:-30px;left:-30px;width:120px;height:120px;border-radius:50%;background:rgba(255,255,255,.08)"></div>
      <div style="position:relative;z-index:1">
        <p style="font-size:12px;opacity:.8;margin-bottom:4px">GIFT CARD</p>
        <p style="font-size:32px;font-weight:800;margin-bottom:16px">¥{{ (card.amount/100).toFixed(2) }}</p>
        <div style="display:flex;justify-content:space-between;align-items:end">
          <div>
            <p style="font-size:11px;opacity:.7">{{ t('giftCard.balance') }}: ¥{{ ((card.balance||0)/100).toFixed(2) }}</p>
            <p style="font-size:11px;opacity:.7">{{ t('giftCard.cardNo') }}: {{ cardNo }}</p>
          </div>
          <span style="padding:4px 10px;background:rgba(255,255,255,.2);border-radius:10px;font-size:11px" :style="{color:statusColor[card.status]}">{{ statusMap[card.status] || '-' }}</span>
        </div>
      </div>
    </div>
    <div v-else-if="loading" style="background:linear-gradient(135deg,#e8e8e8,#d0d0d0);border-radius:16px;padding:28px;margin-bottom:24px;height:180px;position:relative;overflow:hidden;animation:pulse 1.5s ease-in-out infinite">
      <div style="position:absolute;top:-20px;right:-20px;width:100px;height:100px;border-radius:50%;background:rgba(255,255,255,.15)"></div>
      <div style="position:absolute;bottom:-30px;left:-30px;width:120px;height:120px;border-radius:50%;background:rgba(255,255,255,.1)"></div>
      <div style="position:relative;z-index:1">
        <div style="width:40%;height:10px;background:rgba(255,255,255,.3);border-radius:4px;margin-bottom:12px"></div>
        <div style="width:60%;height:28px;background:rgba(255,255,255,.3);border-radius:6px;margin-bottom:16px"></div>
        <div style="display:flex;justify-content:space-between">
          <div style="width:30%;height:10px;background:rgba(255,255,255,.3);border-radius:4px"></div>
          <div style="width:18%;height:18px;background:rgba(255,255,255,.2);border-radius:10px"></div>
        </div>
      </div>
    </div>
    <div style="background:#fff;border-radius:12px;padding:28px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <h2 style="font-size:18px;font-weight:700;margin-bottom:16px;text-align:center">💳 {{ t('giftCard.title') }}</h2>
      <input v-model="cardNo" :placeholder="t('giftCard.placeholder')" style="width:100%;padding:12px;border:2px solid #f10215;border-radius:8px;font-size:15px;text-align:center;box-sizing:border-box;margin-bottom:12px" />
      <div style="display:flex;gap:8px">
        <button @click="query" :disabled="loading" style="flex:1;padding:10px;background:#fff;border:1px solid #f10215;color:#f10215;border-radius:8px;cursor:pointer;font-weight:600;font-size:14px">{{ t('giftCard.query') }}</button>
        <button @click="activate" :disabled="loading" style="flex:1;padding:10px;background:#f10215;color:#fff;border:none;border-radius:8px;cursor:pointer;font-weight:600;font-size:14px">{{ loading ? t('giftCard.activating') : t('giftCard.activate') }}</button>
      </div>
      <p v-if="msg" style="text-align:center;margin-top:12px;font-size:13px" :style="{color:msg.includes('成功')?'#4caf50':'#f10215'}">{{ msg }}</p>
    </div>
  </div>
</template>