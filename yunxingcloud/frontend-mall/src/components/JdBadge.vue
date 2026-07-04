<script setup lang="ts">
withDefaults(defineProps<{
  type?: 'red' | 'orange' | 'green' | 'blue' | 'gray'
  size?: 'sm' | 'md'
  dot?: boolean
}>(), {
  type: 'red',
  size: 'sm',
  dot: false,
})
</script>

<template>
  <span class="jd-badge" :class="[`jd-badge--${type}`, `jd-badge--${size}`, { 'jd-badge--dot': dot }]">
    <span v-if="dot" class="badge-dot" />
    <slot />
  </span>
</template>

<style scoped>
.jd-badge {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  border-radius: var(--radius-sm);
  font-weight: 600;
  white-space: nowrap;
}
.jd-badge--sm { font-size: 10px; padding: 1px 6px; }
.jd-badge--md { font-size: var(--font-xs); padding: 2px 8px; }

.jd-badge--red   { background: var(--jd-red-light); color: var(--jd-red); }
.jd-badge--orange { background: var(--orange-bg); color: var(--orange); }
.jd-badge--green { background: var(--green-bg); color: var(--green); }
.jd-badge--blue  { background: var(--blue-bg); color: var(--blue); }
.jd-badge--gray  { background: var(--bg-hover); color: var(--text-tertiary); }

.jd-badge--dot .badge-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: currentColor;
}
</style>
