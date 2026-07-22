<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBundles } from '@/api/bundle'
import { formatPrice } from '@/utils/format'
import LazyImage from '@/components/LazyImage.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import JdButton from '@/components/JdButton.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const bundles = ref<any[]>([])
const loading = ref(true)
const loadError = ref(false)
const sortBy = ref<'default' | 'priceAsc' | 'priceDesc' | 'savings'>('default')

const sortedBundles = computed(() => {
  const list = [...bundles.value]
  const p = (b: any) => b.bundlePrice || b.price || 0
  const save = (b: any) => (b.originalPrice || 0) - p(b)
  switch (sortBy.value) {
    case 'priceAsc': return list.sort((a, b) => p(a) - p(b))
    case 'priceDesc': return list.sort((a, b) => p(b) - p(a))
    case 'savings': return list.sort((a, b) => save(b) - save(a))
    default: return list
  }
})

const sorts = [
  { key: 'default' as const, label: t('bundle.sortDefault') },
  { key: 'priceAsc' as const, label: t('bundle.sortPriceAsc') },
  { key: 'priceDesc' as const, label: t('bundle.sortPriceDesc') },
  { key: 'savings' as const, label: t('bundle.sortSavings') },
]

onMounted(load)

async function load() {
  loading.value = true
  loadError.value = false
  try {
    const r = await getBundles()
    bundles.value = r.data || []
  } catch {
    toast.error(t('bundle.loadFail'))
    loadError.value = true
  } finally {
    loading.value = false
  }
}

function goDetail(id: number) { router.push(`/bundle/${id}`) }
</script>

<template>
  <div class="bundles-page">
    <div class="bundles-hero">
      <h1>{{ t('bundle.title') }}</h1>
      <p>{{ t('bundle.subtitle') }}</p>
    </div>

    <!-- Sort bar -->
    <div class="bundles-sort" v-if="!loading && bundles.length">
      <span v-for="s in sorts" :key="s.key" class="sort-tag" :class="{ active: sortBy === s.key }" @click="sortBy = s.key">{{ s.label }}</span>
    </div>

    <!-- Loading -->
    <SkeletonBox v-if="loading" variant="card" :columns="3" :count="6" height="280px" />

    <!-- Error -->
    <JdEmpty v-else-if="loadError" icon="⚠️" :title="t('bundle.loadFail')">
      <JdButton @click="load">{{ t('common.retry') }}</JdButton>
    </JdEmpty>

    <!-- Bundles grid -->
    <div v-else-if="sortedBundles.length" class="bundles-grid">
      <div v-for="b in sortedBundles" :key="b.id" class="bundle-card" role="button" tabindex="0" @click="goDetail(b.id)" @keydown.enter.prevent="goDetail(b.id)" @keydown.space.prevent="goDetail(b.id)">
        <div class="bundle-img-wrap">
          <LazyImage :src="b.imageUrl || ''" :alt="b.name" height="200px" />
          <span v-if="b.products?.length" class="bundle-count-badge">{{ b.products.length }}件套装</span>
        </div>
        <div class="bundle-info">
          <h3 class="bundle-name">{{ b.name }}</h3>
          <p v-if="b.description" class="bundle-desc">{{ b.description }}</p>
          <div class="bundle-prices">
            <span class="bundle-price">{{ formatPrice((b.bundlePrice || b.price || 0) / 100, 2) }}</span>
            <span v-if="b.originalPrice" class="bundle-original">{{ formatPrice(b.originalPrice / 100) }}</span>
          </div>
          <span v-if="b.originalPrice && b.originalPrice > (b.bundlePrice || b.price || 0)" class="bundle-tag">
            {{ t('bundle.save') }} ¥{{ formatPrice(((b.originalPrice || 0) - (b.bundlePrice || b.price || 0)) / 100) }}
          </span>
        </div>
      </div>
    </div>

    <JdEmpty v-else icon="🎁" :title="t('bundle.empty')" />
  </div>
</template>

<style scoped>
.bundles-page { max-width: 1000px; margin: 0 auto; }
.bundles-hero { background: linear-gradient(135deg, #f093fb, #f5576c); color: #fff; border-radius: var(--radius-xl); padding: var(--space-xxxl); margin-bottom: var(--space-xxl); text-align: center; }
.bundles-hero h1 { font-size: var(--font-h1); font-weight: 800; margin-bottom: var(--space-sm); }
.bundles-hero p { opacity: .9; }

.bundles-sort { display: flex; gap: var(--space-sm); margin-bottom: var(--space-xl); }
.sort-tag { padding: 6px 16px; border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-sm); color: var(--text-secondary); border: 1px solid var(--border); transition: all var(--transition-fast); background: var(--bg-white); }
.sort-tag.active { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }
.sort-tag:hover:not(.active) { border-color: var(--jd-red); color: var(--jd-red); }

.bundles-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-lg); }
.bundle-card { background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition), box-shadow var(--transition); }
.bundle-card:hover { transform: translateY(-6px); box-shadow: var(--shadow-lg); }

.bundle-img-wrap { position: relative; }
.bundle-count-badge { position: absolute; bottom: 10px; right: 10px; background: rgba(0,0,0,.65); color: #fff; font-size: 11px; padding: 3px 10px; border-radius: var(--radius-round); }

.bundle-info { padding: var(--space-lg); }
.bundle-name { font-size: 16px; font-weight: 600; margin-bottom: var(--space-xs); }
.bundle-desc { font-size: var(--font-sm); color: var(--text-tertiary); margin-bottom: var(--space-sm); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.bundle-prices { display: flex; gap: var(--space-sm); align-items: baseline; }
.bundle-price { color: var(--jd-red); font-size: var(--font-xxl); font-weight: 700; }
.bundle-original { color: var(--text-tertiary); font-size: var(--font-sm); text-decoration: line-through; }
.bundle-tag { display: inline-block; margin-top: var(--space-xs); padding: 3px 10px; background: var(--jd-red-light); color: var(--jd-red); border-radius: var(--radius-round); font-size: 12px; font-weight: 600; }

@media (max-width: 768px) {
  .bundles-page { padding: 0 var(--space-md) calc(80px + env(safe-area-inset-bottom, 0px)); }
  .bundles-hero { padding: var(--space-xl); }
  .bundles-sort { overflow-x: auto; -webkit-overflow-scrolling: touch; }
  .sort-tag { flex-shrink: 0; white-space: nowrap; }
  .bundles-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-md); }
}
</style>
