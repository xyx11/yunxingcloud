<script setup lang="ts">
import { ref, onMounted } from 'vue'

const props = withDefaults(defineProps<{ src: string; alt?: string; placeholder?: string }>(), { placeholder: '📦' })
const loaded = ref(false)
const el = ref<HTMLElement>()

onMounted(() => {
  if (!el.value) return
  const observer = new IntersectionObserver(([entry]) => {
    if (entry.isIntersecting) { loaded.value = true; observer.disconnect() }
  }, { rootMargin: '200px' })
  observer.observe(el.value)
})
</script>

<template>
  <div ref="el" :style="{background:'linear-gradient(135deg,#f8f8f8,#eee)',display:'flex',alignItems:'center',justifyContent:'center',fontSize:'48px',minHeight:'100px'}">
    <img v-if="loaded && src && src !== '📦'" :src="src" :alt="alt" style="width:100%;height:100%;object-fit:cover" @load="(e:any) => e.target.style.opacity='1'" :style="{opacity:0,transition:'opacity .3s'}" />
    <span v-else>{{ placeholder }}</span>
  </div>
</template>