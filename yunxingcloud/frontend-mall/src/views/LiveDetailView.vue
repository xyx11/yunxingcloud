<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/api/request'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import { reportLiveView } from '@/api/live'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import { formatPrice } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const room = ref<any>(null)
const loading = ref(true)
const error = ref(false)
const id = Number(route.params.id)

function statusLabel(s: string) {
  if (s === '1') return t('liveDetail.statusLive')
  if (s === '0') return t('liveDetail.statusUpcoming')
  return t('liveDetail.statusEnded')
}

async function load() {
  loading.value = true
  error.value = false
  try {
    const r = await request.get(`/live/rooms/${id}`)
    room.value = r.data || null
    loading.value = false;
    } catch {
    error.value = true
    toast.error(t('liveDetail.loadFail'))
  }
}

function goProduct(id: number) {
  router.push(`/product/${id}`)
}

onMounted(() => { load(); reportLiveView(id).catch(() => {}) })
</script>

<template>
  <div class="live-detail-page">
    <div class="ld-back">
      <button class="back-btn" @click="router.back()">{{ t('liveDetail.back') }}</button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="ld-skel-wrap">
      <SkeletonBox variant="banner" height="400px" />
      <SkeletonBox variant="text" :count="3" />
    </div>

    <!-- Error -->
    <JdEmpty v-else-if="error" icon="📡" :title="t('common.loading')" :description="t('liveDetail.loadFail')">
      <JdButton @click="load">{{ t('common.retry') }}</JdButton>
    </JdEmpty>

    <!-- Room not found -->
    <JdEmpty v-else-if="!room" icon="📺" :title="t('liveDetail.notFound')" :description="t('liveDetail.notFoundDesc')" />

    <!-- Room content -->
    <template v-else>
      <div class="ld-main">
        <!-- Video player placeholder -->
        <div class="ld-player">
          <div class="ld-player-placeholder">
            <LazyImage v-if="room.coverUrl" :src="room.coverUrl" :alt="room.title" height="400px" />
            <div class="ld-player-overlay">
              <span v-if="room.status === '1'" class="ld-live-dot" />
              <span>{{ statusLabel(room.status) }}</span>
            </div>
          </div>
        </div>

        <!-- Room info -->
        <div class="ld-sidebar">
          <div class="ld-room-header">
            <span class="ld-status-tag" :class="{ live: room.status === '1' }">{{ statusLabel(room.status) }}</span>
            <h1 class="ld-room-title">{{ room.title }}</h1>
          </div>

          <div class="ld-anchor-card">
            <span class="ld-anchor-avatar">🎙</span>
            <div>
              <div class="ld-anchor-name">{{ room.anchorName || t('liveDetail.anchor') }}</div>
              <div class="ld-viewers" v-if="room.status === '1'">👁 {{ room.viewerCount || 0 }} {{ t('liveDetail.viewers') }}</div>
            </div>
          </div>

          <div class="ld-room-meta" v-if="room.description">
            <p>{{ room.description }}</p>
          </div>

          <!-- Products in room -->
          <div class="ld-products" v-if="room.products && room.products.length">
            <h3 class="ld-section-title">{{ t('liveDetail.products') }} ({{ room.products.length }})</h3>
            <div class="ld-product-list">
              <div
                v-for="p in room.products"
                :key="p.id"
                class="ld-product-item"
                role="button"
                tabindex="0"
                @click="goProduct(p.id)"
                @keydown.enter.prevent="goProduct(p.id)"
                @keydown.space.prevent="goProduct(p.id)"
              >
                <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="72px" width="72px" />
                <div class="ld-product-info">
                  <span class="ld-product-name">{{ p.name }}</span>
                  <span class="ld-product-price">{{ formatPrice((p.price || 0) / 100) }}</span>
                </div>
                <JdButton size="sm" type="outline" @click.stop="router.push('/product/' + p.id)">抢购</JdButton>
              </div>
            </div>
          </div>
          <JdEmpty v-else-if="room.status === '1'" icon="📦" :title="t('liveDetail.noProducts')" :description="t('liveDetail.noProductsDesc')" size="sm" />
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.live-detail-page { max-width: 1100px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }
.ld-back { margin-bottom: var(--space-lg); }
.back-btn { background: none; border: none; color: var(--text-secondary); cursor: pointer; font-size: var(--font-md); }

.ld-main { display: grid; grid-template-columns: 1fr 360px; gap: var(--space-xl); }

.ld-player { border-radius: var(--radius-lg); overflow: hidden; background: #000; }
.ld-player-placeholder { position: relative; }
.ld-player-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,.7));
  padding: var(--space-xxl) var(--space-xl) var(--space-xl);
  color: #fff; font-size: var(--font-lg); font-weight: 600;
  display: flex; align-items: center; gap: var(--space-sm);
}
.ld-live-dot {
  width: 10px; height: 10px; background: var(--jd-red); border-radius: 50%;
  animation: pulse 2s infinite;
}

.ld-sidebar { display: flex; flex-direction: column; gap: var(--space-lg); }
.ld-room-header { display: flex; flex-direction: column; gap: var(--space-sm); }
.ld-status-tag {
  display: inline-block; padding: 2px 10px; border-radius: var(--radius-round);
  font-size: 12px; font-weight: 600; width: fit-content;
  background: var(--bg-hover); color: var(--text-secondary);
}
.ld-status-tag.live { background: var(--jd-red); color: #fff; animation: pulse 2s infinite; }
.ld-room-title { font-size: var(--font-xl); font-weight: 700; }

.ld-anchor-card {
  display: flex; align-items: center; gap: var(--space-md);
  padding: var(--space-lg); background: var(--bg-white); border-radius: var(--radius-lg);
}
.ld-anchor-avatar { font-size: 32px; }
.ld-anchor-name { font-weight: 600; }
.ld-viewers { font-size: var(--font-sm); color: var(--text-tertiary); margin-top: 2px; }

.ld-room-meta { font-size: var(--font-sm); color: var(--text-secondary); line-height: 1.6; }

.ld-products { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-lg); }
.ld-section-title { font-size: var(--font-md); font-weight: 600; margin-bottom: var(--space-md); }
.ld-product-list { display: flex; flex-direction: column; gap: var(--space-sm); }
.ld-product-item {
  display: flex; gap: var(--space-md); padding: var(--space-sm);
  border-radius: var(--radius-md); cursor: pointer; transition: background var(--transition);
}
.ld-product-item:hover { background: var(--bg-hover); }
.ld-product-info { display: flex; flex-direction: column; justify-content: center; gap: 4px; overflow: hidden; }
.ld-product-name { font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ld-product-price { color: var(--jd-red); font-weight: 700; font-size: var(--font-md); }

.ld-skel-wrap { display: flex; flex-direction: column; gap: var(--space-lg); }

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .6; } }

@media (max-width: 768px) {
  .live-detail-page { padding: var(--space-lg) var(--space-md) 80px; }
  .ld-main { grid-template-columns: 1fr; }
}
</style>
