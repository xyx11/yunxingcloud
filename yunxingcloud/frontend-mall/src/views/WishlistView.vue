<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import { getFavorites, removeFavorite } from '@/api/order'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useCartFly } from '@/composables/useCartFly'
import { useI18n } from '@/locales'
import { formatPrice } from '@/utils/format'
import type { FavoriteItem } from '@/types'

const router = useRouter()
const toast = useToast()
const { flyToCart } = useCartFly()
const { t } = useI18n()
const items = ref<FavoriteItem[]>([])
const loading = ref(true)

async function load() {
  loading.value = true
  try { const r = await getFavorites(); items.value = r.data || [] } catch {} finally { loading.value = false }
}

async function unfav(productId: number) {
  try { await removeFavorite(productId); items.value = items.value.filter(i => i.productId !== productId && i.id !== productId); toast.info(t('product.unfavorite')) } catch {}
}

async function quickAdd(e: Event, p: FavoriteItem) {
  e.stopPropagation()
  const pid = p.productId || p.id
  try { await addToCart(pid, 1); toast.success('已加入购物车'); flyToCart(e as MouseEvent) } catch { toast.error('添加失败') }
}

function goDetail(id: number) { router.push(`/product/${id}`) }
onMounted(load)
</script>

<template>
  <div>
    <h2 class="page-title">{{ t('wishlist.title') }}</h2>

    <div v-if="loading" class="wl-grid">
      <div v-for="i in 4" :key="i" class="wl-skel">
        <div class="wl-skel-img" /><div class="wl-skel-body"><div class="wl-skel-line" /></div>
      </div>
    </div>

    <div v-else-if="items.length" class="wl-grid">
      <div v-for="p in items" :key="p.id" class="wl-card" @click="goDetail(p.productId || p.id)">
        <button class="wl-unfav" @click.stop="unfav(p.productId || p.id)" aria-label="取消收藏">❤️</button>
        <LazyImage :src="p.imageUrl || ''" :alt="p.productName || p.name" height="180px" />
        <div class="wl-info">
          <h4 class="wl-name">{{ p.productName || p.name || '商品' }}</h4>
          <div class="wl-bottom">
            <span class="wl-price">{{ formatPrice((p.price || 0) / 100, 2) }}</span>
            <button class="wl-add" @click.stop="(e: Event) => quickAdd(e, p)" aria-label="加购">+</button>
          </div>
        </div>
      </div>
    </div>

    <JdEmpty v-else icon="💝" :title="t('wishlist.noItems')">
      <JdButton @click="router.push('/')">{{ t('wishlist.goBrowse') }}</JdButton>
    </JdEmpty>
  </div>
</template>

<style scoped>
.page-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-lg); }
.wl-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }

.wl-skel { background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.wl-skel-img { height: 180px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; }
.wl-skel-body { padding: 12px; }
.wl-skel-line { height: 16px; width: 70%; background: var(--border-light); border-radius: var(--radius-sm); }

.wl-card {
  background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden;
  cursor: pointer; transition: transform var(--transition); box-shadow: var(--shadow-sm); position: relative;
}
.wl-card:hover { transform: translateY(-4px); }
.wl-unfav {
  position: absolute; top: 8px; right: 8px; z-index: 2; width: 28px; height: 28px;
  border-radius: 50%; border: none; background: rgba(255,255,255,.9); cursor: pointer;
  font-size: 14px; display: flex; align-items: center; justify-content: center;
  transition: transform var(--transition-fast);
}
.wl-unfav:hover { transform: scale(1.2); }

.wl-info { padding: 12px; }
.wl-name { font-size: var(--font-md); margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.wl-bottom { display: flex; align-items: center; justify-content: space-between; }
.wl-price { color: var(--jd-red); font-size: 16px; font-weight: 700; }
.wl-add {
  width: 28px; height: 28px; border-radius: 50%; border: 2px solid var(--jd-red);
  background: var(--bg-white); color: var(--jd-red); cursor: pointer; font-size: 14px;
  display: flex; align-items: center; justify-content: center; transition: all var(--transition-fast);
}
.wl-add:hover { background: var(--jd-red); color: #fff; }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

@media (max-width: 768px) { .wl-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
