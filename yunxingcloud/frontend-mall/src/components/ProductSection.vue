<script setup lang="ts">
import { addToCart } from '@/api/cart'
import { useCartFly } from '@/composables/useCartFly'
import { useToast } from '@/composables/useToast'
import { formatPrice, formatCount } from '@/utils/format'
import type { Product } from '@/types'
import LazyImage from '@/components/LazyImage.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'

const toast = useToast()
const { flyToCart } = useCartFly()

withDefaults(defineProps<{
  title: string
  products: Product[]
  loading?: boolean
  columns?: number
}>(), {
  loading: false,
  columns: 4,
})

const emit = defineEmits<{
  (e: 'view-all'): void
  (e: 'product-click', id: number): void
  (e: 'add-cart', product: Product, event: Event): void
}>()

function productImage(p: Product): string {
  return p.imageUrl || (p.images?.length ? p.images[0] : '')
}

async function quickAdd(e: Event, p: Product) {
  e.stopPropagation()
  try {
    await addToCart(p.id, 1)
    toast.success('已加入购物车')
    flyToCart(e as MouseEvent)
    emit('add-cart', p, e)
  } catch { toast.error('添加失败') }
}
</script>

<template>
  <section class="section">
    <div v-if="title" class="section-header">
      <span class="section-title">{{ title }}</span>
      <span class="section-more" @click="emit('view-all')">查看全部 &gt;</span>
    </div>

    <SkeletonBox v-if="loading && !products.length" variant="card" :columns="columns" :count="columns" height="360px" />

    <JdEmpty v-else-if="!products.length" icon="🛍️" title="暂无商品" />

    <div v-else class="product-grid" :style="{ '--cols': columns }">
      <div v-for="p in products" :key="p.id" class="product-card" @click="emit('product-click', p.id)">
        <div class="product-img-wrap">
          <LazyImage :src="productImage(p)" :alt="p.name" height="200px" />
          <span v-if="p.isNew" class="tag-new">新品</span>
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
  </section>
</template>

<style scoped>
.section { margin-top: var(--space-xxxl); }
.section-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg);
}
.section-title { font-size: var(--font-xl); font-weight: 700; color: var(--text-primary); }
.section-more { font-size: var(--font-base); color: var(--text-tertiary); cursor: pointer; }
.section-more:hover { color: var(--jd-red); }

.product-grid { display: grid; gap: var(--space-lg); grid-template-columns: repeat(var(--cols, 4), 1fr); }

.product-card {
  background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden;
  cursor: pointer; box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition), transform var(--transition);
}
.product-card:hover { box-shadow: var(--shadow-card-hover); transform: translateY(-4px); }

.product-img-wrap { position: relative; }
.tag-new {
  position: absolute; top: 6px; left: 6px; background: var(--green); color: #fff;
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

.quick-add-btn {
  width: 30px; height: 30px; border-radius: 50%; border: 2px solid var(--jd-red);
  background: var(--bg-white); color: var(--jd-red); cursor: pointer;
  font-size: var(--font-lg); display: flex; align-items: center; justify-content: center;
  transition: all var(--transition-fast); flex-shrink: 0; font-weight: 700; line-height: 1;
}
.quick-add-btn:hover { background: var(--jd-red); color: #fff; }

@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-sm); }
  .product-price { font-size: var(--font-lg); }
}
</style>
