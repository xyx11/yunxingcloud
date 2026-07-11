<script setup lang="ts">
withDefaults(defineProps<{
  variant?: 'text' | 'card' | 'circle' | 'list-item' | 'banner'
  width?: string
  height?: string
  count?: number
  gap?: string
  columns?: number
}>(), {
  variant: 'text',
  height: '16px',
  count: 3,
  gap: '12px',
})
</script>

<template>
  <!-- Card Grid -->
  <div v-if="variant === 'card' && columns" class="sk-grid" :style="{ gridTemplateColumns: `repeat(${columns},1fr)` }">
    <div v-for="i in (count || 4)" :key="i" class="sk-card">
      <div class="sk-image" />
      <div class="sk-card-body">
        <div class="sk-line sk-card-line-1" />
        <div class="sk-line sk-card-line-2" />
        <div class="sk-line sk-card-line-3" />
      </div>
    </div>
  </div>

  <!-- List Item -->
  <div v-else-if="variant === 'list-item'" class="sk-list">
    <div v-for="i in (count || 4)" :key="i" class="sk-list-item">
      <div class="sk-image sk-list-img" />
      <div class="sk-list-body">
        <div class="sk-line sk-list-line-1" />
        <div class="sk-line sk-list-line-2" />
        <div class="sk-line sk-list-line-3" />
      </div>
    </div>
  </div>

  <!-- Circle (avatar) -->
  <div v-else-if="variant === 'circle'" class="sk-circle" :style="{ width, height }" />

  <!-- Banner -->
  <div v-else-if="variant === 'banner'" class="sk-banner" :style="{ height: height || '200px', borderRadius: '12px' }" />

  <!-- Text Lines -->
  <template v-else>
    <div v-for="i in count" :key="i" class="sk-line" :style="{ height, width: width || (100 - i * 15) + '%', marginBottom: gap }" />
  </template>
</template>

<style scoped>
.sk-grid {
  display: grid;
  gap: var(--gap, 14px);
}
.sk-card {
  background: var(--bg-hover);
  border-radius: var(--radius-md);
  overflow: hidden;
}
.sk-card-body { padding: var(--space-md); }

.sk-image {
  background: linear-gradient(90deg, var(--border-light) 0%, var(--border) 50%, var(--border-light) 100%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  aspect-ratio: 1;
}
.sk-banner {
  background: linear-gradient(90deg, var(--border-light) 0%, var(--border) 50%, var(--border-light) 100%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
.sk-list { display: flex; flex-direction: column; }
.sk-list-item {
  display: flex; gap: var(--space-md); padding: var(--space-md) 0;
  border-bottom: 1px solid var(--border-light);
}
.sk-list-body { flex: 1; }

.sk-line {
  background: linear-gradient(90deg, var(--border-light) 0%, var(--border) 50%, var(--border-light) 100%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-sm);
}
.sk-circle {
  background: linear-gradient(90deg, var(--border-light) 0%, var(--border) 50%, var(--border-light) 100%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 50%;
}

.sk-card-line-1 { width: 80%; height: 14px; margin-bottom: 8px; }
.sk-card-line-2 { width: 50%; height: 18px; margin-bottom: 6px; }
.sk-card-line-3 { width: 35%; height: 12px; }

.sk-list-img { width: 80px; height: 80px; border-radius: 8px; }
.sk-list-line-1 { width: 90%; height: 14px; margin-bottom: 8px; }
.sk-list-line-2 { width: 60%; height: 12px; margin-bottom: 6px; }
.sk-list-line-3 { width: 30%; height: 16px; }

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
