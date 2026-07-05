<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from '@/locales'
const { t } = useI18n()
import { formatPrice, formatRelativeTime } from '@/utils/format'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import { useRecentlyViewed } from '@/composables/useRecentlyViewed'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const toast = useToast()
const { items, clear } = useRecentlyViewed()

function goDetail(id: number) { router.push(`/product/${id}`) }
async function quickAdd(e: Event, p: any) { e.stopPropagation(); try { await addToCart(p.id, 1); toast.success('已加入购物车') } catch { toast.error('添加失败') } }
</script>

<template>
  <div class="rc-page">
    <div class="rc-header">
      <h2 class="rc-title">🕐 最近浏览</h2>
      <button v-if="items.length" class="rc-clear" @click="clear">清空记录</button>
    </div>
    <div v-if="items.length" class="rc-grid">
      <div v-for="p in items" :key="p.id" class="rc-card" @click="goDetail(p.id)">
        <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="180px" />
        <div class="rc-info">
          <h4 class="rc-name">{{ p.name }}</h4>
          <div class="rc-bottom">
            <div>
              <span class="rc-price">{{ formatPrice(p.price / 100, 2) }}</span>
              <div class="rc-time">{{ formatRelativeTime(p.viewedAt) }}</div>
            </div>
            <button class="rc-add" @click="(e: Event) => quickAdd(e, p)" aria-label="加购">+</button>
          </div>
        </div>
      </div>
    </div>
    <JdEmpty v-else icon="🕐" title="暂无浏览记录">
      <JdButton @click="router.push('/')">去逛逛</JdButton>
    </JdEmpty>
  </div>
</template>

<style scoped>
.rc-page { max-width: 900px; margin: 0 auto; }
.rc-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.rc-title { font-size: var(--font-xl); font-weight: 700; }
.rc-clear { padding: 6px 16px; border: 1px solid var(--border); background: var(--bg-white); border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-sm); color: var(--text-tertiary); transition: all var(--transition-fast); }
.rc-clear:hover { border-color: var(--jd-red); color: var(--jd-red); }

.rc-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.rc-card {
  background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden;
  cursor: pointer; transition: transform var(--transition); box-shadow: var(--shadow-sm);
}
.rc-card:hover { transform: translateY(-4px); }
.rc-info { padding: 12px; }
.rc-name { font-size: var(--font-md); margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rc-bottom { display: flex; align-items: center; justify-content: space-between; }
.rc-price { color: var(--jd-red); font-size: 16px; font-weight: 700; }
.rc-time { font-size: 10px; color: var(--text-placeholder); margin-top: 2px; }
.rc-add {
  width: 28px; height: 28px; border-radius: 50%; border: 2px solid var(--jd-red);
  background: var(--bg-white); color: var(--jd-red); cursor: pointer; font-size: 14px;
  display: flex; align-items: center; justify-content: center; transition: all var(--transition-fast);
}
.rc-add:hover { background: var(--jd-red); color: #fff; }

@media (max-width: 768px) { .rc-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
