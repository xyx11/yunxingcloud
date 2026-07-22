<script setup lang="ts">
import { computed, watch, ref } from 'vue'

const props = defineProps<{
  images: string[]
  modelValue: boolean
  initialIndex?: number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const currentIdx = ref(props.initialIndex || 0)
const currentImage = computed(() => props.images[currentIdx.value] || '')

watch(() => props.modelValue, (v) => {
  if (v) {
    currentIdx.value = props.initialIndex || 0
    document.addEventListener('keydown', onKey)
  } else {
    document.removeEventListener('keydown', onKey)
  }
})

function close() { emit('update:modelValue', false) }

function prev() {
  if (props.images.length <= 1) return
  currentIdx.value = (currentIdx.value - 1 + props.images.length) % props.images.length
}

function next() {
  if (props.images.length <= 1) return
  currentIdx.value = (currentIdx.value + 1) % props.images.length
}

function onKey(e: KeyboardEvent) {
  if (e.key === 'Escape') close()
  else if (e.key === 'ArrowLeft') prev()
  else if (e.key === 'ArrowRight') next()
}

function openAt(index: number) {
  if (index >= 0 && index < props.images.length) {
    currentIdx.value = index
  }
}

defineExpose({ openAt })
</script>

<template>
  <div v-if="modelValue" class="iv-overlay" @click="close" @keydown="onKey" tabindex="0">
    <button class="iv-close" @click.stop="close" aria-label="关闭">✕</button>
    <button v-if="images.length > 1" class="iv-nav iv-prev" @click.stop="prev" aria-label="上一张">‹</button>
    <img :src="currentImage" class="iv-img" @click.stop />
    <button v-if="images.length > 1" class="iv-nav iv-next" @click.stop="next" aria-label="下一张">›</button>
    <div v-if="images.length > 1" class="iv-counter">{{ currentIdx + 1 }} / {{ images.length }}</div>
    <!-- Thumbnails -->
    <div v-if="images.length > 1" class="iv-thumbs">
      <div
        v-for="(img, i) in images"
        :key="i"
        class="iv-thumb"
        :class="{ active: i === currentIdx }"
        @click.stop="currentIdx = i"
      >
        <img :src="img" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.iv-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,.93); z-index: 1000;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; outline: none; flex-direction: column;
}
.iv-img { max-width: 85vw; max-height: 70vh; object-fit: contain; border-radius: 8px; cursor: default; }
.iv-close {
  position: fixed; top: 20px; right: 20px; width: 44px; height: 44px;
  border-radius: 50%; background: rgba(255,255,255,.15); color: #fff;
  border: none; cursor: pointer; font-size: 22px; display: flex;
  align-items: center; justify-content: center; z-index: 1001;
  transition: background var(--transition-fast);
}
.iv-close:hover { background: rgba(255,255,255,.3); }
.iv-nav {
  position: fixed; top: 50%; transform: translateY(-50%); z-index: 1001;
  width: 56px; height: 56px; border-radius: 50%;
  background: rgba(255,255,255,.12); color: #fff; border: none;
  cursor: pointer; font-size: 36px; display: flex; align-items: center;
  justify-content: center; transition: background var(--transition-fast);
  user-select: none;
}
.iv-nav:hover { background: rgba(255,255,255,.25); }
.iv-prev { left: 16px; }
.iv-next { right: 16px; }
.iv-counter {
  position: fixed; bottom: 100px; left: 50%; transform: translateX(-50%);
  color: rgba(255,255,255,.7); font-size: 14px; z-index: 1001;
  background: rgba(0,0,0,.4); padding: 4px 16px; border-radius: var(--radius-round);
}
.iv-thumbs {
  display: flex; gap: 8px; margin-top: 16px; max-width: 85vw; overflow-x: auto;
  padding: 4px 0; justify-content: center;
}
.iv-thumb {
  width: 56px; height: 56px; border-radius: 6px; overflow: hidden;
  cursor: pointer; border: 2px solid transparent; flex-shrink: 0;
  transition: border-color var(--transition-fast);
}
.iv-thumb.active { border-color: #fff; }
.iv-thumb img { width: 100%; height: 100%; object-fit: cover; }
.iv-thumb:hover { border-color: rgba(255,255,255,.5); }

@media (max-width: 768px) {
  .iv-nav { width: 44px; height: 44px; font-size: 28px; }
  .iv-prev { left: 8px; }
  .iv-next { right: 8px; }
  .iv-img { max-width: 92vw; max-height: 60vh; }
  .iv-thumbs { margin-top: 12px; }
  .iv-thumb { width: 44px; height: 44px; }
}
</style>