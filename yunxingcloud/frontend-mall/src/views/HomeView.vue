<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHomeData, getRecommend } from '@/api/product'
import { useAuthStore } from '@/stores/auth'
import request from '@/api/request'
import { useI18n } from '@/locales'
import { useToast } from '@/composables/useToast'
import { usePullRefresh } from '@/composables/usePullRefresh'
import { useRecentlyViewed } from '@/composables/useRecentlyViewed'
import { useAbTest } from '@/composables/useAbTest'
import { getExperimentById } from '@/config/abTests'
import { formatPrice, getProductImage } from '@/utils/format'
import type { Product, Banner, Category, Brand, Campaign, FlashSaleItem } from '@/types'
import CountdownTimer from '@/components/CountdownTimer.vue'
import LazyImage from '@/components/LazyImage.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import JdButton from '@/components/JdButton.vue'
import HomeBanner from '@/components/HomeBanner.vue'
import CategoryNav from '@/components/CategoryNav.vue'
import ProductSection from '@/components/ProductSection.vue'

const router = useRouter()
const auth = useAuthStore()
const { t } = useI18n()
const toast = useToast()
const { items: recentItems } = useRecentlyViewed()
const ab = useAbTest()
const homeLayout = computed(() => {
  const exp = getExperimentById('home-layout')
  return exp ? (ab.getConfig(exp).columns as number) || 4 : 4
})

const banners = ref<Banner[]>([])
const hotProducts = ref<Product[]>([])
const newProducts = ref<Product[]>([])
const categories = ref<Category[]>([])
const recommended = ref<Product[]>([])
const activeTab = ref<'hot' | 'new'>('hot')
const loading = ref(true)
const loadError = ref(false)
const campaign = ref<Campaign | null>(null)

// Featured brands
const featuredBrands = ref<Brand[]>([])
async function loadBrands() {
  try { const r = await request.get('/brands'); featuredBrands.value = (r.data || []).slice(0, 6) } catch { console.warn('[Home] Load brands failed') }
}

// Daily check-in
const checkedInToday = ref(false)
const checkingIn = ref(false)
async function loadCheckinStatus() {
  try { if (!auth.isLoggedIn) return; const r = await request.get('/points/checkin/status'); checkedInToday.value = r.data?.checked || false } catch { console.warn("[Home] Load checkin status failed") }
}
async function doCheckin() {
  if (checkingIn.value || checkedInToday.value) return
  checkingIn.value = true
  try { await request.post('/points/checkin'); checkedInToday.value = true; toast.success(t('points.checkinSuccess') || '签到成功！+5 积分') } catch (e: any) { toast.error(e?.response?.data?.message || t('points.checkinFail')) }
  finally { checkingIn.value = false }
}

async function loadCampaign() {
  try { const r = await request.get('/campaigns'); const list = r.data || []; campaign.value = list.length > 0 ? list[0] : null } catch { console.warn('[Home] loadCampaign failed') }
}

// Hot search tags
const hotKeywords = ref<string[]>([])
async function loadHotKeywords() {
  try { const r = await request.get('/search/hot-keywords'); hotKeywords.value = (r.data || []).slice(0, 8) } catch {}
}
function searchTag(kw: string) { router.push('/search?q=' + encodeURIComponent(kw)) }

const flashSales = ref<FlashSaleItem[]>([])
const flashEnd = computed(() => {
  const active = flashSales.value.find((s) => new Date(s.endTime).getTime() > Date.now())
  return active ? new Date(active.endTime).getTime() : 0
})
const hasActiveFlash = computed(() => flashSales.value.some((s) => {
  return new Date(s.startTime).getTime() <= Date.now() && new Date(s.endTime).getTime() > Date.now()
}))
const flashPriceMap = computed(() => {
  const map: Record<number, number> = {}
  flashSales.value.forEach((s) => { if (s.productId) map[s.productId] = s.flashPrice })
  return map
})
const flashProducts = computed(() => {
  return hotProducts.value.filter(p => flashPriceMap.value[p.id] != null).slice(0, 4)
})

async function loadData() {
  loading.value = true; loadError.value = false
  try {
    const home = await getHomeData()
    const d = home.data
    banners.value = d.banners || []
    hotProducts.value = d.hotProducts || []
    newProducts.value = d.newProducts || []
    categories.value = d.categories || []
    recommended.value = d.recommended || []
    // 并行加载推荐和秒杀
    await Promise.allSettled([
      getRecommend().then(r => { const recs = r.data; if (recs && recs.length) recommended.value = recs }).catch(() => {}),
      request.get('/flash-sale').then(r => { flashSales.value = r.data || [] }).catch(() => {}),
    ])
    // 个性化推荐覆盖（需优先于通用推荐）
    try { if (auth.isLoggedIn) { const pr = await request.get('/personalized/home'); const precs = pr.data?.guessYouLike; if (precs && precs.length) recommended.value = precs } } catch { /* 个性化推荐非核心 */ }
  } catch (e) { console.warn('[Home] loadData failed:', e); loadError.value = true; toast.error(t('toast.homeLoadFail') || '首页加载失败') }
  loading.value = false
}

const { pulling, refreshing, pullDistance } = usePullRefresh(loadData)
const tabProducts = computed(() => activeTab.value === 'hot' ? hotProducts.value : newProducts.value)

onMounted(() => {
  loadData(); loadCampaign(); loadBrands(); loadCheckinStatus(); loadHotKeywords()
  setTimeout(() => {
    const exp = getExperimentById('home-layout')
    if (exp) ab.trackExposure(exp.id)
  }, 1000)
})

function goDetail(id: number) { router.push(`/product/${id}`) }
function goProducts(query: Record<string, string>) { router.push({ path: '/products', query }) }
</script>

<template>
  <div>
    <div v-if="campaign" class="campaign-bar" role="button" tabindex="0" @click="router.push('/products')" @keydown.enter.prevent="router.push('/products')" @keydown.space.prevent="router.push('/products')">
      🎉 {{ campaign.name }} — {{ campaign.description || t('home.campaignDefault') }}
    </div>
    <div class="coupon-entry-bar" role="button" tabindex="0" @click="router.push('/coupons')" @keydown.enter.prevent="router.push('/coupons')" @keydown.space.prevent="router.push('/coupons')">
      🎫 {{ t('coupon.center') }} — {{ t('coupon.goClaim') }}
    </div>
    <div v-if="pulling" class="pull-indicator" :style="{ height: pullDistance + 'px' }">
      <span>{{ refreshing ? t('home.refreshing') : t('home.pullRefresh') }}</span>
    </div>

    <div v-if="loadError" class="error-state">
      <JdEmpty icon="🔌" :title="t('home.loadError')" :description="t('home.retryHint')">
        <JdButton @click="loadData">{{ t('home.retryBtn') }}</JdButton>
      </JdEmpty>
    </div>

    <div v-if="loading && !loadError" class="home-skeleton">
      <SkeletonBox variant="banner" height="280px" :count="1" class="mb-24" />
      <SkeletonBox variant="text" :count="1" height="24px" width="120px" />
      <div class="sk-cat-row"><SkeletonBox v-for="i in 8" :key="i" variant="card" width="60px" height="72px" :count="1" /></div>
      <SkeletonBox variant="text" :count="1" height="24px" width="160px" />
      <SkeletonBox variant="card" :columns="4" :count="4" height="340px" />
      <SkeletonBox variant="text" :count="1" height="24px" width="180px" />
      <SkeletonBox variant="card" :columns="4" :count="4" height="340px" />
    </div>

    <template v-if="!loading && !loadError">
      <HomeBanner :banners="banners" />

      <CategoryNav :categories="categories"
        @select="(id: number) => goProducts({ categoryId: String(id) })"
        @view-all="goProducts({})" />

      <!-- Hot Search Tags -->
      <div v-if="hotKeywords.length" class="hot-tags-row">
        <span class="hot-tags-label">🔥 {{ t('search.hotKeywords') }}</span>
        <span v-for="(kw, i) in hotKeywords" :key="i" class="hot-tag" :class="{ 'tag-hot': i < 3 }" @click="searchTag(kw)">{{ kw }}</span>
      </div>

      <!-- Check-in + Brands Row -->
      <div class="brands-checkin-row">
        <!-- Check-in Button -->
        <div class="checkin-card" role="button" tabindex="0" @click="doCheckin" @keydown.enter.prevent="doCheckin" @keydown.space.prevent="doCheckin">
          <div class="checkin-icon">{{ checkedInToday ? '✅' : '📅' }}</div>
          <div class="checkin-text">{{ checkedInToday ? t('points.checkinDone') : t('points.checkinReward') }}</div>
          <div class="checkin-desc">{{ checkedInToday ? t('points.checkinTomorrow') : '+5 ' + t('common.score') }}</div>
        </div>
        <!-- Featured Brands -->
        <div v-for="b in featuredBrands" :key="b.id" class="brand-mini-card" role="button" tabindex="0" @click="router.push('/brand/' + b.id)" @keydown.enter.prevent="router.push('/brand/' + b.id)" @keydown.space.prevent="router.push('/brand/' + b.id)">
          <LazyImage :src="b.logo || ''" :alt="b.name" height="48px" width="48px" rounded="50%" bg="#f5f5f5" />
          <span class="brand-mini-name">{{ b.name }}</span>
        </div>
      </div>

      <!-- Flash Sale -->
      <section v-if="hasActiveFlash && flashProducts.length >= 2" class="flash-section">
        <div class="section-header">
          <div class="flex items-center gap-md">
            <span class="flash-title">⏰ {{ t('product.flashSale') }}</span>
            <CountdownTimer :end-time="flashEnd" label="" />
          </div>
          <span class="section-more" @click="router.push('/flash-sale')">{{ t('common.all') }} &gt;</span>
        </div>
        <div class="flash-grid">
          <div v-for="p in flashProducts" :key="'fs-' + p.id" class="flash-item" role="button" tabindex="0" @click="goDetail(p.id)" @keydown.enter.prevent="goDetail(p.id)" @keydown.space.prevent="goDetail(p.id)">
            <LazyImage :src="getProductImage(p)" :alt="p.name" height="140px" />
            <span class="discount-badge">{{ Math.round(Math.min(flashPriceMap[p.id], p.price) / p.price * 100) }}%OFF</span>
            <div class="flash-item-body">
              <h5 class="item-name">{{ p.name }}</h5>
              <div class="flex-center gap-sm">
                <span class="flash-price">{{ formatPrice(flashPriceMap[p.id] / 100, 2) }}</span>
                <span class="original-price">{{ formatPrice(p.price / 100, 2) }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Tab Products -->
      <div class="section-header">
        <div class="tab-group">
          <span class="tab-item" :class="{ active: activeTab === 'hot' }" role="tab" :aria-selected="activeTab === 'hot'" tabindex="0" @click="activeTab = 'hot'" @keydown.enter.prevent="activeTab = 'hot'" @keydown.space.prevent="activeTab = 'hot'">{{ t('product.hotRecommend') }}</span>
          <span class="tab-item" :class="{ active: activeTab === 'new' }" role="tab" :aria-selected="activeTab === 'new'" tabindex="0" @click="activeTab = 'new'" @keydown.enter.prevent="activeTab = 'new'" @keydown.space.prevent="activeTab = 'new'">{{ t('product.newArrival') }}</span>
        </div>
        <span class="section-more" @click="goProducts({})">{{ t('common.allProducts') }} &gt;</span>
      </div>
      <Transition name="tab-fade" mode="out-in">
        <ProductSection :key="activeTab" title="" :products="tabProducts" :columns="homeLayout" @product-click="goDetail" />
      </Transition>

      <!-- Recently Viewed -->
      <section v-if="recentItems.length" class="section">
        <div class="section-header">
          <span class="section-title">🕐 {{ t('recent.title') }}</span>
          <span class="section-more" @click="router.push('/recent')">{{ t('product.viewAll') }}</span>
        </div>
        <div class="product-grid recent-grid">
          <div v-for="p in recentItems.slice(0, 4)" :key="'rv-' + p.id" class="product-card-recent" role="button" tabindex="0" @click="goDetail(p.id)" @keydown.enter.prevent="goDetail(p.id)" @keydown.space.prevent="goDetail(p.id)">
            <LazyImage :src="getProductImage(p)" :alt="p.name" height="160px" bg="linear-gradient(135deg,#f0f0ff,#e8e8ff)" />
            <div class="product-info">
              <h4 class="product-name">{{ p.name }}</h4>
              <span class="product-price">{{ formatPrice(p.price / 100, 2) }}</span>
              <div class="viewed-time">{{ new Date(p.viewedAt).toLocaleString('zh-CN', { month:'short', day:'numeric', hour:'2-digit', minute:'2-digit' }) }}</div>
            </div>
          </div>
        </div>
      </section>

      <!-- Recommended -->
      <ProductSection :title="'🤖 ' + t('product.recommended')" :products="recommended" :columns="homeLayout" @product-click="goDetail" />

      <JdEmpty v-if="!tabProducts.length && !hotProducts.length && !newProducts.length"
        icon="🛍️" :title="t('common.noResults')" />
    </template>
  </div>
</template>

<style scoped>
.pull-indicator { display: flex; align-items: center; justify-content: center; overflow: hidden; transition: height .2s; font-size: 13px; color: var(--text-tertiary); background: linear-gradient(180deg, var(--jd-red-light), transparent); }
.pull-indicator span { animation: fadeIn .3s ease; }
.error-state { padding: 80px var(--space-xl); }

/* Flash Sale */
.flash-section {
  background: linear-gradient(135deg, var(--jd-red-light), var(--bg-white));
  border: 2px solid var(--jd-red); border-radius: var(--radius-lg);
  padding: var(--space-xl) var(--space-xxl); margin-bottom: var(--space-xxl);
}
.flash-title { font-size: var(--font-title); font-weight: 800; color: var(--jd-red); }
.flash-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: var(--space-md); }
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
.item-name { font-size: var(--font-base); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 6px; }

/* Section header & tabs */
.section-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg);
}
.section-title { font-size: var(--font-xl); font-weight: 700; color: var(--text-primary); }
.section-more { font-size: var(--font-base); color: var(--text-tertiary); cursor: pointer; }
.section-more:hover { color: var(--jd-red); }
.section { margin-top: var(--space-xxxl); }
.tab-group { display: flex; gap: var(--space-xxl); }
.tab-item {
  font-size: var(--font-xl); font-weight: 700; cursor: pointer;
  color: var(--text-tertiary); padding-bottom: var(--space-xs);
  border-bottom: 3px solid transparent; transition: all var(--transition-fast);
}
.tab-item.active { color: var(--jd-red); border-bottom-color: var(--jd-red); }

/* Recently viewed product grid */
.product-grid { display: grid; grid-template-columns: repeat(var(--grid-cols, 4), 1fr); gap: var(--space-lg); }
.recent-grid { --grid-cols: 4; }
.product-card-recent {
  background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden;
  cursor: pointer; box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition), transform var(--transition);
}
.product-card-recent:hover { box-shadow: var(--shadow-card-hover); transform: translateY(-4px); }
.product-info { padding: var(--space-md) var(--space-lg); }
.product-name {
  font-size: var(--font-md); color: var(--text-primary); margin-bottom: var(--space-sm);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.product-price { color: var(--jd-red); font-size: var(--font-xl); font-weight: 700; }
.viewed-time { font-size: var(--font-xs); color: var(--text-placeholder); margin-top: 2px; }

/* Check-in + Brands row */
.brands-checkin-row {
  display: flex; align-items: center; gap: var(--space-lg);
  background: var(--bg-white); border-radius: var(--radius-lg);
  padding: var(--space-lg) var(--space-xl); margin-bottom: var(--space-xxl);
  box-shadow: var(--shadow-sm); overflow-x: auto; -webkit-overflow-scrolling: touch;
}
.checkin-card {
  flex-shrink: 0; text-align: center; cursor: pointer;
  padding: var(--space-sm) var(--space-lg); border-radius: var(--radius-md);
  background: linear-gradient(135deg, #fff9e0, #fff3cd);
  border: 1px solid #ffc107; transition: transform var(--transition-fast);
  min-width: 100px;
}
.checkin-card:hover { transform: scale(1.05); }
.checkin-icon { font-size: 24px; margin-bottom: 2px; }
.checkin-text { font-size: var(--font-xs); font-weight: 600; color: var(--text-primary); }
.checkin-desc { font-size: 11px; color: var(--text-tertiary); margin-top: 2px; }
.brand-mini-card {
  flex-shrink: 0; text-align: center; cursor: pointer;
  padding: var(--space-sm); border-radius: var(--radius-md);
  transition: transform var(--transition-fast); min-width: 72px;
}
.brand-mini-card:hover { transform: translateY(-2px); }
.brand-mini-name { font-size: 11px; color: var(--text-secondary); display: block; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 72px; }

/* Hot search tags */
.hot-tags-row { display: flex; align-items: center; gap: var(--space-sm); flex-wrap: wrap; margin-bottom: var(--space-lg); padding: var(--space-sm) 0; }
.hot-tags-label { font-size: var(--font-sm); font-weight: 700; color: var(--orange); white-space: nowrap; }
.hot-tag { padding: 3px 14px; background: var(--bg-hover); border-radius: var(--radius-round); font-size: var(--font-sm); color: var(--text-secondary); cursor: pointer; transition: all var(--transition-fast); white-space: nowrap; }
.hot-tag:hover { background: var(--jd-red); color: #fff; }
.hot-tag.tag-hot { background: #fff3e0; color: var(--orange); font-weight: 600; }
.hot-tag.tag-hot:hover { background: var(--jd-red); color: #fff; }

.mb-24 { margin-bottom: 24px; }

@media (min-width: 769px) and (max-width: 1024px) {
  .product-grid, .recent-grid { --grid-cols: 3; gap: var(--space-lg); }
}
@media (max-width: 768px) {
  .product-grid, .recent-grid { --grid-cols: 2; gap: var(--space-sm); }
  .flash-grid { grid-template-columns: repeat(2, 1fr); }
  .product-price { font-size: var(--font-lg); }
}

.campaign-bar { background: linear-gradient(90deg, var(--jd-red), #ff6b6b); color: #fff; text-align: center; padding: 10px var(--space-md); font-size: var(--font-md); font-weight: 600; cursor: pointer; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.coupon-entry-bar { background: linear-gradient(90deg, #f90, #ffc107); color: #fff; text-align: center; min-height: 44px; display: flex; align-items: center; justify-content: center; padding: 0 var(--space-md); font-size: var(--font-sm); font-weight: 600; cursor: pointer; }

/* Tab transition */
.tab-fade-enter-active { transition: opacity .2s ease, transform .2s ease; }
.tab-fade-leave-active { transition: opacity .15s ease, transform .15s ease; }
.tab-fade-enter-from { opacity: 0; transform: translateY(8px); }
.tab-fade-leave-to { opacity: 0; transform: translateY(-4px); }
.home-skeleton { padding: var(--space-lg) 0; }
.sk-cat-row { display: flex; gap: var(--space-md); margin: var(--space-lg) 0; overflow: hidden; }
.mb-24 { margin-bottom: var(--space-xxl); }
</style>
