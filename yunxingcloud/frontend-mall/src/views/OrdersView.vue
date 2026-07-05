<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getOrders, cancelOrder } from '@/api/order'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'
import { formatPrice, formatRelativeTime } from '@/utils/format'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdButton from '@/components/JdButton.vue'
import JdBadge from '@/components/JdBadge.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

const router = useRouter()
const { t } = useI18n()
const toast = inject(ToastInjectionKey)!
const orders = ref<any[]>([])
const loading = ref(false)
const activeTab = ref('all')
const canceling = ref<Set<number>>(new Set())
const confirmShow = ref(false)
const confirmId = ref(0)
const loadError = ref(false)

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

async function load() { loading.value = true; loadError.value = false; try { const r = await getOrders(); orders.value = r.data || [] } catch { loadError.value = true } finally { loading.value = false } }
function pay(id: number) { router.push(`/pay/${id}`) }
function askCancel(id: number) { confirmId.value = id; confirmShow.value = true }
async function doCancel() { if (canceling.value.has(confirmId.value)) return; canceling.value.add(confirmId.value); try { await cancelOrder(confirmId.value); toast.info(t('toast.orderCanceled')); load() } catch {} finally { canceling.value.delete(confirmId.value); confirmShow.value = false } }
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
        <div class="sk-spacer"><SkeletonBox height="14px" width="30%" :count="1" /></div>
      </div>
    </div>

    <div v-else-if="filteredOrders().length" class="orders-list">
      <div v-for="o in filteredOrders()" :key="o.id" class="order-card" @click="goDetail(o.id)">
        <div class="order-header">
          <div>
            <span class="order-no-label">{{ t('order.orderNo') }}：</span>
            <span class="order-no">{{ o.orderNo }}</span>
            <span class="order-date">{{ formatRelativeTime(o.createdAt) }}</span>
          </div>
          <JdBadge :type="statusBadge[o.status]?.type || 'gray'">{{ statusBadge[o.status]?.label || o.status }}</JdBadge>
        </div>
        <div class="order-body">
          <div>
            <span v-if="o.receiverName" class="order-receiver">{{ o.receiverName }} {{ o.receiverAddress?.substring(0, 20) }}...</span>
          </div>
          <span class="order-amount">{{ formatPrice(o.totalAmount / 100, 2) }}</span>
        </div>
        <div v-if="o.status === '0'" class="order-actions" @click.stop>
          <JdButton type="ghost" size="sm" :disabled="canceling.has(o.id)" @click="askCancel(o.id)">{{ canceling.has(o.id) ? t('orderDetail.processing') : t('order.cancelOrder') }}</JdButton>
          <JdButton size="sm" @click="pay(o.id)">{{ t('order.toPay') }}</JdButton>
        </div>
      </div>
    </div>

    <div v-else-if="loadError" class="error-state">
      <JdEmpty icon="🔌" title="加载失败" description="请检查网络后重试">
        <JdButton @click="load">重新加载</JdButton>
      </JdEmpty>
    </div>

    <JdEmpty v-else icon="📋" :title="t('common.noOrders')">
      <JdButton @click="router.push('/')">{{ t('common.goShopping') }}</JdButton>
    </JdEmpty>

    <ConfirmDialog :show="confirmShow" title="确认取消" :message="t('order.cancelOrder') + '?'" @confirm="doCancel" @cancel="confirmShow = false" />
  </div>
</template>

<style scoped>
.page-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-lg); }

.tab-bar { display: flex; margin-bottom: var(--space-lg); background: var(--bg-white); border-radius: var(--radius-md); overflow-x: auto; box-shadow: var(--shadow-sm); -webkit-overflow-scrolling: touch; }
.tab { flex: 1; text-align: center; padding: var(--space-md); cursor: pointer; font-size: var(--font-md); transition: all var(--transition-fast); background: var(--bg-white); color: var(--text-secondary); white-space: nowrap; }
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

.sk-spacer { margin-top: var(--space-sm); }
.error-state { padding: 60px var(--space-xl); }
</style>
