<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { formatPrice, formatRelativeTime } from '@/utils/format'
import JdButton from '@/components/JdButton.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import LazyImage from '@/components/LazyImage.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import type { PriceAlertItem } from '@/types'

const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const alerts = ref<PriceAlertItem[]>([])
const loading = ref(true)
const loadError = ref(false)
const deleting = ref<Set<number>>(new Set())
const statusFilter = ref<'all' | 'monitoring' | 'triggered'>('all')

const filteredAlerts = computed(() => {
  if (statusFilter.value === 'all') return alerts.value
  if (statusFilter.value === 'monitoring') return alerts.value.filter(a => !a.notified)
  return alerts.value.filter(a => a.notified)
})

async function load() {
  loading.value = true
  loadError.value = false
  try {
    const r = await request.get('/price-alert/list')
    alerts.value = r.data || []
  } catch {
    toast.error(t('priceAlerts.loadFail'))
    loadError.value = true
  } finally {
    loading.value = false
  }
}

async function removeAlert(id: number) {
  if (deleting.value.has(id)) return
  deleting.value.add(id)
  try {
    await request.delete(`/price-alert/${id}`)
    alerts.value = alerts.value.filter(a => a.id !== id)
    toast.success(t('priceAlerts.deleted'))
  } catch {
    toast.error(t('priceAlerts.deleteFail'))
  } finally {
    deleting.value.delete(id)
  }
}

function goProduct(id: number) {
  router.push(`/product/${id}`)
}

onMounted(load)
</script>

<template>
  <div class="pa-page">
    <div class="pa-header">
      <button class="back-btn" @click="router.push('/profile')">{{ t('priceAlerts.back') }}</button>
      <h1 class="pa-title">{{ t('priceAlerts.title') }}</h1>
    </div>

    <!-- Filter tabs -->
    <div v-if="alerts.length" class="pa-filter-tabs">
      <span class="pa-filter-tab" :class="{ active: statusFilter === 'all' }" @click="statusFilter = 'all'">{{ t('priceAlerts.filterAll') }} ({{ alerts.length }})</span>
      <span class="pa-filter-tab" :class="{ active: statusFilter === 'monitoring' }" @click="statusFilter = 'monitoring'">{{ t('priceAlerts.filterMonitoring') }} ({{ alerts.filter(a => !a.notified).length }})</span>
      <span class="pa-filter-tab" :class="{ active: statusFilter === 'triggered' }" @click="statusFilter = 'triggered'">{{ t('priceAlerts.filterTriggered') }} ({{ alerts.filter(a => a.notified).length }})</span>
    </div>

    <div v-if="loading" class="pa-skel">
      <SkeletonBox variant="text" :count="4" height="60px" gap="var(--space-md)" />
    </div>

    <JdEmpty v-else-if="loadError" icon="⚠️" :title="t('priceAlerts.loadFail')">
      <JdButton @click="load">{{ t('common.retry') }}</JdButton>
    </JdEmpty>

    <div v-else-if="filteredAlerts.length" class="pa-list">
      <div v-for="a in filteredAlerts" :key="a.id" class="pa-item">
        <LazyImage :src="a.productImage || a.imageUrl || ''" :alt="a.productName" height="56px" width="56px" rounded="8px" @click="goProduct(a.productId)" style="cursor:pointer" />
        <div class="pa-item-main" role="button" tabindex="0" @click="goProduct(a.productId)" @keydown.enter.prevent="goProduct(a.productId)" @keydown.space.prevent="goProduct(a.productId)">
          <div class="pa-item-name">{{ a.productName || (t('profile.productDefaultSuffix') + a.productId) }}</div>
          <div class="pa-item-meta">
            <span class="pa-target">{{ t('priceAlerts.targetPrice') }}：{{ formatPrice((a.targetPrice || 0) / 100) }}</span>
            <span v-if="a.currentPrice" class="pa-current">当前 ¥{{ formatPrice((a.currentPrice || 0) / 100) }}</span>
            <span class="pa-status" :class="{ triggered: a.notified }">{{ a.notified ? t('priceAlerts.triggered') : t('priceAlerts.monitoring') }}</span>
            <span class="pa-date">{{ formatRelativeTime(a.createdAt) }}</span>
          </div>
        </div>
        <JdButton size="sm" type="ghost" :loading="deleting.has(a.id)" @click="removeAlert(a.id)">{{ t('common.delete') }}</JdButton>
      </div>
    </div>

    <JdEmpty v-else icon="🔔" :title="t('priceAlerts.empty')" :description="t('priceAlerts.emptyDesc')">
      <JdButton @click="router.push('/products')">{{ t('priceAlerts.goBrowse') }}</JdButton>
    </JdEmpty>
  </div>
</template>

<style scoped>
.pa-page { max-width: 700px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }
.pa-header { margin-bottom: var(--space-xl); }
.back-btn { background: none; border: none; color: var(--text-secondary); cursor: pointer; font-size: var(--font-md); margin-bottom: var(--space-sm); }
.pa-title { font-size: var(--font-xl); font-weight: 700; }

.pa-filter-tabs { display: flex; gap: var(--space-sm); margin-bottom: var(--space-lg); }
.pa-filter-tab { padding: 6px 16px; border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-sm); background: var(--bg-white); border: 1px solid var(--border); color: var(--text-secondary); transition: all var(--transition-fast); }
.pa-filter-tab.active { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }
.pa-filter-tab:hover:not(.active) { border-color: var(--jd-red); color: var(--jd-red); }
.pa-current { color: var(--text-secondary); font-weight: 500; }

.pa-list { display: flex; flex-direction: column; gap: var(--space-md); }
.pa-item {
  display: flex; align-items: center; gap: var(--space-md);
  background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-lg);
  box-shadow: var(--shadow-sm);
}
.pa-item-main { flex: 1; cursor: pointer; }
.pa-item-name { font-weight: 600; font-size: var(--font-md); margin-bottom: 4px; }
.pa-item-meta { display: flex; gap: var(--space-md); font-size: var(--font-sm); color: var(--text-tertiary); }
.pa-target { color: var(--jd-red); font-weight: 500; }
.pa-status { color: var(--green); }
.pa-status.triggered { color: var(--jd-red); }

.pa-skel { display: flex; flex-direction: column; }

@media (max-width: 768px) {
  .pa-page { padding: var(--space-lg) var(--space-md) calc(80px + env(safe-area-inset-bottom, 0px)); }
}
</style>
