<script setup lang="ts">
import type { Sku } from '@/types'
import { formatPrice } from '@/utils/format'

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
</script>

<template>
  <div class="sku-section">
    <div class="sku-label">选择规格</div>
    <div class="sku-grid">
      <span
        v-for="sku in skus"
        :key="sku.id"
        class="sku-item"
        :class="{ selected: modelValue?.id === sku.id }"
        @click="emit('update:modelValue', sku)"
      >
        <span
          v-if="(sku as Sku).specs"
          class="sku-color"
          :style="{
            background:
              SKU_COLORS[
                ((sku as Sku).specs || '')
                  .match(/红|蓝|绿|黄|紫|黑|白|灰|粉|金|银|橙/)?.[0] || ''
              ] || '#ddd',
          }"
        />
        {{ sku.name }}
        <span class="sku-price">{{ formatPrice(sku.price / 100, 2) }}</span>
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
</style>
