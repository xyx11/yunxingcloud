<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getOrders, cancelOrder } from '@/api/order'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdButton from '@/components/JdButton.vue'
import JdBadge from '@/components/JdBadge.vue'

const router = useRouter()
const { t } = useI18n()
const toast = inject(ToastInjectionKey)!
const orders = ref<any[]>([])
const loading = ref(false)
const activeTab = ref('all')
const canceling = ref<Set<number>>(new Set())

const statusBadge: Record<string, { label: string; type: 'orange' | 'green' | 'blue' | 'gray' }> = {
  '0': { label: t('order.statusPending'), type: 'orange' },
  '1': { label: t('order.statusPaid'), type: 'green' },
  '2': { label: t('order.statusShipped'), type: 'blue' },
  '3': { label: t('order.statusDone'), type: 'green' },
  '4': { label: t('order.statusCanceled'), type: 'gray' },
}

const tabs = [
  { key: 'all', label: t('common.all') },
  { key: '0', label: t('order.statusPending') },
  { key: '1', label: t('order.statusPaid') },
  { key: '2', label: t('order.statusShipped') },
  { key: '3', label: t('order.statusDone') },
]

const filteredOrders = () => activeTab.value === 'all' ? orders.value : orders.value.filter(o => o.status === activeTab.value)

async function load() { loading.value = true; try { const r = await getOrders(); orders.value = r.data || [] } catch {} finally { loading.value = false } }
function pay(id: number) { router.push(`/pay/${id}`) }
async function cancel(id: number) { if (!confirm(t('order.cancelOrder') + '?')) return; if (canceling.value.has(id)) return; canceling.value.add(id); try { await cancelOrder(id); toast.info(t('toast.orderCanceled')); load() } catch {} finally { canceling.value.delete(id) } }
function goDetail(id: number) { router.push(`/order/${id}`) }
onMounted(load)
</script>

<template>
  <div>
    <h2 class="page-title">{{ t('order.myOrders') }}</h2>

    <div class="tab-bar">
      <span v-for="tab in tabs" :key="tab.key" class="tab" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">{{ tab.label }}</span>
    </div>

    <div v-if="loading" class="orders-list">
      <div v-for="i in 3" :key="i" class="sk-card">
        <SkeletonBox height="18px" width="50%" :count="1" />
        <SkeletonBox height="14px" width="30%" :count="1" style="margin-top:8px" />
      </div>
    </div>

    <div v-else-if="filteredOrders().length" class="orders-list">
      <div v-for="o in filteredOrders()" :key="o.id" class="order-card" @click="goDetail(o.id)">
        <div class="order-header">
          <div>
            <span class="order-no-label">{{ t('order.orderNo') }}：</span>
            <span class="order-no">{{ o.orderNo }}</span>
            <span class="order-date">{{ o.createdAt?.substring(0, 10) }}</span>
          </div>
          <JdBadge :type="statusBadge[o.status]?.type || 'gray'">{{ statusBadge[o.status]?.label || o.status }}</JdBadge>
        </div>
        <div class="order-body">
          <div>
            <span v-if="o.receiverName" class="order-receiver">{{ o.receiverName }} {{ o.receiverAddress?.substring(0, 20) }}...</span>
          </div>
          <span class="order-amount">¥{{ (o.totalAmount / 100).toFixed(2) }}</span>
        </div>
        <div v-if="o.status === '0'" class="order-actions" @click.stop>
          <JdButton type="ghost" size="sm" :disabled="canceling.has(o.id)" @click="cancel(o.id)">{{ canceling.has(o.id) ? '取消中...' : t('order.cancelOrder') }}</JdButton>
          <JdButton size="sm" @click="pay(o.id)">{{ t('order.toPay') }}</JdButton>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <p class="empty-icon">📋</p><p class="empty-text">{{ t('common.noOrders') }}</p>
      <JdButton @click="router.push('/')">{{ t('common.goShopping') }}</JdButton>
    </div>
  </div>
</template>

<style scoped>
.page-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-lg); }

.tab-bar { display: flex; margin-bottom: var(--space-lg); background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.tab { flex: 1; text-align: center; padding: var(--space-md); cursor: pointer; font-size: var(--font-md); transition: all var(--transition-fast); background: var(--bg-white); color: var(--text-secondary); }
.tab.active { background: var(--jd-red); color: #fff; }

.orders-list { display: flex; flex-direction: column; gap: var(--space-md); }
.sk-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); box-shadow: var(--shadow-sm); }

.order-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); box-shadow: var(--shadow-sm); cursor: pointer; transition: box-shadow var(--transition); }
.order-card:hover { box-shadow: var(--shadow-md); }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-md); }
.order-no-label { color: var(--text-tertiary); font-size: var(--font-base); }
.order-no { font-weight: 600; }
.order-date { color: var(--text-tertiary); font-size: var(--font-sm); margin-left: var(--space-md); }
.order-body { display: flex; justify-content: space-between; align-items: center; }
.order-receiver { color: var(--text-secondary); font-size: var(--font-base); }
.order-amount { font-size: var(--font-xl); color: var(--jd-red); font-weight: 700; }
.order-actions { margin-top: var(--space-md); display: flex; justify-content: flex-end; gap: var(--space-sm); }

.empty-state { background: var(--bg-white); border-radius: var(--radius-lg); padding: 60px; text-align: center; color: var(--text-tertiary); box-shadow: var(--shadow-sm); }
.empty-icon { font-size: 48px; margin-bottom: var(--space-lg); }
.empty-text { font-size: var(--font-lg); margin-bottom: var(--space-lg); }
</style>
