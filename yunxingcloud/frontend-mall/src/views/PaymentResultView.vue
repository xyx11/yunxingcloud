<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderById } from '@/api/order'
import { useI18n } from '@/locales'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const orderNo = ref('')
const orderId = ref('')
const status = ref<'success' | 'fail'>('success')
const orderInfo = ref<any>(null)

onMounted(async () => {
  orderId.value = (route.params.id as string) || ''
  status.value = (route.query.status as string) === 'fail' ? 'fail' : 'success'
  if (orderId.value) {
    try {
      const r = await getOrderById(Number(orderId.value))
      orderInfo.value = r.data.order
      orderNo.value = orderInfo.value?.orderNo || ''
    } catch {}
  }
})
</script>

<template>
  <div style="max-width:500px;margin:40px auto;text-align:center">
    <div style="background:#fff;border-radius:16px;padding:48px 40px;box-shadow:0 2px 12px rgba(0,0,0,.06)">
      <template v-if="status === 'success'">
        <div style="width:80px;height:80px;border-radius:50%;background:#d4edda;color:#28a745;display:flex;align-items:center;justify-content:center;font-size:40px;margin:0 auto 20px;animation:popIn .4s ease-out">✓</div>
        <h2 style="font-size:22px;color:#155724;margin-bottom:8px">{{ t('paymentResult.success') }}</h2>
        <p style="color:#666;font-size:14px;margin-bottom:24px">{{ t('paymentResult.successDesc') }}</p>
        <div v-if="orderNo" style="background:#f5f5f5;border-radius:8px;padding:16px;margin-bottom:24px;text-align:left">
          <div style="display:flex;justify-content:space-between;margin-bottom:8px"><span style="color:#999">{{ t('paymentResult.orderNo') }}</span><span style="font-weight:600">{{ orderNo }}</span></div>
          <div v-if="orderInfo" style="display:flex;justify-content:space-between"><span style="color:#999">{{ t('paymentResult.amount') }}</span><span style="color:#f10215;font-weight:700;font-size:18px">¥{{ (orderInfo.totalAmount/100).toFixed(2) }}</span></div>
        </div>
        <div style="display:flex;gap:12px">
          <button @click="router.push(`/order/${orderId}`)" style="flex:1;height:44px;border:1px solid #ddd;background:#fff;border-radius:8px;cursor:pointer;font-size:14px">{{ t('paymentResult.viewOrder') }}</button>
          <button @click="router.push('/')" style="flex:1;height:44px;background:#f10215;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:14px;font-weight:600">{{ t('paymentResult.continueShopping') }}</button>
        </div>
      </template>
      <template v-else>
        <div style="width:80px;height:80px;border-radius:50%;background:#f8d7da;color:#dc3545;display:flex;align-items:center;justify-content:center;font-size:40px;margin:0 auto 20px">✕</div>
        <h2 style="font-size:22px;color:#721c24;margin-bottom:8px">{{ t('paymentResult.failed') }}</h2>
        <p style="color:#666;font-size:14px;margin-bottom:24px">{{ t('paymentResult.failedDesc') }}</p>
        <div style="display:flex;gap:12px">
          <button @click="router.back()" style="flex:1;height:44px;border:1px solid #ddd;background:#fff;border-radius:8px;cursor:pointer;font-size:14px">{{ t('paymentResult.retryPayment') }}</button>
          <button @click="router.push('/')" style="flex:1;height:44px;background:#f10215;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:14px;font-weight:600">{{ t('paymentResult.backHome') }}</button>
        </div>
      </template>
    </div>
  </div>
</template>
<style scoped>
@keyframes popIn { 0% { transform: scale(0); opacity: 0; } 70% { transform: scale(1.2); } 100% { transform: scale(1); opacity: 1; } }
</style>
