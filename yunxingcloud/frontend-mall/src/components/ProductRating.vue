<script setup lang="ts">
defineProps<{ rating: number; count?: number; size?: number }>()
const stars = (r: number) => {
  const full = Math.floor(r)
  const half = r - full >= 0.5
  const empty = 5 - full - (half ? 1 : 0)
  return { full, half, empty }
}
</script>

<template>
  <span style="display:inline-flex;align-items:center;gap:2px">
    <span v-for="i in stars(rating).full" :key="'f'+i" style="color:#f5a623;font-size:14px">★</span>
    <span v-if="stars(rating).half" style="color:#f5a623;font-size:14px">★</span>
    <span v-for="i in stars(rating).empty" :key="'e'+i" style="color:#ddd;font-size:14px">★</span>
    <span v-if="count !== undefined" style="color:#999;font-size:12px;margin-left:4px">({{ count }})</span>
  </span>
</template>