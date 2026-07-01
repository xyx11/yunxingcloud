<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{ src: string; alt?: string; width?: string; height?: string }>()
const zoomed = ref(false)
const pos = ref({ x: 0, y: 0 })
const bgPos = ref('center center')

function onMove(e: MouseEvent) {
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect()
  const x = ((e.clientX - rect.left) / rect.width) * 100
  const y = ((e.clientY - rect.top) / rect.height) * 100
  pos.value = { x: e.clientX - rect.left, y: e.clientY - rect.top }
  bgPos.value = `${x}% ${y}%`
}
</script>

<template>
  <div
    :style="{width:width||'100%',height:height||'420px',overflow:'hidden',cursor:zoomed?'zoom-out':'zoom-in',position:'relative',borderRadius:'8px'}"
    @mouseenter="zoomed=true" @mouseleave="zoomed=false" @mousemove="onMove"
  >
    <img v-if="src && src !== '📦'" :src="src" :alt="alt" :style="{width:'100%',height:'100%',objectFit:'cover',transition:'opacity .2s',opacity:zoomed?0:1}" />
    <span v-else :style="{width:'100%',height:'100%',display:'flex',alignItems:'center',justifyContent:'center',fontSize:'80px',background:'linear-gradient(135deg,#f8f8f8,#eee)'}">📦</span>
    <div v-if="zoomed && src && src !== '📦'" style="position:absolute;top:0;left:0;width:100%;height:100%;z-index:10;pointer-events:none;border-radius:8px"
         :style="{backgroundImage:`url(${src})`,backgroundPosition:bgPos,backgroundSize:'200% 200%',backgroundRepeat:'no-repeat'}" />
  </div>
</template>