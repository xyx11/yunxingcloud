<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import JdEmpty from '@/components/JdEmpty.vue'
import JdButton from '@/components/JdButton.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import { formatRelativeTime } from '@/utils/format'

interface Notification {
  id: number
  title: string
  content?: string
  message?: string
  type?: string
  isRead?: boolean
  read?: boolean
  linkType?: string
  linkId?: number
  createdAt?: string
}

const router = useRouter()
const toast = useToast()
const { t } = useI18n()

const notifications = ref<Notification[]>([])
const unreadCount = ref(0)
const loading = ref(true)
const filterType = ref<'all' | 'order' | 'promo' | 'system'>('all')

// Pagination
const page = ref(1)
const total = ref(0)
const loadingMore = ref(false)
const PAGE_SIZE = 10

const filters = [
  { key: 'all' as const, label: t('common.all'), icon: '🔔' },
  { key: 'order' as const, label: t('notifications.typeOrder'), icon: '📦' },
  { key: 'promo' as const, label: t('notifications.typePromo'), icon: '🎉' },
  { key: 'system' as const, label: t('notifications.typeSystem'), icon: '⚙️' },
]

const filteredList = computed(() => {
  if (filterType.value === 'all') return notifications.value
  return notifications.value.filter(n => n.type === filterType.value)
})

function typeIcon(type?: string) {
  if (type === 'order') return '📦'
  if (type === 'promo') return '🎉'
  return '🔔'
}

async function load(pageNum = 1) {
  if (pageNum === 1) loading.value = true
  else loadingMore.value = true
  try {
    const params: Record<string, unknown> = { page: pageNum, size: PAGE_SIZE }
    if (filterType.value !== 'all') params.type = filterType.value
    const r = await request.get('/notifications', { params })
    const data = r.data || {}
    const list = data.list || data.records || data || []
    if (pageNum === 1) notifications.value = list
    else notifications.value = [...notifications.value, ...list]
    total.value = data.total || 0
    page.value = pageNum
    loading.value = false;
    } catch { loading.value = false; if (pageNum === 1) toast.error(t('notifications.loadFail')) }
}

async function loadUnread() {
  try { const r = await request.get('/notifications/unread-count'); unreadCount.value = (r.data as any)?.count || 0; loading.value = false;
    } catch { /* silent */ }
}

function switchFilter(type: 'all' | 'order' | 'promo' | 'system') {
  filterType.value = type
  notifications.value = []
  load(1)
}

function loadMore() {
  if (notifications.value.length >= total.value) return
  load(page.value + 1)
}

const hasMore = computed(() => notifications.value.length < total.value)

async function handleClick(n: Notification) {
  if (!(n.isRead ?? !n.read)) {
    try { await request.put(`/notifications/${n.id}/read`); n.isRead = true; n.read = true; unreadCount.value = Math.max(0, unreadCount.value - 1) } catch { /* silent */ }
  }
  // Navigate based on link type
  if (n.linkType === 'order' && n.linkId) router.push(`/order/${n.linkId}`)
  else if (n.linkType === 'product' && n.linkId) router.push(`/product/${n.linkId}`)
  else if (n.linkType === 'coupon') router.push('/coupons')
  else if (n.linkType === 'points') router.push('/points')
}

async function markAllRead() {
  try {
    await request.put('/notifications/read-all')
    notifications.value.forEach(n => { n.isRead = true; n.read = true })
    unreadCount.value = 0
  } catch { toast.error(t('notifications.markAllFail')) }
}

onMounted(() => { load(); loadUnread() })
</script>

<template>
  <div class="notif-page">
    <!-- Header -->
    <div class="notif-header">
      <div class="notif-header-left">
        <h2 class="notif-title">{{ t('notifications.title') }}</h2>
        <span v-if="unreadCount > 0" class="notif-badge">{{ unreadCount }}</span>
      </div>
      <button v-if="unreadCount > 0" class="notif-read-all" @click="markAllRead">
        {{ t('notifications.markAllRead') }}
      </button>
    </div>

    <!-- Filter tabs -->
    <div class="notif-tabs">
      <button
        v-for="f in filters" :key="f.key"
        class="notif-tab"
        :class="{ active: filterType === f.key }"
        @click="switchFilter(f.key)"
      >
        {{ f.icon }} {{ f.label }}
      </button>
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading" class="notif-skel">
      <div v-for="i in 3" :key="i" class="sk-item">
        <div class="sk-icon" />
        <div class="sk-body">
          <div class="sk-line" />
          <div class="sk-line w60" />
        </div>
      </div>
    </div>

    <!-- Notification list -->
    <div v-else-if="filteredList.length" class="notif-list">
      <TransitionGroup name="notif-item">
      <div
        v-for="n in filteredList" :key="n.id"
        class="notif-item"
        :class="{ unread: !(n.isRead ?? !n.read) }"
        role="button"
        tabindex="0"
        @click="handleClick(n)"
        @keydown.enter.prevent="handleClick(n)"
        @keydown.space.prevent="handleClick(n)"
      >
        <div class="notif-icon">{{ typeIcon(n.type) }}</div>
        <div class="notif-body">
          <div class="notif-item-title">{{ n.title }}</div>
          <div class="notif-text">{{ n.content || n.message }}</div>
          <div class="notif-meta">
            <span class="notif-time">{{ formatRelativeTime(n.createdAt || '') }}</span>
            <span v-if="n.linkType" class="notif-link-hint">
              {{ n.linkType === 'order' ? t('notifications.viewOrder') : n.linkType === 'product' ? t('notifications.viewProduct') : t('notifications.viewDetail') }} →
            </span>
          </div>
        </div>
        <div v-if="!(n.isRead ?? !n.read)" class="notif-dot" />
      </div>

      <!-- Load more -->
      <div v-if="hasMore" class="notif-load-more" key="load-more">
        <JdButton type="outline" size="sm" :loading="loadingMore" @click="loadMore">
          {{ loadingMore ? t('common.loading') : t('common.loadMore') }}
        </JdButton>
      </div>
      </TransitionGroup>
    </div>

    <!-- Empty -->
    <JdEmpty
      v-else
      :icon="filterType === 'all' ? '🔔' : filters.find(f => f.key === filterType)?.icon || '🔔'"
      :title="t('notifications.empty')"
    />
  </div>
</template>

<style scoped>
.notif-page { max-width: 600px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }

/* Header */
.notif-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.notif-header-left { display: flex; align-items: center; gap: var(--space-sm); }
.notif-title { font-size: var(--font-xl); font-weight: 700; }
.notif-badge {
  background: var(--jd-red); color: #fff; font-size: 11px; font-weight: 700;
  padding: 2px 7px; border-radius: var(--radius-round); min-width: 18px; text-align: center;
}
.notif-read-all {
  padding: 6px 14px; border: 1px solid var(--jd-red); background: var(--bg-white);
  color: var(--jd-red); border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-sm);
  transition: all var(--transition-fast);
}
.notif-read-all:hover { background: var(--jd-red); color: #fff; }

/* Filter tabs */
.notif-tabs { display: flex; gap: 0; margin-bottom: var(--space-lg); background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.notif-tab {
  flex: 1; text-align: center; padding: 10px 4px; cursor: pointer; font-size: var(--font-sm);
  background: var(--bg-white); border: none; color: var(--text-secondary);
  transition: all var(--transition-fast); font-weight: 500;
}
.notif-tab.active { background: var(--jd-red); color: #fff; font-weight: 600; }
.notif-tab:not(.active):hover { background: var(--bg-hover); }

/* List */
.notif-list { display: flex; flex-direction: column; gap: 10px; }
.notif-item {
  display: flex; gap: var(--space-md); padding: var(--space-lg);
  background: var(--bg-white); border-radius: var(--radius-md);
  cursor: pointer; box-shadow: var(--shadow-sm);
  transition: background var(--transition-fast); position: relative;
}
.notif-item:hover { background: var(--bg-hover); }
.notif-item.unread { background: var(--jd-red-light); border-left: 3px solid var(--jd-red); }
.notif-icon { font-size: 24px; flex-shrink: 0; margin-top: 2px; }
.notif-body { flex: 1; min-width: 0; }
.notif-item-title { font-weight: 600; font-size: var(--font-md); margin-bottom: 4px; color: var(--text-primary); }
.notif-text { font-size: var(--font-base); color: var(--text-secondary); overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; line-height: 1.4; }
.notif-meta { display: flex; justify-content: space-between; align-items: center; margin-top: var(--space-xs); }
.notif-time { font-size: var(--font-xs); color: var(--text-placeholder); }
.notif-link-hint { font-size: var(--font-xs); color: var(--jd-red); font-weight: 500; }
.notif-dot {
  position: absolute; top: 12px; right: 12px; width: 8px; height: 8px;
  border-radius: 50%; background: var(--jd-red);
}

/* Skeleton */
.notif-skel { display: flex; flex-direction: column; gap: var(--space-md); }
.sk-item { padding: var(--space-lg); background: var(--bg-white); border-radius: var(--radius-md); display: flex; gap: var(--space-md); }
.sk-icon { width: 24px; height: 24px; border-radius: 50%; background: var(--border-light); flex-shrink: 0; }
.sk-body { flex: 1; display: flex; flex-direction: column; gap: var(--space-sm); }
.sk-line { height: 16px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-sm); width: 100%; }
.sk-line.w60 { width: 60%; }

.notif-load-more { text-align: center; padding-top: var(--space-md); }

.notif-item-enter-active { transition: all .4s ease-out; }
.notif-item-leave-active { transition: all .25s ease-in; }
.notif-item-enter-from { opacity: 0; transform: translateY(12px); }
.notif-item-leave-to { opacity: 0; transform: translateX(-20px); }
.notif-item-move { transition: transform .3s ease; }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

@media (max-width: 768px) {
  .notif-page { padding: var(--space-lg) var(--space-md) calc(80px + env(safe-area-inset-bottom, 0px)); }
  .notif-tabs { border-radius: 0; margin: 0 calc(-1 * var(--space-md)) var(--space-lg); }
  .notif-tab { font-size: 12px; padding: 8px 4px; }
}
</style>