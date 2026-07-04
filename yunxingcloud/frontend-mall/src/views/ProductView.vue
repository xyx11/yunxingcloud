<script setup lang="ts">
import { ref, onMounted, onUnmounted, inject, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail } from '@/api/product'
import { addToCart } from '@/api/cart'
import { checkFavorite, addFavorite, removeFavorite } from '@/api/order'
import { useAuthStore } from '@/stores/auth'
import { useRecentlyViewed } from '@/composables/useRecentlyViewed'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'
import type { Product, Sku } from '@/types'
import ProductRating from '@/components/ProductRating.vue'
import ReviewSummary from '@/components/ReviewSummary.vue'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { t } = useI18n()
const toast = inject(ToastInjectionKey)!
const product = ref<Product | null>(null)
const skus = ref<Sku[]>([])
const reviews = ref<any[]>([])
const related = ref<any[]>([])
const selectedSku = ref<Sku | null>(null)
const qty = ref(1)
const favorited = ref(false)
const loading = ref(true)
const addAnim = ref(false)
const shareMenu = ref(false)
const fullscreen = ref(false)
const showFloatingBar = ref(false)
const activeImage = ref(0)
const images = ref<string[]>([])
const alertPrice = ref('')
const showPriceAlert = ref(false)
const alertSet = ref(false)
const viewerCount = ref(Math.floor(Math.random() * 200) + 50)
let viewerTimer: ReturnType<typeof setInterval> | null = null

const displayPrice = () => selectedSku.value ? selectedSku.value.price : product.value?.price || 0
const displayStock = () => selectedSku.value ? selectedSku.value.stock : product.value?.stock || 0

function productImage(p: any): string {
  if (p?.imageUrl && p.imageUrl !== '📦') return p.imageUrl
  if (p?.images?.length) return p.images[0]
  return ''
}

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

  window.addEventListener('scroll', () => { showFloatingBar.value = window.scrollY > 500 })

  if (product.value?.imageUrl) images.value = [product.value.imageUrl, ...(product.value.images || [])]
  else if (product.value?.images) images.value = product.value.images
  if (!images.value.length) images.value = ['📦']

  viewerTimer = setInterval(() => {
    viewerCount.value = Math.max(10, viewerCount.value + Math.floor(Math.random() * 7) - 3)
  }, 5000)
})

onUnmounted(() => {
  window.removeEventListener('scroll', () => {})
  if (viewerTimer) clearInterval(viewerTimer)
})

async function onAddToCart() {
  if (!auth.isLoggedIn) { router.push('/login'); return }
  if (!product.value) return
  try { await addToCart(product.value.id, qty.value); toast.success(t('toast.addedToCart')); addAnim.value = true; setTimeout(() => addAnim.value = false, 600) } catch { toast.error(t('toast.addCartFail')) }
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

function setPriceAlert() {
  if (!auth.isLoggedIn) { router.push('/login'); return }
  if (!product.value) return
  alertPrice.value = String((displayPrice() / 100).toFixed(2))
  showPriceAlert.value = true
}
async function confirmPriceAlert() {
  if (!product.value) return
  try { await request.post('/price-alert', { productId: product.value.id, targetPrice: Number(alertPrice.value) * 100 }); alertSet.value = true; showPriceAlert.value = false; toast.success('降价时将通过通知提醒您') } catch { toast.error('设置失败') }
}

async function shareProduct() {
  const url = window.location.href
  const title = product.value?.name || ''
  if (navigator.share) { try { await navigator.share({ title, url }) } catch {} }
  else { await navigator.clipboard.writeText(url); toast.success('链接已复制') }
  shareMenu.value = false
}

function goDetail(id: number) { router.push(`/product/${id}`) }

// Touch swipe
let touchX = 0
function onTouchStart(e: TouchEvent) { touchX = e.touches[0].clientX }
function onTouchEnd(e: TouchEvent) {
  const dx = e.changedTouches[0].clientX - touchX
  if (Math.abs(dx) > 50 && images.value.length > 1) {
    activeImage.value = dx < 0 ? Math.min(images.value.length - 1, activeImage.value + 1) : Math.max(0, activeImage.value - 1)
  }
}

const SKU_COLORS: Record<string, string> = { '红': '#f10215', '蓝': '#1677ff', '绿': '#4caf50', '黄': '#ffc107', '紫': '#9c27b0', '黑': '#333', '白': '#fff', '灰': '#999', '粉': '#e91e63', '金': '#ffc107', '银': '#ccc', '橙': '#ff9800' }
</script>

<template>
  <!-- Breadcrumb -->
  <div class="breadcrumb">
    <span @click="router.push('/')" class="crumb-link">首页</span><span>/</span>
    <span v-if="product?.categoryId" @click="router.push('/products?categoryId=' + product.categoryId)" class="crumb-link">{{ (product as any).categoryName || '分类' }}</span><span v-if="product?.categoryId">/</span>
    <span class="crumb-current">{{ product?.name || '商品详情' }}</span>
  </div>

  <!-- Skeleton -->
  <div v-if="loading" class="pdp-skeleton">
    <div class="sk-img" />
    <div class="sk-body"><div class="sk-line" style="width:60%;height:28px;margin-bottom:16px" /></div>
  </div>

  <!-- Main Product -->
  <div v-else-if="product" class="pdp-main">
    <!-- Gallery -->
    <div class="gallery">
      <div @touchstart="onTouchStart" @touchend="onTouchEnd" @click="fullscreen = true" class="gallery-main">
        <LazyImage :src="images[activeImage]" :alt="product.name" height="420px" rounded="8px" />
      </div>

      <!-- Fullscreen -->
      <div v-if="fullscreen" class="fullscreen-overlay" @click="fullscreen = false">
        <button class="fs-close" @click.stop="fullscreen = false" aria-label="关闭">✕</button>
        <button v-if="images.length > 1" class="fs-nav fs-nav--prev" @click.stop="activeImage = activeImage > 0 ? activeImage - 1 : images.length - 1" aria-label="上一张">‹</button>
        <button v-if="images.length > 1" class="fs-nav fs-nav--next" @click.stop="activeImage = activeImage < images.length - 1 ? activeImage + 1 : 0" aria-label="下一张">›</button>
        <img v-if="images[activeImage] && images[activeImage] !== '📦'" :src="images[activeImage]" class="fs-img" />
        <span v-else class="fs-placeholder">📦</span>
        <div v-if="images.length > 1" class="fs-dots">
          <button v-for="(_img, i) in images" :key="i" class="fs-dot" :class="{ active: activeImage === i }" @click.stop="activeImage = i" />
        </div>
      </div>

      <!-- Thumbnails -->
      <div v-if="images.length > 1" class="thumbnails">
        <div v-for="(img, i) in images" :key="i" class="thumb" :class="{ active: activeImage === i }" @click="activeImage = i">
          <LazyImage :src="img" alt="" height="100%" />
        </div>
      </div>
    </div>

    <!-- Info -->
    <div class="pdp-info">
      <h1 class="pdp-name">{{ product.name }}<span class="jd-tag">自营</span></h1>
      <p class="pdp-desc">{{ product.description || '' }}</p>

      <div class="pdp-rating">
        <ProductRating v-if="product.rating" :rating="product.rating || 0" :count="reviews.length" />
        <span v-else class="no-rating">暂无评分</span>
      </div>

      <!-- Price Box -->
      <div class="price-box">
        <div class="price-row">
          <span class="price-label">{{ t('product.price') }}</span>
          <span class="price-value">¥{{ (displayPrice() / 100).toFixed(2) }}</span>
          <span v-if="displayStock() > 0 && displayStock() <= 10" class="stock-warn">仅剩 {{ displayStock() }} 件</span>
          <span v-if="displayStock() === 0" class="stock-out">暂时缺货</span>
          <button class="alert-btn" :class="{ set: alertSet }" @click="setPriceAlert">{{ alertSet ? '🔔 已设置' : '🔔 降价提醒' }}</button>
        </div>
        <div class="meta-row">
          <span>{{ t('product.salesCount') }} <b class="text-red">{{ product.sales || 0 }}</b></span>
          <span>👁 <b class="text-blue">{{ viewerCount }}</b> 人正在看</span>
          <span>{{ t('product.stock') }} <b>{{ displayStock() }}</b></span>
        </div>
      </div>

      <!-- SKU Selector -->
      <div v-if="skus.length" class="sku-section">
        <div class="sku-label">{{ t('product.skuSelector') }}</div>
        <div class="sku-grid">
          <span v-for="sku in skus" :key="sku.id" class="sku-item" :class="{ selected: selectedSku?.id === sku.id }" @click="selectedSku = sku">
            <span v-if="(sku as any).specs" class="sku-color" :style="{ background: SKU_COLORS[((sku as any).specs || '').match(/红|蓝|绿|黄|紫|黑|白|灰|粉|金|银|橙/)?.[0] || ''] || '#ddd' }" />
            {{ sku.name }}
            <span class="sku-price">¥{{ (sku.price / 100).toFixed(2) }}</span>
          </span>
        </div>
      </div>

      <!-- Quantity -->
      <div class="qty-row">
        <span class="qty-label">{{ t('product.quantity') }}</span>
        <button class="qty-btn" @click="qty = Math.max(1, qty - 1)">-</button>
        <span class="qty-val">{{ qty }}</span>
        <button class="qty-btn" @click="qty = Math.min(displayStock(), qty + 1)">+</button>
      </div>

      <!-- Actions -->
      <div class="action-row">
        <JdButton type="outline" size="lg" class="flex-1" @click="onAddToCart">{{ t('product.addToCart') }}</JdButton>
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
    <div v-if="reviews.length">
      <div v-for="r in reviews" :key="r.id" class="review-item">
        <div class="review-header">
          <div class="review-user">
            <span class="review-username">{{ r.username }}</span>
            <span class="review-stars">{{ '★'.repeat(r.rating) }}</span>
          </div>
          <span class="review-date">{{ r.createdAt?.substring(0, 10) }}</span>
        </div>
        <p class="review-content">{{ r.content }}</p>
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
      <div v-if="(product as any).brandId" class="spec-row"><span class="spec-label">品牌</span><span>{{ (product as any).brandName || '品牌#' + (product as any).brandId }}</span></div>
      <div class="spec-row"><span class="spec-label">上架时间</span><span>{{ (product as any).createdAt?.substring(0, 10) || '-' }}</span></div>
    </div>
  </div>

  <!-- Related -->
  <div v-if="related.length">
    <h3 class="section-title">{{ t('product.relatedProducts') }}</h3>
    <div class="related-grid">
      <div v-for="p in related" :key="p.id" class="related-card" @click="goDetail(p.id)">
        <LazyImage :src="productImage(p)" :alt="p.name" height="140px" />
        <div class="related-info">
          <h5 class="related-name">{{ p.name }}</h5>
          <span class="related-price">¥{{ (p.price / 100).toFixed(2) }}</span>
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
    <LazyImage :src="images[activeImage]" :alt="product.name" height="48px" width="48px" rounded="6px" />
    <div class="floating-name">{{ product.name }}</div>
    <span class="floating-price">¥{{ (displayPrice() / 100).toFixed(2) }}</span>
    <JdButton type="outline" @click="onAddToCart">{{ t('product.addToCart') }}</JdButton>
    <JdButton @click="buyNow">{{ t('product.buyNow') }}</JdButton>
  </div>

  <!-- Mobile Sticky Bar -->
  <div v-if="product" class="mobile-bar">
    <div class="mobile-bar-price">¥{{ (displayPrice() / 100).toFixed(2) }}</div>
    <JdButton type="outline" class="flex-1" @click="onAddToCart">加入购物车</JdButton>
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

/* Main */
.pdp-main { display: flex; gap: var(--space-xxxl); background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxxl); box-shadow: var(--shadow-sm); margin-bottom: var(--space-xxl); }
.gallery { width: 420px; flex-shrink: 0; }
.gallery-main { cursor: zoom-in; }
.thumbnails { display: flex; gap: var(--space-sm); margin-top: var(--space-sm); }
.thumb { width: 60px; height: 60px; border-radius: var(--radius-sm); cursor: pointer; overflow: hidden; border: 1px solid var(--border); transition: border var(--transition-fast); }
.thumb.active { border: 2px solid var(--jd-red); }

/* Fullscreen */
.fullscreen-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,.95); z-index: 9999; display: flex; align-items: center; justify-content: center; animation: fadeIn .2s; }
.fs-close { position: absolute; top: var(--space-lg); right: var(--space-lg); z-index: 2; width: 36px; height: 36px; border-radius: 50%; background: rgba(255,255,255,.2); color: #fff; border: none; font-size: var(--font-lg); cursor: pointer; }
.fs-nav { position: absolute; top: 50%; transform: translateY(-50%); z-index: 2; width: 44px; height: 44px; border-radius: 50%; background: rgba(255,255,255,.15); color: #fff; border: none; font-size: var(--font-xxl); cursor: pointer; transition: background var(--transition); }
.fs-nav:hover { background: rgba(255,255,255,.3); }
.fs-nav--prev { left: var(--space-lg); }
.fs-nav--next { right: var(--space-lg); }
.fs-img { max-width: 100%; max-height: 100%; object-fit: contain; }
.fs-placeholder { font-size: 120px; }
.fs-dots { position: absolute; bottom: var(--space-xl); left: 50%; transform: translateX(-50%); display: flex; gap: var(--space-xs); }
.fs-dot { width: 8px; height: 8px; border-radius: 50%; border: none; cursor: pointer; background: rgba(255,255,255,.4); }
.fs-dot.active { background: var(--jd-red); }

/* Info */
.pdp-info { flex: 1; }
.pdp-name { font-size: var(--font-title); margin-bottom: var(--space-sm); display: flex; align-items: center; }
.jd-tag { font-size: var(--font-xs); background: var(--jd-red); color: #fff; padding: 1px 6px; border-radius: 3px; margin-left: var(--space-sm); font-weight: 400; }
.pdp-desc { color: var(--jd-red); font-size: var(--font-base); margin-bottom: var(--space-sm); }
.pdp-rating { margin-bottom: var(--space-md); display: flex; align-items: center; gap: var(--space-sm); }
.no-rating { color: var(--text-placeholder); font-size: var(--font-sm); }

/* Price Box */
.price-box { background: linear-gradient(135deg, var(--jd-red-light), var(--jd-red-bg)); padding: var(--space-xl); border-radius: var(--radius-md); margin-bottom: var(--space-lg); }
.price-row { display: flex; align-items: baseline; gap: var(--space-md); flex-wrap: wrap; }
.price-label { color: var(--text-tertiary); font-size: var(--font-base); }
.price-value { color: var(--jd-red); font-size: 32px; font-weight: 700; }
.stock-warn { padding: 2px 6px; background: #fff3cd; color: #856404; border-radius: 3px; font-size: var(--font-xs); }
.stock-out { padding: 2px 6px; background: #f8d7da; color: #721c24; border-radius: 3px; font-size: var(--font-xs); }
.alert-btn { background: none; border: 1px solid var(--jd-red); color: var(--jd-red); border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-sm); padding: 2px 8px; white-space: nowrap; }
.alert-btn.set { background: var(--jd-red); color: #fff; }
.meta-row { display: flex; gap: var(--space-xxl); margin-top: var(--space-md); font-size: var(--font-base); color: var(--text-secondary); }
.text-red { color: var(--jd-red); }
.text-blue { color: var(--blue); }

/* SKU */
.sku-section { margin-bottom: var(--space-lg); }
.sku-label { font-size: var(--font-base); color: var(--text-secondary); margin-bottom: var(--space-sm); }
.sku-grid { display: flex; flex-wrap: wrap; gap: var(--space-sm); }
.sku-item { padding: var(--space-sm) var(--space-lg); border-radius: var(--radius-md); cursor: pointer; font-size: var(--font-base); transition: all var(--transition); font-weight: 500; display: flex; align-items: center; gap: 6px; border: 1px solid var(--border); color: var(--text-primary); background: var(--bg-white); }
.sku-item.selected { border: 2px solid var(--jd-red); color: var(--jd-red); background: var(--jd-red-light); }
.sku-color { width: 14px; height: 14px; border-radius: 50%; border: 1px solid var(--border); display: inline-block; flex-shrink: 0; }
.sku-price { font-size: var(--font-xs); color: var(--text-tertiary); }

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
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideDown { from { transform: translateY(-100%); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

@media (min-width: 769px) { .floating-bar { display: flex; } }
@media (max-width: 768px) {
  .pdp-main { flex-direction: column; padding: var(--space-md); }
  .gallery { width: 100%; }
  .related-grid { grid-template-columns: repeat(2, 1fr); }
  .pdp-section { padding: var(--space-md); }
  .mobile-bar { display: flex; }
  .floating-bar { display: none; }
}
</style>
