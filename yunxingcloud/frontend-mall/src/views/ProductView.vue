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
const displayPrice = () => selectedSku.value ? selectedSku.value.price : product.value?.price || 0
const displayStock = () => selectedSku.value ? selectedSku.value.stock : product.value?.stock || 0
function goDetail(id: number) { router.push(`/product/${id}`) }

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
      <div style="width:100%;height:420px;background:linear-gradient(135deg,#f8f8f8,#eee);border-radius:8px;display:flex;align-items:center;justify-content:center;font-size:100px;margin-bottom:12px;overflow:hidden">
        <img v-if="images[activeImage] && images[activeImage] !== '📦'" :src="images[activeImage]" style="width:100%;height:100%;object-fit:cover" :alt="product.name" />
        <span v-else style="font-size:100px">📦</span>
      </div>
      <div v-if="images.length > 1" style="display:flex;gap:8px">
        <div v-for="(img, i) in images" :key="i" @click="activeImage = i"
             style="width:60px;height:60px;border-radius:4px;cursor:pointer;display:flex;align-items:center;justify-content:center;font-size:24px;overflow:hidden;transition:border .2s"
             :style="{border: activeImage===i ? '2px solid #e4393c' : '1px solid #ddd'}">
          <img v-if="img !== '📦'" :src="img" style="width:100%;height:100%;object-fit:cover" />
          <span v-else>📦</span>
        </div>
      </div>
    </div>
    <div style="flex:1">
      <h1 style="font-size:22px;margin-bottom:8px">{{ product.name }}</h1>
      <p style="color:#e4393c;font-size:13px;margin-bottom:8px">{{ product.description || '' }}</p>
      <div style="margin-bottom:12px;display:flex;align-items:center;gap:8px">
        <ProductRating v-if="product.rating" :rating="product.rating || 0" :count="reviews.length" />
        <span v-if="!product.rating" style="color:#aaa;font-size:12px">暂无评分</span>
      </div>
      <div style="background:linear-gradient(135deg,#fff5f5,#fff0f0);padding:20px;border-radius:8px;margin-bottom:16px">
        <div style="display:flex;align-items:baseline;gap:12px">
          <span style="color:#999;font-size:13px">{{ t('product.price') }}</span>
          <span style="color:#e4393c;font-size:32px;font-weight:700">¥{{ (displayPrice() / 100).toFixed(2) }}</span>
        </div>
        <div style="display:flex;gap:24px;margin-top:12px;font-size:13px;color:#666">
          <span>{{ t('product.salesCount') }} <b style="color:#e4393c">{{ product.sales || 0 }}</b></span>
          <span>{{ t('product.stock') }} <b>{{ displayStock() }}</b></span>
        </div>
      </div>
      <div v-if="skus.length" style="margin-bottom:16px">
        <div style="font-size:13px;color:#666;margin-bottom:8px">{{ t('product.skuSelector') }}</div>
        <div style="display:flex;flex-wrap:wrap;gap:8px">
          <span v-for="sku in skus" :key="sku.id" @click="selectedSku = sku" style="padding:6px 14px;border-radius:4px;cursor:pointer;font-size:13px;transition:all .2s"
                :style="{border:selectedSku?.id===sku.id?'2px solid #e4393c':'1px solid #ddd',color:selectedSku?.id===sku.id?'#e4393c':'#333',background:selectedSku?.id===sku.id?'#fff0f0':'#fff'}">{{ sku.name }}</span>
        </div>
      </div>
      <div style="display:flex;align-items:center;gap:12px;margin-bottom:24px">
        <span style="font-size:13px;color:#666">{{ t('product.quantity') }}</span>
        <button @click="qty = Math.max(1, qty - 1)" style="width:32px;height:32px;border:1px solid #ddd;background:#fff;cursor:pointer;font-size:18px;border-radius:4px">-</button>
        <span style="width:44px;text-align:center;font-size:15px">{{ qty }}</span>
        <button @click="qty = Math.min(displayStock(), qty + 1)" style="width:32px;height:32px;border:1px solid #ddd;background:#fff;cursor:pointer;font-size:18px;border-radius:4px">+</button>
      </div>
      <div style="display:flex;gap:12px">
        <button @click="onAddToCart" style="flex:1;height:48px;background:#fff;border:2px solid #e4393c;color:#e4393c;border-radius:8px;font-size:16px;cursor:pointer;font-weight:600;transition:all .2s"
                @mouseenter="(e:any) => { e.target.style.background='#fff0f0' }" @mouseleave="(e:any) => { e.target.style.background='#fff' }" :class="{ 'cart-bounce': addAnim }">{{ t('product.addToCart') }}</button>
        <button @click="buyNow" style="flex:1;height:48px;background:#e4393c;color:#fff;border:none;border-radius:8px;font-size:16px;cursor:pointer;font-weight:600;transition:all .2s"
                @mouseenter="(e:any) => { e.target.style.background='#c82930' }" @mouseleave="(e:any) => { e.target.style.background='#e4393c' }">{{ t('product.buyNow') }}</button>
        <button @click="toggleFavorite" style="width:48px;height:48px;border:1px solid #ddd;border-radius:8px;cursor:pointer;font-size:22px;background:#fff;transition:all .2s"
                :style="{color:favorited?'#e4393c':'#999'}">{{ favorited ? '❤️' : '🤍' }}</button>
        <button @click="shareProduct" style="width:48px;height:48px;border:1px solid #ddd;border-radius:8px;cursor:pointer;font-size:18px;background:#fff;transition:all .2s;color:#666"
                @mouseenter="(e:any) => { e.target.style.background='#f0f0f0' }" @mouseleave="(e:any) => { e.target.style.background='#fff' }">📤</button>
      </div>
    </div>
  </div>
  <div v-if="product" style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06);margin-bottom:24px">
    <h3 style="font-size:18px;font-weight:700;margin-bottom:16px">{{ t('product.reviews') }} ({{ reviews.length }})</h3>
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
        <div style="padding:8px 12px"><h5 style="font-size:13px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;margin-bottom:4px">{{ p.name }}</h5><span style="color:#e4393c;font-size:16px;font-weight:700">¥{{ (p.price/100).toFixed(2) }}</span></div>
      </div>
    </div>
  </div>
</template>
<style scoped>@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
@keyframes cartBounce { 0%,100% { transform: scale(1); } 50% { transform: scale(1.05); } }
.cart-bounce { animation: cartBounce .3s ease; }</style>