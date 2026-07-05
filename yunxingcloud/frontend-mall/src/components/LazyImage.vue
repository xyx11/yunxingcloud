<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = withDefaults(defineProps<{
  src?: string
  alt?: string
  width?: number | string
  height?: number | string
  rounded?: string
  bg?: string
}>(), {
  src: '',
  alt: '',
  rounded: '0',
  bg: 'var(--border-light)',
})

const loaded = ref(false)
const error = ref(false)
const inView = ref(false)
const el = ref<HTMLElement>()
let observer: IntersectionObserver | null = null

const hasSrc = () => props.src && props.src.length > 0

onMounted(() => {
  if (!el.value || !hasSrc()) return
  observer = new IntersectionObserver(([entry]) => {
    if (entry.isIntersecting) {
      inView.value = true
    }
  }, { rootMargin: '300px' })
  observer.observe(el.value)
})

onUnmounted(() => { observer?.disconnect() })

watch(() => props.src, () => {
  loaded.value = false
  error.value = false
  // 如果元素当前不在视口内，重置 inView 并重新 observe
  if (el.value && observer && !inView.value) {
    inView.value = false
    observer.disconnect()
    observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) inView.value = true
    }, { rootMargin: '300px' })
    observer.observe(el.value)
  }
})

function onError() { error.value = true }
function onLoad() { loaded.value = true }
</script>

<template>
  <div
    ref="el"
    class="lazy-image"
    :style="{ background: bg, borderRadius: rounded }"
  >
    <img
      v-if="hasSrc() && inView"
      :src="src"
      :alt="alt"
      loading="lazy"
      :width="width"
      :height="height"
      @load="onLoad"
      @error="onError"
      :class="{ 'img-loaded': loaded }"
    />
    <span v-if="!loaded && !error" class="lazy-placeholder">📦</span>
    <span v-if="error" class="lazy-error">🖼️</span>
  </div>
</template>

<style scoped>
.lazy-image {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}
.lazy-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity .4s ease;
}
.lazy-image img.img-loaded { opacity: 1; }

.lazy-placeholder,
.lazy-error {
  position: absolute;
  font-size: 32px;
  pointer-events: none;
}
</style>
