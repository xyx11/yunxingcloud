<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import type { Banner } from '@/types'

const props = defineProps<{
  banners: Banner[]
}>()

const currentBanner = ref(0)
let bannerTimer: ReturnType<typeof setInterval> | null = null

function startBanner() {
  stopBanner()
  if (props.banners.length <= 1) return
  bannerTimer = setInterval(() => {
    currentBanner.value = (currentBanner.value + 1) % props.banners.length
  }, 4000)
}
function stopBanner() {
  if (bannerTimer) {
    clearInterval(bannerTimer)
    bannerTimer = null
  }
}

let touchX = 0
function onTouchStart(e: TouchEvent) { touchX = e.touches[0].clientX; stopBanner() }
function onTouchEnd(e: TouchEvent) {
  const dx = e.changedTouches[0].clientX - touchX
  if (Math.abs(dx) > 40 && props.banners.length > 1) {
    currentBanner.value = dx > 0
      ? (currentBanner.value - 1 + props.banners.length) % props.banners.length
      : (currentBanner.value + 1) % props.banners.length
  }
  startBanner()
}

onMounted(() => { startBanner() })
onUnmounted(() => { stopBanner() })
</script>

<template>
  <div v-if="banners.length" class="banner-wrapper"
    @mouseenter="stopBanner" @mouseleave="startBanner"
    @touchstart.passive="onTouchStart" @touchend.passive="onTouchEnd">
    <div v-for="(b, i) in banners" :key="b.id" class="banner-slide"
      :class="{ active: i === currentBanner }"
      :style="{ background: `linear-gradient(135deg, ${i % 2 ? '#d4000f' : '#f10215'}, ${i % 2 ? '#ff6b6b' : '#f90'})` }">
      <div class="banner-content">
        <h2 class="banner-title">{{ b.title }}</h2>
        <p class="banner-subtitle">{{ b.subtitle || '热门推荐' }}</p>
      </div>
    </div>
    <div class="banner-dots">
      <span v-for="(b, i) in banners" :key="b.id" class="banner-dot"
        :class="{ active: i === currentBanner }" @click="currentBanner = i" />
    </div>
  </div>
</template>

<style scoped>
.banner-wrapper {
  position: relative; border-radius: var(--radius-lg); overflow: hidden;
  margin-bottom: var(--space-xxl); height: 360px;
}
.banner-slide {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity .6s;
}
.banner-slide.active { opacity: 1; }
.banner-content { text-align: center; color: #fff; }
.banner-title { font-size: 42px; margin-bottom: var(--space-lg); }
.banner-subtitle { font-size: var(--font-xl); opacity: .9; }
.banner-dots {
  position: absolute; bottom: var(--space-lg); left: 50%; transform: translateX(-50%);
  display: flex; gap: var(--space-sm);
}
.banner-dot {
  width: 10px; height: 10px; border-radius: 50%; cursor: pointer;
  background: rgba(255,255,255,.5); transition: all .3s;
}
.banner-dot.active { background: #fff; transform: scale(1.3); }
@media (max-width: 768px) {
  .banner-wrapper { height: 200px; border-radius: var(--radius-md); }
  .banner-title { font-size: var(--font-xxl); }
  .banner-subtitle { font-size: var(--font-md); }
}
</style>
