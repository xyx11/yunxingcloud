<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted } from 'vue'
import { fetchMaintenanceStats, cleanOldLogs, cleanExpiredTokens, vacuumDatabase } from '@/api/maintenance'
import { useNotify } from '@/composables/useNotify'
import { NCard, NGrid, NGridItem, NStatistic, NButton, NSpace, NPopconfirm, NInputNumber } from 'naive-ui'

const { t } = useI18n()

const notify = useNotify()
const stats = ref<Record<string, unknown>>({})
const cleanDays = ref(90)

async function loadStats() {
  try {
    const res = await fetchMaintenanceStats()
    stats.value = res.data
  } catch { /* ignore */ }
}

async function cleanLogs() {
  try {
    await cleanOldLogs(cleanDays.value)
    notify.success(t('maintenance.logCleanupSuccess', { days: cleanDays.value }))
    await loadStats()
  } catch { notify.error(t('maintenance.cleanFailed')) }
}

async function cleanTokens() {
  try {
    await cleanExpiredTokens()
    notify.success(t('maintenance.tokenCleanupSuccess'))
    await loadStats()
  } catch { notify.error(t('maintenance.cleanFailed')) }
}

async function vacuum() {
  try {
    await vacuumDatabase()
    notify.success(t('maintenance.vacuumSuccess'))
    await loadStats()
  } catch { notify.error(t('maintenance.cleanFailed')) }
}

onMounted(loadStats)
</script>

<template>
  <div class="view-pad">
    <n-card :title="t('maintenance.title')">
      <n-grid cols="4" x-gap="16" y-gap="16" responsive="screen">
        <n-grid-item span="4 m:2 l:1">
          <n-card size="small"><n-statistic :label="t('maintenance.userCount')" :value="stats.userCount ?? '-'" /></n-card>
        </n-grid-item>
        <n-grid-item span="4 m:2 l:1">
          <n-card size="small"><n-statistic :label="t('maintenance.roleCount')" :value="stats.roleCount ?? '-'" /></n-card>
        </n-grid-item>
        <n-grid-item span="4 m:2 l:1">
          <n-card size="small"><n-statistic :label="t('maintenance.menuCount')" :value="stats.menuCount ?? '-'" /></n-card>
        </n-grid-item>
        <n-grid-item span="4 m:2 l:1">
          <n-card size="small"><n-statistic :label="t('maintenance.logCount')" :value="stats.operLogCount ?? '-'" /></n-card>
        </n-grid-item>
      </n-grid>
    </n-card>

    <n-card :title="t('maintenance.logCleanup')" class="mt-16">
      <n-space align="center">
        <span>{{ t('maintenance.cleanLabel') }}</span>
        <n-input-number v-model:value="cleanDays" :min="1" :max="365" class="w-100" size="small" />
        <span>{{ t('maintenance.daysBefore') }}</span>
        <n-popconfirm @positive-click="cleanLogs">
          <template #trigger><n-button type="warning" size="small">{{ t('maintenance.executeClean') }}</n-button></template>
          {{ t('maintenance.cleanConfirm', { days: cleanDays }) }}
        </n-popconfirm>
      </n-space>
    </n-card>

    <n-card :title="t('maintenance.tokenCleanup')" class="mt-16">
      <n-space align="center">
        <span>{{ t('maintenance.tokenCleanupDesc') }}</span>
        <n-popconfirm @positive-click="cleanTokens">
          <template #trigger><n-button type="warning" size="small">{{ t('maintenance.cleanTokenBtn') }}</n-button></template>
          {{ t('maintenance.tokenCleanConfirm') }}
        </n-popconfirm>
      </n-space>
    </n-card>

    <n-card :title="t('maintenance.vacuum')" class="mt-16">
      <n-space align="center">
        <span>{{ t('maintenance.vacuumDesc') }}</span>
        <n-popconfirm @positive-click="vacuum">
          <template #trigger><n-button type="error" size="small">{{ t('maintenance.vacuumBtn') }}</n-button></template>
          {{ t('maintenance.vacuumConfirm') }}
        </n-popconfirm>
      </n-space>
    </n-card>
  </div>
</template>

<style scoped>
.w-100 { width: 100px; }
</style>
