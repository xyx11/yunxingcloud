<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import LazyImage from '@/components/LazyImage.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const rooms = ref<any[]>([])
const loading = ref(true)
const router = useRouter()
const toast = useToast()
const { t } = useI18n()

async function load() {
  loading.value = true
  try { const r = await request.get('/live/rooms'); rooms.value = r.data || [] } catch { toast.error(t('live.loadFail')) } finally { loading.value = false }
}

function statusLabel(s: string) {
  if (s === '1') return t('live.statusLive')
  if (s === '0') return t('live.statusUpcoming')
  return t('live.statusEnded')
}

function goRoom(id: number) { router.push(`/live/${id}`) }

onMounted(load)
</script>

<template>
  <div class="live-page">
    <div class="live-hero">
      <h1 class="live-hero-title">{{ t('live.title') }}</h1>
      <p class="live-hero-sub">{{ t('live.subtitle') }}</p>
    </div>

    <div v-if="loading" class="live-grid">
      <div v-for="i in 6" :key="i" class="live-skel">
        <div class="sk-img" />
        <div class="sk-body"><div class="sk-line" /><div class="sk-line w60" /></div>
      </div>
    </div>

    <div v-else-if="rooms.length" class="live-grid">
      <div v-for="r in rooms" :key="r.id" class="live-card" role="button" tabindex="0" @click="goRoom(r.id)" @keydown.enter.prevent="goRoom(r.id)" @keydown.space.prevent="goRoom(r.id)">
        <div class="live-cover">
          <LazyImage :src="r.coverUrl || ''" :alt="r.title" height="200px" />
          <span class="live-status" :class="{ live: r.status === '1' }">{{ statusLabel(r.status) }}</span>
          <span v-if="r.status === '1'" class="live-viewers">👁 {{ r.viewerCount || 0 }}</span>
        </div>
        <div class="live-info">
          <h3 class="live-title">{{ r.title }}</h3>
          <div class="live-anchor">🎙 {{ r.anchorName || t('live.anchor') }}</div>
        </div>
      </div>
    </div>

    <JdEmpty v-else icon="📺" :title="t('live.empty')" :description="t('live.emptyDesc')" />
  </div>
</template>

<style scoped>
.live-page { max-width: 1100px; margin: 0 auto; }
.live-hero { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; border-radius: var(--radius-xl); padding: var(--space-xxxl); margin-bottom: var(--space-xxl); text-align: center; }
.live-hero-title { font-size: var(--font-h1); font-weight: 800; margin-bottom: var(--space-sm); }
.live-hero-sub { font-size: 15px; opacity: .9; }
.live-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-lg); }
.live-card { background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition); }
.live-card:hover { transform: translateY(-4px); }
.live-cover { position: relative; }
.live-status { position: absolute; top: 10px; left: 10px; padding: 2px 10px; border-radius: var(--radius-round); font-size: 12px; font-weight: 600; background: rgba(0,0,0,.5); color: #fff; }
.live-status.live { background: var(--jd-red); animation: pulse 2s infinite; }
.live-viewers { position: absolute; top: 10px; right: 10px; font-size: 12px; color: #fff; background: rgba(0,0,0,.4); padding: 2px 8px; border-radius: var(--radius-round); }
.live-info { padding: var(--space-lg); }
.live-title { font-size: 15px; font-weight: 600; margin-bottom: var(--space-xs); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.live-anchor { font-size: 13px; color: var(--text-tertiary); }
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
  .live-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-md); }
}
</style>
