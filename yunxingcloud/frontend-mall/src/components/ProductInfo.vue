<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from '@/locales'
import { formatPrice } from '@/utils/format'
import type { Product, Sku } from '@/types'
import ProductRating from '@/components/ProductRating.vue'

const props = defineProps<{
  product: Product
  selectedSku: Sku | null
  favorited: boolean
  reviewsCount?: number
  alertSet?: boolean
  viewerCount?: number
}>()

const emit = defineEmits<{
  'toggle-favorite': []
  'share': []
  'set-price-alert': []
}>()

const { t } = useI18n()

const displayPrice = computed(() =>
  props.selectedSku ? props.selectedSku.price : props.product.price || 0
)

const displayStock = computed(() =>
  props.selectedSku ? props.selectedSku.stock : props.product.stock || 0
)
</script>

<template>
  <h1 class="pdp-name">
    {{ product.name }}<span class="jd-tag">自营</span>
  </h1>
  <p class="pdp-desc">{{ product.description || '' }}</p>

  <div class="pdp-rating">
    <ProductRating
      v-if="product.rating"
      :rating="product.rating || 0"
      :count="reviewsCount || 0"
    />
    <span v-else class="no-rating">暂无评分</span>
  </div>

  <!-- Price Box -->
  <div class="price-box">
    <div class="price-row">
      <span class="price-label">{{ t('product.price') }}</span>
      <span class="price-value">{{ formatPrice(displayPrice / 100, 2) }}</span>
      <span
        v-if="displayStock > 0 && displayStock <= 10"
        class="stock-warn"
      >仅剩 {{ displayStock }} 件</span>
      <span
        v-if="displayStock === 0"
        class="stock-out"
      >暂时缺货</span>
      <button
        class="alert-btn"
        :class="{ set: alertSet }"
        @click="emit('set-price-alert')"
      >{{ alertSet ? t('product.alertSet') : t('product.priceAlert') }}</button>
    </div>
    <div class="meta-row">
      <span>{{ t('product.salesCount') }} <b class="text-red">{{ product.sales || 0 }}</b></span>
      <span>👁 <b class="text-blue">{{ viewerCount }}</b> 人正在看</span>
      <span>{{ t('product.stock') }} <b>{{ displayStock }}</b></span>
    </div>
  </div>
</template>

<style scoped>
.pdp-name {
  font-size: var(--font-title);
  margin-bottom: var(--space-sm);
  display: flex;
  align-items: center;
}
.jd-tag {
  font-size: var(--font-xs);
  background: var(--jd-red);
  color: #fff;
  padding: 1px 6px;
  border-radius: 3px;
  margin-left: var(--space-sm);
  font-weight: 400;
}
.pdp-desc {
  color: var(--jd-red);
  font-size: var(--font-base);
  margin-bottom: var(--space-sm);
}
.pdp-rating {
  margin-bottom: var(--space-md);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}
.no-rating {
  color: var(--text-placeholder);
  font-size: var(--font-sm);
}

/* Price Box */
.price-box {
  background: linear-gradient(135deg, var(--jd-red-light), var(--jd-red-bg));
  padding: var(--space-xl);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-lg);
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: var(--space-md);
  flex-wrap: wrap;
}
.price-label {
  color: var(--text-tertiary);
  font-size: var(--font-base);
}
.price-value {
  color: var(--jd-red);
  font-size: 32px;
  font-weight: 700;
}
.stock-warn {
  padding: 2px 6px;
  background: #fff3cd;
  color: #856404;
  border-radius: 3px;
  font-size: var(--font-xs);
}
.stock-out {
  padding: 2px 6px;
  background: #f8d7da;
  color: #721c24;
  border-radius: 3px;
  font-size: var(--font-xs);
}
.alert-btn {
  background: none;
  border: 1px solid var(--jd-red);
  color: var(--jd-red);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: var(--font-sm);
  padding: 2px 8px;
  white-space: nowrap;
}
.alert-btn.set {
  background: var(--jd-red);
  color: #fff;
}
.meta-row {
  display: flex;
  gap: var(--space-xxl);
  margin-top: var(--space-md);
  font-size: var(--font-base);
  color: var(--text-secondary);
}
.text-red {
  color: var(--jd-red);
}
.text-blue {
  color: var(--blue);
}
</style>
