<script setup lang="ts">
import LazyImage from './LazyImage.vue'

export interface ProductCardData {
  id: number
  name: string
  price: number
  originalPrice?: number
  imageUrl?: string
  sales?: number
  rating?: number
  badge?: string
  tags?: string[]
  stock?: number
}

withDefaults(defineProps<{
  product: ProductCardData
  showAddCart?: boolean
  showRating?: boolean
  showSales?: boolean
  layout?: 'grid' | 'list'
}>(), {
  showAddCart: true,
  showRating: true,
  showSales: true,
  layout: 'grid',
})

const emit = defineEmits<{
  (e: 'click', id: number): void
  (e: 'add-cart', product: ProductCardData): void
}>()

function defaultImage(name: string): string {
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=f10215&color=fff&size=200&bold=true&format=png`
}
</script>

<template>
  <div
    class="product-card"
    :class="{ 'product-card--list': layout === 'list' }"
    @click="emit('click', product.id)"
  >
    <div class="card-image">
      <LazyImage
        :src="product.imageUrl || defaultImage(product.name)"
        :alt="product.name"
        class="card-img"
      />
      <span v-if="product.badge" class="card-badge">{{ product.badge }}</span>
    </div>

    <div class="card-body">
      <h3 class="card-name line-clamp-2">{{ product.name }}</h3>

      <div v-if="showRating && product.rating" class="card-rating">
        <span class="stars" v-html="'★'.repeat(Math.round(product.rating)) + '☆'.repeat(5 - Math.round(product.rating))" />
        <span class="rating-num">{{ product.rating }}</span>
      </div>

      <div class="card-price-row">
        <span class="card-price">
          <span class="price-symbol">¥</span>{{ product.price }}
        </span>
        <span v-if="product.originalPrice" class="card-original-price">¥{{ product.originalPrice }}</span>
      </div>

      <div v-if="showSales && product.sales" class="card-meta">
        <span>{{ product.sales > 10000 ? Math.floor(product.sales / 1000) / 10 + '万' : product.sales }}+人已购</span>
      </div>

      <div v-if="product.tags && product.tags.length" class="card-tags">
        <span v-for="tag in product.tags.slice(0, 3)" :key="tag" class="card-tag">{{ tag }}</span>
      </div>

      <button
        v-if="showAddCart"
        class="add-cart-btn"
        @click.stop="emit('add-cart', product)"
      >
        🛒 加入购物车
      </button>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: transform var(--transition), box-shadow var(--transition);
  box-shadow: var(--shadow-sm);
}
.product-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: var(--shadow-card-hover);
}

.card-image {
  position: relative;
  overflow: hidden;
  background: #f5f5f5;
}
.card-img { width: 100%; aspect-ratio: 1; object-fit: cover; }
.card-image :deep(img) {
  transition: transform var(--transition-slow);
}
.product-card:hover .card-image :deep(img) {
  transform: scale(1.05);
}
.card-badge {
  position: absolute; top: var(--space-sm); left: var(--space-sm);
  background: var(--jd-red); color: #fff; font-size: var(--font-xs);
  padding: 2px 8px; border-radius: var(--radius-sm); font-weight: 600;
}

.card-body {
  padding: var(--space-md);
}
.card-name {
  font-size: var(--font-md);
  color: var(--text-primary);
  font-weight: 500;
  margin-bottom: var(--space-sm);
  line-height: 1.5;
  min-height: 2.2em;
}

.card-rating {
  display: flex; align-items: center; gap: var(--space-xs);
  margin-bottom: var(--space-sm); font-size: var(--font-xs);
}
.stars { color: var(--orange); letter-spacing: 1px; }
.rating-num { color: var(--text-tertiary); }

.card-price-row {
  display: flex; align-items: baseline; gap: var(--space-sm);
  margin-bottom: var(--space-xs);
}
.card-price {
  font-size: var(--font-xl); font-weight: 700; color: var(--jd-red);
}
.price-symbol { font-size: var(--font-sm); }
.card-original-price {
  font-size: var(--font-sm); color: var(--text-tertiary); text-decoration: line-through;
}

.card-meta {
  font-size: var(--font-xs); color: var(--text-tertiary); margin-bottom: var(--space-sm);
}

.card-tags {
  display: flex; flex-wrap: wrap; gap: var(--space-xs); margin-bottom: var(--space-sm);
}
.card-tag {
  font-size: var(--font-xs); color: var(--jd-red); background: var(--jd-red-light);
  padding: 1px 6px; border-radius: var(--radius-sm);
}

.add-cart-btn {
  width: 100%; height: 32px; background: var(--jd-red); color: #fff; border: none;
  border-radius: var(--radius-md); font-size: var(--font-sm); cursor: pointer;
  font-weight: 600; transition: background var(--transition-fast); margin-top: var(--space-sm);
}
.add-cart-btn:hover { background: var(--jd-red-dark); }

/* List layout */
.product-card--list {
  display: flex;
  flex-direction: row;
}
.product-card--list .card-image {
  width: 180px;
  min-width: 180px;
}
.product-card--list .card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.product-card--list .add-cart-btn {
  width: auto;
  align-self: flex-start;
  padding: 0 var(--space-xl);
}

@media (max-width: 768px) {
  .product-card--list .card-image { width: 120px; min-width: 120px; }
}
</style>
