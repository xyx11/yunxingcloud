<script setup lang="ts">
import { computed } from 'vue'
import { useCompare } from '@/composables/useCompare'
import { formatPrice, formatCount } from '@/utils/format'
import { useI18n } from '@/locales'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useRouter, useRoute } from 'vue-router'

const { items, remove, clear } = useCompare()
const router = useRouter()
const route = useRoute()
const toast = useToast()
const { t } = useI18n()
const isFullscreen = route.path === '/compare'

interface SpecRow { label: string; values: string[]; diff: boolean }

const specRows = computed<SpecRow[]>(() => {
  if (items.value.length < 2) return []
  const rows: { label: string; key: string; format?: (v: any) => string }[] = [
    { label: t('product.price'), key: 'price', format: (v: number) => formatPrice(v / 100, 2) },
    { label: t('product.salesCount'), key: 'sales', format: (v: number) => formatCount(v || 0) },
    { label: t('rating.title'), key: 'rating', format: (v: number) => v ? v + ' ' + t('common.score') : '-' },
    { label: t('product.reviewCount'), key: 'reviewCount', format: (v: number) => v ? formatCount(v) : '-' },
    { label: t('product.brand'), key: 'brandName', format: (v: any) => v || '-' },
    { label: t('product.category'), key: 'categoryName', format: (v: any) => v || '-' },
    { label: t('product.stock'), key: 'stock', format: (v: number) => v !== undefined ? String(v) : '-' },
    { label: t('product.originalPrice'), key: 'originalPrice', format: (v: number) => v ? formatPrice(v / 100, 2) : '-' },
  ]
  return rows.map(row => {
    const values = items.value.map(p => row.format ? row.format((p as any)[row.key]) : ((p as any)[row.key] || '-'))
    const diff = values.length >= 2 && !values.every(v => v === values[0])
    return { label: row.label, values, diff }
  })
})

const hasDiffs = computed(() => specRows.value.some(r => r.diff))

async function quickAdd(productId: number) {
  try { await addToCart(productId, 1); toast.success(t('toast.addedToCart')) } catch { toast.error(t('toast.addCartFail')) }
}
</script>

<template>
  <!-- Fullscreen compare page -->
  <div v-if="isFullscreen" class="compare-page">
    <div class="compare-back">
      <button class="back-btn" @click="router.back()">← {{ t('common.cancel') }}</button>
    </div>
    <h2 class="compare-page-title">{{ t('compare.title') }}</h2>

    <div v-if="items.length" class="compare-page-content">
      <!-- Product cards -->
      <div class="compare-page-grid">
        <div v-for="p in items" :key="p.id" class="compare-page-card">
          <button class="compare-page-remove" @click="remove(p.id)" aria-label="移除">✕</button>
          <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="200px" />
          <h3 class="compare-page-name">{{ p.name }}</h3>
          <div class="compare-page-price">{{ formatPrice(p.price / 100, 2) }}</div>
          <div class="compare-page-sales">{{ t('product.salesCount') }} {{ formatCount(p.sales || 0) }}</div>
          <div class="compare-page-actions">
            <JdButton size="sm" @click="router.push('/product/' + p.id)">{{ t('product.detail') }}</JdButton>
            <JdButton size="sm" type="outline" @click="quickAdd(p.id)">+ {{ t('product.addToCart') }}</JdButton>
          </div>
        </div>
      </div>

      <!-- Spec comparison table -->
      <div v-if="specRows.length" class="compare-table-wrap">
        <div class="compare-table-title">
          <span>{{ t('compare.specComparison') }}</span>
          <span v-if="hasDiffs" class="compare-diff-hint">{{ t('compare.diffHighlight') }}</span>
        </div>
        <div class="compare-table-scroll">
          <table class="compare-spec-table">
            <thead>
              <tr>
                <th class="spec-label-col" />
                <th v-for="p in items" :key="p.id" class="spec-col">{{ p.name }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in specRows" :key="row.label" :class="{ 'diff-row': row.diff }">
                <td class="spec-label-col">{{ row.label }}</td>
                <td v-for="(val, j) in row.values" :key="j" class="spec-col" :class="{ 'diff-val': row.diff }">
                  {{ val }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Clear button -->
      <div class="compare-clear-wrap">
        <JdButton type="outline" @click="clear">{{ t('compare.clearAll') }}</JdButton>
      </div>
    </div>

    <div v-else class="compare-page-empty">{{ t('compare.empty') }}</div>
  </div>

  <!-- Floating compare bar -->
  <div v-else-if="items.length" class="compare-bar">
    <div class="compare-header">
      <h3 class="compare-title">{{ t('compare.title') }} ({{ items.length }}/3)</h3>
      <div class="compare-header-actions">
        <button class="btn-clear" @click="clear">{{ t('wishlist.remove') }}</button>
        <button class="btn-full" @click="router.push('/compare')">{{ t('compare.fullscreen') }}</button>
      </div>
    </div>
    <div class="compare-grid">
      <div v-for="p in items" :key="p.id" class="compare-item">
        <button class="compare-item-remove" @click="remove(p.id)" aria-label="移除">✕</button>
        <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="100px" rounded="8px" />
        <h4 class="compare-item-name">{{ p.name }}</h4>
        <span class="compare-item-price">{{ formatPrice(p.price / 100, 2) }}</span>
        <div class="compare-item-sales">{{ t('product.salesCount') }} {{ formatCount(p.sales || 0) }}</div>
      </div>
      <div v-for="i in (3 - items.length)" :key="'empty-' + i" class="compare-empty-slot">
        {{ t('compare.addHint') }}
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ===== Floating bar ===== */
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
.compare-empty-slot {
  text-align: center; border: 2px dashed var(--border); border-radius: var(--radius-md);
  height: 160px; display: flex; align-items: center; justify-content: center;
  color: var(--text-placeholder); font-size: 12px;
}

/* ===== Fullscreen page ===== */
.compare-page { max-width: 960px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }
.compare-back { margin-bottom: var(--space-lg); }
.back-btn { background: none; border: none; color: var(--text-secondary); cursor: pointer; font-size: var(--font-md); padding: 0; }
.back-btn:hover { color: var(--jd-red); }
.compare-page-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-xl); }

.compare-page-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--space-lg); margin-bottom: var(--space-xxl);
}
.compare-page-card {
  background: var(--bg-white); border-radius: var(--radius-lg);
  padding: var(--space-xl); text-align: center; position: relative;
  box-shadow: var(--shadow-sm); transition: box-shadow var(--transition);
}
.compare-page-card:hover { box-shadow: var(--shadow-md); }
.compare-page-remove {
  position: absolute; top: 10px; right: 10px; width: 24px; height: 24px;
  border-radius: 50%; background: var(--jd-red); color: #fff; border: none;
  cursor: pointer; font-size: 12px; display: flex; align-items: center; justify-content: center;
}
.compare-page-remove:hover { background: #d32f2f; }
.compare-page-name { font-size: var(--font-md); font-weight: 600; margin: var(--space-sm) 0; }
.compare-page-price { color: var(--jd-red); font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-xs); }
.compare-page-sales { color: var(--text-tertiary); font-size: var(--font-sm); margin-bottom: var(--space-md); }
.compare-page-actions { display: flex; gap: var(--space-sm); justify-content: center; flex-wrap: wrap; }
.compare-page-empty { text-align: center; padding: 80px var(--space-md); color: var(--text-tertiary); }

/* Spec table */
.compare-table-wrap { margin-bottom: var(--space-xxl); }
.compare-table-title {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--space-md); font-size: var(--font-lg); font-weight: 600;
}
.compare-diff-hint {
  font-size: var(--font-xs); color: var(--orange); font-weight: 500;
  background: #fff3e0; padding: 2px 10px; border-radius: var(--radius-round);
}
.compare-table-scroll { overflow-x: auto; -webkit-overflow-scrolling: touch; }
.compare-spec-table {
  width: 100%; border-collapse: collapse; background: var(--bg-white);
  border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-sm);
}
.compare-spec-table thead { position: sticky; top: 0; z-index: 1; }
.compare-spec-table th, .compare-spec-table td {
  padding: var(--space-md) var(--space-lg); text-align: center;
  border-bottom: 1px solid var(--border-light); font-size: var(--font-sm);
}
.compare-spec-table th {
  background: var(--bg-hover); font-weight: 600; white-space: nowrap;
}
.spec-label-col { text-align: left; color: var(--text-secondary); font-weight: 500; width: 90px; white-space: nowrap; }
.spec-col { min-width: 100px; }
.diff-row { background: #fff8e1; }
[data-theme="dark"] .diff-row { background: rgba(255,193,7,.08); }
.diff-val { color: var(--orange); font-weight: 700; }

.compare-clear-wrap { text-align: center; }

@media (max-width: 768px) {
  .compare-bar { padding: 12px 16px; border-radius: var(--radius-lg); }
  .compare-grid { gap: 8px; }
  .compare-item-name { font-size: 11px; }
  .compare-item-price { font-size: 14px; }
  .compare-page { padding: var(--space-lg) var(--space-md) 80px; }
  .compare-page-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-md); }
  .compare-page-card { padding: var(--space-md); }
  .compare-spec-table th, .compare-spec-table td { padding: var(--space-sm) var(--space-md); font-size: 12px; }
}
</style>