<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHomeData } from '@/api/product'
import { useI18n } from '@/locales'
import { usePullRefresh } from '@/composables/usePullRefresh'
import { useRecentlyViewed } from '@/composables/useRecentlyViewed'
import { formatPrice } from '@/utils/format'
import type { Product, Banner, Category } from '@/types'
import CountdownTimer from '@/components/CountdownTimer.vue'
import LazyImage from '@/components/LazyImage.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import JdButton from '@/components/JdButton.vue'
import HomeBanner from '@/components/HomeBanner.vue'
import CategoryNav from '@/components/CategoryNav.vue'
import ProductSection from '@/components/ProductSection.vue'

const router = useRouter()
const { t } = useI18n()
const { items: recentItems } = useRecentlyViewed()

const banners = ref<Banner[]>([])
const hotProducts = ref<Product[]>([])
const newProducts = ref<Product[]>([])
const categories = ref<Category[]>([])
const recommended = ref<Product[]>([])
const activeTab = ref<'hot' | 'new'>('hot')
const loading = ref(true)
const loadError = ref(false)

const flashEnd = ref(Date.now() + 6 * 3600 * 1000)
const flashPrice = (price: number) => Math.floor(price * 0.7)

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
  } catch { loadError.value = true }
  loading.value = false
}

const { pulling, refreshing, pullDistance } = usePullRefresh(loadData)
const tabProducts = computed(() => activeTab.value === 'hot' ? hotProducts.value : newProducts.value)

onMounted(() => { loadData() })

function goDetail(id: number) { router.push(`/product/${id}`) }
function goProducts(query: Record<string, string>) { router.push({ path: '/products', query }) }
function productImage(p: Product): string {
  return p.imageUrl || (p.images?.length ? p.images[0] : '')
}
</script>

<template>
  <div>
    <div v-if="pulling" class="pull-indicator" :style="{ height: pullDistance + 'px' }">
      <span>{{ refreshing ? '⏳ 刷新中...' : '↓ 下拉刷新' }}</span>
    </div>

    <div v-if="loadError" class="error-state">
      <JdEmpty icon="🔌" title="加载失败" description="请检查网络后重试">
        <JdButton @click="loadData">重新加载</JdButton>
      </JdEmpty>
    </div>

    <SkeletonBox v-if="loading && !loadError" variant="card" :columns="4" :count="4" height="360px" class="mb-24" />

    <template v-if="!loading && !loadError">
      <HomeBanner :banners="banners" />

      <CategoryNav :categories="categories"
        @select="(id: number) => goProducts({ categoryId: String(id) })"
        @view-all="goProducts({})" />

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
          <span class="tab-item" :class="{ active: activeTab === 'hot' }" @click="activeTab = 'hot'">{{ t('product.hotRecommend') }}</span>
          <span class="tab-item" :class="{ active: activeTab === 'new' }" @click="activeTab = 'new'">{{ t('product.newArrival') }}</span>
        </div>
        <span class="section-more" @click="goProducts({})">{{ t('common.allProducts') }} &gt;</span>
      </div>
      <ProductSection title="" :products="tabProducts" @product-click="goDetail" />

      <!-- Recently Viewed -->
      <section v-if="recentItems.length" class="section">
        <div class="section-header">
          <span class="section-title">🕐 最近浏览</span>
          <span class="section-more" @click="router.push('/recent')">查看全部 &gt;</span>
        </div>
        <div class="product-grid">
          <div v-for="p in recentItems.slice(0, 4)" :key="'rv-' + p.id" class="product-card-recent" @click="goDetail(p.id)">
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
      <ProductSection title="🤖 为你推荐" :products="recommended" @product-click="goDetail" />

      <JdEmpty v-if="activeTab==='hot' && !hotProducts.length && !newProducts.length"
        icon="🛍️" :title="t('common.noResults')" />
    </template>
  </div>
</template>

<style scoped>
.pull-indicator { text-align: center; overflow: hidden; transition: height .2s; font-size: 13px; color: var(--text-tertiary); }
.error-state { padding: 80px var(--space-xl); }

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
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-lg); }
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

.mb-24 { margin-bottom: 24px; }

@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-sm); }
  .flash-grid { grid-template-columns: repeat(2, 1fr); }
  .product-price { font-size: var(--font-lg); }
}
</style>
