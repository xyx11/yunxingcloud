<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from '@/locales'
import type { Review } from '@/types'

const { t } = useI18n()
const props = defineProps<{ reviews: Review[] }>()

const total = computed(() => props.reviews.length)
const avg = computed(() => total.value ? (props.reviews.reduce((s, r) => s + (r.rating || 0), 0) / total.value).toFixed(1) : 0)

const distribution = computed(() => {
  const dist: Record<number, number> = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 }
  props.reviews.forEach(r => { if (r.rating >= 1 && r.rating <= 5) dist[r.rating]++ })
  return Object.entries(dist).sort((a, b) => Number(b[0]) - Number(a[0]))
})
</script>

<template>
  <div v-if="total" class="rs-wrap">
    <div class="rs-score">
      <div class="rs-avg">{{ avg }}</div>
      <div class="rs-stars">{{ '★'.repeat(Math.round(Number(avg))) }}</div>
      <div class="rs-total">{{ t('product.reviewCount', { n: total }) }}</div>
    </div>
    <div class="rs-bars">
      <div v-for="[star, count] in distribution" :key="star" class="rs-bar-row">
        <span class="rs-bar-label">{{ t('product.starLabel', { n: star }) }}</span>
        <div class="rs-bar-track">
          <div class="rs-bar-fill" :style="{ width: (count / Math.max(1, total) * 100) + '%' }" />
        </div>
        <span class="rs-bar-count">{{ count }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.rs-wrap { display: flex; gap: 20px; align-items: center; padding: 16px 0; border-bottom: 1px solid var(--border-light); margin-bottom: 16px; }
.rs-score { text-align: center; flex-shrink: 0; }
.rs-avg { font-size: 36px; font-weight: 700; color: var(--jd-red); line-height: 1; }
.rs-stars { color: #f90; font-size: var(--font-md); }
.rs-total { color: var(--text-tertiary); font-size: 11px; }
.rs-bars { flex: 1; }
.rs-bar-row { display: flex; align-items: center; gap: 8px; margin-bottom: 2px; }
.rs-bar-label { font-size: 11px; color: var(--text-secondary); width: 32px; text-align: right; }
.rs-bar-track { flex: 1; height: 6px; background: var(--border-light); border-radius: 3px; overflow: hidden; }
.rs-bar-fill { height: 100%; background: linear-gradient(90deg, #f90, #ffc107); border-radius: 3px; transition: width .6s; }
.rs-bar-count { font-size: 10px; color: var(--text-tertiary); width: 20px; }
</style>
