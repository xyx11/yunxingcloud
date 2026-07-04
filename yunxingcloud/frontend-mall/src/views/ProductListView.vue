<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategories, getBrands, getProducts } from '@/api/product'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useCompare } from '@/composables/useCompare'
import { useCartFly } from '@/composables/useCartFly'
import { useI18n } from '@/locales'
import QuickViewModal from '@/components/QuickViewModal.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import LazyImage from '@/components/LazyImage.vue'
import JdBadge from '@/components/JdBadge.vue'
import type { Product } from '@/types'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const toast = useToast()
const { toggle: toggleCompare, isSelected } = useCompare()
const { flyToCart } = useCartFly()

const products = ref<any[]>([])
const totalPages = ref(0)
const currentPage = ref(0)
const pageSize = 20
const categories = ref<any[]>([])
const brands = ref<any[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const quickViewProduct = ref<Product | null>(null)

const filters = ref({ categoryId: '', brandId: '', minPrice: '', maxPrice: '', sort: '' })
const priceRanges = [
  { label: t('sort.under100'), min: '', max: '100' },
  { label: t('sort.r100500'), min: '100', max: '500' },
  { label: t('sort.r5001k'), min: '500', max: '1000' },
  { label: t('sort.over1k'), min: '1000', max: '' },
]

onMounted(async () => {
  try { const r = await getCategories(); categories.value = r.data || [] } catch {}
  try { const r = await getBrands(); brands.value = r.data || [] } catch {}
  filters.value.categoryId = (route.query.categoryId as string) || ''
  loadProducts()
  window.addEventListener('scroll', onScroll)
})

onUnmounted(() => { window.removeEventListener('scroll', onScroll) })

watch(() => route.query, (q) => {
  filters.value.categoryId = (q.categoryId as string) || ''
  filters.value.brandId = (q.brandId as string) || ''
  currentPage.value = 0
  loadProducts()
})

function onScroll() {
  if (loadingMore.value || currentPage.value >= totalPages.value - 1) return
  const h = document.documentElement
  if (h.scrollTop + h.clientHeight >= h.scrollHeight - 300) loadMore(currentPage.value + 1)
}

async function loadProducts() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: pageSize }
    if (filters.value.sort) params.sort = filters.value.sort
    if (filters.value.categoryId) params.categoryId = filters.value.categoryId
    if (filters.value.brandId) params.brandId = filters.value.brandId
    if (filters.value.minPrice) params.minPrice = Number(filters.value.minPrice) * 100
    if (filters.value.maxPrice) params.maxPrice = Number(filters.value.maxPrice) * 100
    const r = await getProducts(params)
    products.value = r.data.content || r.data || []
    totalPages.value = r.data.totalPages || 0
  } catch { products.value = [] } finally { loading.value = false }
}

function applyFilters() { currentPage.value = 0; const q: any = {}; if (filters.value.categoryId) q.categoryId = filters.value.categoryId; if (filters.value.brandId) q.brandId = filters.value.brandId; router.push({ query: q }); loadProducts(); window.scrollTo(0, 0) }
function clearFilters() { filters.value = { categoryId: '', brandId: '', minPrice: '', maxPrice: '', sort: '' }; router.push({ query: {} }); loadProducts(); window.scrollTo(0, 0) }
function setSort(s: string) { filters.value.sort = filters.value.sort === s ? '' : s; currentPage.value = 0; products.value = []; loadProducts(); window.scrollTo(0, 0) }

async function loadMore(nextPage?: number) {
  if (loadingMore.value) return
  loadingMore.value = true
  const page = nextPage || currentPage.value + 1
  try {
    const params: any = { page, size: pageSize }
    if (filters.value.sort) params.sort = filters.value.sort
    if (filters.value.categoryId) params.categoryId = filters.value.categoryId
    if (filters.value.brandId) params.brandId = filters.value.brandId
    const r = await getProducts(params)
    const data = r.data
    products.value = [...products.value, ...(data.content || data || [])]
    totalPages.value = data.totalPages || 0
    currentPage.value = page
  } catch {} finally { loadingMore.value = false }
}

function goDetail(id: number) { router.push(`/product/${id}`) }
function goPage(p: number) { currentPage.value = p; loadProducts(); window.scrollTo(0, 0) }

async function quickAdd(e: Event, p: any) {
  e.stopPropagation()
  try { await addToCart({ productId: p.id, quantity: 1 }); toast.success('已加入购物车'); p._added = true; setTimeout(() => p._added = false, 1500); flyToCart(e as MouseEvent) } catch { toast.error('添加失败') }
}

function openQuickView(e: Event, p: any) { e.stopPropagation(); quickViewProduct.value = p }
</script>

<template>
  <div class="plp">
    <!-- Sidebar -->
    <aside class="sidebar">
      <!-- Categories -->
      <div class="filter-card">
        <h4 class="filter-title">{{ t('product.hotRecommend') }}</h4>
        <div v-for="cat in categories" :key="cat.id" class="filter-item">
          <span class="filter-link" :class="{ active: filters.categoryId === cat.id + '' }"
                @click="filters.categoryId = cat.id + ''; applyFilters()">{{ cat.name }}</span>
        </div>
      </div>

      <!-- Brands -->
      <div class="filter-card">
        <h4 class="filter-title">{{ t('common.brand') }}</h4>
        <div v-for="b in brands" :key="b.id" class="filter-item">
          <span class="filter-link" :class="{ active: filters.brandId === b.id + '' }"
                @click="filters.brandId = b.id + ''; applyFilters()">{{ b.name }}</span>
        </div>
      </div>

      <!-- Price Range -->
      <div class="filter-card">
        <h4 class="filter-title">{{ t('common.priceRange') }}</h4>
        <div class="price-inputs">
          <input v-model="filters.minPrice" :placeholder="'¥' + t('common.lowest')" type="number" class="price-input" />
          <span class="price-sep">-</span>
          <input v-model="filters.maxPrice" :placeholder="'¥' + t('common.highest')" type="number" class="price-input" />
          <button class="price-confirm" @click="applyFilters()">{{ t('common.confirm') }}</button>
        </div>
        <div class="price-tags">
          <span v-for="r in priceRanges" :key="r.label" class="price-tag" @click="filters.minPrice = r.min; filters.maxPrice = r.max; applyFilters()">
            {{ r.label }}
          </span>
        </div>
      </div>
    </aside>

    <!-- Main -->
    <div class="main">
      <!-- Sort Bar -->
      <div class="sort-bar">
        <span class="sort-label">排序：</span>
        <span class="sort-item" :class="{ active: !filters.sort }" @click="setSort('')">综合</span>
        <span class="sort-item" :class="{ active: filters.sort === 'sales' }" @click="setSort('sales')">{{ t('sort.sales') }}</span>
        <span class="sort-item" :class="{ active: filters.sort === 'price_asc' }" @click="setSort('price_asc')">价格↑</span>
        <span class="sort-item" :class="{ active: filters.sort === 'price_desc' }" @click="setSort('price_desc')">价格↓</span>
        <span class="sort-item" :class="{ active: filters.sort === 'newest' }" @click="setSort('newest')">最新</span>
      </div>

      <!-- Products -->
      <SkeletonBox v-if="loading" variant="card" :columns="3" :count="6" height="280px" />

      <div v-else class="product-grid">
        <div v-for="p in products" :key="p.id" class="product-card" @click="goDetail(p.id)">
          <div class="card-img-wrap">
            <LazyImage :src="p.imageUrl || (p.images?.[0]) || ''" :alt="p.name" height="180px" />
            <JdBadge v-if="p.isNew" type="green" class="card-badge-tl">新品</JdBadge>
            <JdBadge v-else-if="p.isHot" class="card-badge-tl">热卖</JdBadge>
            <JdBadge v-if="p.originalPrice && p.originalPrice > p.price" type="orange" class="card-badge-tr">
              -{{ Math.round((1 - p.price / p.originalPrice) * 100) }}%
            </JdBadge>
            <button class="compare-btn" :class="{ active: isSelected(p.id) }" @click.stop="toggleCompare({ id: p.id, name: p.name, price: p.price })">
              {{ isSelected(p.id) ? '✓ 对比' : '+ 对比' }}
            </button>
            <button class="preview-btn" @click.stop="openQuickView($event, p)">👁 预览</button>
          </div>
          <div class="card-info">
            <h4 class="card-name">{{ p.name }}</h4>
            <div class="card-bottom">
              <div>
                <span class="card-price">¥{{ (p.price / 100).toFixed(2) }}</span>
                <span class="card-sales" v-if="p.sales">{{ p.sales > 1000 ? '🔥 ' + (p.sales / 1000).toFixed(1) + 'k人已购' : p.sales + '人已购' }}</span>
              </div>
              <button class="add-btn" :class="{ added: (p as any)._added }" @click="(e: Event) => quickAdd(e, p)">
                {{ (p as any)._added ? '✓' : '+' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty -->
      <div v-if="!loading && !products.length" class="empty-state">
        <p style="font-size:48px">📭</p><p>{{ t('common.noResults') }}</p>
      </div>

      <!-- Load More -->
      <div v-if="loadingMore" class="load-more">{{ t('common.loadMore') }}</div>

      <!-- Pagination -->
      <div v-if="totalPages > 1 && !loadingMore" class="pagination">
        <button v-for="p in Math.min(totalPages, 10)" :key="p" class="page-btn" :class="{ active: currentPage === p - 1 }" @click="goPage(p - 1)">
          {{ p }}
        </button>
      </div>
    </div>

    <QuickViewModal :product="quickViewProduct" :show="!!quickViewProduct" @close="quickViewProduct = null" />
  </div>
</template>

<style scoped>
.plp { display: flex; gap: var(--space-xxl); }

/* Sidebar */
.sidebar { width: 220px; flex-shrink: 0; }
.filter-card { background: var(--bg-white); border-radius: var(--radius-md); padding: var(--space-lg); margin-bottom: var(--space-lg); box-shadow: var(--shadow-sm); }
.filter-title { font-size: var(--font-md); font-weight: 700; margin-bottom: var(--space-md); }
.filter-item { margin-bottom: var(--space-xs); }
.filter-link { display: block; padding: 6px 8px; cursor: pointer; font-size: var(--font-base); border-radius: var(--radius-sm); color: var(--text-secondary); transition: all var(--transition-fast); }
.filter-link:hover, .filter-link.active { background: var(--jd-red-light); color: var(--jd-red); }

.price-inputs { display: flex; align-items: center; gap: 6px; }
.price-input { width: 70px; padding: 6px 8px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--font-sm); background: var(--bg-white); color: var(--text-primary); }
.price-sep { color: var(--text-tertiary); }
.price-confirm { padding: 6px 10px; background: var(--jd-red); color: #fff; border: none; border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-sm); }
.price-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: var(--space-md); }
.price-tag { padding: 4px 10px; background: var(--bg-hover); border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-xs); color: var(--text-secondary); transition: all var(--transition-fast); }
.price-tag:hover { background: var(--jd-red-light); color: var(--jd-red); }

/* Main */
.main { flex: 1; }
.sort-bar { background: var(--bg-white); border-radius: var(--radius-md); padding: var(--space-md) var(--space-lg); margin-bottom: var(--space-lg); display: flex; gap: var(--space-lg); align-items: center; box-shadow: var(--shadow-sm); }
.sort-label { font-size: var(--font-base); color: var(--text-tertiary); }
.sort-item { cursor: pointer; font-size: var(--font-base); padding: 4px 8px; border-radius: var(--radius-sm); color: var(--text-secondary); }
.sort-item.active { color: var(--jd-red); background: var(--jd-red-light); }

.product-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.product-card { background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: box-shadow var(--transition), transform var(--transition); }
.product-card:hover { box-shadow: var(--shadow-card-hover); transform: translateY(-4px); }

.card-img-wrap { height: 180px; position: relative; }
.card-badge-tl { position: absolute; top: 6px; left: 6px; z-index: 1; }
.card-badge-tr { position: absolute; top: 6px; right: 50px; z-index: 1; }
.compare-btn { position: absolute; top: 6px; right: 6px; padding: 1px 8px; border: 1px solid var(--jd-red); border-radius: var(--radius-round); background: var(--bg-white); color: var(--jd-red); cursor: pointer; font-size: 10px; z-index: 1; transition: all var(--transition-fast); }
.compare-btn.active { background: var(--jd-red); color: #fff; }
.preview-btn { position: absolute; bottom: 60px; right: 6px; padding: 2px 8px; border: 1px solid var(--blue); border-radius: var(--radius-round); background: var(--bg-white); color: var(--blue); cursor: pointer; font-size: 10px; z-index: 1; transition: all var(--transition-fast); }
.preview-btn:hover { background: var(--blue); color: #fff; }

.card-info { padding: var(--space-md); }
.card-name { font-size: var(--font-md); margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: var(--text-primary); }
.card-bottom { display: flex; align-items: center; justify-content: space-between; }
.card-price { color: var(--jd-red); font-size: var(--font-lg); font-weight: 700; }
.card-sales { color: var(--text-tertiary); font-size: var(--font-xs); margin-left: var(--space-xs); }

.add-btn { width: 32px; height: 32px; border-radius: 50%; border: 2px solid var(--jd-red); background: var(--bg-white); color: var(--jd-red); cursor: pointer; font-size: var(--font-lg); display: flex; align-items: center; justify-content: center; transition: all var(--transition-fast); flex-shrink: 0; }
.add-btn:hover { background: var(--jd-red); color: #fff; }
.add-btn.added { background: var(--jd-red); color: #fff; }

.empty-state { text-align: center; padding: 60px; color: var(--text-tertiary); }
.load-more { text-align: center; padding: var(--space-lg); color: var(--text-tertiary); font-size: var(--font-base); }
.pagination { display: flex; justify-content: center; gap: var(--space-sm); margin-top: var(--space-xxl); }
.page-btn { width: 36px; height: 36px; border: 1px solid var(--border); border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-base); background: var(--bg-white); color: var(--text-primary); }
.page-btn.active { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }

@media (max-width: 768px) {
  .sidebar { display: none; }
  .product-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
