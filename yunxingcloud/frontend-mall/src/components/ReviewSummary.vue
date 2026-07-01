<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ reviews: any[] }>()

const total = computed(() => props.reviews.length)
const avg = computed(() => total.value ? (props.reviews.reduce((s, r) => s + (r.rating || 0), 0) / total.value).toFixed(1) : 0)

const distribution = computed(() => {
  const dist: Record<number, number> = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 }
  props.reviews.forEach(r => { if (r.rating >= 1 && r.rating <= 5) dist[r.rating]++ })
  return Object.entries(dist).sort((a, b) => Number(b[0]) - Number(a[0]))
})
</script>

<template>
  <div v-if="total" style="display:flex;gap:20px;align-items:center;padding:16px 0;border-bottom:1px solid #f0f0f0;margin-bottom:16px">
    <div style="text-align:center;flex-shrink:0">
      <div style="font-size:36px;font-weight:700;color:#e4393c;line-height:1">{{ avg }}</div>
      <div style="color:#f90;font-size:14px">{{ '★'.repeat(Math.round(Number(avg))) }}</div>
      <div style="color:#999;font-size:11px">{{ total }} 条评价</div>
    </div>
    <div style="flex:1">
      <div v-for="[star, count] in distribution" :key="star" style="display:flex;align-items:center;gap:8px;margin-bottom:2px">
        <span style="font-size:11px;color:#666;width:24px;text-align:right">{{ star }}星</span>
        <div style="flex:1;height:6px;background:#f0f0f0;border-radius:3px;overflow:hidden">
          <div style="height:100%;background:linear-gradient(90deg,#f90,#ffc107);border-radius:3px;transition:width .6s" :style="{width:(count/Math.max(1,total)*100)+'%'}"></div>
        </div>
        <span style="font-size:10px;color:#999;width:24px">{{ count }}</span>
      </div>
    </div>
  </div>
</template>