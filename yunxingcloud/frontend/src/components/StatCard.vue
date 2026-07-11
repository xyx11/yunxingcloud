<script setup lang="ts">
import { NCard, NStatistic, NSkeleton } from 'naive-ui'

defineProps<{
  title: string
  value: string | number
  icon: string
  color: string
  loading?: boolean
}>()
</script>

<template>
  <n-card size="small" hoverable class="stat-card" :class="[color ? `stat-${color}` : '']">
    <n-statistic :label="title">
      <template #prefix>
        <span v-if="color" class="stat-icon" :class="`stat-icon--${color}`">{{ icon }}</span>
        <span v-else>{{ icon }}</span>
      </template>
      <n-skeleton v-if="loading" text :width="40" />
      <template v-else>{{ value }}</template>
    </n-statistic>
  </n-card>
</template>

<style scoped>
.stat-card { cursor: pointer; }
.stat-purple { border-left: 3px solid #667eea; }
.stat-orange { border-left: 3px solid #f0a020; }
.stat-green { border-left: 3px solid #18a058; }
.stat-blue { border-left: 3px solid #2080f0; }
.stat-red { border-left: 3px solid #d03050; }
.stat-purple2 { border-left: 3px solid #7c3aed; }
.stat-icon { display: inline-block; padding: 4px 8px; border-radius: 6px; margin-right: 8px; }
.stat-icon--purple { background: #667eea15; }
.stat-icon--orange { background: #f0a02015; }
.stat-icon--green { background: #18a05815; }
.stat-icon--blue { background: #2080f015; }
.stat-icon--red { background: #d0305015; }
.stat-icon--purple2 { background: #7c3aed15; }
</style>
