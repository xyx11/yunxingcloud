<script setup lang="ts">
defineProps<{
  width?: string; height?: string; rounded?: string
  count?: number; gap?: string; columns?: number
}>()
</script>

<template>
  <div v-if="columns" :style="{display:'grid',gridTemplateColumns:`repeat(${columns},1fr)`,gap:gap||'14px'}">
    <div v-for="i in (count||4)" :key="i" :style="{height:height||'200px',background:'linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0)',backgroundSize:'200% 100%',animation:'shimmer 1.5s infinite',borderRadius:rounded||'8px'}" />
  </div>
  <template v-else>
    <div v-for="i in (count||3)" :key="i"
         :style="{height:height||'16px',width:width||(100-i*15)+'%',background:'linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0)',backgroundSize:'200% 100%',animation:'shimmer 1.5s infinite',borderRadius:rounded||'4px',marginBottom:gap||'12px'}" />
  </template>
</template>

<style scoped>
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
</style>