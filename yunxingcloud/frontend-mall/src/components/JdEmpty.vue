<script setup lang="ts">
withDefaults(defineProps<{
  icon?: string
  title?: string
  description?: string
}>(), {
  icon: '📭',
  title: '暂无数据',
  description: '',
})
</script>

<template>
  <div class="empty-state">
    <span class="empty-icon">{{ icon }}</span>
    <h3 class="empty-title">{{ title }}</h3>
    <p v-if="description" class="empty-desc">{{ description }}</p>
    <div v-if="$slots.default" class="empty-action">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-xxxl) var(--space-xl);
  min-height: 240px;
}
.empty-icon { font-size: 56px; margin-bottom: var(--space-lg); }
.empty-title { font-size: var(--font-lg); color: var(--text-secondary); font-weight: 500; margin-bottom: var(--space-sm); }
.empty-desc { font-size: var(--font-base); color: var(--text-tertiary); margin-bottom: var(--space-lg); }
.empty-action { margin-top: var(--space-md); }
</style>
