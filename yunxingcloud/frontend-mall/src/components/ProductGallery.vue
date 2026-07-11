<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import LazyImage from '@/components/LazyImage.vue'

const props = defineProps<{
  images: string[]
  productName: string
}>()

const activeImage = ref(0)
const fullscreen = ref(false)
let touchX = 0

function onTouchStart(e: TouchEvent) {
  touchX = e.touches[0].clientX
}

function onTouchEnd(e: TouchEvent) {
  const dx = e.changedTouches[0].clientX - touchX
  if (Math.abs(dx) > 50 && props.images.length > 1) {
    activeImage.value = dx < 0
      ? Math.min(props.images.length - 1, activeImage.value + 1)
      : Math.max(0, activeImage.value - 1)
  }
}

function onFsKeydown(e: KeyboardEvent) {
  if (!fullscreen.value) return
  if (e.key === 'ArrowLeft') {
    activeImage.value = activeImage.value > 0 ? activeImage.value - 1 : props.images.length - 1
  } else if (e.key === 'ArrowRight') {
    activeImage.value = activeImage.value < props.images.length - 1 ? activeImage.value + 1 : 0
  } else if (e.key === 'Escape') {
    fullscreen.value = false
  }
}

onMounted(() => {
  window.addEventListener('keydown', onFsKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onFsKeydown)
})
</script>

<template>
  <div class="gallery">
    <div
      class="gallery-main"
      @touchstart="onTouchStart"
      @touchend="onTouchEnd"
      @click="fullscreen = true"
    >
      <LazyImage
        :src="images[activeImage]"
        :alt="productName"
        height="420px"
        rounded="8px"
      />
    </div>

    <!-- Fullscreen overlay -->
    <div v-if="fullscreen" class="fullscreen-overlay" @click="fullscreen = false">
      <button
        class="fs-close"
        @click.stop="fullscreen = false"
        aria-label="关闭"
      >✕</button>
      <button
        v-if="images.length > 1"
        class="fs-nav fs-nav--prev"
        @click.stop="activeImage = activeImage > 0 ? activeImage - 1 : images.length - 1"
        aria-label="上一张"
      >‹</button>
      <button
        v-if="images.length > 1"
        class="fs-nav fs-nav--next"
        @click.stop="activeImage = activeImage < images.length - 1 ? activeImage + 1 : 0"
        aria-label="下一张"
      >›</button>
      <img
        v-if="images[activeImage] && images[activeImage] !== '\u{1F4E6}'"
        :src="images[activeImage]"
        class="fs-img"
      />
      <span v-else class="fs-placeholder">📦</span>
      <div v-if="images.length > 1" class="fs-dots">
        <button
          v-for="(_img, i) in images"
          :key="i"
          class="fs-dot"
          :class="{ active: activeImage === i }"
          @click.stop="activeImage = i"
        />
      </div>
    </div>

    <!-- Thumbnails -->
    <div v-if="images.length > 1" class="thumbnails">
      <div
        v-for="(img, i) in images"
        :key="i"
        class="thumb"
        :class="{ active: activeImage === i }"
        @click="activeImage = i"
      >
        <LazyImage :src="img" alt="" height="100%" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.gallery {
  width: 420px;
  flex-shrink: 0;
}
.gallery-main {
  cursor: zoom-in;
  overflow: hidden;
  border-radius: var(--radius-md);
}
.gallery-main :deep(img) {
  transition: transform 0.3s ease;
}
.gallery-main:hover :deep(img) {
  transform: scale(1.5);
}
.thumbnails {
  display: flex;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}
.thumb {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  overflow: hidden;
  border: 1px solid var(--border);
  transition: border var(--transition-fast);
}
.thumb.active {
  border: 2px solid var(--jd-red);
}

/* Fullscreen */
.fullscreen-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.95);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s;
}
.fs-close {
  position: absolute;
  top: var(--space-lg);
  right: var(--space-lg);
  z-index: 2;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: none;
  font-size: var(--font-lg);
  cursor: pointer;
}
.fs-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  border: none;
  font-size: var(--font-xxl);
  cursor: pointer;
  transition: background var(--transition);
}
.fs-nav:hover {
  background: rgba(255, 255, 255, 0.3);
}
.fs-nav--prev {
  left: var(--space-lg);
}
.fs-nav--next {
  right: var(--space-lg);
}
.fs-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
.fs-placeholder {
  font-size: 120px;
}
.fs-dots {
  position: absolute;
  bottom: var(--space-xl);
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: var(--space-xs);
}
.fs-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.4);
}
.fs-dot.active {
  background: var(--jd-red);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
