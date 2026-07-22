<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import LazyImage from '@/components/LazyImage.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import JdButton from '@/components/JdButton.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

interface LiveRoom {
  id: number
  title: string
  coverUrl?: string
  anchorName?: string
  viewerCount?: number
  status: string // '1'=live, '0'=upcoming, '-1'=ended
  description?: string
  startTime?: string
  hasReplay?: boolean
}

const rooms = ref<LiveRoom[]>([])
const loading = ref(true)
const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const activeFilter = ref<'all' | 'live' | 'upcoming' | 'ended'>('all')
const subscribing = ref<number | null>(null)

const filters = [
  { key: 'all' as const, icon: '📺', label: t('common.all') },
  { key: 'live' as const, icon: '🔴', label: t('live.statusLive') },
  { key: 'upcoming' as const, icon: '⏰', label: t('live.statusUpcoming') },
  { key: 'ended' as const, icon: '📼', label: t('live.statusEnded') },
]

const filterCounts = computed(() => ({
  all: rooms.value.length,
  live: rooms.value.filter(r => r.status === '1').length,
  upcoming: rooms.value.filter(r => r.status === '0').length,
  ended: rooms.value.filter(r => r.status === '-1').length,
}))

const filteredRooms = computed(() => {
  // Sort: live first, then upcoming, then ended
  const sorted = [...rooms.value].sort((a, b) => {
    const order: Record<string, number> = { '1': 0, '0': 1, '-1': 2 }
    return (order[a.status] ?? 3) - (order[b.status] ?? 3)
  })
  const statusMap: Record<string, string> = { live: '1', upcoming: '0', ended: '-1' }
  if (activeFilter.value === 'all') return sorted
  return sorted.filter(r => r.status === statusMap[activeFilter.value])
})

function statusLabel(s: string) {
  if (s === '1') return t('live.statusLive')
  if (s === '0') return t('live.statusUpcoming')
  return t('live.statusEnded')
}

function goRoom(id: number) { router.push(`/live/${id}`) }

function formatTimeUntil(startTime?: string): string {
  if (!startTime) return ''
  const now = Date.now()
  const target = new Date(startTime).getTime()
  const diff = target - now
  if (diff <= 0) return ''
  const hours = Math.floor(diff / 3600000)
  if (hours < 24) return `${hours}小时后`
  const days = Math.floor(hours / 24)
  return `${days}天后`
}

async function subscribeRemind(roomId: number) {
  subscribing.value = roomId
  try {
    await request.post(`/live/${roomId}/remind`)
    toast.success(t('live.remindSet'))
  } catch { toast.error(t('live.remindFail')) }
  finally { subscribing.value = null }
}

async function load() {
  loading.value = true
  try { const r = await request.get('/live/rooms'); rooms.value = r.data || []; loading.value = false;
    } catch { toast.error(t('live.loadFail')) }
}

onMounted(load)
</script>

<template>
  <div class="live-page">
    <!-- Hero -->
    <div class="live-hero">
      <div class="live-hero-bg" />
      <h1 class="live-hero-title">{{ t('live.title') }}</h1>
      <p class="live-hero-sub">{{ t('live.subtitle') }}</p>
    </div>

    <!-- Category tags -->
    <div class="live-categories">
      <span class="live-cat-tag active">全部</span>
      <span class="live-cat-tag">👗 穿搭</span>
      <span class="live-cat-tag">💄 美妆</span>
      <span class="live-cat-tag">📱 数码</span>
      <span class="live-cat-tag">🍜 食品</span>
      <span class="live-cat-tag">🏠 家居</span>
    </div>

    <!-- Filter tabs -->
    <div class="live-tabs">
      <button
        v-for="f in filters" :key="f.key"
        class="live-tab"
        :class="{ active: activeFilter === f.key }"
        @click="activeFilter = f.key"
      >
        <span>{{ f.icon }} {{ f.label }}</span>
        <span class="live-tab-count">{{ filterCounts[f.key] }}</span>
      </button>
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading" class="live-grid">
      <div v-for="i in 6" :key="i" class="live-skel">
        <div class="sk-img" />
        <div class="sk-body">
          <div class="sk-line" />
          <div class="sk-line w60" />
        </div>
      </div>
    </div>

    <!-- Room list -->
    <div v-else-if="filteredRooms.length" class="live-grid">
      <div
        v-for="r in filteredRooms" :key="r.id"
        class="live-card"
        role="button"
        tabindex="0"
        @click="goRoom(r.id)"
        @keydown.enter.prevent="goRoom(r.id)"
        @keydown.space.prevent="goRoom(r.id)"
      >
        <div class="live-cover">
          <LazyImage :src="r.coverUrl || ''" :alt="r.title" height="200px" />
          <!-- Status badge -->
          <span class="live-status" :class="{
            live: r.status === '1',
            upcoming: r.status === '0',
            ended: r.status === '-1',
          }">
            <span v-if="r.status === '1'" class="live-dot" />
            {{ statusLabel(r.status) }}
          </span>
          <!-- Viewer count for live -->
          <span v-if="r.status === '1'" class="live-viewers">
            👁 {{ (r.viewerCount || 0).toLocaleString() }}
          </span>
          <!-- Replay badge for ended -->
          <span v-if="r.status === '-1' && r.hasReplay" class="live-replay">
            📼 {{ t('live.hasReplay') }}
          </span>
          <!-- Countdown for upcoming -->
          <span v-if="r.status === '0' && r.startTime" class="live-countdown">
            ⏰ {{ formatTimeUntil(r.startTime) }}
          </span>
        </div>
        <div class="live-info">
          <h3 class="live-title">{{ r.title }}</h3>
          <div class="live-meta">
            <span class="live-anchor">🎙 {{ r.anchorName || t('live.anchor') }}</span>
          </div>
          <!-- Subscribe button for upcoming -->
          <div v-if="r.status === '0'" class="live-actions">
            <JdButton
              size="sm"
              type="outline"
              :loading="subscribing === r.id"
              @click.stop="subscribeRemind(r.id)"
            >
              🔔 {{ t('live.remindMe') }}
            </JdButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <JdEmpty v-else icon="📺" :title="t('live.empty')" :description="t('live.emptyDesc')" />
  </div>
</template>

<style scoped>
.live-page { max-width: 1100px; margin: 0 auto; }

/* Hero */
.live-hero {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; border-radius: var(--radius-xl); padding: var(--space-xxxl);
  margin-bottom: var(--space-xxl); text-align: center; position: relative; overflow: hidden;
}
.live-hero-bg {
  position: absolute; top: -40px; right: -40px; width: 160px; height: 160px;
  border-radius: 50%; background: rgba(255,255,255,.08);
}
.live-hero-title { font-size: var(--font-h1); font-weight: 800; margin-bottom: var(--space-sm); position: relative; }
.live-hero-sub { font-size: 15px; opacity: .9; position: relative; }

/* Filter tabs */
.live-categories { display: flex; gap: var(--space-sm); flex-wrap: wrap; margin-bottom: var(--space-lg); }
.live-cat-tag { padding: 6px 16px; border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-sm); background: var(--bg-white); border: 1px solid var(--border); color: var(--text-secondary); transition: all var(--transition-fast); }
.live-cat-tag.active { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }
.live-cat-tag:hover:not(.active) { border-color: var(--jd-red); color: var(--jd-red); }

.live-tabs { display: flex; gap: 8px; margin-bottom: var(--space-xl); }
.live-tab {
  display: flex; align-items: center; gap: 6px; padding: 8px 16px;
  border: 1px solid var(--border); background: var(--bg-white);
  border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-sm);
  color: var(--text-secondary); transition: all var(--transition-fast);
}
.live-tab.active { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }
.live-tab:not(.active):hover { border-color: var(--jd-red); color: var(--jd-red); }
.live-tab-count {
  font-size: 11px; background: var(--bg-hover); color: var(--text-tertiary);
  padding: 1px 7px; border-radius: var(--radius-round); font-weight: 600;
}
.live-tab.active .live-tab-count { background: rgba(255,255,255,.25); color: #fff; }

/* Grid */
.live-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-lg); }
.live-card {
  background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden;
  cursor: pointer; box-shadow: var(--shadow-sm);
  transition: transform var(--transition), box-shadow var(--transition);
}
.live-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-md); }
.live-cover { position: relative; }
.live-status {
  position: absolute; top: 10px; left: 10px; padding: 2px 10px;
  border-radius: var(--radius-round); font-size: 12px; font-weight: 600;
  background: rgba(0,0,0,.5); color: #fff; display: flex; align-items: center; gap: 4px;
}
.live-status.live { background: var(--jd-red); animation: pulse 2s infinite; }
.live-status.upcoming { background: var(--orange); }
.live-status.ended { background: rgba(0,0,0,.6); }

.live-dot { width: 6px; height: 6px; border-radius: 50%; background: #fff; animation: livePulse 1.5s ease-in-out infinite; }
@keyframes livePulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 0 0 rgba(255,255,255,.6); }
  50% { opacity: .7; box-shadow: 0 0 0 4px rgba(255,255,255,.3); }
}
.live-viewers { position: absolute; top: 10px; right: 10px; font-size: 12px; color: #fff; background: rgba(0,0,0,.4); padding: 2px 8px; border-radius: var(--radius-round); }
.live-replay { position: absolute; top: 10px; right: 10px; font-size: 12px; color: #fff; background: rgba(0,0,0,.4); padding: 2px 8px; border-radius: var(--radius-round); }
.live-countdown {
  position: absolute; bottom: 10px; left: 10px; font-size: 12px; color: #fff;
  background: rgba(255,152,0,.8); padding: 2px 8px; border-radius: var(--radius-round);
}

.live-info { padding: var(--space-lg); }
.live-title { font-size: 15px; font-weight: 600; margin-bottom: var(--space-xs); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.live-meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-sm); }
.live-anchor { font-size: 13px; color: var(--text-tertiary); }
.live-actions { margin-top: var(--space-sm); }

/* Skeleton */
.live-skel { background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-sm); }
.sk-img { height: 200px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; }
.sk-body { padding: var(--space-lg); display: flex; flex-direction: column; gap: var(--space-sm); }
.sk-line { height: 16px; background: var(--border-light); border-radius: var(--radius-sm); width: 100%; }
.sk-line.w60 { width: 60%; }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .6; } }

@media (max-width: 768px) {
  .live-page { padding: 0 var(--space-md) 80px; }
  .live-hero { padding: var(--space-xl); }
  .live-tabs { gap: 4px; overflow-x: auto; }
  .live-tab { padding: 6px 12px; font-size: 12px; white-space: nowrap; flex-shrink: 0; }
  .live-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-md); }
}
</style>