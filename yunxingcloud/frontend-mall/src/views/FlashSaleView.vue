<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from '@/locales'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'
import { formatPrice } from '@/utils/format'
import CountdownTimer from '@/components/CountdownTimer.vue'
import LazyImage from '@/components/LazyImage.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdButton from '@/components/JdButton.vue'

interface FlashSaleItem {
  id: number
  productId: number
  productName?: string
  imageUrl?: string
  flashPrice: number
  originalPrice: number
  startTime: string
  endTime: string
  stock: number
  sold?: number
}

const toast = useToast()
const { t } = useI18n()
const router = useRouter()
const sales = ref<FlashSaleItem[]>([])
const loading = ref(true)

onMounted(async () => {
  try { const r = await request.get('/flash-sale'); sales.value = r.data || [] } catch { toast.error('秒杀列表加载失败') }
  finally { loading.value = false }
})

function goProduct(id: number) { router.push(`/product/${id}`) }
const isActive = (s: FlashSaleItem) => new Date(s.startTime).getTime() <= Date.now() && new Date(s.endTime).getTime() > Date.now()
const stockPct = (s: FlashSaleItem) => Math.max(0, Math.round(((s.stock - (s.sold || 0)) / Math.max(1, s.stock)) * 100))
</script>

<template>
  <div class="flash-page">
    <div class="flash-hero">
      <h1 class="flash-hero-title">⚡ 限时秒杀</h1>
      <p class="flash-hero-sub">超值好货，手慢无！</p>
    </div>

    <div v-if="loading" class="flash-grid">
      <div v-for="i in 6" :key="i" class="flash-card">
        <SkeletonBox height="200px" :count="1" />
        <div class="sk-pad"><SkeletonBox height="16px" width="60%" :count="1" /></div>
      </div>
    </div>

    <div v-else-if="sales.length" class="flash-grid">
      <div v-for="s in sales" :key="s.id" class="flash-card" @click="goProduct(s.productId)">
        <div class="flash-img">
          <LazyImage :src="s.imageUrl || ''" :alt="s.productName" height="200px" bg="linear-gradient(135deg,#1a1a1a,#333)" />
          <div class="flash-countdown">
            <CountdownTimer :end-time="s.endTime" :label="new Date(s.startTime).getTime() > Date.now() ? '距开始' : '距结束'" compact />
          </div>
          <span class="flash-stock">{{ stockPct(s) }}%剩余</span>
        </div>
        <div class="flash-info">
          <h3 class="flash-name">{{ s.productName || '秒杀商品' }}</h3>
          <div class="flash-prices">
            <span class="flash-price">{{ formatPrice(s.flashPrice / 100, 2) }}</span>
            <span class="flash-original">{{ formatPrice(s.originalPrice / 100, 2) }}</span>
          </div>
          <div class="progress-bar"><div class="progress-fill" :style="{ width: (100 - stockPct(s)) + '%' }" /></div>
          <JdButton block size="sm" :style="{ opacity: isActive(s) ? '1' : '.5' }" @click.stop="goProduct(s.productId)">
            {{ isActive(s) ? t('flashSale.buyNow') : t('flashSale.comingSoon') }}
          </JdButton>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <p class="empty-icon">⚡</p><p>{{ t('flashSale.noSales') }}</p>
    </div>
  </div>
</template>

<style scoped>
.flash-page { max-width: 900px; margin: 0 auto; }
.flash-hero { background: linear-gradient(135deg, #1a1a1a, #333); color: #fff; border-radius: var(--radius-xl); padding: var(--space-xxxl); margin-bottom: var(--space-xxl); text-align: center; box-shadow: var(--shadow-lg); }
.flash-hero-title { font-size: var(--font-h1); font-weight: 800; margin-bottom: var(--space-xs); }
.flash-hero-sub { font-size: var(--font-md); opacity: .7; }

.flash-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-lg); }
.flash-card { background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition); }
.flash-card:hover { transform: translateY(-6px); }

.flash-img { height: 200px; position: relative; }
.flash-countdown { position: absolute; top: 0; left: 0; right: 0; background: rgba(0,0,0,.6); color: #fff; text-align: center; padding: 6px; font-size: var(--font-sm); font-weight: 600; z-index: 1; }
.flash-stock { position: absolute; bottom: 10px; left: 10px; background: var(--jd-red); color: #fff; font-size: var(--font-xs); padding: 2px 8px; border-radius: var(--radius-sm); }

.flash-info { padding: var(--space-lg); }
.flash-name { font-size: 15px; font-weight: 600; margin-bottom: var(--space-sm); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.flash-prices { display: flex; align-items: baseline; gap: var(--space-sm); margin-bottom: var(--space-xs); }
.flash-price { color: var(--jd-red); font-size: var(--font-title); font-weight: 700; }
.flash-original { color: var(--text-tertiary); font-size: var(--font-sm); text-decoration: line-through; }

.progress-bar { background: var(--bg-hover); border-radius: var(--radius-sm); height: 6px; overflow: hidden; margin-bottom: var(--space-md); }
.progress-fill { height: 100%; background: linear-gradient(90deg, var(--jd-red), #ff6b6b); border-radius: var(--radius-sm); transition: width 1s; }

.empty-state { background: var(--bg-white); border-radius: var(--radius-lg); padding: 60px; text-align: center; color: var(--text-tertiary); box-shadow: var(--shadow-sm); }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.sk-pad { padding: 16px; }

@media (max-width: 768px) { .flash-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
