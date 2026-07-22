<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProducts } from '@/api/product'
import { formatPrice, formatCount } from '@/utils/format'
import LazyImage from '@/components/LazyImage.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import JdButton from '@/components/JdButton.vue'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { t } = useI18n()

interface BrandInfo { id: number; name: string; logo?: string; description?: string; productCount?: number }

const brand = ref<BrandInfo | null>(null)
const products = ref<any[]>([])
const loading = ref(true)
const page = ref(0)
const hasMore = ref(true)
const loadingMore = ref(false)
const sortBy = ref<'default' | 'priceAsc' | 'priceDesc' | 'sales' | 'newest'>('default')
const brandCoupons = ref<any[]>([])

const brandId = computed(() => Number(route.params.id))

const sortOptions = [
  { key: 'default' as const, label: t('sort.defaultSort') },
  { key: 'sales' as const, label: t('sort.sales') },
  { key: 'newest' as const, label: t('sort.newest') },
  { key: 'priceAsc' as const, label: t('sort.priceAsc') },
  { key: 'priceDesc' as const, label: t('sort.priceDesc') },
]

async function load() {
  loading.value = true
  page.value = 0
  try {
    const [brandR, productR, couponR] = await Promise.all([
      request.get(`/brands/${brandId.value}`).catch(() => ({ data: null })),
      getProducts({ brandId: brandId.value, page: 0, size: 20, sort: sortBy.value === 'default' ? undefined : sortBy.value }),
      request.get(`/coupons?brandId=${brandId.value}`).catch(() => ({ data: [] })),
    ])
    brand.value = brandR.data
    products.value = productR.data?.content || productR.data || []
    brandCoupons.value = couponR.data || []
    hasMore.value = products.value.length >= 20
    loading.value = false;
    } catch { toast.error(t('brandDetail.loadFail')); loading.value = false }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    const nextPage = page.value + 1
    const r = await getProducts({ brandId: brandId.value, page: nextPage, size: 20, sort: sortBy.value === 'default' ? undefined : sortBy.value })
    const newItems = r.data?.content || r.data || []
    products.value.push(...newItems)
    page.value = nextPage
    hasMore.value = newItems.length >= 20
  } catch { toast.error(t('brandDetail.loadFail')) }
  finally { loadingMore.value = false }
}

function changeSort(sort: string) {
  sortBy.value = sort as any
  load()
}

function goDetail(id: number) { router.push(`/product/${id}`) }

async function quickAdd(e: Event, productId: number) {
  e.stopPropagation()
  try { await addToCart(productId, 1); toast.success(t('toast.addedToCart')) }
  catch { toast.error(t('toast.addCartFail')) }
}

onMounted(load)
</script>

<template>
  <div class="brand-detail-page">
    <!-- Back -->
    <div class="bd-back">
      <button class="back-btn" @click="router.back()">{{ t('brandDetail.back') }}</button>
    </div>

    <!-- Brand hero -->
    <div v-if="brand" class="bd-hero">
      <div class="bd-hero-content">
        <img v-if="brand.logo" :src="brand.logo" :alt="brand.name" class="bd-logo" />
        <div class="bd-hero-text">
          <h1 class="bd-hero-name">{{ brand.name }}</h1>
          <p v-if="brand.description" class="bd-hero-desc">{{ brand.description }}</p>
          <p v-if="brand.productCount" class="bd-hero-count">
            {{ t('brandDetail.productCount', { n: brand.productCount }) }}
          </p>
        </div>
      </div>
    </div>

    <!-- Brand coupons -->
    <div v-if="brandCoupons.length" class="bd-coupons">
      <h3 class="bd-section-title">🎫 {{ t('brandDetail.brandCoupons') }}</h3>
      <div class="bd-coupon-list">
        <div v-for="c in brandCoupons" :key="c.id" class="bd-coupon">
          <div class="bd-coupon-left">
            <span class="bd-coupon-value">¥{{ (c.amount || c.value || 0) / 100 }}</span>
            <span v-if="c.minAmount" class="bd-coupon-min">满{{ c.minAmount / 100 }}可用</span>
          </div>
          <div class="bd-coupon-right">
            <span class="bd-coupon-name">{{ c.name }}</span>
            <span class="bd-coupon-expire">{{ c.endTime?.substring(0, 10) }}前有效</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Sort bar -->
    <div v-if="products.length || !loading" class="bd-toolbar">
      <span class="bd-toolbar-count">{{ t('brandDetail.productCount', { n: products.length }) }}</span>
      <div class="bd-sort-bar">
        <button
          v-for="o in sortOptions" :key="o.key"
          class="bd-sort-btn"
          :class="{ active: sortBy === o.key }"
          @click="changeSort(o.key)"
        >{{ o.label }}</button>
      </div>
    </div>

    <!-- Product grid -->
    <div v-if="loading" class="bd-grid">
      <div v-for="i in 6" :key="i" class="bd-skel"><div class="sk-img" /><div class="sk-line" /></div>
    </div>

    <div v-else-if="products.length" class="bd-grid">
      <div
        v-for="p in products" :key="p.id"
        class="bd-card"
        role="button"
        tabindex="0"
        @click="goDetail(p.id)"
        @keydown.enter.prevent="goDetail(p.id)"
        @keydown.space.prevent="goDetail(p.id)"
      >
        <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="180px" />
        <div class="bd-info">
          <h4 class="bd-name">{{ p.name }}</h4>
          <div class="bd-card-bottom">
            <div>
              <span class="bd-price">{{ formatPrice(p.price / 100, 2) }}</span>
              <div v-if="p.sales" class="bd-sales">{{ t('product.salesCount') }} {{ formatCount(p.sales) }}</div>
            </div>
            <button
              class="bd-add"
              @click.stop="(e: Event) => quickAdd(e, p.id)"
              :aria-label="t('product.addToCart')"
            >+</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Load more -->
    <div v-if="products.length && hasMore" class="load-more-wrap">
      <JdButton type="outline" :loading="loadingMore" @click="loadMore">
        {{ t('common.loadMore') }}
      </JdButton>
    </div>

    <!-- Empty -->
    <JdEmpty v-if="!loading && !products.length" icon="🏷️" :title="t('brandDetail.empty')">
      <JdButton @click="router.push('/brands')">{{ t('brandDetail.viewAllBrands') }}</JdButton>
    </JdEmpty>
  </div>
</template>

<style scoped>
.brand-detail-page { max-width: 1000px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }

/* Back */
.bd-back { margin-bottom: var(--space-lg); }
.back-btn { background: none; border: none; color: var(--text-secondary); cursor: pointer; font-size: var(--font-md); padding: 0; }
.back-btn:hover { color: var(--jd-red); }

/* Hero */
.bd-hero {
  background: var(--bg-white); border-radius: var(--radius-xl); padding: var(--space-xxl);
  margin-bottom: var(--space-xl); box-shadow: var(--shadow-sm);
}
.bd-hero-content { display: flex; align-items: center; gap: var(--space-xl); }
.bd-logo { width: 80px; height: 80px; border-radius: var(--radius-lg); object-fit: cover; border: 1px solid var(--border-light); }
.bd-hero-text { flex: 1; }
.bd-hero-name { font-size: var(--font-h1); font-weight: 800; margin-bottom: var(--space-xs); }
.bd-hero-desc { color: var(--text-secondary); font-size: var(--font-md); line-height: 1.6; margin-bottom: var(--space-sm); }
.bd-hero-count { color: var(--text-tertiary); font-size: var(--font-sm); }

/* Section title */
.bd-section-title { font-size: var(--font-lg); font-weight: 600; margin-bottom: var(--space-md); }

/* Coupons */
.bd-coupons { margin-bottom: var(--space-xl); }
.bd-coupon-list { display: flex; gap: var(--space-md); overflow-x: auto; padding-bottom: var(--space-xs); }
.bd-coupon {
  background: linear-gradient(135deg, var(--jd-red-light), #fff5f5);
  border: 1px solid var(--jd-red); border-radius: var(--radius-md);
  padding: var(--space-md) var(--space-lg); display: flex; gap: var(--space-lg);
  align-items: center; min-width: 220px; flex-shrink: 0;
}
.bd-coupon-left { text-align: center; }
.bd-coupon-value { font-size: 22px; font-weight: 800; color: var(--jd-red); }
.bd-coupon-min { font-size: 11px; color: var(--text-placeholder); display: block; }
.bd-coupon-right { display: flex; flex-direction: column; }
.bd-coupon-name { font-weight: 600; font-size: var(--font-sm); }
.bd-coupon-expire { font-size: 11px; color: var(--text-tertiary); }

/* Toolbar */
.bd-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); flex-wrap: wrap; gap: var(--space-sm); }
.bd-toolbar-count { font-size: var(--font-sm); color: var(--text-tertiary); }
.bd-sort-bar { display: flex; gap: 4px; }
.bd-sort-btn {
  padding: 4px 12px; border: 1px solid var(--border); background: var(--bg-white);
  border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-sm);
  color: var(--text-secondary); transition: all var(--transition-fast);
}
.bd-sort-btn.active { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }
.bd-sort-btn:not(.active):hover { border-color: var(--jd-red); color: var(--jd-red); }

/* Grid */
.bd-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-lg); }
.bd-card {
  background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden;
  cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition);
}
.bd-card:hover { transform: translateY(-4px); }
.bd-info { padding: var(--space-md); }
.bd-name { font-size: 14px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 6px; }
.bd-card-bottom { display: flex; justify-content: space-between; align-items: flex-end; }
.bd-price { color: var(--jd-red); font-size: var(--font-lg); font-weight: 700; }
.bd-sales { font-size: 11px; color: var(--text-placeholder); margin-top: 2px; }
.bd-add {
  width: 28px; height: 28px; border-radius: 50%; border: 2px solid var(--jd-red);
  background: var(--bg-white); color: var(--jd-red); cursor: pointer; font-size: 14px;
  display: flex; align-items: center; justify-content: center; transition: all var(--transition-fast);
  flex-shrink: 0;
}
.bd-add:hover { background: var(--jd-red); color: #fff; }

/* Skeleton */
.bd-skel { background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-sm); }
.sk-img { height: 180px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; }
.sk-line { height: 16px; width: 60%; background: var(--border-light); border-radius: var(--radius-sm); margin: var(--space-md); }

.load-more-wrap { text-align: center; margin-top: var(--space-xl); }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

@media (max-width: 768px) {
  .brand-detail-page { padding: var(--space-lg) var(--space-md) calc(80px + env(safe-area-inset-bottom, 0px)); }
  .bd-hero { padding: var(--space-lg); }
  .bd-hero-content { flex-direction: column; text-align: center; }
  .bd-logo { width: 60px; height: 60px; }
  .bd-hero-name { font-size: var(--font-xxl); }
  .bd-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-md); }
  .bd-sort-bar { gap: 2px; }
  .bd-sort-btn { padding: 4px 8px; font-size: 11px; }
}
</style>