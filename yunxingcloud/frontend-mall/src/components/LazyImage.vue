<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'

const props = withDefaults(defineProps<{
  src?: string
  alt?: string
  width?: number | string
  height?: number | string
  rounded?: string
  bg?: string
  srcset?: string
  sizes?: string
  webp?: boolean
  fetchpriority?: 'high' | 'low' | 'auto'
}>(), {
  src: '',
  alt: '',
  rounded: '0',
  bg: 'var(--border-light)',
  srcset: '',
  sizes: '',
  webp: false,
  fetchpriority: 'auto',
})

const loaded = ref(false)
const error = ref(false)
const inView = ref(false)
const el = ref<HTMLElement>()
let observer: IntersectionObserver | null = null

const hasSrc = () => props.src && props.src.length > 0

const autoSrcset = computed(() => {
  if (props.srcset || !props.src) return props.srcset
  // Auto-generate responsive srcset for local/uploads images
  if (props.src.startsWith('/uploads/') || props.src.startsWith('/images/')) {
    const base = props.src.replace(/\.\w+$/, '')
    const ext = props.src.match(/\.(\w+)$/)?.[1] || 'jpg'
    return `${base}-400w.${ext} 400w, ${base}-800w.${ext} 800w, ${props.src} 1200w`
  }
  return ''
})

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
  if (!el.value || !hasSrc()) return
  if (!observer) {
    observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) inView.value = true
    }, { rootMargin: '300px' })
    observer.observe(el.value)
  } else if (!inView.value) {
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
    <picture v-if="hasSrc() && inView && webp">
      <source :srcset="autoSrcset || src.replace(/\.(png|jpg|jpeg)(\?.*)?$/i, '.webp$2')" type="image/webp" />
      <img
        :src="src"
        :alt="alt"
        :srcset="autoSrcset || undefined"
        :sizes="sizes || undefined"
        loading="lazy"
        decoding="async"
        :fetchpriority="fetchpriority"
        :width="width"
        :height="height"
        @load="onLoad"
        @error="onError"
        :class="{ 'img-loaded': loaded }"
      />
    </picture>
    <img
      v-else-if="hasSrc() && inView"
      :src="src"
      :alt="alt"
      :srcset="autoSrcset || undefined"
      :sizes="sizes || undefined"
      loading="lazy"
      decoding="async"
      :fetchpriority="fetchpriority"
      :width="width"
      :height="height"
      @load="onLoad"
      @error="onError"
      :class="{ 'img-loaded': loaded }"
    />
    <span v-if="inView && !loaded && !error" class="lazy-placeholder" />
    <span v-if="inView && error" class="lazy-error" />
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

.lazy-placeholder {
  position: absolute; inset: 0;
  background: linear-gradient(90deg, var(--border-light) 25%, var(--border) 50%, var(--border-light) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
  pointer-events: none;
}
.lazy-error {
  position: absolute; inset: 0;
  background: var(--bg-hover);
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; color: var(--text-tertiary);
  pointer-events: none;
}
.lazy-error::after { content: '⚠'; font-size: 24px; }
</style>
