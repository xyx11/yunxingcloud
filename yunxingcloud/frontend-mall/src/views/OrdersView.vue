<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrders, cancelOrder } from '@/api/order'
import { useI18n } from '@/locales'
import { useToast } from '@/composables/useToast'
import { formatPrice, formatRelativeTime } from '@/utils/format'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdButton from '@/components/JdButton.vue'
import JdBadge from '@/components/JdBadge.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

const router = useRouter()
const { t } = useI18n()
const toast = useToast()
const orders = ref<any[]>([])
const loading = ref(false)
const activeTab = ref('all')
const page = ref(0)
const totalPages = ref(1)
const canceling = ref<Set<number>>(new Set())
const confirmShow = ref(false)
const confirmId = ref(0)
const loadError = ref(false)
const searchQuery = ref('')
const refreshing = ref(false)

const statusBadge: Record<string, { label: string; type: 'orange' | 'green' | 'blue' | 'gray' }> = {
  '0': { label: t('order.statusPending'), type: 'orange' },
  '1': { label: t('order.statusPaid'), type: 'green' },
  '2': { label: t('order.statusShipped'), type: 'blue' },
  '3': { label: t('order.statusDone'), type: 'green' },
  '4': { label: t('order.statusCanceled'), type: 'gray' },
}

const statusCounts = computed(() => {
  const cnt: Record<string, number> = {}
  orders.value.forEach(o => { cnt[o.status] = (cnt[o.status] || 0) + 1 })
  return cnt
})

const tabs = [
  { key: 'all', label: t('common.all'), count: orders.value.length },
  { key: '0', label: t('order.statusPending'), count: statusCounts.value['0'] || 0 },
  { key: '1', label: t('order.statusPaid'), count: statusCounts.value['1'] || 0 },
  { key: '2', label: t('order.statusShipped'), count: statusCounts.value['2'] || 0 },
  { key: '3', label: t('order.statusDone'), count: statusCounts.value['3'] || 0 },
]

const filteredOrders = computed(() => {
  let list = activeTab.value === 'all' ? orders.value : orders.value.filter(o => o.status === activeTab.value)
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(o => o.orderNo?.toLowerCase().includes(q))
  }
  return list
})

function statusTabLabel(tab: typeof tabs[0]) {
  return tab.count > 0 ? `${tab.label}(${tab.count})` : tab.label
}

async function load() {
  loading.value = true; loadError.value = false
  try {
    const r = await getOrders({ page: page.value, size: 10 })
    const data = r.data
    orders.value = data.content || data || []
    totalPages.value = data.totalPages || 1
    loading.value = false;
    } catch { loadError.value = true; loading.value = false }
}

async function onRefresh() {
  refreshing.value = true; page.value = 0
  await load()
}

function goPage(p: number) { page.value = p; load(); window.scrollTo({ top: 0, behavior: 'smooth' }) }
function pay(id: number) { router.push(`/pay/${id}`) }
function askCancel(id: number) { confirmId.value = id; confirmShow.value = true }
async function doCancel() { if (canceling.value.has(confirmId.value)) return; canceling.value.add(confirmId.value); try { await cancelOrder(confirmId.value); toast.info(t('toast.orderCanceled')); load() } catch { toast.error(t('toast.updateFailed')) } finally { canceling.value.delete(confirmId.value); confirmShow.value = false } }
function goDetail(id: number) { router.push(`/order/${id}`) }
function copyOrderNo(no: string) { navigator.clipboard.writeText(no).then(() => toast.success(t('toast.orderNoCopied'))).catch(() => {}) }
onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">{{ t('order.myOrders') }}</h2>
      <button class="refresh-btn" :class="{ spinning: refreshing }" :disabled="refreshing" @click="onRefresh" aria-label="刷新">🔄</button>
    </div>

    <!-- Search -->
    <div class="search-wrap">
      <input v-model="searchQuery" placeholder="搜索订单号..." class="search-input" />
      <span v-if="searchQuery" class="search-clear" @click="searchQuery = ''">✕</span>
    </div>

    <!-- Status tabs -->
    <div class="tab-bar">
      <span v-for="tab in tabs" :key="tab.key" class="tab" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">{{ statusTabLabel(tab) }}</span>
    </div>

    <div v-if="loading" class="orders-list">
      <div v-for="i in 3" :key="i" class="sk-card">
        <SkeletonBox height="18px" width="50%" :count="1" />
        <div class="sk-spacer"><SkeletonBox height="14px" width="30%" :count="1" /></div>
      </div>
    </div>

    <div v-else-if="filteredOrders.length" class="orders-list">
      <div v-for="o in filteredOrders" :key="o.id" class="order-card" :class="'order-status-' + o.status" role="button" tabindex="0" @click="goDetail(o.id)" @keydown.enter.prevent="goDetail(o.id)" @keydown.space.prevent="goDetail(o.id)">
        <div class="order-header">
          <div>
            <span class="order-no-label">{{ t('order.orderNo') }}：</span>
            <span class="order-no">{{ o.orderNo }}</span>
            <span class="order-copy" @click.stop="copyOrderNo(o.orderNo)" title="复制">📋</span>
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

    <div v-if="!loading && totalPages > 1" class="pagination">
      <button :disabled="page === 0" @click="goPage(page - 1)">上一页</button>
      <span v-for="p in totalPages" :key="p" class="page-num" :class="{ active: p - 1 === page }" @click="goPage(p - 1)">{{ p }}</span>
      <button :disabled="page >= totalPages - 1" @click="goPage(page + 1)">下一页</button>
    </div>

    <div v-else-if="loadError" class="error-state">
      <JdEmpty icon="🔌" :title="t('search.retryHint')" :description="t('search.retryHint')">
        <JdButton @click="load">{{ t('common.retry') }}</JdButton>
      </JdEmpty>
    </div>

    <JdEmpty v-else icon="📋" :title="t('common.noOrders')">
      <JdButton @click="router.push('/')">{{ t('common.goShopping') }}</JdButton>
    </JdEmpty>

    <ConfirmDialog :show="confirmShow" title="确认取消" :message="t('order.cancelOrder') + '?'" @confirm="doCancel" @cancel="confirmShow = false" />
  </div>
</template>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.page-title { font-size: var(--font-xl); font-weight: 700; }
.refresh-btn { background: none; border: none; font-size: 20px; cursor: pointer; padding: 4px; border-radius: 50%; transition: transform var(--transition-fast); }
.refresh-btn:hover { background: var(--bg-hover); }
.refresh-btn.spinning { animation: spin .8s linear infinite; }
@keyframes spin { 100% { transform: rotate(360deg); } }

/* Search */
.search-wrap { position: relative; margin-bottom: var(--space-lg); }
.search-input {
  width: 100%; padding: var(--space-md) var(--space-lg); border: 1px solid var(--border);
  border-radius: var(--radius-round); font-size: var(--font-base); box-sizing: border-box;
  background: var(--bg-white); color: var(--text-primary); outline: none;
  transition: border-color var(--transition-fast);
}
.search-input:focus { border-color: var(--jd-red); box-shadow: 0 0 0 2px var(--jd-red-light); }
.search-clear { position: absolute; right: 14px; top: 50%; transform: translateY(-50%); cursor: pointer; color: var(--text-tertiary); font-size: 14px; padding: 4px; }
.search-clear:hover { color: var(--jd-red); }

.order-copy { cursor: pointer; font-size: 13px; margin: 0 4px; opacity: .5; transition: opacity var(--transition-fast); }
.order-copy:hover { opacity: 1; }

.tab-bar { display: flex; margin-bottom: var(--space-lg); background: var(--bg-white); border-radius: var(--radius-md); overflow-x: auto; box-shadow: var(--shadow-sm); -webkit-overflow-scrolling: touch; }
.tab { flex: 1; text-align: center; padding: var(--space-md); cursor: pointer; font-size: var(--font-md); transition: all var(--transition-fast); background: var(--bg-white); color: var(--text-secondary); white-space: nowrap; }
.tab.active { background: var(--jd-red); color: #fff; }

.orders-list { display: flex; flex-direction: column; gap: var(--space-md); }
.sk-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); box-shadow: var(--shadow-sm); }

.order-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); box-shadow: var(--shadow-sm); cursor: pointer; transition: box-shadow var(--transition); border-left: 4px solid transparent; }
.order-card:hover { box-shadow: var(--shadow-md); }
.order-status-0 { border-left-color: var(--orange) !important; }
.order-status-1 { border-left-color: var(--blue) !important; }
.order-status-2 { border-left-color: #1677ff !important; }
.order-status-3 { border-left-color: var(--green) !important; }
.order-status-4 { border-left-color: var(--text-tertiary) !important; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-md); }
.order-no-label { color: var(--text-tertiary); font-size: var(--font-base); }
.order-no { font-weight: 600; }
.order-date { color: var(--text-tertiary); font-size: var(--font-sm); margin-left: var(--space-md); }
.order-body { display: flex; justify-content: space-between; align-items: center; }
.order-receiver { color: var(--text-secondary); font-size: var(--font-base); }
.order-amount { font-size: var(--font-xl); color: var(--jd-red); font-weight: 700; }
.order-actions { margin-top: var(--space-md); display: flex; justify-content: flex-end; gap: var(--space-sm); }

.sk-spacer { margin-top: var(--space-sm); }
.pagination { display: flex; justify-content: center; align-items: center; gap: var(--space-sm); margin-top: var(--space-xl); }
.pagination button { padding: 6px 14px; border: 1px solid var(--border); background: var(--bg-white); border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-sm); color: var(--text-secondary); }
.pagination button:disabled { opacity: .4; cursor: default; }
.page-num { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-sm); background: var(--bg-white); border: 1px solid var(--border); }
.page-num.active { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }
.error-state { padding: 60px var(--space-xl); }

@media (max-width: 768px) {
  .page-title { font-size: var(--font-lg); padding: 0 var(--space-md); }
  .tab-bar { overflow-x: auto; -webkit-overflow-scrolling: touch; }
  .tab { flex-shrink: 0; min-width: fit-content; padding: var(--space-sm) var(--space-md); font-size: var(--font-sm); }
  .order-card { padding: var(--space-md); }
  .order-body { flex-direction: column; align-items: flex-start; gap: var(--space-sm); }
  .order-amount { font-size: var(--font-lg); }
  .order-actions { justify-content: flex-start; }
  .order-header { flex-wrap: wrap; gap: var(--space-xs); }
  .order-date { margin-left: 0; display: block; }
}
</style>
