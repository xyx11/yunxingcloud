<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductById, getProductSkus, getProductReviews, getRelatedProducts } from '@/api/product'
import { addToCart } from '@/api/cart'
import { checkFavorite, addFavorite, removeFavorite } from '@/api/order'
import { useAuthStore } from '@/stores/auth'
import { useRecentlyViewed } from '@/composables/useRecentlyViewed'
import { useI18n } from '@/locales'
import ProductRating from '@/components/ProductRating.vue'
import ImageZoom from '@/components/ImageZoom.vue'
import ReviewSummary from '@/components/ReviewSummary.vue'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { t } = useI18n()
const toast = inject<any>('toast')
const product = ref<any>(null)
const skus = ref<any[]>([])
const reviews = ref<any[]>([])
const related = ref<any[]>([])
const selectedSku = ref<any>(null)
const qty = ref(1)
const favorited = ref(false)
const loading = ref(true)
const addAnim = ref(false)

onMounted(async () => {
  const id = route.params.id
  try {
    const [pRes, skuRes, revRes, relRes] = await Promise.all([
      getProductById(Number(id)), getProductSkus(Number(id)),
      getProductReviews(Number(id)), getRelatedProducts(Number(id)),
    ])
    product.value = pRes.data; skus.value = skuRes.data || []
    reviews.value = revRes.data || []; related.value = relRes.data || []
    const { add } = useRecentlyViewed()
    if (pRes.data) add({ id: pRes.data.id, name: pRes.data.name, price: pRes.data.price, imageUrl: pRes.data.imageUrl })
  } catch {} finally { loading.value = false }
  if (auth.isLoggedIn()) {
    try { const r = await checkFavorite(Number(id)); favorited.value = r.data.favorited } catch {}
  }
})

async function onAddToCart() {
  if (!auth.isLoggedIn()) { router.push('/login'); return }
  try { await addToCart(product.value.id, qty.value); toast.success(t('toast.addedToCart')); addAnim.value = true; setTimeout(() => addAnim.value = false, 600) } catch { toast.error(t('toast.addCartFail')) }
}
async function buyNow() { if (!auth.isLoggedIn()) { router.push('/login'); return }; try { await addToCart(product.value.id, qty.value); router.push('/cart') } catch {} }
async function toggleFavorite() {
  if (!auth.isLoggedIn()) { router.push('/login'); return }
  try { if (favorited.value) { await removeFavorite(product.value.id); toast.info(t('product.unfavorite')) } else { await addFavorite(product.value.id); toast.success(t('product.favorite')) }; favorited.value = !favorited.value } catch {}
}

// 价格提醒
const alertPrice = ref('')
const showPriceAlert = ref(false)
const alertSet = ref(false)
function setPriceAlert() { if (!auth.isLoggedIn()) { router.push('/login'); return }; alertPrice.value = String((displayPrice() / 100).toFixed(2)); showPriceAlert.value = true }
async function confirmPriceAlert() {
  try { await request.post('/price-alert', { productId: product.value.id, targetPrice: Number(alertPrice.value) * 100 }); alertSet.value = true; showPriceAlert.value = false; toast.success('降价时将通过通知提醒您') } catch { toast.error('设置失败') }
}
const displayPrice = () => selectedSku.value ? selectedSku.value.price : product.value?.price || 0
const displayStock = () => selectedSku.value ? selectedSku.value.stock : product.value?.stock || 0
function goDetail(id: number) { router.push(`/product/${id}`) }

// 触屏滑动
let touchX = 0
function onTouchStart(e: TouchEvent) { touchX = e.touches[0].clientX }
function onTouchEnd(e: TouchEvent) {
  const dx = e.changedTouches[0].clientX - touchX
  if (Math.abs(dx) > 50 && images.value.length > 1) {
    activeImage.value = dx < 0 ? Math.min(images.value.length - 1, activeImage.value + 1) : Math.max(0, activeImage.value - 1)
  }
}

// 分享
const shareMenu = ref(false)
async function shareProduct() {
  const url = window.location.href; const title = product.value?.name || ''
  if (navigator.share) { try { await navigator.share({ title, url }) } catch {} }
  else { await navigator.clipboard.writeText(url); toast.success('链接已复制') }
  shareMenu.value = false
}

// 图片画廊
const images = ref<string[]>([])
const activeImage = ref(0)
onMounted(() => {
  if (product.value?.imageUrl) images.value = [product.value.imageUrl, ...(product.value.images || [])]
  else if (product.value?.images) images.value = product.value.images
  if (!images.value.length) images.value = ['📦']
})
</script>

<template>
  <div v-if="loading" style="display:flex;gap:32px;background:#fff;border-radius:12px;padding:32px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
    <div style="width:420px;height:420px;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:8px;flex-shrink:0"></div>
    <div style="flex:1"><div style="height:28px;width:60%;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:4px;margin-bottom:16px"></div></div>
  </div>
  <div v-else-if="product" style="display:flex;gap:32px;background:#fff;border-radius:12px;padding:32px;box-shadow:0 2px 8px rgba(0,0,0,.06);margin-bottom:24px">
    <div style="width:420px;flex-shrink:0">
      <div @touchstart="onTouchStart" @touchend="onTouchEnd">
        <ImageZoom :src="images[activeImage]" :alt="product.name" height="420px" style="margin-bottom:12px" />
      </div>
      <div v-if="images.length > 1" style="display:flex;gap:8px">
        <div v-for="(img, i) in images" :key="i" @click="activeImage = i"
             style="width:60px;height:60px;border-radius:4px;cursor:pointer;display:flex;align-items:center;justify-content:center;font-size:24px;overflow:hidden;transition:border .2s"
             :style="{border: activeImage===i ? '2px solid #f10215' : '1px solid #ddd'}">
          <img v-if="img !== '📦'" :src="img" style="width:100%;height:100%;object-fit:cover" />
          <span v-else>📦</span>
        </div>
      </div>
    </div>
    <div style="flex:1">
      <h1 style="font-size:22px;margin-bottom:8px">{{ product.name }}<span style="font-size:11px;background:#f10215;color:#fff;padding:1px 6px;border-radius:3px;margin-left:8px;vertical-align:middle;font-weight:400">自营</span></h1>
      <p style="color:#f10215;font-size:13px;margin-bottom:8px">{{ product.description || '' }}</p>
      <div style="margin-bottom:12px;display:flex;align-items:center;gap:8px">
        <ProductRating v-if="product.rating" :rating="product.rating || 0" :count="reviews.length" />
        <span v-if="!product.rating" style="color:#aaa;font-size:12px">暂无评分</span>
      </div>
      <div style="background:linear-gradient(135deg,#fff5f5,#fff0f0);padding:20px;border-radius:8px;margin-bottom:16px">
        <div style="display:flex;align-items:baseline;gap:12px">
          <span style="color:#999;font-size:13px">{{ t('product.price') }}</span>
          <span style="color:#f10215;font-size:32px;font-weight:700">¥{{ (displayPrice() / 100).toFixed(2) }}</span>
          <span v-if="displayStock() > 0 && displayStock() <= 10" style="margin-left:8px;padding:2px 6px;background:#fff3cd;color:#856404;border-radius:3px;font-size:11px">仅剩 {{ displayStock() }} 件</span>
          <span v-if="displayStock() === 0" style="margin-left:8px;padding:2px 6px;background:#f8d7da;color:#721c24;border-radius:3px;font-size:11px">暂时缺货</span>
          <button @click="setPriceAlert" style="margin-left:8px;background:none;border:1px solid #f10215;color:#f10215;border-radius:4px;cursor:pointer;font-size:12px;padding:2px 8px;white-space:nowrap" :style="alertSet?{background:'#f10215',color:'#fff'}:{}">{{ alertSet ? '🔔 已设置' : '🔔 降价提醒' }}</button>
        </div>
        <div style="display:flex;gap:24px;margin-top:12px;font-size:13px;color:#666">
          <span>{{ t('product.salesCount') }} <b style="color:#f10215">{{ product.sales || 0 }}</b></span>
          <span>{{ t('product.stock') }} <b>{{ displayStock() }}</b></span>
        </div>
      </div>
      <div v-if="skus.length" style="margin-bottom:16px">
        <div style="font-size:13px;color:#666;margin-bottom:8px">{{ t('product.skuSelector') }}</div>
        <div style="display:flex;flex-wrap:wrap;gap:8px">
          <span v-for="sku in skus" :key="sku.id" @click="selectedSku = sku"
                style="padding:8px 16px;border-radius:8px;cursor:pointer;font-size:13px;transition:all .2s;font-weight:500;display:flex;align-items:center;gap:6px"
                :style="{border:selectedSku?.id===sku.id?'2px solid #f10215':'1px solid #ddd',color:selectedSku?.id===sku.id?'#f10215':'#333',background:selectedSku?.id===sku.id?'#fff0f0':'#fff'}">
            <span v-if="sku.specs && /红|蓝|绿|黄|紫|黑|白|灰|粉|金|银|橙/i.test(sku.specs || sku.name)"
                  style="width:14px;height:14px;border-radius:50%;border:1px solid #ddd;display:inline-block;flex-shrink:0"
                  :style="{background:({'红':'#f10215','蓝':'#1677ff','绿':'#4caf50','黄':'#ffc107','紫':'#9c27b0','黑':'#333','白':'#fff','灰':'#999','粉':'#e91e63','金':'#ffc107','银':'#ccc','橙':'#ff9800'} as any)[sku.specs?.match(/红|蓝|绿|黄|紫|黑|白|灰|粉|金|银|橙/)?.[0] || ''] || '#ddd'}">
            </span>
            {{ sku.name }}
            <span style="font-size:11px;color:#999">¥{{ (sku.price/100).toFixed(2) }}</span>
          </span>
        </div>
      </div>
      <div style="display:flex;align-items:center;gap:12px;margin-bottom:24px">
        <span style="font-size:13px;color:#666">{{ t('product.quantity') }}</span>
        <button @click="qty = Math.max(1, qty - 1)" style="width:32px;height:32px;border:1px solid #ddd;background:#fff;cursor:pointer;font-size:18px;border-radius:4px">-</button>
        <span style="width:44px;text-align:center;font-size:15px">{{ qty }}</span>
        <button @click="qty = Math.min(displayStock(), qty + 1)" style="width:32px;height:32px;border:1px solid #ddd;background:#fff;cursor:pointer;font-size:18px;border-radius:4px">+</button>
      </div>
      <div style="display:flex;gap:12px">
        <button @click="onAddToCart" style="flex:1;height:48px;background:#fff;border:2px solid #f10215;color:#f10215;border-radius:8px;font-size:16px;cursor:pointer;font-weight:600;transition:all .2s"
                @mouseenter="(e:any) => { e.target.style.background='#fff0f0' }" @mouseleave="(e:any) => { e.target.style.background='#fff' }" :class="{ 'cart-bounce': addAnim }">{{ t('product.addToCart') }}</button>
        <button @click="buyNow" style="flex:1;height:48px;background:#f10215;color:#fff;border:none;border-radius:8px;font-size:16px;cursor:pointer;font-weight:600;transition:all .2s"
                @mouseenter="(e:any) => { e.target.style.background='#d4000f' }" @mouseleave="(e:any) => { e.target.style.background='#f10215' }">{{ t('product.buyNow') }}</button>
        <button @click="toggleFavorite" style="width:48px;height:48px;border:1px solid #ddd;border-radius:8px;cursor:pointer;font-size:22px;background:#fff;transition:all .2s"
                :style="{color:favorited?'#f10215':'#999'}">{{ favorited ? '❤️' : '🤍' }}</button>
        <button @click="shareProduct" style="width:48px;height:48px;border:1px solid #ddd;border-radius:8px;cursor:pointer;font-size:18px;background:#fff;transition:all .2s;color:#666"
                @mouseenter="(e:any) => { e.target.style.background='#f0f0f0' }" @mouseleave="(e:any) => { e.target.style.background='#fff' }">📤</button>
      </div>
    </div>
  </div>
  <div v-if="product" style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06);margin-bottom:24px">
    <h3 style="font-size:18px;font-weight:700;margin-bottom:16px">{{ t('product.reviews') }} ({{ reviews.length }})</h3>
    <ReviewSummary v-if="reviews.length" :reviews="reviews" />
    <div v-if="reviews.length"><div v-for="r in reviews" :key="r.id" style="padding:16px 0;border-bottom:1px solid #f5f5f5">
      <div style="display:flex;justify-content:space-between;margin-bottom:8px"><div style="display:flex;align-items:center;gap:8px"><span style="font-weight:600;font-size:14px">{{ r.username }}</span><span style="color:#f90;font-size:14px">{{ '★'.repeat(r.rating) }}</span></div><span style="color:#999;font-size:12px">{{ r.createdAt?.substring(0,10) }}</span></div>
      <p style="font-size:14px;color:#333;line-height:1.6">{{ r.content }}</p>
    </div></div>
    <div v-else style="text-align:center;padding:30px;color:#999">{{ t('product.noReviews') }}</div>
  </div>
  <div v-if="related.length">
    <h3 style="font-size:18px;font-weight:700;margin-bottom:16px">{{ t('product.relatedProducts') }}</h3>
    <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px">
      <div v-for="p in related" :key="p.id" @click="goDetail(p.id)" style="background:#fff;border-radius:8px;overflow:hidden;cursor:pointer;transition:transform .3s"
           @mouseenter="(e:any) => e.target.style.transform='translateY(-4px)'" @mouseleave="(e:any) => e.target.style.transform=''" :style="{boxShadow:'0 2px 8px rgba(0,0,0,.06)'}">
        <div style="height:140px;background:linear-gradient(135deg,#f8f8f8,#eee);display:flex;align-items:center;justify-content:center;font-size:36px">📦</div>
        <div style="padding:8px 12px"><h5 style="font-size:13px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;margin-bottom:4px">{{ p.name }}</h5><span style="color:#f10215;font-size:16px;font-weight:700">¥{{ (p.price/100).toFixed(2) }}</span></div>
      </div>
    </div>
    <div v-if="showPriceAlert" style="position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,.4);display:flex;align-items:center;justify-content:center;z-index:300">
      <div style="background:#fff;border-radius:12px;padding:24px;width:360px;max-width:90vw">
        <h3 style="font-size:18px;font-weight:600;margin-bottom:16px">🔔 设置降价提醒</h3>
        <p style="color:#666;font-size:13px;margin-bottom:12px">当商品价格低于目标价时，将通知您</p>
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:16px">
          <span style="font-size:13px;color:#666">目标价 ¥</span>
          <input v-model="alertPrice" type="number" step="0.01" style="flex:1;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:14px" />
        </div>
        <div style="display:flex;gap:8px;justify-content:flex-end">
          <button @click="showPriceAlert=false" style="padding:8px 16px;border:1px solid #ddd;background:#fff;border-radius:6px;cursor:pointer;font-size:13px">取消</button>
          <button @click="confirmPriceAlert" style="padding:8px 16px;background:#f10215;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:13px;font-weight:600">确认设置</button>
        </div>
      </div>
    <div class="mobile-sticky-bar" style="display:none;position:fixed;bottom:0;left:0;right:0;background:#fff;border-top:1px solid #eee;padding:10px 16px;z-index:200;align-items:center;gap:12px;box-shadow:0 -2px 8px rgba(0,0,0,.06)">
      <div style="flex:1"><span style="color:#f10215;font-size:20px;font-weight:700">¥{{ (displayPrice()/100).toFixed(2) }}</span></div>
      <button @click="onAddToCart" style="flex:1;height:40px;background:#fff;border:2px solid #f10215;color:#f10215;border-radius:20px;font-size:14px;font-weight:600">加入购物车</button>
      <button @click="buyNow" style="flex:1;height:40px;background:#f10215;color:#fff;border:none;border-radius:20px;font-size:14px;font-weight:600">立即购买</button>
    </div>
  </div>
</template>
<style scoped>
@media (max-width: 768px) { .mobile-sticky-bar { display: flex !important; } }
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
@keyframes cartBounce { 0%,100% { transform: scale(1); } 50% { transform: scale(1.05); } }
.cart-bounce { animation: cartBounce .3s ease; }</style>