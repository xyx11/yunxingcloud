<script setup lang="ts">
import { useCompare } from '@/composables/useCompare'
import { formatPrice } from '@/utils/format'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import { useRouter, useRoute } from 'vue-router'

const { items, remove, clear } = useCompare()
const router = useRouter()
const route = useRoute()
const isFullscreen = route.path === '/compare'
</script>

<template>
  <!-- Fullscreen compare page -->
  <div v-if="isFullscreen" class="compare-page">
    <div class="compare-back">
      <button class="back-btn" @click="router.back()">← 返回</button>
    </div>
    <h2 class="compare-page-title">商品对比</h2>
    <div v-if="items.length" class="compare-page-grid">
      <div v-for="p in items" :key="p.id" class="compare-page-card">
        <button class="compare-page-remove" @click="remove(p.id)" aria-label="移除">✕</button>
        <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="200px" />
        <h3 class="compare-page-name">{{ p.name }}</h3>
        <div class="compare-page-price">{{ formatPrice(p.price / 100, 2) }}</div>
        <div class="compare-page-sales">已售 {{ p.sales || 0 }} 件</div>
        <JdButton size="sm" @click="router.push('/product/' + p.id)">查看详情</JdButton>
      </div>
    </div>
    <div v-else class="compare-page-empty">暂无对比商品，去逛逛吧</div>
  </div>

  <!-- Floating bar -->
  <div v-else-if="items.length" class="compare-bar">
    <div class="compare-header">
      <h3 class="compare-title">商品对比 ({{ items.length }}/3)</h3>
      <div class="compare-header-actions">
        <button class="btn-clear" @click="clear">清空</button>
        <button class="btn-full" @click="router.push('/compare')">全屏对比</button>
      </div>
    </div>
    <div class="compare-grid">
      <div v-for="p in items" :key="p.id" class="compare-item">
        <button class="compare-item-remove" @click="remove(p.id)" aria-label="移除">✕</button>
        <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="100px" rounded="8px" />
        <h4 class="compare-item-name">{{ p.name }}</h4>
        <span class="compare-item-price">{{ formatPrice(p.price / 100, 2) }}</span>
        <div class="compare-item-sales">销量 {{ p.sales || 0 }}</div>
      </div>
      <div v-for="i in (3 - items.length)" :key="'empty-' + i" class="compare-empty">
        点击商品下方"对比"添加
      </div>
    </div>
  </div>
</template>

<style scoped>
.compare-bar {
  position: fixed; bottom: 70px; left: 50%; transform: translateX(-50%); z-index: 210;
  background: var(--bg-white); border-radius: var(--radius-xl);
  box-shadow: 0 8px 40px rgba(0,0,0,.15); padding: 20px 24px;
  max-width: 900px; width: calc(100% - 40px);
}
[data-theme="dark"] .compare-bar { background: var(--bg-card); box-shadow: 0 8px 40px rgba(0,0,0,.4); }
.compare-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.compare-title { font-size: 16px; font-weight: 700; }
.compare-header-actions { display: flex; gap: 8px; }
.btn-clear {
  padding: 4px 12px; border: 1px solid var(--border); background: var(--bg-white);
  border-radius: var(--radius-sm); cursor: pointer; font-size: 12px; color: var(--text-tertiary);
  transition: all var(--transition-fast);
}
.btn-clear:hover { border-color: var(--text-tertiary); }
.btn-full {
  padding: 4px 12px; background: var(--jd-red); color: #fff; border: none;
  border-radius: var(--radius-sm); cursor: pointer; font-size: 12px;
}
.compare-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.compare-item { text-align: center; position: relative; }
.compare-item-remove {
  position: absolute; top: -8px; right: -8px; width: 28px; height: 28px;
  border-radius: 50%; background: #f44336; color: #fff; border: none;
  cursor: pointer; font-size: 11px; line-height: 20px;
}
.compare-item-name {
  font-size: 12px; overflow: hidden; text-overflow: ellipsis;
  white-space: nowrap; margin-bottom: 4px; color: var(--text-primary);
}
.compare-item-price { color: var(--jd-red); font-size: 16px; font-weight: 700; }
.compare-item-sales { font-size: 11px; color: var(--text-tertiary); }
.compare-empty {
  text-align: center; border: 2px dashed var(--border); border-radius: var(--radius-md);
  height: 160px; display: flex; align-items: center; justify-content: center;
  color: var(--text-placeholder); font-size: 12px;
}
[data-theme="dark"] .compare-empty { border-color: var(--border-light); }

.compare-page { max-width: 900px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }
.compare-back { margin-bottom: var(--space-lg); }
.back-btn { background: none; border: none; color: var(--text-secondary); cursor: pointer; font-size: var(--font-md); }
.compare-page-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-xl); }
.compare-page-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: var(--space-lg); }
.compare-page-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); text-align: center; position: relative; box-shadow: var(--shadow-sm); }
.compare-page-remove { position: absolute; top: 10px; right: 10px; width: 24px; height: 24px; border-radius: 50%; background: var(--jd-red); color: #fff; border: none; cursor: pointer; font-size: 12px; }
.compare-page-name { font-size: var(--font-md); font-weight: 600; margin: var(--space-sm) 0; }
.compare-page-price { color: var(--jd-red); font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-xs); }
.compare-page-sales { color: var(--text-tertiary); font-size: var(--font-sm); margin-bottom: var(--space-md); }
.compare-page-empty { text-align: center; padding: 80px var(--space-md); color: var(--text-tertiary); }

@media (max-width: 768px) {
  .compare-bar { padding: 12px 16px; border-radius: var(--radius-lg); }
  .compare-grid { gap: 8px; }
  .compare-item-name { font-size: 11px; }
  .compare-item-price { font-size: 14px; }
  .compare-page { padding: var(--space-lg) var(--space-md) 80px; }
  .compare-page-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-md); }
}
</style>
