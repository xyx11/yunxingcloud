<script setup lang="ts">
import type { Sku } from '@/types'
import { formatPrice } from '@/utils/format'
import { useI18n } from '@/locales'

const { t } = useI18n()

defineProps<{
  skus: Sku[]
  modelValue: Sku | null
}>()

const emit = defineEmits<{
  'update:modelValue': [sku: Sku]
}>()

const SKU_COLORS: Record<string, string> = {
  '红': '#f10215',
  '蓝': '#1677ff',
  '绿': '#4caf50',
  '黄': '#ffc107',
  '紫': '#9c27b0',
  '黑': '#333',
  '白': '#fff',
  '灰': '#999',
  '粉': '#e91e63',
  '金': '#ffc107',
  '银': '#ccc',
  '橙': '#ff9800',
}

function isCurrentSku(_sku: Sku) {
  // Placeholder — expose if needed by parent
  return false
}
</script>

<template>
  <div class="sku-section">
    <div class="sku-label">{{ t('product.chooseSpecs') }}</div>
    <div class="sku-grid">
      <span
        v-for="sku in skus"
        :key="sku.id"
        class="sku-item"
        :class="{ selected: modelValue?.id === sku.id, disabled: sku.stock === 0 }"
        role="radio"
        :aria-checked="modelValue?.id === sku.id"
        :aria-disabled="sku.stock === 0"
        :tabindex="sku.stock === 0 ? -1 : 0"
        :title="sku.stock === 0 ? t('product.soldOut') : sku.name"
        @click="sku.stock !== 0 && emit('update:modelValue', sku)"
        @keydown.enter.prevent="sku.stock !== 0 && emit('update:modelValue', sku)"
        @keydown.space.prevent="sku.stock !== 0 && emit('update:modelValue', sku)"
      >
        <span
          v-if="sku.specs"
          class="sku-color"
          :style="{
            background:
              SKU_COLORS[
                (sku.specs || '')
                  .match(/红|蓝|绿|黄|紫|黑|白|灰|粉|金|银|橙/)?.[0] || ''
              ] || '#ddd',
          }"
        />
        {{ sku.name }}
        <span class="sku-price" :class="{ highlight: modelValue && modelValue.id !== sku.id }">
          {{ formatPrice(sku.price / 100, 2) }}
        </span>
        <span v-if="isCurrentSku(sku)" class="sku-check">✓</span>
      </span>
    </div>
  </div>
</template>

<style scoped>
.sku-section {
  margin-bottom: var(--space-lg);
}
.sku-label {
  font-size: var(--font-base);
  color: var(--text-secondary);
  margin-bottom: var(--space-sm);
}
.sku-grid {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
}
.sku-item {
  padding: var(--space-sm) var(--space-lg);
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: var(--font-base);
  transition: all var(--transition);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  border: 1px solid var(--border);
  color: var(--text-primary);
  background: var(--bg-white);
}
.sku-item.selected {
  border: 2px solid var(--jd-red);
  color: var(--jd-red);
  background: var(--jd-red-light);
}
.sku-color {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 1px solid var(--border);
  display: inline-block;
  flex-shrink: 0;
}
.sku-price {
  font-size: var(--font-xs);
  color: var(--text-tertiary);
}
.sku-price.highlight { color: var(--jd-red); font-weight: 600; }
.sku-item.disabled { opacity: .4; cursor: not-allowed; text-decoration: line-through; background: var(--bg-hover); }
.sku-check { font-size: 12px; color: var(--jd-red); margin-left: 2px; }
</style>
