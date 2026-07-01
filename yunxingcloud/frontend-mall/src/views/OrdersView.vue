<script setup lang="ts">
import { ref, onMounted, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getOrders, cancelOrder } from '@/api/order'
import { useI18n } from '@/locales'

const router = useRouter()
const { t } = useI18n()
const toast = inject<any>('toast')
const orders = ref<any[]>([])
const loading = ref(false)
const activeTab = ref('all')

const statusLabels = computed(() => ({
  '0': { label: t('order.statusPending'), color: '#856404', bg: '#fff3cd' },
  '1': { label: t('order.statusPaid'), color: '#155724', bg: '#d4edda' },
  '2': { label: t('order.statusShipped'), color: '#004085', bg: '#cce5ff' },
  '3': { label: t('order.statusDone'), color: '#155724', bg: '#d4edda' },
  '4': { label: t('order.statusCanceled'), color: '#383d41', bg: '#e2e3e5' },
}))

const filteredOrders = () => activeTab.value === 'all' ? orders.value : orders.value.filter(o => o.status === activeTab.value)

const tabs = [
  { key: 'all', label: t('common.all') },
  { key: '0', label: t('order.statusPending') },
  { key: '1', label: t('order.statusPaid') },
  { key: '2', label: t('order.statusShipped') },
  { key: '3', label: t('order.statusDone') },
]

const canceling = ref<Set<number>>(new Set())

async function load() { loading.value = true; try { const r = await getOrders(); orders.value = r.data || [] } catch {} finally { loading.value = false } }
function pay(id: number) { router.push(`/pay/${id}`) }
async function cancel(id: number) { if (!confirm(t('order.cancelOrder') + '?')) return; if (canceling.value.has(id)) return; canceling.value.add(id); try { await cancelOrder(id); toast.info(t('toast.orderCanceled')); load() } catch {} finally { canceling.value.delete(id) } }
function goDetail(id: number) { router.push(`/order/${id}`) }
onMounted(load)
</script>

<template>
  <div>
    <h2 style="font-size:20px;font-weight:700;margin-bottom:16px">{{ t('order.myOrders') }}</h2>
    <div style="display:flex;gap:0;margin-bottom:16px;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <span v-for="tab in tabs" :key="tab.key" @click="activeTab=tab.key"
            style="flex:1;text-align:center;padding:12px;cursor:pointer;font-size:14px;transition:all .2s"
            :style="{background:activeTab===tab.key?'#f10215':'#fff',color:activeTab===tab.key?'#fff':'#666'}">{{ tab.label }}</span>
    </div>
    <div v-if="loading" style="display:flex;flex-direction:column;gap:12px">
      <div v-for="i in 3" :key="i" style="background:#fff;border-radius:12px;padding:20px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
        <div style="height:18px;width:50%;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:4px;margin-bottom:12px"></div>
      </div>
    </div>
    <div v-else-if="filteredOrders().length">
      <div v-for="o in filteredOrders()" :key="o.id"
           style="background:#fff;border-radius:12px;padding:20px;margin-bottom:12px;box-shadow:0 2px 8px rgba(0,0,0,.06);cursor:pointer"
           @click="goDetail(o.id)">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px">
          <div><span style="color:#999;font-size:13px">{{ t('order.orderNo') }}：</span><span style="font-weight:600">{{ o.orderNo }}</span><span style="color:#999;font-size:12px;margin-left:12px">{{ o.createdAt?.substring(0,10) }}</span></div>
          <span style="padding:2px 10px;border-radius:12px;font-size:12px;font-weight:600"
                :style="{background:statusLabels[o.status]?.bg,color:statusLabels[o.status]?.color}">{{ statusLabels[o.status]?.label || o.status }}</span>
        </div>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div><span v-if="o.receiverName" style="color:#666;font-size:13px">{{ o.receiverName }} {{ o.receiverAddress?.substring(0,20) }}...</span></div>
          <span style="font-size:20px;color:#f10215;font-weight:700">¥{{ (o.totalAmount / 100).toFixed(2) }}</span>
        </div>
        <div v-if="o.status==='0'" style="margin-top:12px;display:flex;justify-content:flex-end;gap:8px" @click.stop>
          <button @click="cancel(o.id)" :disabled="canceling.has(o.id)" style="padding:4px 16px;border:1px solid #ddd;background:#fff;border-radius:4px;cursor:pointer;font-size:12px" :style="{opacity:canceling.has(o.id)?'.5':'1'}">{{ canceling.has(o.id) ? '取消中...' : t('order.cancelOrder') }}</button>
          <button @click="pay(o.id)" style="padding:4px 16px;background:#f10215;color:#fff;border:none;border-radius:4px;cursor:pointer;font-size:12px;font-weight:600">{{ t('order.toPay') }}</button>
        </div>
      </div>
    </div>
    <div v-else style="background:#fff;border-radius:12px;padding:60px;text-align:center;color:#999;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <p style="font-size:48px;margin-bottom:16px">📋</p><p style="font-size:16px">{{ t('common.noOrders') }}</p>
      <button @click="router.push('/')" style="margin-top:16px;padding:8px 24px;background:#f10215;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:13px">{{ t('common.goShopping') }}</button>
    </div>
  </div>
</template>
<style scoped>@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }</style>