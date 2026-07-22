<script setup lang="ts">
import { ref, onMounted, computed, inject } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'
import request from '@/api/request'
import JdButton from '@/components/JdButton.vue'

const route = useRoute()
const { t } = useI18n()
const toast = inject(ToastInjectionKey)!
const traces = ref<any[]>([])
const trackingNo = ref('')
const carrier = ref('')
const estimatedDelivery = ref('')
const copying = ref(false)
const loading = ref(false)
const carrierPhone = ref('')
const showMapHint = ref(false)

const statusIcon = (status: string): string => {
  const s = (status || '').toLowerCase()
  if (s.includes('签收') || s.includes('delivered') || s.includes('signed')) return '✅'
  if (s.includes('派送') || s.includes('delivering') || s.includes('out for delivery')) return '📮'
  if (s.includes('到达') || s.includes('arrived') || s.includes('arrival')) return '🏢'
  if (s.includes('运输') || s.includes('transit') || s.includes('departed')) return '🚚'
  if (s.includes('揽件') || s.includes('pickup') || s.includes('collected')) return '📦'
  return '📍'
}

const packageStatus = computed(() => {
  if (!traces.value.length) return null
  const latest = traces.value[0]?.status || traces.value[0]?.description || ''
  if (/签收|delivered|signed/i.test(latest)) return { icon: '✅', text: t('logistics.delivered'), cls: 'delivered' }
  if (/派送|delivering|out for delivery/i.test(latest)) return { icon: '📮', text: t('logistics.delivering'), cls: 'delivering' }
  return { icon: '🚚', text: t('logistics.inTransit'), cls: 'transit' }
})

const traceCount = computed(() => traces.value.length)
const hasTracking = computed(() => !!(carrier.value || trackingNo.value))

onMounted(async () => {
  const orderId = route.query.orderId
  if (orderId) {
    loading.value = true
    try {
      const r = await request.get('/logistics/order/' + orderId)
      traces.value = r.data?.traces || r.data || []
      carrier.value = r.data?.carrier || ''
      trackingNo.value = r.data?.trackingNo || ''
      estimatedDelivery.value = r.data?.estimatedDelivery || ''
      carrierPhone.value = r.data?.carrierPhone || ''
      loading.value = false;
    } catch { loading.value = false; toast.error(t('logistics.loadFail')) }
  }
})

async function track() {
  if (!trackingNo.value.trim()) return
  loading.value = true
  try { const r = await request.get('/logistics/track/' + trackingNo.value.trim()); traces.value = r.data || []; loading.value = false;
    } catch { toast.error(t('logistics.trackFail')) }
}
async function copyNo() { try { await navigator.clipboard.writeText(trackingNo.value); copying.value = true; toast.success(t('toast.copied')); setTimeout(() => copying.value = false, 1500) } catch {} }
function toggleMapHint() { showMapHint.value = !showMapHint.value }

function scanBarcode() {
  // Use Barcode Detection API if available
  if ('BarcodeDetector' in window) {
    toast.info(t('logistics.scanStarting'))
  } else {
    toast.info(t('logistics.scanHint'))
  }
}
</script>

<template>
  <div class="log-page">
    <div class="log-card">
      <h2 class="log-title">{{ t('logistics.title') }}</h2>
      <div class="log-search">
        <input v-model="trackingNo" :placeholder="t('logistics.placeholder')" class="log-input" @keyup.enter="track" />
        <JdButton @click="track">{{ t('logistics.track') }}</JdButton>
        <JdButton type="outline" size="sm" @click="scanBarcode" title="扫码查件">📷</JdButton>
      </div>

      <!-- Status Banner -->
      <div v-if="packageStatus" class="log-status" :class="packageStatus.cls">
        <span class="log-status-icon">{{ packageStatus.icon }}</span>
        <div>
          <div class="log-status-text">{{ packageStatus.text }}</div>
          <div v-if="estimatedDelivery" class="log-status-eta">{{ t('logistics.estimatedDelivery') }}: {{ estimatedDelivery }}</div>
        </div>
      </div>

      <div v-if="hasTracking" class="log-info">
        <div class="log-info-grid">
          <div class="log-info-item">
            <span class="log-info-label">{{ t('logistics.carrier') }}</span>
            <span class="log-info-val">{{ carrier || t('logistics.unknown') }}</span>
          </div>
          <div class="log-info-item">
            <span class="log-info-label">{{ t('logistics.trackingNo') }}</span>
            <span class="log-info-val">{{ trackingNo }}</span>
          </div>
          <div v-if="carrierPhone" class="log-info-item">
            <span class="log-info-label">{{ t('logistics.carrierPhone') }}</span>
            <span class="log-info-val">{{ carrierPhone }}</span>
          </div>
          <div v-if="estimatedDelivery" class="log-info-item">
            <span class="log-info-label">{{ t('logistics.estimatedDelivery') }}</span>
            <span class="log-info-val eta">{{ estimatedDelivery }}</span>
          </div>
        </div>
        <div class="log-info-actions">
          <JdButton type="ghost" size="sm" @click="copyNo">{{ copying ? t('logistics.copied') : t('logistics.copyNo') }}</JdButton>
          <JdButton type="ghost" size="sm" @click="toggleMapHint">🗺 {{ t('logistics.viewMap') }}</JdButton>
        </div>
      </div>

      <div v-if="showMapHint" class="log-map-hint">
        <div class="map-placeholder">
          <span class="map-icon">🗺️</span>
          <p>{{ t('logistics.mapHint') }}</p>
          <p class="map-link-hint">{{ t('logistics.mapLinkHint', { no: trackingNo }) }}</p>
        </div>
      </div>

      <div v-if="loading" class="log-loading"><div class="spinner" /><span>{{ t('logistics.querying') }}</span></div>

      <div v-else-if="traces.length" class="log-timeline">
        <div v-for="(trace, i) in traces" :key="trace.id || i" class="log-trace">
          <div class="trace-dot-col">
            <div class="trace-dot" :class="{ active: i === 0 }">{{ statusIcon(trace.status || trace.description) }}</div>
            <div v-if="i < traces.length - 1" class="trace-line" :class="{ active: i === 0 }" />
          </div>
          <div class="trace-body">
            <div class="trace-status" :class="{ active: i === 0 }">{{ trace.status || trace.description }}</div>
            <div class="trace-meta">{{ trace.location || '' }}<span v-if="trace.traceTime"> · {{ trace.traceTime?.substring(0, 16) }}</span></div>
          </div>
          <div class="trace-time">{{ trace.traceTime?.substring(11, 16) }}</div>
        </div>
      </div>

      <!-- Summary -->
      <div v-if="traceCount > 0" class="log-summary">
        {{ t('logistics.totalRecords', { n: traceCount }) }}
      </div>

      <div v-else-if="!loading && hasTracking" class="log-empty">{{ t('logistics.noData') }}</div>
    </div>
  </div>
</template>

<style scoped>
.log-page { max-width: 600px; margin: 0 auto; }
.log-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-sm); }
.log-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-lg); }
.log-search { display: flex; gap: var(--space-sm); margin-bottom: var(--space-xl); }
.log-input { flex: 1; padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md); font-size: var(--font-md); background: var(--bg-white); color: var(--text-primary); }

.log-status { display: flex; align-items: center; gap: var(--space-md); padding: var(--space-lg); border-radius: var(--radius-md); margin-bottom: var(--space-lg); }
.log-status.delivered { background: #e8f5e9; }
.log-status.delivering { background: #fff3e0; }
.log-status.transit { background: #e3f2fd; }
.log-status-icon { font-size: 28px; }
.log-status-text { font-weight: 700; font-size: var(--font-md); }
.log-status-eta { font-size: var(--font-sm); color: var(--text-secondary); margin-top: 2px; }

.log-info { background: var(--bg-hover); border-radius: var(--radius-md); padding: var(--space-lg); margin-bottom: var(--space-lg); }
.log-info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-sm); margin-bottom: var(--space-md); }
.log-info-item { display: flex; flex-direction: column; gap: 2px; }
.log-info-label { font-size: var(--font-xs); color: var(--text-placeholder); }
.log-info-val { font-size: var(--font-base); font-weight: 600; color: var(--text-primary); }
.log-info-val.eta { color: var(--green); font-weight: 700; }
.log-info-actions { display: flex; gap: var(--space-sm); justify-content: flex-end; }

.log-map-hint { background: #f0f7ff; border-radius: var(--radius-md); padding: var(--space-lg); margin-bottom: var(--space-lg); text-align: center; }
.map-placeholder { color: var(--text-secondary); font-size: var(--font-sm); }
.map-icon { font-size: 36px; display: block; margin-bottom: var(--space-sm); }
.map-link-hint { font-size: var(--font-xs); color: var(--text-placeholder); margin-top: var(--space-xs); }

.log-summary { text-align: center; padding: var(--space-md); color: var(--text-placeholder); font-size: var(--font-xs); }

.log-loading { text-align: center; padding: 40px 0; display: flex; flex-direction: column; align-items: center; gap: var(--space-lg); font-size: var(--font-base); color: var(--text-tertiary); }
.spinner { width: 48px; height: 48px; border: 3px solid var(--border-light); border-top-color: var(--jd-red); border-radius: 50%; animation: spin 1s linear infinite; }

.log-timeline { display: flex; flex-direction: column; }
.log-trace { display: flex; gap: var(--space-lg); position: relative; }
.trace-dot-col { display: flex; flex-direction: column; align-items: center; }
.trace-dot { width: 26px; height: 26px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 13px; background: var(--bg-hover); }
.trace-dot.active { background: var(--jd-red); }
.trace-line { width: 2px; flex: 1; min-height: 30px; background: var(--border-light); }
.trace-line.active { background: var(--jd-red); }
.trace-body { padding-bottom: var(--space-xxl); flex: 1; }
.trace-status { font-weight: 600; font-size: var(--font-md); color: var(--text-primary); }
.trace-status.active { color: var(--jd-red); }
.trace-meta { color: var(--text-tertiary); font-size: var(--font-sm); margin-top: 2px; }
.trace-time { font-size: var(--font-sm); color: var(--text-placeholder); white-space: nowrap; }

.log-empty { text-align: center; color: var(--text-tertiary); padding: 40px; }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .log-page { padding: 0 var(--space-md) 80px; }
  .log-card { padding: var(--space-lg); }
  .log-track-form { flex-direction: column; }
  .log-track-form input { flex: unset; }
  .log-title { font-size: var(--font-lg); }
  .trace-status { font-size: var(--font-sm); }
  .trace-meta { font-size: 11px; }
}
</style>
