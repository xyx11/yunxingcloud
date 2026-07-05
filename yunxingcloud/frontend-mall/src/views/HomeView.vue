<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHomeData } from '@/api/product'
import { addToCart } from '@/api/cart'
import { useCartFly } from '@/composables/useCartFly'
import { usePullRefresh } from '@/composables/usePullRefresh'
import { useRecentlyViewed } from '@/composables/useRecentlyViewed'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import { formatPrice, formatCount } from '@/utils/format'
import CountdownTimer from '@/components/CountdownTimer.vue'
import LazyImage from '@/components/LazyImage.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import JdButton from '@/components/JdButton.vue'

const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const { flyToCart } = useCartFly()
const { items: recentItems } = useRecentlyViewed()

const banners = ref<any[]>([])
const hotProducts = ref<any[]>([])
const newProducts = ref<any[]>([])
const categories = ref<any[]>([])
const recommended = ref<any[]>([])
const activeTab = ref<'hot' | 'new' | 'flash'>('hot')
const currentBanner = ref(0)
const loading = ref(true)
const loadError = ref(false)
let bannerTimer: ReturnType<typeof setInterval> | null = null

const flashEnd = ref(Date.now() + 6 * 3600 * 1000)
const flashPrice = (price: number) => Math.floor(price * 0.7)

async function loadData() {
  loading.value = true
  loadError.value = false
  try {
    const home = await getHomeData()
    const d = home.data
    banners.value = d.banners || []
    hotProducts.value = d.hotProducts || []
    newProducts.value = d.newProducts || []
    categories.value = d.categories || []
    recommended.value = d.recommended || []
  } catch { loadError.value = true }
  loading.value = false
}

const { pulling, refreshing, pullDistance } = usePullRefresh(loadData)

function startBanner() {
  bannerTimer = setInterval(() => {
    if (banners.value.length) currentBanner.value = (currentBanner.value + 1) % banners.value.length
  }, 4000)
}
function stopBanner() { if (bannerTimer) clearInterval(bannerTimer) }

let bannerTouchX = 0
function onBannerTouchStart(e: TouchEvent) { bannerTouchX = e.touches[0].clientX; stopBanner() }
function onBannerTouchEnd(e: TouchEvent) {
  const dx = e.changedTouches[0].clientX - bannerTouchX
  if (Math.abs(dx) > 40 && banners.value.length > 1) {
    currentBanner.value = dx > 0
      ? (currentBanner.value - 1 + banners.value.length) % banners.value.length
      : (currentBanner.value + 1) % banners.value.length
  }
  startBanner()
}

onMounted(async () => { await loadData(); startBanner() })
onUnmounted(() => { if (bannerTimer) clearInterval(bannerTimer) })

function goDetail(id: number) { router.push(`/product/${id}`) }
function goProducts(query: Record<string, string>) { router.push({ path: '/products', query }) }

async function quickAdd(e: Event, p: any) {
  e.stopPropagation()
  try {
    await addToCart(p.id, 1)
    toast.success('已加入购物车')
    flyToCart(e as MouseEvent)
  } catch { toast.error('添加失败') }
}

function productImage(p: any): string {
  return p.imageUrl || (p.images?.length ? p.images[0] : '')
}
</script>

<template>
  <div>
    <!-- Pull to refresh -->
    <div v-if="pulling" class="pull-indicator" :style="{ height: pullDistance + 'px' }">
      <span>{{ refreshing ? '⏳ 刷新中...' : '↓ 下拉刷新' }}</span>
    </div>

    <!-- Error -->
    <div v-if="loadError" class="error-state">
      <JdEmpty icon="🔌" title="加载失败" description="请检查网络后重试">
        <JdButton @click="loadData">重新加载</JdButton>
      </JdEmpty>
    </div>

    <!-- Skeleton -->
    <SkeletonBox v-if="loading && !loadError" variant="card" :columns="4" :count="4" height="360px" style="margin-bottom:24px" />

    <template v-if="!loading && !loadError">
      <!-- Banners -->
      <div v-if="banners.length" class="banner-wrapper"
           @mouseenter="stopBanner"
           @mouseleave="startBanner"
           @touchstart.passive="onBannerTouchStart"
           @touchend.passive="onBannerTouchEnd">
        <div v-for="(b, i) in banners" :key="b.id" class="banner-slide" :class="{ active: i === currentBanner }"
             :style="{ background: `linear-gradient(135deg, ${i % 2 ? '#d4000f' : '#f10215'}, ${i % 2 ? '#ff6b6b' : '#f90'})` }">
          <div class="banner-content">
            <h2 class="banner-title">{{ b.title }}</h2>
            <p class="banner-subtitle">{{ b.subtitle || t('product.hotRecommend') }}</p>
          </div>
        </div>
        <div class="banner-dots">
          <span v-for="(b, i) in banners" :key="b.id" class="banner-dot" :class="{ active: i === currentBanner }"
                @click="currentBanner = i" />
        </div>
      </div>

      <!-- Categories -->
      <section v-if="categories.length" class="categories-section">
        <div class="section-header">
          <span class="section-title">📂 商品分类</span>
          <span class="section-more" @click="goProducts({})">查看全部 &gt;</span>
        </div>
        <div class="categories">
          <div v-for="cat in categories.slice(0, 10)" :key="cat.id" class="cat-item" @click="goProducts({ categoryId: cat.id })">
            <div class="cat-icon">{{ cat.icon || '📁' }}</div>
            <div class="cat-name">{{ cat.name }}</div>
          </div>
        </div>
      </section>

      <!-- Flash Sale -->
      <section v-if="hotProducts.length >= 3" class="flash-section">
        <div class="section-header">
          <div class="flex items-center gap-md">
            <span class="flash-title">⏰ {{ t('product.flashSale') }}</span>
            <CountdownTimer :end-time="flashEnd" label="" />
          </div>
          <span class="section-more" @click="goProducts({})">{{ t('common.allProducts') }} &gt;</span>
        </div>
        <div class="flash-grid">
          <div v-for="p in hotProducts.slice(0, 4)" :key="'fs-' + p.id" class="flash-item" @click="goDetail(p.id)">
            <LazyImage :src="productImage(p)" :alt="p.name" height="140px" />
            <span class="discount-badge">7折</span>
            <div class="flash-item-body">
              <h5 class="item-name">{{ p.name }}</h5>
              <div class="flex-center gap-sm">
                <span class="flash-price">{{ formatPrice(flashPrice(p.price) / 100, 2) }}</span>
                <span class="original-price">¥{{ (p.price / 100).toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Tab Products -->
      <div class="section-header">
        <div class="tab-group">
          <span class="tab-item" :class="{ active: activeTab === 'hot' }" @click="activeTab = 'hot'">
            {{ t('product.hotRecommend') }}
          </span>
          <span class="tab-item" :class="{ active: activeTab === 'new' }" @click="activeTab = 'new'">
            {{ t('product.newArrival') }}
          </span>
        </div>
        <span class="section-more" @click="goProducts({})">{{ t('common.allProducts') }} &gt;</span>
      </div>

      <!-- Product Grid -->
      <div class="product-grid">
        <div v-for="p in (activeTab === 'hot' ? hotProducts : newProducts)" :key="p.id" class="product-card-home" @click="goDetail(p.id)">
          <div class="product-img-wrap">
            <LazyImage :src="productImage(p)" :alt="p.name" height="200px" />
            <span v-if="activeTab === 'new' || p.isNew" class="tag-new">新品</span>
            <span v-else class="tag-hot">热卖</span>
          </div>
          <div class="product-info">
            <h4 class="product-name">{{ p.name }}</h4>
            <div class="product-bottom">
              <div>
                <span class="product-price">{{ formatPrice(p.price / 100, 2) }}</span>
                <span v-if="p.sales" class="product-sales">🔥 {{ formatCount(p.sales) }}人已购</span>
              </div>
              <button class="quick-add-btn" @click="(e: Event) => quickAdd(e, p)">+</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty -->
      <JdEmpty v-if="activeTab==='hot' && !hotProducts.length && !newProducts.length"
               icon="🛍️" :title="t('common.noResults')" />

      <!-- Recently Viewed -->
      <section v-if="recentItems.length" class="section">
        <div class="section-header">
          <span class="section-title">🕐 最近浏览</span>
          <span class="section-more" @click="router.push('/recent')">查看全部 &gt;</span>
        </div>
        <div class="product-grid">
          <div v-for="p in recentItems.slice(0, 4)" :key="'rv-' + p.id" class="product-card-home" @click="goDetail(p.id)">
            <LazyImage :src="productImage(p)" :alt="p.name" height="160px" bg="linear-gradient(135deg,#f0f0ff,#e8e8ff)" />
            <div class="product-info">
              <h4 class="product-name">{{ p.name }}</h4>
              <span class="product-price">{{ formatPrice(p.price / 100, 2) }}</span>
              <div class="viewed-time">{{ new Date(p.viewedAt).toLocaleString('zh-CN', { month:'short', day:'numeric', hour:'2-digit', minute:'2-digit' }) }}</div>
            </div>
          </div>
        </div>
      </section>

      <!-- Recommended -->
      <section v-if="recommended.length" class="section">
        <div class="section-header">
          <span class="section-title">🤖 为你推荐</span>
        </div>
        <div class="product-grid">
          <div v-for="p in recommended.slice(0, 8)" :key="'rec-' + p.id" class="product-card-home" @click="goDetail(p.id)">
            <LazyImage :src="productImage(p)" :alt="p.name" height="200px" bg="linear-gradient(135deg,#f8f0ff,#e8e0ff)" />
            <div class="product-info">
              <h4 class="product-name">{{ p.name }}</h4>
              <div class="product-bottom">
                <span class="product-price">{{ formatPrice(p.price / 100, 2) }}</span>
                <button class="quick-add-btn" @click="(e: Event) => quickAdd(e, p)">+</button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
/* Pull */
.pull-indicator { text-align: center; overflow: hidden; transition: height .2s; font-size: 13px; color: var(--text-tertiary); }

/* Error */
.error-state { padding: 80px var(--space-xl); }

/* Banners */
.banner-wrapper { position: relative; border-radius: var(--radius-lg); overflow: hidden; margin-bottom: var(--space-xxl); height: 360px; }
.banner-slide {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity .6s;
}
.banner-slide.active { opacity: 1; }
.banner-content { text-align: center; color: #fff; }
.banner-title { font-size: 42px; margin-bottom: var(--space-lg); }
.banner-subtitle { font-size: var(--font-xl); opacity: .9; }
.banner-dots {
  position: absolute; bottom: var(--space-lg); left: 50%; transform: translateX(-50%);
  display: flex; gap: var(--space-sm);
}
.banner-dot {
  width: 10px; height: 10px; border-radius: 50%; cursor: pointer;
  background: rgba(255,255,255,.5); transition: all .3s;
}
.banner-dot.active { background: #fff; transform: scale(1.3); }

/* Categories */
.categories-section {
  background: var(--bg-white); border-radius: var(--radius-lg);
  padding: var(--space-xl); margin-bottom: var(--space-xxl);
  box-shadow: var(--shadow-sm);
}
.categories {
  display: flex; gap: var(--space-lg); justify-content: center;
  overflow-x: auto; padding: var(--space-xs) 0; -webkit-overflow-scrolling: touch;
}
.categories::-webkit-scrollbar { display: none; }
.cat-item { text-align: center; cursor: pointer; flex-shrink: 0; width: 72px; transition: transform var(--transition); }
.cat-item:hover { transform: scale(1.08); }
.cat-icon {
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, var(--jd-red-light), #ffe0e0);
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; margin: 0 auto 6px;
  box-shadow: 0 2px 6px rgba(241,2,21,.08);
}
.cat-name { font-size: var(--font-xs); color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* Flash Sale */
.flash-section {
  background: linear-gradient(135deg, var(--jd-red-light), var(--bg-white));
  border: 2px solid var(--jd-red); border-radius: var(--radius-lg);
  padding: var(--space-xl) var(--space-xxl); margin-bottom: var(--space-xxl);
}
.flash-title { font-size: var(--font-title); font-weight: 800; color: var(--jd-red); }
.flash-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-md); }
.flash-item {
  background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden;
  cursor: pointer; text-align: center; transition: transform var(--transition); position: relative;
}
.flash-item:hover { transform: translateY(-4px); }
.flash-item-body { padding: var(--space-sm) var(--space-md) var(--space-md); }
.discount-badge {
  position: absolute; top: 6px; right: 6px; background: var(--jd-red); color: #fff;
  font-size: 10px; padding: 2px 8px; border-radius: var(--radius-round); z-index: 1; font-weight: 700;
}
.flash-price { color: var(--jd-red); font-size: 18px; font-weight: 700; }
.original-price { color: var(--text-tertiary); font-size: var(--font-xs); text-decoration: line-through; }

/* Section */
.section { margin-top: var(--space-xxxl); }
.section-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg);
}
.section-title { font-size: var(--font-xl); font-weight: 700; color: var(--text-primary); }
.section-more { font-size: var(--font-base); color: var(--text-tertiary); cursor: pointer; }
.section-more:hover { color: var(--jd-red); }

/* Tabs */
.tab-group { display: flex; gap: var(--space-xxl); }
.tab-item {
  font-size: var(--font-xl); font-weight: 700; cursor: pointer;
  color: var(--text-tertiary); padding-bottom: var(--space-xs);
  border-bottom: 3px solid transparent; transition: all var(--transition-fast);
}
.tab-item.active { color: var(--jd-red); border-bottom-color: var(--jd-red); }

/* Product Grid */
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-lg); }

.product-card-home {
  background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden;
  cursor: pointer; box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition), transform var(--transition);
}
.product-card-home:hover { box-shadow: var(--shadow-card-hover); transform: translateY(-4px); }

.product-img-wrap { position: relative; }
.tag-new {
  position: absolute; top: 6px; left: 6px; background: var(--green); color: #fff;
  font-size: 10px; padding: 1px 6px; border-radius: var(--radius-sm); z-index: 1; font-weight: 700;
}
.tag-hot {
  position: absolute; top: 6px; left: 6px; background: var(--jd-red); color: #fff;
  font-size: 10px; padding: 1px 6px; border-radius: var(--radius-sm); z-index: 1; font-weight: 700;
}

.product-info { padding: var(--space-md) var(--space-lg); }
.product-name {
  font-size: var(--font-md); color: var(--text-primary); margin-bottom: var(--space-sm);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.product-bottom { display: flex; align-items: center; justify-content: space-between; }
.product-price { color: var(--jd-red); font-size: var(--font-xl); font-weight: 700; }
.product-sales { color: var(--text-tertiary); font-size: var(--font-xs); margin-left: var(--space-xs); }
.viewed-time { font-size: var(--font-xs); color: var(--text-placeholder); margin-top: 2px; }

.quick-add-btn {
  width: 30px; height: 30px; border-radius: 50%; border: 2px solid var(--jd-red);
  background: var(--bg-white); color: var(--jd-red); cursor: pointer;
  font-size: var(--font-lg); display: flex; align-items: center; justify-content: center;
  transition: all var(--transition-fast); flex-shrink: 0; font-weight: 700; line-height: 1;
}
.quick-add-btn:hover { background: var(--jd-red); color: #fff; }

.item-name {
  font-size: var(--font-base); overflow: hidden; text-overflow: ellipsis;
  white-space: nowrap; margin-bottom: 6px;
}

@media (max-width: 768px) {
  .banner-wrapper { height: 200px; border-radius: var(--radius-md); }
  .banner-title { font-size: var(--font-xxl); }
  .banner-subtitle { font-size: var(--font-md); }
  .product-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-sm); }
  .flash-grid { grid-template-columns: repeat(2, 1fr); }
  .product-price { font-size: var(--font-lg); }
}
</style>
