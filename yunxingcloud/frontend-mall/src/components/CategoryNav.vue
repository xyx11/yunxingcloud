<script setup lang="ts">
import type { Category } from '@/types'

defineProps<{
  categories: Category[]
}>()

const emit = defineEmits<{
  (e: 'select', categoryId: number): void
  (e: 'view-all'): void
}>()
</script>

<template>
  <section v-if="categories.length" class="categories-section">
    <div class="section-header">
      <span class="section-title">📂 商品分类</span>
      <span class="section-more" @click="emit('view-all')">查看全部 &gt;</span>
    </div>
    <div class="categories">
      <div v-for="cat in categories.slice(0, 10)" :key="cat.id" class="cat-item" @click="emit('select', cat.id)">
        <div class="cat-icon">{{ cat.icon || '📁' }}</div>
        <div class="cat-name">{{ cat.name }}</div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.categories-section {
  background: var(--bg-white); border-radius: var(--radius-lg);
  padding: var(--space-xl); margin-bottom: var(--space-xxl);
  box-shadow: var(--shadow-sm);
}
.section-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg);
}
.section-title { font-size: var(--font-xl); font-weight: 700; color: var(--text-primary); }
.section-more { font-size: var(--font-base); color: var(--text-tertiary); cursor: pointer; }
.section-more:hover { color: var(--jd-red); }
.categories {
  display: flex; gap: var(--space-lg); justify-content: center;
  overflow-x: auto; padding: var(--space-xs) 0; -webkit-overflow-scrolling: touch;
}
.categories::-webkit-scrollbar { display: none; }
.cat-item { text-align: center; cursor: pointer; flex-shrink: 0; width: 72px; transition: transform var(--transition); }
.cat-item:hover { transform: scale(1.08); }
.cat-icon {
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, var(--jd-red-light), #ffe0e0);
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; margin: 0 auto 6px;
  box-shadow: 0 2px 6px rgba(241,2,21,.08);
}
.cat-name { font-size: var(--font-xs); color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>
