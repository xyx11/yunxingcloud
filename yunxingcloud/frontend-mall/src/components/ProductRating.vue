<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  rating: number
  count?: number
  size?: number
  showCount?: boolean
}>(), { size: 14, showCount: true })

const starStyle = computed(() => ({
  fontSize: props.size + 'px',
  lineHeight: 1,
}))

interface StarItem { type: 'full' | 'half' | 'empty'; key: number }

const starList = computed<StarItem[]>(() => {
  const r = Math.max(0, Math.min(5, props.rating))
  const full = Math.floor(r)
  const hasHalf = r - full >= 0.25 && r - full < 0.75
  const roundUp = r - full >= 0.75
  const fullCount = roundUp ? Math.min(5, full + 1) : full
  const halfCount = hasHalf ? 1 : 0
  const emptyCount = 5 - fullCount - halfCount

  const result: StarItem[] = []
  for (let i = 0; i < fullCount; i++) result.push({ type: 'full', key: i })
  if (hasHalf) result.push({ type: 'half', key: fullCount })
  for (let i = 0; i < emptyCount; i++) result.push({ type: 'empty', key: fullCount + halfCount + i })
  return result
})
</script>

<template>
  <span class="star-container" role="img" :aria-label="`${rating} 星`">
    <span v-for="s in starList" :key="s.key" :style="[starStyle, {
      color: s.type === 'empty' ? '#ddd' : '#f5a623',
      position: 'relative',
      display: 'inline-block'
    }]">
      <template v-if="s.type === 'half'">
        <span class="star-half">★</span>
        <span class="star-empty-bg">★</span>
      </template>
      <template v-else>★</template>
    </span>
    <span v-if="count !== undefined && showCount" class="rating-count">({{ count }})</span>
  </span>
</template>

<style scoped>
.star-container { display: inline-flex; align-items: center; gap: 1px; }
.star-half { position: absolute; left: 0; top: 0; overflow: hidden; width: 50%; color: #f5a623; }
.star-empty-bg { color: #ddd; }
.rating-count { color: #999; font-size: 12px; margin-left: 4px; }
</style>