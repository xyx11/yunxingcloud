<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import { getFavorites, removeFavorite } from '@/api/order'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useCartFly } from '@/composables/useCartFly'
import { useI18n } from '@/locales'
import { formatPrice } from '@/utils/format'
import type { FavoriteItem } from '@/types'

const router = useRouter()
const toast = useToast()
const { flyToCart } = useCartFly()
const { t } = useI18n()

const items = ref<FavoriteItem[]>([])
const loading = ref(true)
const loadError = ref(false)
const selectMode = ref(false)
const selectedIds = ref<Set<number>>(new Set())
const sortBy = ref<'newest' | 'oldest' | 'priceAsc' | 'priceDesc'>('newest')
const batchLoading = ref(false)

function getPid(p: FavoriteItem) { return p.productId || p.id }

const sortedItems = computed(() => {
  const list = [...items.value]
  switch (sortBy.value) {
    case 'priceAsc': return list.sort((a, b) => (a.price || 0) - (b.price || 0))
    case 'priceDesc': return list.sort((a, b) => (b.price || 0) - (a.price || 0))
    case 'oldest': return list.reverse()
    default: return list
  }
})

const allSelected = computed(() =>
  sortedItems.value.length > 0 && sortedItems.value.every(p => selectedIds.value.has(getPid(p)))
)

const selectedCount = computed(() => selectedIds.value.size)

const sortOptions = [
  { key: 'newest' as const, label: t('wishlist.sortNewest') },
  { key: 'oldest' as const, label: t('wishlist.sortOldest') },
  { key: 'priceAsc' as const, label: t('wishlist.sortPriceAsc') },
  { key: 'priceDesc' as const, label: t('wishlist.sortPriceDesc') },
]

async function load() {
  loading.value = true
  loadError.value = false
  try { const r = await getFavorites(); items.value = r.data || []; loading.value = false;
    } catch { loadError.value = true; toast.error(t('wishlist.loadFail')) }
}

function toggleSelect(id: number) {
  const s = new Set(selectedIds.value)
  s.has(id) ? s.delete(id) : s.add(id)
  selectedIds.value = s
}

function toggleSelectAll() {
  selectedIds.value = allSelected.value
    ? new Set()
    : new Set(sortedItems.value.map(p => getPid(p)))
}

function exitSelectMode() {
  selectMode.value = false
  selectedIds.value = new Set()
}

async function batchRemove() {
  const ids = [...selectedIds.value]
  batchLoading.value = true
  try {
    await Promise.all(ids.map(id => removeFavorite(id)))
    items.value = items.value.filter(i => !ids.includes(getPid(i)))
    toast.success(t('wishlist.batchRemoveDone', { n: ids.length }))
    exitSelectMode()
  } catch { toast.error(t('common.updateFailed')) }
  finally { batchLoading.value = false }
}

async function batchAddCart() {
  const ids = [...selectedIds.value]
  batchLoading.value = true
  let success = 0
  for (const id of ids) {
    try { await addToCart(id, 1); success++ } catch { /* skip failed */ }
  }
  if (success > 0) { toast.success(t('wishlist.batchAddDone', { n: success })); exitSelectMode() }
  else toast.error(t('toast.addCartFail'))
  batchLoading.value = false
}

async function unfav(productId: number) {
  try { await removeFavorite(productId); items.value = items.value.filter(i => getPid(i) !== productId); toast.info(t('product.unfavorite')) } catch { toast.error(t('common.updateFailed')) }
}

async function quickAdd(e: Event, p: FavoriteItem) {
  e.stopPropagation()
  const pid = getPid(p)
  try { await addToCart(pid, 1); toast.success(t('toast.addedToCart')); flyToCart(e as MouseEvent) } catch { toast.error(t('toast.addCartFail')) }
}

function goDetail(id: number) {
  if (selectMode.value) { toggleSelect(id); return }
  router.push(`/product/${id}`)
}

onMounted(load)
</script>

<template>
  <div>
    <!-- Header -->
    <div class="wl-header">
      <div class="wl-header-left">
        <h2 class="page-title">{{ t('wishlist.title') }}</h2>
        <span v-if="items.length" class="wl-count">{{ t('wishlist.count', { n: items.length }) }}</span>
      </div>
      <div class="wl-header-right">
        <!-- Sort dropdown -->
        <select v-if="items.length && !selectMode" v-model="sortBy" class="wl-sort">
          <option v-for="o in sortOptions" :key="o.key" :value="o.key">{{ o.label }}</option>
        </select>
        <!-- Select mode controls -->
        <template v-if="selectMode">
          <button class="wl-exit-select" @click="exitSelectMode">{{ t('wishlist.exitSelect') }}</button>
        </template>
        <button
          v-if="items.length && !selectMode"
          class="wl-enter-select"
          @click="selectMode = true"
        >
          {{ t('wishlist.manage') }}
        </button>
      </div>
    </div>

    <!-- Batch action bar -->
    <div v-if="selectMode && items.length" class="wl-batch-bar">
      <label class="wl-select-all">
        <input type="checkbox" :checked="allSelected" @change="toggleSelectAll" />
        <span>{{ allSelected ? t('wishlist.deselectAll') : t('wishlist.selectAll') }}</span>
        <span class="wl-selected-count">{{ t('wishlist.selected', { n: selectedCount }) }}</span>
      </label>
      <div class="wl-batch-actions">
        <JdButton size="sm" type="outline" :loading="batchLoading" @click="batchAddCart">
          🛒 {{ t('wishlist.batchAddCart') }}
        </JdButton>
        <JdButton size="sm" type="outline" class="wl-batch-del" :loading="batchLoading" @click="batchRemove">
          🗑 {{ t('wishlist.batchRemove') }}
        </JdButton>
      </div>
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading" class="wl-grid">
      <div v-for="i in 4" :key="i" class="wl-skel">
        <div class="wl-skel-img" /><div class="wl-skel-body"><div class="wl-skel-line" /></div>
      </div>
    </div>

    <!-- Product grid -->
    <div v-else-if="sortedItems.length" class="wl-grid">
      <div
        v-for="p in sortedItems" :key="p.id"
        class="wl-card"
        :class="{ 'wl-card--selected': selectMode && selectedIds.has(getPid(p)) }"
        role="button"
        tabindex="0"
        @click="goDetail(getPid(p))"
        @keydown.enter.prevent="goDetail(getPid(p))"
        @keydown.space.prevent="goDetail(getPid(p))"
      >
        <!-- Select checkbox -->
        <div v-if="selectMode" class="wl-checkbox" @click.stop>
          <input
            type="checkbox"
            :checked="selectedIds.has(getPid(p))"
            @change="toggleSelect(getPid(p))"
          />
        </div>
        <!-- Remove/unfav button -->
        <button
          v-if="!selectMode"
          class="wl-unfav"
          @click.stop="unfav(getPid(p))"
          :aria-label="t('product.unfavorite')"
        >❤️</button>
        <LazyImage :src="p.imageUrl || ''" :alt="p.productName || p.name" height="180px" />
        <div class="wl-info">
          <h4 class="wl-name">{{ p.productName || p.name || t('nav.products') }}</h4>
          <div class="wl-bottom">
            <span class="wl-price">{{ formatPrice((p.price || 0) / 100, 2) }}</span>
            <button
              v-if="!selectMode"
              class="wl-add"
              @click.stop="(e: Event) => quickAdd(e, p)"
              :aria-label="t('product.addToCart')"
            >+</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty / Error states -->
    <JdEmpty v-else-if="loadError" icon="🔌" :title="t('wishlist.loadFail')">
      <JdButton @click="load">{{ t('common.retry') }}</JdButton>
    </JdEmpty>
    <JdEmpty v-else icon="💝" :title="t('wishlist.noItems')">
      <JdButton @click="router.push('/')">{{ t('wishlist.goBrowse') }}</JdButton>
    </JdEmpty>
  </div>
</template>

<style scoped>
.page-title { font-size: var(--font-xl); font-weight: 700; }

.wl-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--space-lg); flex-wrap: wrap; gap: var(--space-sm);
}
.wl-header-left { display: flex; align-items: baseline; gap: var(--space-sm); }
.wl-header-right { display: flex; align-items: center; gap: var(--space-sm); }
.wl-count { color: var(--text-tertiary); font-size: var(--font-sm); }

.wl-sort {
  padding: 6px 12px; border: 1px solid var(--border); border-radius: var(--radius-sm);
  background: var(--bg-white); color: var(--text-primary); font-size: var(--font-sm);
  cursor: pointer; outline: none;
}
.wl-sort:focus { border-color: var(--jd-red); }

.wl-enter-select {
  padding: 6px 16px; border: 1px solid var(--jd-red); background: var(--bg-white);
  color: var(--jd-red); border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-sm);
  transition: all var(--transition-fast);
}
.wl-enter-select:hover { background: var(--jd-red); color: #fff; }

.wl-exit-select {
  padding: 6px 16px; border: 1px solid var(--border); background: var(--bg-white);
  color: var(--text-secondary); border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-sm);
}
.wl-exit-select:hover { border-color: var(--text-primary); }

/* Batch action bar */
.wl-batch-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: var(--space-md) var(--space-lg); margin-bottom: var(--space-lg);
  background: var(--bg-white); border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm); flex-wrap: wrap; gap: var(--space-sm);
}
.wl-select-all { display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: var(--font-sm); }
.wl-select-all input[type="checkbox"] { width: 16px; height: 16px; accent-color: var(--jd-red); cursor: pointer; }
.wl-selected-count { color: var(--jd-red); font-weight: 600; margin-left: var(--space-xs); }
.wl-batch-actions { display: flex; gap: var(--space-sm); }
.wl-batch-del { border-color: var(--text-tertiary) !important; color: var(--text-secondary) !important; }
.wl-batch-del:hover { border-color: var(--jd-red) !important; color: var(--jd-red) !important; background: var(--jd-red-light) !important; }

/* Grid */
.wl-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }

.wl-skel { background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.wl-skel-img { height: 180px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; }
.wl-skel-body { padding: 12px; }
.wl-skel-line { height: 16px; width: 70%; background: var(--border-light); border-radius: var(--radius-sm); }

.wl-card {
  background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden;
  cursor: pointer; transition: transform var(--transition), box-shadow var(--transition);
  box-shadow: var(--shadow-sm); position: relative;
}
.wl-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-md); }
.wl-card--selected { outline: 2px solid var(--jd-red); outline-offset: -2px; }

/* Checkbox */
.wl-checkbox {
  position: absolute; top: 8px; left: 8px; z-index: 2;
  width: 24px; height: 24px; display: flex; align-items: center; justify-content: center;
  background: rgba(255,255,255,.9); border-radius: var(--radius-sm);
}
.wl-checkbox input[type="checkbox"] { width: 16px; height: 16px; accent-color: var(--jd-red); cursor: pointer; }

.wl-unfav {
  position: absolute; top: 8px; right: 8px; z-index: 2; width: 28px; height: 28px;
  border-radius: 50%; border: none; background: rgba(255,255,255,.9); cursor: pointer;
  font-size: 14px; display: flex; align-items: center; justify-content: center;
  transition: transform var(--transition-fast);
}
.wl-unfav:hover { transform: scale(1.2); }

.wl-info { padding: 12px; }
.wl-name { font-size: var(--font-md); margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.wl-bottom { display: flex; align-items: center; justify-content: space-between; }
.wl-price { color: var(--jd-red); font-size: 16px; font-weight: 700; }
.wl-add {
  width: 28px; height: 28px; border-radius: 50%; border: 2px solid var(--jd-red);
  background: var(--bg-white); color: var(--jd-red); cursor: pointer; font-size: 14px;
  display: flex; align-items: center; justify-content: center; transition: all var(--transition-fast);
}
.wl-add:hover { background: var(--jd-red); color: #fff; }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

@media (max-width: 768px) {
  .wl-grid { grid-template-columns: repeat(2, 1fr); }
  .wl-batch-bar { padding: var(--space-sm) var(--space-md); }
  .wl-batch-actions { width: 100%; justify-content: flex-end; }
}
</style>