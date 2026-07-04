<script setup lang="ts">
import { ref } from 'vue'
import LazyImage from './LazyImage.vue'

const props = defineProps<{ src: string; alt?: string; height?: string }>()
const zoomed = ref(false)
const bgPos = ref('center center')

function calcPos(clientX: number, clientY: number, el: HTMLElement) {
  const rect = el.getBoundingClientRect()
  const x = ((clientX - rect.left) / rect.width) * 100
  const y = ((clientY - rect.top) / rect.height) * 100
  bgPos.value = `${x}% ${y}%`
}

function onMouseMove(e: MouseEvent) {
  calcPos(e.clientX, e.clientY, e.currentTarget as HTMLElement)
}

// Touch support
let lastDist = 0
function onTouchMove(e: TouchEvent) {
  if (e.touches.length === 1) {
    calcPos(e.touches[0].clientX, e.touches[0].clientY, e.currentTarget as HTMLElement)
  }
  if (e.touches.length === 2) {
    const dx = e.touches[0].clientX - e.touches[1].clientX
    const dy = e.touches[0].clientY - e.touches[1].clientY
    lastDist = Math.hypot(dx, dy)
  }
}
</script>

<template>
  <div
    :style="{
      width:'100%', height:height||'420px',
      overflow:'hidden', cursor:zoomed?'zoom-out':'zoom-in',
      position:'relative', borderRadius:'8px',
    }"
    @mouseenter="zoomed=true" @mouseleave="zoomed=false"
    @mousemove="onMouseMove"
    @touchstart="zoomed=true" @touchend="zoomed=false"
    @touchmove="onTouchMove"
  >
    <LazyImage :src="src" :alt="alt" :height="height||'420px'" />
    <div
      v-if="zoomed && src && src !== '📦'"
      style="position:absolute;top:0;left:0;width:100%;height:100%;z-index:10;pointer-events:none;border-radius:8px"
      :style="{
        backgroundImage:`url(${src})`,
        backgroundPosition:bgPos,
        backgroundSize:'250% 250%',
        backgroundRepeat:'no-repeat',
      }"
    />
  </div>
</template>