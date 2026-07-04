<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const router = useRouter()
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
  try { await request.post(`/coupons/${couponId}/claim`); toast.success(t('toast.couponClaimed')) } catch { toast.error(t('toast.addCartFail')) }
  finally { claiming.value.delete(couponId) }
}

const discountLabel = (c: any) => c.type === 'full_reduction'
  ? t('coupon.fullReduction', {0: (c.threshold/100).toFixed(0), 1: (c.amount/100).toFixed(0)})
  : t('coupon.discountLabel', {0: c.discount})

const statusLabel = (s: string) => s === '1' ? t('coupon.statusUsed') : s === '0' ? t('coupon.statusUnused') : t('coupon.statusExpired')

onMounted(load)
</script>

<template>
  <div style="max-width:900px;margin:0 auto">
    <h2 style="font-size:20px;font-weight:700;margin-bottom:16px">{{ t('coupon.center') }}</h2>
    <div style="display:flex;gap:0;margin-bottom:16px;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <span @click="activeTab='available'"
            style="flex:1;text-align:center;padding:12px;cursor:pointer;font-size:14px;transition:all .2s"
            :style="{background:activeTab==='available'?'#f10215':'#fff',color:activeTab==='available'?'#fff':'#666'}">{{ t('coupon.available') }}</span>
      <span @click="activeTab='mine'"
            style="flex:1;text-align:center;padding:12px;cursor:pointer;font-size:14px;transition:all .2s"
            :style="{background:activeTab==='mine'?'#f10215':'#fff',color:activeTab==='mine'?'#fff':'#666'}">{{ t('coupon.myCoupons') }} ({{ myCoupons.length }})</span>
    </div>
    <div v-if="loading" style="display:grid;grid-template-columns:repeat(2,1fr);gap:14px">
      <div v-for="i in 4" :key="i" style="background:#fff;border-radius:10px;padding:20px;box-shadow:0 2px 8px rgba(0,0,0,.06);height:120px">
        <div style="height:16px;width:60%;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:4px;margin-bottom:12px"></div>
      </div>
    </div>
    <div v-else-if="activeTab==='available'" style="display:grid;grid-template-columns:repeat(2,1fr);gap:14px">
      <div v-for="c in availableCoupons" :key="c.id"
           style="background:linear-gradient(135deg,#fff,#fff5f5);border-radius:10px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.06);display:flex;cursor:default">
        <div style="width:120px;background:linear-gradient(135deg,#f10215,#ff6b6b);color:#fff;display:flex;flex-direction:column;align-items:center;justify-content:center;padding:20px;text-align:center;flex-shrink:0">
          <span style="font-size:28px;font-weight:800">¥{{ (c.amount/100).toFixed(0) }}</span>
          <span style="font-size:11px;opacity:.8;margin-top:4px">{{ discountLabel(c) }}</span>
        </div>
        <div style="flex:1;padding:16px;display:flex;flex-direction:column;justify-content:space-between">
          <div>
            <div style="font-weight:600;font-size:14px;margin-bottom:4px">{{ c.name }}</div>
            <div style="color:#999;font-size:11px">{{ t('coupon.minAmount', {0: ((c.threshold||0)/100).toFixed(0)}) }} · {{ c.startTime?.substring(0,10) }} ~ {{ c.endTime?.substring(0,10) }}</div>
          </div>
          <button @click="claim(c.id)" :disabled="claiming.has(c.id)"
                  style="align-self:flex-end;padding:4px 16px;border:1px solid #f10215;color:#f10215;background:#fff;border-radius:14px;cursor:pointer;font-size:12px;transition:all .2s;margin-top:8px"
                  :style="claiming.has(c.id)?{opacity:'.5'}:{}"
                  @mouseenter="(e:any) => { if(!claiming.has(c.id)) { e.target.style.background='#f10215'; e.target.style.color='#fff' } }"
                  @mouseleave="(e:any) => { if(!claiming.has(c.id)) { e.target.style.background='#fff'; e.target.style.color='#f10215' } }">
            {{ claiming.has(c.id) ? t('coupon.claiming') : t('coupon.claim') }}
          </button>
        </div>
      </div>
      <div v-if="!availableCoupons.length" style="grid-column:1/-1;text-align:center;padding:60px;color:#999;background:#fff;border-radius:12px">
        <p style="font-size:48px;margin-bottom:12px">🎫</p><p>{{ t('coupon.noAvailable') }}</p>
      </div>
    </div>
    <div v-else-if="activeTab==='mine'" style="display:grid;grid-template-columns:repeat(2,1fr);gap:14px">
      <div v-for="c in myCoupons" :key="c.id"
           style="background:#fff;border-radius:10px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.06);display:flex"
           :style="{opacity: c.status==='1' ? '.5' : '1'}">
        <div style="width:120px;background:linear-gradient(135deg,#999,#bbb);color:#fff;display:flex;flex-direction:column;align-items:center;justify-content:center;padding:20px;text-align:center;flex-shrink:0"
             :style="{background: c.status==='0' ? 'linear-gradient(135deg,#f10215,#ff6b6b)' : 'linear-gradient(135deg,#999,#bbb)'}">
          <span style="font-size:28px;font-weight:800">¥{{ (c.amount/100).toFixed(0) }}</span>
          <span style="font-size:10px;opacity:.8;margin-top:4px">{{ statusLabel(c.status) }}</span>
        </div>
        <div style="flex:1;padding:16px">
          <div style="font-weight:600;font-size:14px;margin-bottom:4px">{{ c.name || t('coupon.defaultName') }}</div>
          <div style="color:#999;font-size:11px">{{ t('coupon.validUntil') }} {{ c.endTime?.substring(0,10) || '-' }}</div>
        </div>
      </div>
      <div v-if="!myCoupons.length" style="grid-column:1/-1;text-align:center;padding:60px;color:#999;background:#fff;border-radius:12px">
        <p style="font-size:48px;margin-bottom:12px">📭</p><p>{{ t('coupon.noMyCoupons') }}</p>
        <button @click="activeTab='available'" style="margin-top:12px;padding:8px 24px;background:#f10215;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:13px">{{ t('coupon.goClaim') }}</button>
      </div>
    </div>
  </div>
</template>
<style scoped>@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }</style>
