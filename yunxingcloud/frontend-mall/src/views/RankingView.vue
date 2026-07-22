<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { formatPrice, formatCount } from '@/utils/format'
import type { Product } from '@/types'
import LazyImage from '@/components/LazyImage.vue'
import { getHotProducts, getNewProducts, getProducts } from '@/api/product'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const ranks = ref<Product[]>([])
const loading = ref(true)
const tab = ref<'sales' | 'new' | 'price'>('sales')

const tabs = [
  { key: 'sales' as const, label: t('ranking.hot'), icon: '🔥', desc: '实时热卖' },
  { key: 'new' as const, label: t('ranking.new'), icon: '✨', desc: '本周新品' },
  { key: 'price' as const, label: t('ranking.price'), icon: '💰', desc: '好价精选' },
]

const period = ref<'all' | 'week' | 'month'>('all')
const periods = [
  { key: 'all' as const, label: t('ranking.tabAll') },
  { key: 'week' as const, label: t('ranking.tabWeek') },
  { key: 'month' as const, label: t('ranking.tabMonth') },
]

async function loadRank() {
  loading.value = true
  try {
    let r
    const params: Record<string, any> = { size: 20 }
    if (period.value === 'week') params.period = 'week'
    else if (period.value === 'month') params.period = 'month'
    if (tab.value === 'sales') r = await getHotProducts()
    else if (tab.value === 'new') r = await getNewProducts()
    else r = await getProducts({ ...params, sort: 'price' })
    ranks.value = (r.data || []).slice(0, 20)
    loading.value = false;
    } catch { toast.error(t('ranking.loadFail')) }
}

onMounted(loadRank)
watch(tab, loadRank)
watch(period, loadRank)

function goDetail(id: number) { router.push(`/product/${id}`) }
async function quickAdd(e: Event, p: Product) { e.stopPropagation(); try { await addToCart(p.id, 1); toast.success(t('toast.addedToCart')) } catch { toast.error(t('toast.addCartFail')) } }
</script>

<template>
  <div class="rank-page">
    <div class="rank-hero">
      <h1 class="rank-hero-title">🏆 {{ t('ranking.title') }}</h1>
      <p class="rank-hero-sub">{{ t('ranking.subtitle') }}</p>
    </div>
    <div class="rank-tabs">
      <span v-for="t in tabs" :key="t.key" class="rank-tab" :class="{ active: tab === t.key }" @click="tab = t.key">
        {{ t.icon }} {{ t.label }}
      </span>
    </div>
    <div class="rank-period-tabs">
      <span v-for="p in periods" :key="p.key" class="rank-period-tab" :class="{ active: period === p.key }" @click="period = p.key">{{ p.label }}</span>
    </div>
    <div v-if="loading" class="rank-list">
      <div v-for="i in 10" :key="i" class="rank-skel" />
    </div>
    <div v-else-if="ranks.length" class="rank-list">
      <div v-for="(p, i) in ranks" :key="p.id" class="rank-item" :class="{ 'top-3': i < 3 }" role="button" tabindex="0" @click="goDetail(p.id)" @keydown.enter.prevent="goDetail(p.id)" @keydown.space.prevent="goDetail(p.id)">
        <span class="rank-medal" :class="{ 'top-num': i >= 3 }">{{ i < 3 ? ['🥇','🥈','🥉'][i] : i + 1 }}</span>
        <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="60px" width="60px" rounded="6px" />
        <div class="rank-info">
          <div class="rank-name">{{ p.name }}</div>
          <div class="rank-sales">{{ t('product.salesCount') }} {{ formatCount(p.sales || 0) }}</div>
        </div>
        <div class="rank-price-col">
          <div class="rank-price">{{ formatPrice(p.price / 100, 2) }}</div>
          <button class="rank-add" @click="(e:Event) => quickAdd(e, p)">+ {{ t('product.addToCart') }}</button>
        </div>
      </div>
    </div>
    <div v-else class="rank-empty">{{ t('ranking.empty') }}</div>
  </div>
</template>

<style scoped>
.rank-page { max-width: 900px; margin: 0 auto; }
.rank-hero {
  background: linear-gradient(135deg, var(--jd-red), #ff4444); color: #fff;
  border-radius: var(--radius-xl); padding: 32px; margin-bottom: var(--space-xl);
  text-align: center; box-shadow: 0 4px 20px rgba(241,2,21,.25);
}
.rank-hero-title { font-size: var(--font-h1); font-weight: 800; margin-bottom: 4px; }
.rank-hero-sub { font-size: var(--font-md); opacity: .85; }

.rank-tabs { display: flex; margin-bottom: var(--space-sm); background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.rank-tab {
  flex: 1; text-align: center; padding: 12px; cursor: pointer; font-size: var(--font-md);
  transition: all var(--transition-fast); font-weight: 600; color: var(--text-secondary);
}
.rank-tab.active { background: var(--jd-red); color: #fff; }

/* Period tabs */
.rank-period-tabs { display: flex; gap: var(--space-sm); margin-bottom: var(--space-lg); }
.rank-period-tab {
  padding: 4px 16px; border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-sm);
  color: var(--text-secondary); border: 1px solid var(--border); transition: all var(--transition-fast);
}
.rank-period-tab.active { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }
.rank-period-tab:hover:not(.active) { border-color: var(--jd-red); color: var(--jd-red); }

.rank-list { display: flex; flex-direction: column; gap: 10px; }
.rank-skel {
  background: var(--bg-white); border-radius: var(--radius-md); padding: 16px;
  box-shadow: var(--shadow-sm); height: 60px;
  background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light));
  background-size: 200% 100%; animation: shimmer 1.5s infinite;
}

.rank-item {
  background: var(--bg-white); border-radius: var(--radius-md); padding: 14px 20px;
  cursor: pointer; box-shadow: var(--shadow-sm); display: flex; align-items: center;
  gap: 16px; transition: transform var(--transition), box-shadow var(--transition);
}
.rank-item:hover { transform: translateX(4px); box-shadow: var(--shadow-md); }
.rank-medal { font-size: 22px; font-weight: 800; width: 32px; text-align: center; flex-shrink: 0; }
.rank-medal.top-num { font-size: 18px; color: var(--text-tertiary); }
.rank-item.top-3 { border-left: 3px solid var(--jd-red); background: linear-gradient(90deg, var(--jd-red-light), var(--bg-white) 30%); }
.rank-info { flex: 1; }
.rank-name { font-weight: 600; font-size: 15px; margin-bottom: 2px; }
.rank-sales { color: var(--text-tertiary); font-size: var(--font-sm); }
.rank-price-col { text-align: right; flex-shrink: 0; }
.rank-price { color: var(--jd-red); font-size: 20px; font-weight: 700; }
.rank-add {
  margin-top: 4px; padding: 2px 12px; border: 1px solid var(--jd-red);
  background: var(--bg-white); color: var(--jd-red); border-radius: var(--radius-round);
  cursor: pointer; font-size: 11px; transition: all var(--transition-fast);
}
.rank-add:hover { background: var(--jd-red); color: #fff; }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

@media (max-width: 768px) {
  .rank-item { padding: 12px; gap: 10px; }
  .rank-medal { font-size: 18px; width: 24px; }
  .rank-price { font-size: 16px; }
}
</style>
