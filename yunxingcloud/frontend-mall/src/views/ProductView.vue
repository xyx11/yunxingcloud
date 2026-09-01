<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, getAlsoBought } from '@/api/product'
import { addToCart } from '@/api/cart'
import { checkFavorite, addFavorite, removeFavorite } from '@/api/order'
import { useAuthStore } from '@/stores/auth'
import { useRecentlyViewed } from '@/composables/useRecentlyViewed'
import { useCartFly } from '@/composables/useCartFly'
import { useAnalytics } from '@/composables/useAnalytics'
import { useI18n } from '@/locales'
import { useToast } from '@/composables/useToast'
import { formatPrice, getProductImage } from '@/utils/format'
import type { Product, Sku, Review } from '@/types'

// Sub-components
import ProductGallery from '@/components/ProductGallery.vue'
import SkuSelector from '@/components/SkuSelector.vue'
import ProductInfo from '@/components/ProductInfo.vue'
import ProductReviews from '@/components/ProductReviews.vue'

// Retained existing components
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import JdModal from '@/components/JdModal.vue'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { t } = useI18n()
const toast = useToast()
const { flyToCart } = useCartFly()
const analytics = useAnalytics()

// Data state
const product = ref<Product | null>(null)
const notFound = ref(false)
const skus = ref<Sku[]>([])
const reviews = ref<Review[]>([])
const reviewAnalytics = ref<{ avgRating?: number; total?: number; distribution?: Record<number, number> }>()
const restockSubscribing = ref(false)
const restockDone = ref(false)

async function subscribeRestock() {
  if (!product.value || !auth.isLoggedIn) { router.push('/login'); return }
  restockSubscribing.value = true
  try {
    await request.post('/products/' + product.value.id + '/back-in-stock-alert')
    restockDone.value = true
    toast.success(t('product.restockNotifySuccess'))
  } catch { toast.error(t('product.restockNotifyFail')) }
  finally { restockSubscribing.value = false }
}

function openChat() {
  window.dispatchEvent(new CustomEvent('chat:open'))
}

function injectStructuredData(p: Product) {
  const el = document.getElementById('product-ld') || (() => { const s = document.createElement('script'); s.id = 'product-ld'; s.type = 'application/ld+json'; document.head.appendChild(s); return s })()
  el.textContent = JSON.stringify({
    '@context': 'https://schema.org',
    '@type': 'Product',
    name: p.name,
    description: p.description?.replace(/<[^>]*>/g, '') || '',
    image: images.value[0],
    offers: {
      '@type': 'Offer',
      price: (p.price / 100).toFixed(2),
      priceCurrency: 'CNY',
      availability: displayStock.value > 0 ? 'https://schema.org/InStock' : 'https://schema.org/OutOfStock',
    },
    ...(p.rating && { aggregateRating: { '@type': 'AggregateRating', ratingValue: p.rating, reviewCount: p.reviewCount || 0 } }),
    ...(p.brandName && { brand: { '@type': 'Brand', name: p.brandName } }),
  })
}

const related = ref<Product[]>([])
const alsoBought = ref<Product[]>([])
const selectedSku = ref<Sku | null>(null)
const qty = ref(1)
// 选SKU时显示该SKU图片
watch(selectedSku, (sku) => { if (sku?.imageUrl && images.value[0] !== sku.imageUrl) { images.value = [sku.imageUrl, ...images.value.filter(i => i !== sku.imageUrl)] } })
const favorited = ref(false)
const alertSet = ref(false)
const loading = ref(true)
const addingToCart = ref(false)
const addedToCart = ref(false)
const buyingNow = ref(false)
const shareMenu = ref(false)
const showFloatingBar = ref(false)
const images = ref<string[]>([])
const alertPrice = ref('')
const showPriceAlert = ref(false)
const lightboxRef = ref<HTMLElement | null>(null)
const previewImage = ref('')
const previewIndex = ref(0)
const allPreviewImages = computed(() => {
  const imgs: string[] = []
  reviews.value.forEach(r => {
    if (r.images?.length) imgs.push(...r.images)
  })
  // Also add product images
  if (product.value?.imageUrl) imgs.unshift(product.value.imageUrl)
  if (product.value?.images?.length) imgs.push(...product.value.images)
  return imgs
})
const lastFocusedEl = ref<HTMLElement | null>(null)

function openPreview(img: string) {
  lastFocusedEl.value = document.activeElement as HTMLElement
  previewIndex.value = allPreviewImages.value.indexOf(img)
  if (previewIndex.value < 0) previewIndex.value = 0
  previewImage.value = img
  setTimeout(() => lightboxRef.value?.focus(), 50)
}

function prevPreview() {
  if (allPreviewImages.value.length <= 1) return
  previewIndex.value = (previewIndex.value - 1 + allPreviewImages.value.length) % allPreviewImages.value.length
  previewImage.value = allPreviewImages.value[previewIndex.value]
}

function nextPreview() {
  if (allPreviewImages.value.length <= 1) return
  previewIndex.value = (previewIndex.value + 1) % allPreviewImages.value.length
  previewImage.value = allPreviewImages.value[previewIndex.value]
}

function closePreview() {
  previewImage.value = ''
  setTimeout(() => lastFocusedEl.value?.focus(), 50)
}

function onLightboxKey(e: KeyboardEvent) {
  if (e.key === 'Escape') { closePreview(); return }
  if (e.key === 'ArrowLeft') { prevPreview(); return }
  if (e.key === 'ArrowRight') { nextPreview(); return }
  if (e.key === 'Tab' && lightboxRef.value) {
    const focusable = lightboxRef.value.querySelectorAll<HTMLElement>('button, [tabindex]:not([tabindex="-1"])')
    const first = focusable[0]; const last = focusable[focusable.length - 1]
    if (e.shiftKey && document.activeElement === first) { e.preventDefault(); last?.focus() }
    else if (!e.shiftKey && document.activeElement === last) { e.preventDefault(); first?.focus() }
  }
}
const couponCount = ref(0)

let scrollTicking = false
function onScroll() { if (!scrollTicking) { scrollTicking = true; requestAnimationFrame(() => { showFloatingBar.value = window.scrollY > 500; scrollTicking = false }) } }

const displayPrice = computed(() =>
  selectedSku.value ? selectedSku.value.price : product.value?.price || 0
)
const displayStock = computed(() =>
  selectedSku.value ? selectedSku.value.stock : product.value?.stock || 0
)

// --- Lifecycle ---

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await getProductDetail(Number(id))
    product.value = res.data.product
    skus.value = res.data.skus || []
    reviews.value = res.data.reviews || []
    related.value = res.data.related || []
        if (product.value?.imageUrl) images.value = [product.value.imageUrl, ...(product.value.images || [])]
        else if (product.value?.images) images.value = product.value.images
        if (!images.value.length) images.value = ['📦']
    const { add } = useRecentlyViewed()
    if (product.value) {
      add({ id: product.value.id, name: product.value.name, price: product.value.price, imageUrl: product.value.imageUrl })
    }

    // 并行加载非关键数据
    const parallel = [
      request.get('/reviews/summary/' + Number(id)).then(ar => { reviewAnalytics.value = ar.data || null }).catch(() => {}),
      getAlsoBought(Number(id), 4).then(r => { alsoBought.value = r.data || [] }).catch(() => {}),
    ]
    if (auth.isLoggedIn) {
      parallel.push(checkFavorite(Number(id)).then(r => { favorited.value = r.data.favorited }).catch(() => {}))
    }
    parallel.push(request.get('/coupons/available?productId=' + Number(id)).then(cr => { couponCount.value = cr.data?.length || cr.data?.count || 0 }).catch(() => {}))
    await Promise.allSettled(parallel)
  } catch (e: unknown) {
    const err = e as { response?: { status?: number; data?: { message?: string } }; message?: string }
    const status = err.response?.status
    if (status === 404) { notFound.value = true }
    else if (status === 401 || status === 403) { /* handled by request interceptor */ }
    else {
      const msg = err.response?.data?.message || err.message || ''
      toast.error(msg || t('product.loadFail'))
    }
  } finally { loading.value = false }

  window.addEventListener('scroll', onScroll)

  // Structured data for search engines
  if (product.value) { injectStructuredData(product.value); analytics.productView(product.value.id) }
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})

// --- Actions ---

async function onAddToCart(e?: MouseEvent) {
  if (!auth.isLoggedIn) { router.push('/login'); return }
  if (!product.value || addingToCart.value) return
  if (skus.value.length > 0 && !selectedSku.value) {
    toast.error(t('product.selectSpecsFirst'))
    const el = document.querySelector('.sku-section') as HTMLElement | null
    if (el) { el.scrollIntoView({ behavior: 'smooth', block: 'center' }); el.classList.add('sku-flash'); setTimeout(() => el.classList.remove('sku-flash'), 600) }
    return
  }
  addingToCart.value = true
  try {
    await addToCart(product.value.id, qty.value)
    toast.success(t('toast.addedToCart'))
    addedToCart.value = true; setTimeout(() => addedToCart.value = false, 1500)
    if (e) flyToCart(e)
  } catch { toast.error(t('toast.addCartFail')) }
  finally { addingToCart.value = false }
}

async function buyNow() {
  if (!auth.isLoggedIn) { router.push('/login'); return }
  if (!product.value || buyingNow.value) return
  buyingNow.value = true
  try {
    await addToCart(product.value.id, qty.value)
    localStorage.setItem('checkout_buy_now', String(product.value.id))
    router.push('/checkout')
  }
  catch { toast.error(t('toast.addCartFail')) }
  finally { buyingNow.value = false }
}

async function toggleFavorite() {
  if (!auth.isLoggedIn) { router.push('/login'); return }
  if (!product.value) return
  try {
    if (favorited.value) { await removeFavorite(product.value.id); toast.info(t('product.unfavorite')) }
    else { await addFavorite(product.value.id); toast.success(t('product.favorite')) }
    favorited.value = !favorited.value
  } catch { toast.error(t('toast.favOpFail')) }
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
    toast.success(t('toast.priceAlertSet'))
  } catch { toast.error(t('toast.priceAlertFail')) }
}

async function shareProduct(channel?: string) {
  const url = window.location.href
  const title = product.value?.name || ''
  if (channel === 'copy') {
    try { await navigator.clipboard.writeText(url); toast.success(t('toast.copied')) } catch { toast.error(t('toast.copyFail')) }
  } else if (channel === 'wechat') {
    toast.info(t('product.shareWechat'))
    try { await navigator.clipboard.writeText(url) } catch {}
  } else if (navigator.share) {
    try { await navigator.share({ title, url }) } catch {}
  } else {
    try { await navigator.clipboard.writeText(url); toast.success(t('toast.copied')) } catch {}
  }
  shareMenu.value = false
  try { await request.post('/social/share', { productId: product.value?.id, channel: channel || 'copy' }) } catch {}
}

function goDetail(id: number) { router.push(`/product/${id}`) }
</script>

<template>
  <div>
  <!-- Breadcrumb -->
  <div class="breadcrumb">
    <span @click="router.push('/')" class="crumb-link">{{ t('product.breadcrumbHome') }}</span><span>/</span>
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

  <!-- Not Found -->
  <div v-else-if="notFound" class="pdp-not-found">
    <span style="font-size:64px">📦</span>
    <h2>{{ t('product.notFoundTitle') }}</h2>
    <p style="color:var(--text-tertiary);margin-bottom:var(--space-xl)">{{ t('product.notFoundDesc') }}</p>
    <router-link to="/" class="nf-back-btn">{{ t('product.breadcrumbHome') }}</router-link>
  </div>

  <!-- Fallback: product loaded but null (API error without 404) -->
  <div v-else-if="!product" class="pdp-not-found">
    <span style="font-size:64px">🔌</span>
    <h2>{{ t('product.loadFail') }}</h2>
    <p style="color:var(--text-tertiary);margin-bottom:var(--space-xl)">{{ t('product.notFoundDesc') }}</p>
    <router-link to="/products" class="nf-back-btn">{{ t('product.viewAll') }}</router-link>
  </div>

  <!-- Main Product -->
  <div v-else class="pdp-main">
    <ProductGallery :images="images" :product-name="product.name" />

    <div class="pdp-info">
      <ProductInfo
        :product="product"
        :selected-sku="selectedSku"
        :favorited="favorited"
        :reviews-count="reviews.length"
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
        <template v-if="displayStock === 0">
          <JdButton size="lg" class="flex-1" :loading="restockSubscribing" :disabled="restockDone" @click="subscribeRestock">
            {{ restockDone ? t('product.restockDone') : restockSubscribing ? t('product.restocking') : t('product.restockBtn') }}
          </JdButton>
        </template>
        <template v-else>
          <JdButton type="outline" size="lg" class="flex-1" :loading="addingToCart" :disabled="addingToCart || addedToCart" @click="() => onAddToCart()">{{ addedToCart ? '✓ ' + t('toast.addedToCart') : t('product.addToCart') }}</JdButton>
          <JdButton size="lg" class="flex-1" :loading="buyingNow" :disabled="buyingNow" @click="buyNow">{{ t('product.buyNow') }}</JdButton>
        </template>
        <button class="icon-btn" :class="{ active: favorited }" @click="toggleFavorite" :aria-label="t('product.favorite')">{{ favorited ? '❤️' : '🤍' }}</button>
        <button class="icon-btn" @click="() => shareProduct()" :aria-label="t('product.share')">📤</button>
        <button class="icon-btn" @click="openChat" :aria-label="t('product.contactService')" :title="t('product.contactService')">💬</button>
      </div>
    </div>
  </div>

  <!-- Stock Alert + Coupon Banner -->
  <div v-if="product" class="info-banners">
    <div v-if="displayStock <= 10 && displayStock > 0" class="stock-alert-banner">
      ⚡ {{ t('product.stockLabel') }}: {{ displayStock }}
    </div>
    <div class="coupon-banner" @click="router.push('/coupons')">
      🎫 {{ t('product.couponAvailable', { n: String(couponCount > 0 ? couponCount : 0) }) }}
    </div>
    <div class="recent-sales-banner">
      🔥 {{ t('product.recentViewers', { n: 5 }) }}
    </div>
  </div>

  <!-- Reviews -->
  <ProductReviews
    v-if="product"
    :product-id="product.id"
    :reviews="reviews"
    :review-analytics="reviewAnalytics"
    @open-preview="openPreview"
    @reviews-updated="reviews = $event"
  />

  <!-- Specs -->
  <div v-if="product" class="pdp-section">
    <h3 class="section-title">{{ t('product.specsTitle') }}</h3>
    <div class="spec-grid">
      <div class="spec-row"><span class="spec-label">{{ t('product.specName') }}</span><span>{{ product.name }}</span></div>
      <div class="spec-row"><span class="spec-label">{{ t('product.specNo') }}</span><span>{{ product.id }}</span></div>
      <div v-if="product.brandId" class="spec-row"><span class="spec-label">{{ t('product.brand') }}</span><span>{{ product.brandName || t('product.brand') + '#' + product.brandId }}</span></div>
      <div class="spec-row"><span class="spec-label">{{ t('common.createdAt') }}</span><span>{{ String(product.createdAt || '').substring(0, 10) || '-' }}</span></div>
    </div>
  </div>

  <!-- Product Description -->
  <div v-if="product?.description" class="pdp-section">
    <h3 class="section-title">{{ t('product.detail') }}</h3>
    <div class="product-desc">{{ product.description.replace(/<[^>]*>/g, '') }}</div>
  </div>

  <!-- Key highlights -->
  <div class="pdp-section highlights-section">
    <h3 class="section-title">{{ t('help.service') }}</h3>
    <div class="highlights-grid">
      <div class="highlight-item">
        <span class="highlight-icon">✓</span>
        <div><strong>{{ t('product.highlightGenuine') }}</strong><p class="highlight-desc">{{ t('footer.promise1Desc') }}</p></div>
      </div>
      <div class="highlight-item">
        <span class="highlight-icon">🚚</span>
        <div><strong>{{ t('product.highlightDelivery') }}</strong><p class="highlight-desc">{{ t('footer.promise2Desc') }}</p></div>
      </div>
      <div class="highlight-item">
        <span class="highlight-icon">↩</span>
        <div><strong>{{ t('product.highlightReturn') }}</strong><p class="highlight-desc">{{ t('footer.promise3Desc') }}</p></div>
      </div>
      <div class="highlight-item">
        <span class="highlight-icon">🎧</span>
        <div><strong>{{ t('product.highlightService') }}</strong><p class="highlight-desc">{{ t('footer.promise4Desc') }}</p></div>
      </div>
    </div>
  </div>

  <!-- FAQ -->
  <div class="pdp-section">
    <h3 class="section-title">{{ t('product.faqTitle') }}</h3>
    <div class="faq-list">
      <details class="faq-item">
        <summary class="faq-q">{{ t('product.faqQ1') }}</summary>
        <p class="faq-a">{{ t('product.faqA1') }}</p>
      </details>
      <details class="faq-item">
        <summary class="faq-q">{{ t('product.faqQ2') }}</summary>
        <p class="faq-a">{{ t('product.faqA2') }}</p>
      </details>
      <details class="faq-item">
        <summary class="faq-q">{{ t('product.faqQ3') }}</summary>
        <p class="faq-a">{{ t('product.faqA3') }}</p>
      </details>
      <details class="faq-item">
        <summary class="faq-q">💳 {{ t('help.catPayment') }}</summary>
        <p class="faq-a">{{ t('help.a5') }}</p>
      </details>
    </div>
  </div>

  <!-- Related -->
  <div v-if="related.length" class="pdp-section">
    <h3 class="section-title">{{ t('product.relatedProducts') }}</h3>
    <div class="related-grid">
      <div v-for="p in related" :key="p.id" class="related-card" role="button" tabindex="0" @click="goDetail(p.id)" @keydown.enter.prevent="goDetail(p.id)" @keydown.space.prevent="goDetail(p.id)">
        <LazyImage :src="getProductImage(p)" :alt="p.name" height="140px" />
        <div class="related-info">
          <h5 class="related-name">{{ p.name }}</h5>
          <span class="related-price">{{ formatPrice(p.price / 100, 2) }}</span>
        </div>
      </div>
    </div>
  </div>

  <!-- Also Bought -->
  <div v-if="alsoBought.length" class="pdp-section">
    <h3 class="section-title">{{ t('product.alsoBought') }}</h3>
    <div class="related-grid">
      <div v-for="p in alsoBought" :key="p.id" class="related-card" role="button" tabindex="0" @click="goDetail(p.id)" @keydown.enter.prevent="goDetail(p.id)" @keydown.space.prevent="goDetail(p.id)">
        <LazyImage :src="getProductImage(p)" :alt="p.name" height="140px" />
        <div class="related-info">
          <h5 class="related-name">{{ p.name }}</h5>
          <span class="related-price">{{ formatPrice(p.price / 100, 2) }}</span>
        </div>
      </div>
    </div>
  </div>

  <!-- Price Alert Modal -->
  <JdModal v-model:visible="showPriceAlert" :title="'🔔 ' + t('product.setPriceAlert')" width="380px">
    <p class="alert-desc">{{ t('product.restockNotifySuccess') }}</p>
    <div class="alert-input-row">
      <span class="alert-input-label">{{ t('product.targetPrice') }} ¥</span>
      <input v-model="alertPrice" type="number" step="0.01" class="alert-input" />
    </div>
    <template #footer>
      <JdButton type="ghost" @click="showPriceAlert = false">{{ t('common.cancel') }}</JdButton>
      <JdButton @click="confirmPriceAlert">{{ t('product.confirmSet') }}</JdButton>
    </template>
  </JdModal>

  <!-- Image lightbox -->
  <div v-if="previewImage" class="img-lightbox" @click="closePreview" @keydown="onLightboxKey" tabindex="0" ref="lightboxRef">
    <button class="lightbox-close" @click.stop="closePreview" :aria-label="t('common.close')">✕</button>
    <button v-if="allPreviewImages.length > 1" class="lightbox-nav lightbox-prev" @click.stop="prevPreview" aria-label="‹">‹</button>
    <img :src="previewImage" :alt="t('product.previewImage')" @click.stop />
    <button v-if="allPreviewImages.length > 1" class="lightbox-nav lightbox-next" @click.stop="nextPreview" aria-label="›">›</button>
    <div v-if="allPreviewImages.length > 1" class="lightbox-counter">{{ previewIndex + 1 }} / {{ allPreviewImages.length }}</div>
  </div>

  <!-- Floating Bar -->
  <div v-if="showFloatingBar && product" class="floating-bar">
    <LazyImage :src="images[0]" :alt="product.name" height="48px" width="48px" rounded="6px" />
    <div class="floating-name">{{ product.name }}</div>
    <span class="floating-price">{{ formatPrice(displayPrice / 100, 2) }}</span>
    <JdButton type="outline" :loading="addingToCart" :disabled="addingToCart || addedToCart" @click="() => onAddToCart()">{{ addedToCart ? '✓ ' + t('toast.addedToCart') : t('product.addToCart') }}</JdButton>
    <JdButton :loading="buyingNow" :disabled="buyingNow" @click="buyNow">{{ t('product.buyNow') }}</JdButton>
  </div>

  <!-- Mobile Sticky Bar -->
  <div v-if="product" class="mobile-bar">
    <button class="mobile-bar-fav" :class="{ faved: favorited }" @click="toggleFavorite" :aria-label="favorited ? t('product.favoritedAria') : t('product.unfavoriteAria')">{{ favorited ? '❤️' : '🤍' }}</button>
    <div class="mobile-bar-price">{{ formatPrice(displayPrice / 100, 2) }}</div>
    <JdButton type="outline" class="flex-1" :loading="addingToCart" :disabled="addingToCart || addedToCart" @click="() => onAddToCart()">{{ addedToCart ? '✓ ' + t('toast.addedToCart') : t('product.addToCart') }}</JdButton>
    <JdButton class="flex-1" :loading="buyingNow" :disabled="buyingNow" @click="buyNow">{{ t('product.buyNow') }}</JdButton>
  </div>
  </div>
</template>

<style scoped>
.breadcrumb { font-size: var(--font-sm); color: var(--text-tertiary); margin-bottom: var(--space-md); display: flex; align-items: center; gap: 6px; }
.crumb-link { cursor: pointer; color: var(--text-secondary); }
.crumb-link:hover { color: var(--jd-red); }
.crumb-current { color: var(--text-primary); }

/* Skeleton */
.pdp-not-found { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 400px; text-align: center; background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxxl); }
.nf-back-btn { display: inline-block; padding: 10px 32px; background: var(--jd-red); color: #fff; border-radius: var(--radius-round); text-decoration: none; font-weight: 600; font-size: var(--font-md); }
.nf-back-btn:hover { background: var(--jd-red-dark); }

/* SKU flash on validation */
.sku-flash { animation: skuFlash .6s ease; }
@keyframes skuFlash {
  0%, 100% { box-shadow: 0 0 0 0 rgba(241,2,21,.4); }
  50% { box-shadow: 0 0 0 8px rgba(241,2,21,.15); }
}

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
.pdp-section { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-sm); margin-bottom: var(--space-xxl); content-visibility: auto; contain-intrinsic-size: auto 300px; }
.section-title { font-size: var(--font-lg); font-weight: 700; margin-bottom: var(--space-lg); }

.img-lightbox { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,.92); z-index: 1000; display: flex; align-items: center; justify-content: center; cursor: pointer; outline: none; }
.img-lightbox img { max-width: 85vw; max-height: 85vh; object-fit: contain; border-radius: 8px; cursor: default; }
.lightbox-close { position: fixed; top: 20px; right: 20px; width: 44px; height: 44px; border-radius: 50%; background: rgba(255,255,255,.15); color: #fff; border: none; cursor: pointer; font-size: 22px; display: flex; align-items: center; justify-content: center; transition: background var(--transition-fast); z-index: 1001; }
.lightbox-close:hover { background: rgba(255,255,255,.3); }
.lightbox-nav { position: fixed; top: 50%; transform: translateY(-50%); z-index: 1001; width: 56px; height: 56px; border-radius: 50%; background: rgba(255,255,255,.12); color: #fff; border: none; cursor: pointer; font-size: 36px; display: flex; align-items: center; justify-content: center; transition: background var(--transition-fast); user-select: none; }
.lightbox-nav:hover { background: rgba(255,255,255,.25); }
.lightbox-prev { left: 16px; }
.lightbox-next { right: 16px; }
.lightbox-counter { position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%); color: rgba(255,255,255,.7); font-size: 14px; z-index: 1001; background: rgba(0,0,0,.4); padding: 4px 16px; border-radius: var(--radius-round); }

/* Specs */
.spec-grid { display: grid; grid-template-columns: 1fr 1fr; font-size: var(--font-base); }
.spec-row { display: flex; border-bottom: 1px solid var(--border-light); padding: var(--space-md) 0; }
.spec-label { color: var(--text-tertiary); width: 100px; flex-shrink: 0; }

/* Product description */
.product-desc { padding: var(--space-md) 0; line-height: 1.8; color: var(--text-secondary); font-size: var(--font-base); }
.product-desc :deep(img) { max-width: 100%; border-radius: var(--radius-md); margin: var(--space-md) 0; }
.product-desc :deep(p) { margin-bottom: var(--space-sm); }

/* Highlights */
.highlights-section { background: linear-gradient(135deg, #f9fafb, #f0f4f8); border-radius: var(--radius-lg); padding: var(--space-xl); }
.highlights-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-lg); }
.highlight-item { display: flex; align-items: flex-start; gap: var(--space-md); }
.highlight-icon { font-size: 24px; width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; background: var(--bg-white); border-radius: var(--radius-md); box-shadow: var(--shadow-sm); flex-shrink: 0; }
.highlight-desc { font-size: var(--font-xs); color: var(--text-tertiary); margin-top: 2px; }

/* FAQ */
.faq-list { display: flex; flex-direction: column; gap: var(--space-sm); }
.faq-item { border: 1px solid var(--border-light); border-radius: var(--radius-md); overflow: hidden; transition: border-color var(--transition-fast); }
.faq-item[open] { border-color: var(--jd-red); }
.faq-q { padding: var(--space-md) var(--space-lg); cursor: pointer; font-size: var(--font-sm); font-weight: 600; color: var(--text-primary); background: var(--bg-white); user-select: none; list-style: none; display: flex; justify-content: space-between; align-items: center; }
.faq-q::-webkit-details-marker { display: none; }
.faq-q::after { content: '▸'; font-size: 14px; transition: transform var(--transition-fast); }
.faq-item[open] .faq-q::after { transform: rotate(90deg); }
.faq-a { padding: 0 var(--space-lg) var(--space-md); font-size: var(--font-sm); color: var(--text-secondary); line-height: 1.6; }

/* Related */
.related-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-md); }
.related-card { background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition); will-change: transform; }
.related-card:hover { transform: translateY(-4px); }
.related-info { padding: var(--space-sm) var(--space-md); }
.related-name { font-size: var(--font-base); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: var(--space-xs); color: var(--text-primary); }
.related-price { color: var(--jd-red); font-size: var(--font-lg); font-weight: 700; }

/* Alert Modal (hosted inside JdModal) */
.alert-desc { color: var(--text-secondary); font-size: var(--font-base); margin-bottom: var(--space-md); }
.alert-input-row { display: flex; align-items: center; gap: var(--space-sm); margin-bottom: var(--space-lg); }
.alert-input-label { font-size: var(--font-base); color: var(--text-secondary); }
.alert-input { flex: 1; padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md); font-size: var(--font-md); background: var(--bg-white); color: var(--text-primary); }

/* Floating Bar */
.floating-bar { display: flex; position: fixed; top: 60px; left: 0; right: 0; z-index: 90; background: var(--bg-white); box-shadow: var(--shadow-md); padding: var(--space-sm) var(--space-xxl); align-items: center; gap: var(--space-xl); animation: slideDown .3s ease-out; }
.floating-name { flex: 1; min-width: 0; font-size: 15px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.floating-price { color: var(--jd-red); font-size: var(--font-title); font-weight: 700; white-space: nowrap; }

/* Mobile Bar */
.mobile-bar { display: none; position: fixed; bottom: 0; left: 0; right: 0; background: var(--bg-white); border-top: 1px solid var(--border-light); padding: var(--space-md) var(--space-lg); z-index: 210; align-items: center; gap: var(--space-md); box-shadow: 0 -2px 8px rgba(0,0,0,.06); padding-bottom: calc(var(--space-md) + env(safe-area-inset-bottom, 0px)); }
.mobile-bar-fav { background: none; border: none; font-size: 22px; cursor: pointer; padding: 4px; transition: transform .2s; flex-shrink: 0; }
.mobile-bar-fav:active { transform: scale(1.3); }
.mobile-bar-fav.faved { animation: heartBeat .4s ease; }
@keyframes heartBeat {
  0%, 100% { transform: scale(1); }
  25% { transform: scale(1.3); }
  50% { transform: scale(1); }
  75% { transform: scale(1.2); }
}
.mobile-bar-price { color: var(--jd-red); font-size: var(--font-xl); font-weight: 700; white-space: nowrap; }

.flex-1 { flex: 1; }

/* Info banners */
.info-banners { display: flex; flex-direction: column; gap: var(--space-sm); margin-bottom: var(--space-lg); }
.stock-alert-banner { background: #fff0f0; border: 1px solid var(--jd-red); color: var(--jd-red); padding: var(--space-sm) var(--space-lg); border-radius: var(--radius-md); font-size: var(--font-sm); font-weight: 600; text-align: center; animation: pulse-stock 1.5s ease-in-out infinite; }
@keyframes pulse-stock { 0%, 100% { opacity: 1; } 50% { opacity: .7; } }
.coupon-banner { background: #fff9e0; border: 1px solid #ffc107; color: #856404; padding: var(--space-sm) var(--space-lg); border-radius: var(--radius-md); font-size: var(--font-sm); font-weight: 600; text-align: center; cursor: pointer; transition: background var(--transition-fast); }
.coupon-banner:hover { background: #fff3cd; }
.recent-sales-banner { background: #f0f7ff; color: var(--blue); padding: var(--space-sm) var(--space-lg); border-radius: var(--radius-md); font-size: var(--font-sm); text-align: center; }

@keyframes slideDown { from { transform: translateY(-100%); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

@media (min-width: 769px) { .floating-bar { display: flex; } }
@media (max-width: 768px) {
  .pdp-main { flex-direction: column; padding: var(--space-md); }
  .related-grid { grid-template-columns: repeat(2, 1fr); }
  .pdp-section { padding: var(--space-md); }
  .spec-grid { grid-template-columns: 1fr; }
  .action-row { flex-wrap: wrap; }
  .action-row .icon-btn { width: 40px; height: 40px; }
  .mobile-bar { display: flex; }
  .floating-bar { display: none; }
}
</style>
