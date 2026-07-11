<script setup lang="ts">
import { useCompare } from '@/composables/useCompare'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { formatPrice } from '@/utils/format'
import type { CompareItem } from '@/types'
import LazyImage from '@/components/LazyImage.vue'
import { useRouter } from 'vue-router'
import { useI18n } from '@/locales'
const { t } = useI18n()

const { items, remove, clear } = useCompare()
const toast = useToast()
const router = useRouter()

async function quickAdd(p: CompareItem) { try { await addToCart(p.id, 1); toast.success('已加入购物车') } catch { toast.error('添加失败') } }

const specs = ['price', 'sales', 'description']
const specLabel: Record<string, string> = { price: '价格', sales: '销量', description: '描述' }
</script>

<template>
  <div v-if="items.length" class="compare-bar">
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
  position: fixed; bottom: 20px; left: 50%; transform: translateX(-50%); z-index: 200;
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
  position: absolute; top: -6px; right: -6px; width: 20px; height: 20px;
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

@media (max-width: 768px) {
  .compare-bar { padding: 12px 16px; border-radius: var(--radius-lg); }
  .compare-grid { gap: 8px; }
  .compare-item-name { font-size: 11px; }
  .compare-item-price { font-size: 14px; }
}
</style>
