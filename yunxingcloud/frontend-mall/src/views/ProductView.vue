<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail } from '@/api/product'
import { addToCart } from '@/api/cart'
import { checkFavorite, addFavorite, removeFavorite } from '@/api/order'
import { useAuthStore } from '@/stores/auth'
import { useRecentlyViewed } from '@/composables/useRecentlyViewed'
import { useCartFly } from '@/composables/useCartFly'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'
import { formatPrice, formatRelativeTime } from '@/utils/format'
import type { Product, Sku, Review } from '@/types'

// Sub-components
import ProductGallery from '@/components/ProductGallery.vue'
import SkuSelector from '@/components/SkuSelector.vue'
import ProductInfo from '@/components/ProductInfo.vue'

// Retained existing components
import ReviewSummary from '@/components/ReviewSummary.vue'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { t } = useI18n()
const toast = inject(ToastInjectionKey)!
const { flyToCart } = useCartFly()

// Data state
const product = ref<Product | null>(null)
const skus = ref<Sku[]>([])
const reviews = ref<Review[]>([])
const reviewSort = ref<'newest' | 'highest' | 'lowest'>('newest')
const reviewShow = ref(3)
const sortedReviews = computed(() => {
  const arr = [...reviews.value]
  if (reviewSort.value === 'newest') arr.sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
  else if (reviewSort.value === 'highest') arr.sort((a, b) => b.rating - a.rating)
  else arr.sort((a, b) => a.rating - b.rating)
  return arr.slice(0, reviewShow.value)
})
function showMoreReviews() { reviewShow.value = Math.min(reviewShow.value + 3, reviews.value.length) }

const related = ref<Product[]>([])
const selectedSku = ref<Sku | null>(null)
const qty = ref(1)
const favorited = ref(false)
const alertSet = ref(false)
const loading = ref(true)
const shareMenu = ref(false)
const showFloatingBar = ref(false)
const images = ref<string[]>([])
const alertPrice = ref('')
const showPriceAlert = ref(false)
const viewerCount = ref(Math.floor(Math.random() * 200) + 50)
let viewerTimer: ReturnType<typeof setInterval> | null = null

function onScroll() { showFloatingBar.value = window.scrollY > 500 }

const displayPrice = computed(() =>
  selectedSku.value ? selectedSku.value.price : product.value?.price || 0
)
const displayStock = computed(() =>
  selectedSku.value ? selectedSku.value.stock : product.value?.stock || 0
)

function productImage(p: Product): string {
  if (p?.imageUrl && p.imageUrl !== '\u{1F4E6}') return p.imageUrl
  if (p?.images?.length) return p.images[0]
  return ''
}

// --- Lifecycle ---

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await getProductDetail(Number(id))
    product.value = res.data.product
    skus.value = res.data.skus || []
    reviews.value = res.data.reviews || []
    related.value = res.data.related || []
    const { add } = useRecentlyViewed()
    if (product.value) {
      add({ id: product.value.id, name: product.value.name, price: product.value.price, imageUrl: product.value.imageUrl })
    }
  } catch {} finally { loading.value = false }

  if (auth.isLoggedIn) {
    try { const r = await checkFavorite(Number(id)); favorited.value = r.data.favorited } catch {}
  }

  window.addEventListener('scroll', onScroll)

  if (product.value?.imageUrl) images.value = [product.value.imageUrl, ...(product.value.images || [])]
  else if (product.value?.images) images.value = product.value.images
  if (!images.value.length) images.value = ['\u{1F4E6}']

  viewerTimer = setInterval(() => {
    viewerCount.value = Math.max(10, viewerCount.value + Math.floor(Math.random() * 7) - 3)
  }, 5000)
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  if (viewerTimer) clearInterval(viewerTimer)
})

// --- Actions ---

async function onAddToCart(e?: MouseEvent) {
  if (!auth.isLoggedIn) { router.push('/login'); return }
  if (!product.value) return
  try {
    await addToCart(product.value.id, qty.value)
    toast.success(t('toast.addedToCart'))
    if (e) flyToCart(e)
  } catch { toast.error(t('toast.addCartFail')) }
}

async function buyNow() {
  if (!auth.isLoggedIn) { router.push('/login'); return }
  if (!product.value) return
  try { await addToCart(product.value.id, qty.value); router.push('/cart') } catch {}
}

async function toggleFavorite() {
  if (!auth.isLoggedIn) { router.push('/login'); return }
  if (!product.value) return
  try {
    if (favorited.value) { await removeFavorite(product.value.id); toast.info(t('product.unfavorite')) }
    else { await addFavorite(product.value.id); toast.success(t('product.favorite')) }
    favorited.value = !favorited.value
  } catch {}
}

function onSetPriceAlert() {
  if (!auth.isLoggedIn) { router.push('/login'); return }
  if (!product.value) return
  alertPrice.value = String((displayPrice.value / 100).toFixed(2))
  showPriceAlert.value = true
}

async function confirmPriceAlert() {
  if (!product.value) return
  try {
    await request.post('/price-alert', { productId: product.value.id, targetPrice: Number(alertPrice.value) * 100 })
    alertSet.value = true
    showPriceAlert.value = false
    toast.success('降价时将通过通知提醒您')
  } catch { toast.error('设置失败') }
}

async function shareProduct() {
  const url = window.location.href
  const title = product.value?.name || ''
  if (navigator.share) { try { await navigator.share({ title, url }) } catch {} }
  else { await navigator.clipboard.writeText(url); toast.success(t('toast.copied')) }
  shareMenu.value = false
}

function goDetail(id: number) { router.push(`/product/${id}`) }
</script>

<template>
  <!-- Breadcrumb -->
  <div class="breadcrumb">
    <span @click="router.push('/')" class="crumb-link">首页</span><span>/</span>
    <span
      v-if="product?.categoryId"
      class="crumb-link"
      @click="router.push('/products?categoryId=' + product.categoryId)"
    >{{ (product as Product).categoryName || t('product.category') }}</span><span v-if="product?.categoryId">/</span>
    <span class="crumb-current">{{ product?.name || t('product.detail') }}</span>
  </div>

  <!-- Skeleton -->
  <div v-if="loading" class="pdp-skeleton">
    <div class="sk-img" />
    <div class="sk-body">
      <div class="sk-line sk-line-lg" />
    </div>
  </div>

  <!-- Main Product -->
  <div v-else-if="product" class="pdp-main">
    <ProductGallery :images="images" :product-name="product.name" />

    <div class="pdp-info">
      <ProductInfo
        :product="product"
        :selected-sku="selectedSku"
        :favorited="favorited"
        :reviews-count="reviews.length"
        :alert-set="alertSet"
        :viewer-count="viewerCount"
        @toggle-favorite="toggleFavorite"
        @share="shareProduct"
        @set-price-alert="onSetPriceAlert"
      />

      <SkuSelector v-if="skus.length" v-model="selectedSku" :skus="skus" />

      <!-- Quantity -->
      <div class="qty-row">
        <span class="qty-label">{{ t('product.quantity') }}</span>
        <button class="qty-btn" @click="qty = Math.max(1, qty - 1)">-</button>
        <span class="qty-val">{{ qty }}</span>
        <button class="qty-btn" @click="qty = Math.min(displayStock, qty + 1)">+</button>
      </div>

      <!-- Actions -->
      <div class="action-row">
        <JdButton type="outline" size="lg" class="flex-1" @click="() => onAddToCart()">{{ t('product.addToCart') }}</JdButton>
        <JdButton size="lg" class="flex-1" @click="buyNow">{{ t('product.buyNow') }}</JdButton>
        <button class="icon-btn" :class="{ active: favorited }" @click="toggleFavorite" aria-label="收藏">{{ favorited ? '❤️' : '🤍' }}</button>
        <button class="icon-btn" @click="shareProduct" aria-label="分享">📤</button>
      </div>
    </div>
  </div>

  <!-- Reviews -->
  <div v-if="product" class="pdp-section">
    <h3 class="section-title">{{ t('product.reviews') }} ({{ reviews.length }})</h3>
    <ReviewSummary v-if="reviews.length" :reviews="reviews" />
    <div v-if="reviews.length" class="review-sort">
      <span class="rs-opt" :class="{ active: reviewSort === 'newest' }" @click="reviewSort = 'newest'">最新</span>
      <span class="rs-opt" :class="{ active: reviewSort === 'highest' }" @click="reviewSort = 'highest'">好评</span>
      <span class="rs-opt" :class="{ active: reviewSort === 'lowest' }" @click="reviewSort = 'lowest'">差评</span>
    </div>
    <div v-if="reviews.length">
      <div v-for="r in sortedReviews" :key="r.id" class="review-item">
        <div class="review-header">
          <div class="review-user">
            <span class="review-username">{{ r.username }}</span>
            <span class="review-stars">{{ '★'.repeat(r.rating) }}</span>
          </div>
          <span class="review-date">{{ formatRelativeTime(r.createdAt) }}</span>
        </div>
        <p class="review-content">{{ r.content }}</p>
      </div>
      <div v-if="reviewShow < reviews.length" class="review-more">
        <button class="review-more-btn" @click="showMoreReviews">加载更多评论 ({{ reviews.length - reviewShow }}条)</button>
      </div>
    </div>
    <div v-else class="empty-text">{{ t('product.noReviews') }}</div>
  </div>

  <!-- Specs -->
  <div v-if="product" class="pdp-section">
    <h3 class="section-title">规格参数</h3>
    <div class="spec-grid">
      <div class="spec-row"><span class="spec-label">商品名称</span><span>{{ product.name }}</span></div>
      <div class="spec-row"><span class="spec-label">商品编号</span><span>{{ product.id }}</span></div>
      <div v-if="product.brandId" class="spec-row"><span class="spec-label">{{ t('product.brand') }}</span><span>{{ product.brandName || t('product.brand') + '#' + product.brandId }}</span></div>
      <div class="spec-row"><span class="spec-label">上架时间</span><span>{{ product.createdAt?.substring(0, 10) || '-' }}</span></div>
    </div>
  </div>

  <!-- Related -->
  <div v-if="related.length" class="pdp-section">
    <h3 class="section-title">{{ t('product.relatedProducts') }}</h3>
    <div class="related-grid">
      <div v-for="p in related" :key="p.id" class="related-card" @click="goDetail(p.id)">
        <LazyImage :src="productImage(p)" :alt="p.name" height="140px" />
        <div class="related-info">
          <h5 class="related-name">{{ p.name }}</h5>
          <span class="related-price">{{ formatPrice(p.price / 100, 2) }}</span>
        </div>
      </div>
    </div>
  </div>

  <!-- Price Alert Modal -->
  <div v-if="showPriceAlert" class="modal-overlay" @click.self="showPriceAlert = false">
    <div class="alert-modal">
      <h3 class="alert-title">🔔 设置降价提醒</h3>
      <p class="alert-desc">当商品价格低于目标价时，将通知您</p>
      <div class="alert-input-row">
        <span class="alert-input-label">目标价 ¥</span>
        <input v-model="alertPrice" type="number" step="0.01" class="alert-input" />
      </div>
      <div class="alert-actions">
        <JdButton type="ghost" @click="showPriceAlert = false">取消</JdButton>
        <JdButton @click="confirmPriceAlert">确认设置</JdButton>
      </div>
    </div>
  </div>

  <!-- Floating Bar -->
  <div v-if="showFloatingBar && product" class="floating-bar">
    <LazyImage :src="images[0]" :alt="product.name" height="48px" width="48px" rounded="6px" />
    <div class="floating-name">{{ product.name }}</div>
    <span class="floating-price">{{ formatPrice(displayPrice / 100, 2) }}</span>
    <JdButton type="outline" @click="() => onAddToCart()">{{ t('product.addToCart') }}</JdButton>
    <JdButton @click="buyNow">{{ t('product.buyNow') }}</JdButton>
  </div>

  <!-- Mobile Sticky Bar -->
  <div v-if="product" class="mobile-bar">
    <div class="mobile-bar-price">{{ formatPrice(displayPrice / 100, 2) }}</div>
    <JdButton type="outline" class="flex-1" @click="() => onAddToCart()">加入购物车</JdButton>
    <JdButton class="flex-1" @click="buyNow">立即购买</JdButton>
  </div>
</template>

<style scoped>
.breadcrumb { font-size: var(--font-sm); color: var(--text-tertiary); margin-bottom: var(--space-md); display: flex; align-items: center; gap: 6px; }
.crumb-link { cursor: pointer; color: var(--text-secondary); }
.crumb-link:hover { color: var(--jd-red); }
.crumb-current { color: var(--text-primary); }

/* Skeleton */
.pdp-skeleton { display: flex; gap: var(--space-xxxl); background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxxl); box-shadow: var(--shadow-sm); }
.sk-img { width: 420px; height: 420px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-md); flex-shrink: 0; }
.sk-body { flex: 1; }
.sk-line { background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-sm); }
.sk-line-lg { width: 60%; height: 28px; margin-bottom: 16px; }

/* Main layout */
.pdp-main { display: flex; gap: var(--space-xxxl); background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxxl); box-shadow: var(--shadow-sm); margin-bottom: var(--space-xxl); }
.pdp-info { flex: 1; }

/* Quantity */
.qty-row { display: flex; align-items: center; gap: var(--space-md); margin-bottom: var(--space-xxl); }
.qty-label { font-size: var(--font-base); color: var(--text-secondary); }
.qty-btn { width: 32px; height: 32px; border: 1px solid var(--border); background: var(--bg-white); cursor: pointer; font-size: var(--font-lg); border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; }
.qty-btn:hover { background: var(--bg-hover); }
.qty-val { width: 44px; text-align: center; font-size: 15px; }

/* Actions */
.action-row { display: flex; gap: var(--space-md); }
.icon-btn { width: 48px; height: 48px; border: 1px solid var(--border); border-radius: var(--radius-md); cursor: pointer; font-size: var(--font-xl); background: var(--bg-white); transition: all var(--transition-fast); color: var(--text-tertiary); display: flex; align-items: center; justify-content: center; }
.icon-btn:hover { background: var(--bg-hover); }
.icon-btn.active { color: var(--jd-red); }

/* Sections */
.pdp-section { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-sm); margin-bottom: var(--space-xxl); }
.section-title { font-size: var(--font-lg); font-weight: 700; margin-bottom: var(--space-lg); }

/* Reviews */
.review-sort { display: flex; gap: var(--space-md); margin-bottom: var(--space-md); }
.rs-opt { cursor: pointer; padding: 4px 10px; border-radius: var(--radius-sm); font-size: var(--font-sm); color: var(--text-secondary); transition: all var(--transition-fast); }
.rs-opt.active { color: var(--jd-red); background: var(--jd-red-light); }
.rs-opt:hover:not(.active) { color: var(--jd-red); }
.review-more { text-align: center; padding: var(--space-lg) 0; }
.review-more-btn { padding: var(--space-md) 32px; border: 1px solid var(--border); background: var(--bg-white); border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-md); color: var(--text-secondary); transition: all var(--transition-fast); }
.review-more-btn:hover { border-color: var(--jd-red); color: var(--jd-red); }
.review-item { padding: var(--space-lg) 0; border-bottom: 1px solid var(--border-light); }
.review-header { display: flex; justify-content: space-between; margin-bottom: var(--space-sm); }
.review-user { display: flex; align-items: center; gap: var(--space-sm); }
.review-username { font-weight: 600; font-size: var(--font-md); }
.review-stars { color: var(--orange); font-size: var(--font-md); }
.review-date { color: var(--text-tertiary); font-size: var(--font-sm); }
.review-content { font-size: var(--font-md); color: var(--text-primary); line-height: 1.6; }

/* Specs */
.spec-grid { display: grid; grid-template-columns: 1fr 1fr; font-size: var(--font-base); }
.spec-row { display: flex; border-bottom: 1px solid var(--border-light); padding: var(--space-md) 0; }
.spec-label { color: var(--text-tertiary); width: 100px; flex-shrink: 0; }

/* Related */
.related-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-md); }
.related-card { background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition); }
.related-card:hover { transform: translateY(-4px); }
.related-info { padding: var(--space-sm) var(--space-md); }
.related-name { font-size: var(--font-base); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: var(--space-xs); color: var(--text-primary); }
.related-price { color: var(--jd-red); font-size: var(--font-lg); font-weight: 700; }

.empty-text { text-align: center; padding: 30px; color: var(--text-tertiary); }

/* Alert Modal */
.alert-modal { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); width: 360px; max-width: 90vw; }
.alert-title { font-size: var(--font-lg); font-weight: 600; margin-bottom: var(--space-lg); }
.alert-desc { color: var(--text-secondary); font-size: var(--font-base); margin-bottom: var(--space-md); }
.alert-input-row { display: flex; align-items: center; gap: var(--space-sm); margin-bottom: var(--space-lg); }
.alert-input-label { font-size: var(--font-base); color: var(--text-secondary); }
.alert-input { flex: 1; padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md); font-size: var(--font-md); background: var(--bg-white); color: var(--text-primary); }
.alert-actions { display: flex; gap: var(--space-sm); justify-content: flex-end; }

/* Floating Bar */
.floating-bar { display: flex; position: fixed; top: 0; left: 0; right: 0; z-index: 150; background: var(--bg-white); box-shadow: var(--shadow-md); padding: var(--space-sm) var(--space-xxl); align-items: center; gap: var(--space-xl); animation: slideDown .3s ease-out; }
.floating-name { flex: 1; min-width: 0; font-size: 15px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.floating-price { color: var(--jd-red); font-size: var(--font-title); font-weight: 700; white-space: nowrap; }

/* Mobile Bar */
.mobile-bar { display: none; position: fixed; bottom: 0; left: 0; right: 0; background: var(--bg-white); border-top: 1px solid var(--border-light); padding: var(--space-md) var(--space-lg); z-index: 200; align-items: center; gap: var(--space-md); box-shadow: 0 -2px 8px rgba(0,0,0,.06); }
.mobile-bar-price { color: var(--jd-red); font-size: var(--font-xl); font-weight: 700; white-space: nowrap; }

.flex-1 { flex: 1; }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
@keyframes slideDown { from { transform: translateY(-100%); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

@media (min-width: 769px) { .floating-bar { display: flex; } }
@media (max-width: 768px) {
  .pdp-main { flex-direction: column; padding: var(--space-md); }
  .related-grid { grid-template-columns: repeat(2, 1fr); }
  .pdp-section { padding: var(--space-md); }
  .mobile-bar { display: flex; }
  .floating-bar { display: none; }
}
</style>
