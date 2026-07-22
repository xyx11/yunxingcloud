<script setup lang="ts">
withDefaults(defineProps<{
  count?: number
  dot?: boolean
  max?: number
  color?: string
  size?: 'sm' | 'md' | 'lg'
}>(), {
  count: 0,
  dot: false,
  max: 99,
  color: '#f10215',
  size: 'md',
})

const sizeMap: Record<string, string> = { sm: '16px', md: '20px', lg: '24px' }
const fontSizeMap: Record<string, string> = { sm: '10px', md: '12px', lg: '14px' }
</script>

<template>
  <span v-if="dot || count > 0" class="nb-badge" :class="`nb-${size}`" :style="{ background: color, minWidth: sizeMap[size], height: sizeMap[size], fontSize: fontSizeMap[size] }">
    <template v-if="!dot">{{ count > max ? `${max}+` : count }}</template>
  </span>
</template>

<style scoped>
.nb-badge {
  display: inline-flex; align-items: center; justify-content: center;
  border-radius: 100px; color: #fff; font-weight: 700;
  padding: 0 5px; line-height: 1;
  animation: nb-pop .3s ease-out;
}
@keyframes nb-pop { 0% { transform: scale(0); } 70% { transform: scale(1.2); } 100% { transform: scale(1); } }
.nb-sm { min-width: 16px; height: 16px; font-size: 10px; padding: 0 4px; }
.nb-md { min-width: 20px; height: 20px; font-size: 12px; padding: 0 5px; }
.nb-lg { min-width: 24px; height: 24px; font-size: 14px; padding: 0 6px; }
</style>