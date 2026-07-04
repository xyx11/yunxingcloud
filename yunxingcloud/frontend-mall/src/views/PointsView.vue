<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { useI18n } from '@/locales'

const { t } = useI18n()
const account = ref<any>(null)
const records = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  try { const r = await request.get('/points/account'); account.value = r.data } catch {}
  try { const r = await request.get('/points/records'); records.value = r.data || [] } catch {}
  finally { loading.value = false }
})
</script>
<template>
  <div style="max-width:600px;margin:0 auto">
    <div style="background:linear-gradient(135deg,#ff9800,#ffc107);color:#fff;border-radius:16px;padding:36px;margin-bottom:24px;text-align:center;box-shadow:0 8px 32px rgba(255,152,0,.25);position:relative;overflow:hidden">
      <div style="position:absolute;top:-30px;right:-30px;width:120px;height:120px;border-radius:50%;background:rgba(255,255,255,.1)"></div>
      <div style="position:absolute;bottom:-20px;left:-20px;width:80px;height:80px;border-radius:50%;background:rgba(255,255,255,.08)"></div>
      <div style="position:relative;z-index:1">
        <p style="font-size:13px;opacity:.85;margin-bottom:4px">⭐ {{ t('points.available') }}</p>
        <p style="font-size:56px;font-weight:800;margin:8px 0">{{ account?.balance || 0 }}</p>
        <div style="display:flex;justify-content:center;gap:24px;margin-top:12px;font-size:13px;opacity:.8">
          <span>{{ t('points.totalEarned') }} <b>{{ account?.totalEarned || 0 }}</b></span>
          <span>{{ t('points.totalSpent') }} <b>{{ account?.totalSpent || 0 }}</b></span>
        </div>
      </div>
    </div>
    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin-bottom:24px">
      <div v-for="a in [{icon:'🛒',label:t('points.shopReward'),desc:t('points.shopDesc')},{icon:'✍️',label:t('points.reviewReward'),desc:t('points.reviewDesc')},{icon:'🎁',label:t('points.checkinReward'),desc:t('points.checkinDesc')}]" :key="a.label"
           style="background:#fff;border-radius:10px;padding:16px;text-align:center;box-shadow:0 1px 4px rgba(0,0,0,.04);cursor:pointer;transition:transform .2s"
           @mouseenter="(e:any) => e.target.style.transform='translateY(-2px)'" @mouseleave="(e:any) => e.target.style.transform=''">
        <div style="font-size:28px;margin-bottom:6px">{{ a.icon }}</div>
        <div style="font-size:13px;font-weight:600;margin-bottom:2px">{{ a.label }}</div>
        <div style="font-size:11px;color:#999">{{ a.desc }}</div>
      </div>
    </div>
    <div style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <h3 style="font-size:16px;font-weight:600;margin-bottom:16px">{{ t('points.detail') }}</h3>
      <div v-if="loading" style="display:flex;flex-direction:column;gap:12px">
        <div v-for="i in 4" :key="i" style="height:40px;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:4px"></div>
      </div>
      <div v-else-if="records.length">
        <div v-for="r in records.slice(0, 20)" :key="r.id" style="display:flex;justify-content:space-between;align-items:center;padding:12px 0;border-bottom:1px solid #f5f5f5">
          <div style="flex:1">
            <div style="font-size:14px;font-weight:500">{{ r.remark || ({earn:t('points.typeShop'),purchase:t('points.typeExchange'),redeem:t('points.typeExchange'),expire:t('points.typeRefund'),refund:t('points.typeRefund')} as any)[r.type] || r.type }}</div>
            <div style="color:#999;font-size:12px">{{ r.createdAt?.substring(0,10) }}</div>
          </div>
          <span style="font-size:18px;font-weight:700" :style="{color:r.amount>0?'#4caf50':'#999'}">{{ r.amount > 0 ? '+' : '' }}{{ r.amount }}</span>
        </div>
      </div>
      <div v-else style="text-align:center;color:#999;padding:20px">{{ t('points.noRecords') }}</div>
    </div>
  </div>
</template>
<style scoped>@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }</style>