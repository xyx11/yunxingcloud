<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from '@/locales'
const { t } = useI18n()
import { ToastInjectionKey } from '@/composables/useToast'
import request from '@/api/request'
import JdButton from '@/components/JdButton.vue'

const route = useRoute()
const toast = inject(ToastInjectionKey)!
const traces = ref<any[]>([])
const trackingNo = ref('')
const carrier = ref('')
const copying = ref(false)
const loading = ref(false)

onMounted(async () => {
  const orderId = route.query.orderId
  if (orderId) {
    loading.value = true
    try { const r = await request.get('/logistics/order/' + orderId); traces.value = r.data?.traces || r.data || []; carrier.value = r.data?.carrier || ''; trackingNo.value = r.data?.trackingNo || '' } catch {} finally { loading.value = false }
  }
})

async function track() { if(!trackingNo.value.trim())return; loading.value=true; try{const r=await request.get('/logistics/track/'+trackingNo.value.trim());traces.value=r.data||[]}catch{} finally{loading.value=false} }
async function copyNo() { try { await navigator.clipboard.writeText(trackingNo.value); copying.value = true; toast.success('已复制'); setTimeout(() => copying.value = false, 1500) } catch {} }
</script>

<template>
  <div class="log-page">
    <div class="log-card">
      <h2 class="log-title">物流追踪</h2>
      <div class="log-search">
        <input v-model="trackingNo" placeholder="输入快递单号" class="log-input" />
        <JdButton @click="track">查询</JdButton>
      </div>

      <div v-if="carrier || trackingNo" class="log-info">
        <div>
          <span class="log-info-text">快递公司：<b>{{ carrier || '未知' }}</b></span>
          <span class="log-info-text">单号：<b>{{ trackingNo }}</b></span>
        </div>
        <JdButton type="ghost" size="sm" @click="copyNo">{{ copying ? '✓ 已复制' : '复制单号' }}</JdButton>
      </div>

      <div v-if="loading" class="log-loading"><div class="spinner" /><span>查询物流信息...</span></div>

      <div v-else-if="traces.length" class="log-timeline">
        <div v-for="(trace, i) in traces" :key="trace.id || i" class="log-trace">
          <div class="trace-dot-col">
            <div class="trace-dot" :class="{ active: i === 0 }">{{ i === 0 ? '✓' : '' }}</div>
            <div v-if="i < traces.length - 1" class="trace-line" :class="{ active: i === 0 }" />
          </div>
          <div class="trace-body">
            <div class="trace-status" :class="{ active: i === 0 }">{{ trace.status || trace.description }}</div>
            <div class="trace-meta">{{ trace.location || '' }}<span v-if="trace.traceTime"> · {{ trace.traceTime?.substring(0, 16) }}</span></div>
          </div>
          <div class="trace-time">{{ trace.traceTime?.substring(11, 16) }}</div>
        </div>
      </div>

      <div v-else class="log-empty">暂无物流信息</div>
    </div>
  </div>
</template>

<style scoped>
.log-page { max-width: 600px; margin: 0 auto; }
.log-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-sm); }
.log-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-lg); }
.log-search { display: flex; gap: var(--space-sm); margin-bottom: var(--space-xl); }
.log-input { flex: 1; padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md); font-size: var(--font-md); background: var(--bg-white); color: var(--text-primary); }

.log-info { background: var(--bg-hover); border-radius: var(--radius-md); padding: var(--space-md) var(--space-lg); margin-bottom: var(--space-lg); display: flex; justify-content: space-between; align-items: center; }
.log-info-text { font-size: var(--font-base); color: var(--text-secondary); margin-right: var(--space-lg); }

.log-loading { text-align: center; padding: 40px 0; display: flex; flex-direction: column; align-items: center; gap: var(--space-lg); font-size: var(--font-base); color: var(--text-tertiary); }
.spinner { width: 48px; height: 48px; border: 3px solid var(--border-light); border-top-color: var(--jd-red); border-radius: 50%; animation: spin 1s linear infinite; }

.log-timeline { display: flex; flex-direction: column; }
.log-trace { display: flex; gap: var(--space-lg); position: relative; }
.trace-dot-col { display: flex; flex-direction: column; align-items: center; }
.trace-dot { width: 14px; height: 14px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 8px; color: #fff; background: var(--border); }
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
</style>
