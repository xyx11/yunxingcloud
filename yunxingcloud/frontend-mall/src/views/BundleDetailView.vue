<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getBundleById } from '@/api/bundle'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import { formatPrice } from '@/utils/format'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const bundle = ref<any>(null)
const loading = ref(true)
const error = ref(false)
const adding = ref(false)
const id = Number(route.params.id)

async function load() {
  loading.value = true
  error.value = false
  try {
    const r = await getBundleById(id)
    bundle.value = r.data || null
    loading.value = false;
    } catch {
    error.value = true
    toast.error(t('bundleDetail.loadFail'))
  }
}

function savings() {
  if (!bundle.value) return 0
  return (bundle.value.originalPrice || 0) - (bundle.value.bundlePrice || bundle.value.price || 0)
}

async function addBundleToCart() {
  adding.value = true
  try {
    if (bundle.value.products && bundle.value.products.length) {
      for (const p of bundle.value.products) {
        await request.post('/cart', { productId: p.id, quantity: p.quantity || 1 })
      }
    }
    toast.success(t('bundleDetail.addedToCart'))
  } catch {
    toast.error(t('bundleDetail.addToCartFail'))
  } finally {
    adding.value = false
  }
}

function goProduct(id: number) {
  router.push(`/product/${id}`)
}

onMounted(load)
</script>

<template>
  <div class="bd-page">
    <div class="bd-back">
      <button class="back-btn" @click="router.back()">{{ t('bundleDetail.back') }}</button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="bd-skel">
      <SkeletonBox variant="banner" height="360px" />
      <SkeletonBox variant="text" :count="3" />
    </div>

    <!-- Error -->
    <JdEmpty v-else-if="error" icon="⚠️" :title="t('bundleDetail.loadFail')" :description="t('bundleDetail.loadFail')">
      <JdButton @click="load">{{ t('common.retry') }}</JdButton>
    </JdEmpty>

    <!-- Not found -->
    <JdEmpty v-else-if="!bundle" icon="🎁" :title="t('bundleDetail.notFound')" :description="t('bundleDetail.notFoundDesc')" />

    <!-- Content -->
    <template v-else>
      <div class="bd-main">
        <div class="bd-image">
          <LazyImage :src="bundle.imageUrl || ''" :alt="bundle.name" height="360px" />
        </div>

        <div class="bd-info">
          <h1 class="bd-name">{{ bundle.name }}</h1>
          <p class="bd-desc" v-if="bundle.description">{{ bundle.description }}</p>

          <div class="bd-price-card">
            <div class="bd-price-row">
              <span class="bd-price-label">{{ t('bundleDetail.bundlePrice') }}</span>
              <span class="bd-price-value">{{ formatPrice((bundle.bundlePrice || bundle.price || 0) / 100, 2) }}</span>
            </div>
            <div class="bd-price-row" v-if="bundle.originalPrice">
              <span class="bd-price-label">{{ t('bundleDetail.originalPrice') }}</span>
              <span class="bd-price-original">{{ formatPrice(bundle.originalPrice / 100) }}</span>
            </div>
            <div class="bd-savings" v-if="savings() > 0">
              {{ t('bundleDetail.save') }} ¥{{ formatPrice(savings() / 100) }}
            </div>
          </div>

          <JdButton size="lg" class="bd-btn" :loading="adding" @click="addBundleToCart">
            {{ t('bundleDetail.buyNow') }} {{ formatPrice((bundle.bundlePrice || bundle.price || 0) / 100) }}
          </JdButton>
        </div>
      </div>

      <!-- Bundle products -->
      <div class="bd-products-section" v-if="bundle.products && bundle.products.length">
        <h2 class="bd-section-title">{{ t('bundleDetail.products') }} ({{ bundle.products.length }}{{ t('bundleDetail.products').includes('items') ? '' : '件' }})</h2>
        <div class="bd-products-grid">
          <div
            v-for="(p, i) in bundle.products"
            :key="p.id"
            class="bd-product-card"
            role="button"
            tabindex="0"
            @click="goProduct(p.id)"
            @keydown.enter.prevent="goProduct(p.id)"
            @keydown.space.prevent="goProduct(p.id)"
          >
            <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="160px" />
            <div class="bd-product-info">
              <span class="bd-product-num">{{ i + 1 }}</span>
              <h4 class="bd-product-name">{{ p.name }}</h4>
              <div class="bd-product-price">
                <span class="bd-product-current">{{ formatPrice((p.price || 0) / 100) }}</span>
                <span v-if="p.originalPrice && p.originalPrice > p.price" class="bd-product-orig">{{ formatPrice((p.originalPrice || 0) / 100) }}</span>
                <span class="bd-product-qty" v-if="p.quantity">x{{ p.quantity }}</span>
              </div>
              <span v-if="p.stock !== undefined && p.stock <= 10" class="bd-product-stock">仅剩 {{ p.stock }} 件</span>
            </div>
            <JdButton size="sm" type="outline" @click.stop="goProduct(p.id)">查看</JdButton>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.bd-page { max-width: 1000px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }
.bd-back { margin-bottom: var(--space-lg); }
.back-btn { background: none; border: none; color: var(--text-secondary); cursor: pointer; font-size: var(--font-md); }

.bd-main { display: grid; grid-template-columns: 1fr 420px; gap: var(--space-xxl); margin-bottom: var(--space-xxxl); }
.bd-image { border-radius: var(--radius-lg); overflow: hidden; background: var(--bg-white); }

.bd-info { display: flex; flex-direction: column; gap: var(--space-xl); }
.bd-name { font-size: var(--font-xxl); font-weight: 700; }
.bd-desc { font-size: var(--font-md); color: var(--text-secondary); line-height: 1.6; }

.bd-price-card {
  background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl);
  display: flex; flex-direction: column; gap: var(--space-md);
}
.bd-price-row { display: flex; justify-content: space-between; align-items: baseline; }
.bd-price-label { font-size: var(--font-sm); color: var(--text-secondary); }
.bd-price-value { color: var(--jd-red); font-weight: 800; font-size: var(--font-xxxl); }
.bd-price-original { color: var(--text-tertiary); font-size: var(--font-md); text-decoration: line-through; }
.bd-savings {
  background: var(--jd-red-light); color: var(--jd-red); padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-sm); font-weight: 600; font-size: var(--font-sm); text-align: center;
}

.bd-btn { width: 100%; }

.bd-products-section { margin-top: var(--space-xxl); }
.bd-section-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-xl); }
.bd-products-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-lg); }
.bd-product-card {
  background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden;
  cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition);
}
.bd-product-card:hover { transform: translateY(-4px); }
.bd-product-info { padding: var(--space-md); position: relative; }
.bd-product-num {
  position: absolute; top: -12px; right: var(--space-md);
  width: 24px; height: 24px; border-radius: 50%; background: var(--jd-red);
  color: #fff; font-size: 12px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.bd-product-name { font-size: 13px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 4px; }
.bd-product-price { display: flex; justify-content: space-between; align-items: center; }
.bd-product-current { color: var(--jd-red); font-weight: 700; font-size: var(--font-md); }
.bd-product-qty { font-size: 12px; color: var(--text-tertiary); }
.bd-product-orig { font-size: var(--font-xs); color: var(--text-tertiary); text-decoration: line-through; margin-left: 6px; }
.bd-product-stock { font-size: 11px; color: var(--jd-red); font-weight: 600; }

.bd-skel { display: flex; flex-direction: column; gap: var(--space-lg); }

@media (max-width: 768px) {
  .bd-page { padding: var(--space-lg) var(--space-md) 80px; }
  .bd-main { grid-template-columns: 1fr; }
  .bd-products-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-md); }
}
</style>
