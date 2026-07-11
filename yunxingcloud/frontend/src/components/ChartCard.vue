<script setup lang="ts">
import { NCard, NSkeleton, NEmpty } from 'naive-ui'

defineProps<{
  title: string
  loading?: boolean
  height?: string
  hasData?: boolean | null
  emptyText?: string
}>()
</script>

<template>
  <n-card :title="title" size="small">
    <n-skeleton v-if="loading" :style="{ height: height || '300px' }" />
    <n-empty v-else-if="hasData === false" :description="emptyText" class="chart-empty" :style="{ height: height || '300px' }" />
    <div v-else class="chart-content" :style="{ height: height || '300px' }">
      <slot />
    </div>
  </n-card>
</template>

<style scoped>
.chart-content { position: relative; width: 100%; }
.chart-empty { display: flex; align-items: center; justify-content: center; }
</style>
