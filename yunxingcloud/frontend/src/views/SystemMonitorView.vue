<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, h, onBeforeUnmount } from 'vue'
import { fetchSystemInfo, fetchSessions, fetchCacheInfo, fetchDbHealth, fetchDiskHealth, fetchBenchmark, fetchFlags, clearCache, revokeSession, saveAnnouncement as saveAnnouncementApi } from '@/api/system'
import { NCard, NGrid, NGridItem, NTag, NButton, NSpace, NDataTable, NStatistic, NPopconfirm, NInput } from 'naive-ui'

const { t, locale } = useI18n()
import { useNotify } from '@/composables/useNotify'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const sysInfo = ref<any>({})
const sessions = ref<any[]>([])
const cacheInfo = ref<any>({})
const dbHealth = ref<any>({})
const diskHealth = ref<any>({})
const serviceHealth = ref<Record<string,string>>({})
const benchmark = ref<any>({})
const flags = ref<any>({})
const editingAnnouncement = ref(false)
const announcementText = ref('')
const chartOption = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: [t('monitor.memoryMb'), t('monitor.sessionCount')], bottom: 0 },
  grid: { left: 50, right: 20, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: [] as string[] },
  yAxis: { type: 'value' },
  series: [
    { name: t('monitor.memoryMb'), type: 'line', data: [] as number[], smooth: true, itemStyle: { color: '#667eea' } },
    { name: t('monitor.sessionCount'), type: 'line', data: [] as number[], smooth: true, itemStyle: { color: '#43e97b' } },
  ],
})
const showCharts = ref(false)
let sseSource: EventSource | null = null
let historyTimer: ReturnType<typeof setInterval>

async function saveAnnouncement() {
  try {
    await saveAnnouncementApi(announcementText.value)
    notify.success(t('monitor.announcementUpdated'))
    editingAnnouncement.value = false
    await loadAll()
  } catch { notify.error(t('monitor.updateFailed')) }
}

async function loadAll() {
  const [info, sess, cache, db, disk, bench, flg] = await Promise.all([
    fetchSystemInfo().catch(() => ({ data: {} })),
    fetchSessions().catch(() => ({ data: { sessions: [], count: 0 } })),
    fetchCacheInfo().catch(() => ({ data: {} })),
    fetchDbHealth().catch(() => ({ data: {} })),
    fetchDiskHealth().catch(() => ({ data: {} })),
    fetchBenchmark().catch(() => ({ data: {} })),
    fetchFlags().catch(() => ({ data: {} })),
  ])
  sysInfo.value = info.data
  sessions.value = sess.data.sessions || []
  cacheInfo.value = cache.data
  dbHealth.value = db.data; diskHealth.value = disk.data
  benchmark.value = bench.data
  flags.value = flg.data
  // Check service health via gateway
  try { const r = await fetch('/api/system/health-check').then(r=>r.json()); serviceHealth.value = r } catch { serviceHealth.value = {} }
}

async function clearSystemCache() {
  await clearCache()
  await loadAll()
}

const notify = useNotify()

async function revokeUserSession(username: string) {
  try {
    await revokeSession(username)
    notify.success(t('monitor.revokeSuccess', { username }))
    await loadAll()
  } catch { notify.error(t('monitor.operFailed')) }
}

const sessionColumns = [
  { title: t('monitor.user'), key: 'username', width: 100 },
  { title: t('monitor.sessionId'), key: 'token', width: 180, ellipsis: { tooltip: true } },
  { title: t('monitor.loginTime'), key: 'createdAt', width: 150, render: (row: any) => row.createdAt ? row.createdAt.substring(0,19).replace('T',' ') : '-' },
  { title: t('monitor.lastActive'), key: 'lastAccessTime', width: 150, render: (row: any) => row.lastAccessTime ? row.lastAccessTime.substring(0,19).replace('T',' ') : '-' },
  { title: t('monitor.actions'), key: 'actions', width: 90, render: (row: any) => h(NPopconfirm, { onPositiveClick: () => revokeUserSession(row.username) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error', secondary: true }, { default: () => t('monitor.revoke') }), default: () => t('monitor.revokeConfirm', { username: row.username }) }) },
]

const hasSysInfo = () => Object.keys(sysInfo.value).length > 0

onMounted(() => {
  loadAll()
  showCharts.value = true
  sseSource = new EventSource('/api/sse/dashboard')
  sseSource.addEventListener('stats', (e) => {
    const sse = JSON.parse(e.data)
    if (sse.heapUsed && sse.activeSessions !== undefined) {
      const loc = locale.value === 'zh' ? 'zh-CN' : 'en-US'
      const time = new Date().toLocaleTimeString(loc, { hour: '2-digit', minute: '2-digit', second: '2-digit' })
      const xData = chartOption.value.xAxis.data as string[]
      const memSeries = chartOption.value.series[0].data as number[]
      const sessSeries = chartOption.value.series[1].data as number[]
      xData.push(time)
      memSeries.push(Number(sse.heapUsed.replace(/[^0-9.]/g, '')) || 0)
      sessSeries.push(Number(sse.activeSessions) || 0)
      if (xData.length > 30) { xData.shift(); memSeries.shift(); sessSeries.shift() }
    }
  })
  historyTimer = setInterval(() => { fetchSystemInfo().then(() => {}).catch(() => {}) }, 15000)
})
onBeforeUnmount(() => { sseSource?.close(); clearInterval(historyTimer) })
</script>

<template>
  <div class="view-pad">
    <div style="display:flex;justify-content:flex-end;margin-bottom:12px">
      <n-button size="small" @click="loadAll">{{ t('monitor.refresh') }}</n-button>
    </div>
    <n-grid cols="4" x-gap="16" y-gap="16" responsive="screen">
      <n-grid-item span="4 m:2 l:1">
        <n-card><n-statistic :label="t('dashboard.uptime')" :value="sysInfo.uptime || '-'" /></n-card>
      </n-grid-item>
      <n-grid-item span="4 m:2 l:1">
        <n-card><n-statistic :label="t('dashboard.onlineUsers')" :value="sessions.length" /></n-card>
      </n-grid-item>
      <n-grid-item span="4 m:2 l:1">
        <n-card><n-statistic :label="t('dashboard.heapMemory')" :value="sysInfo.heapUsed ? `${sysInfo.heapUsed} / ${sysInfo.heapMax || '?'}` : '-'" /></n-card>
      </n-grid-item>
      <n-grid-item span="4 m:2 l:1">
        <n-card><n-statistic :label="t('monitor.dbQuery')" :value="benchmark.db_ms ? `${benchmark.db_ms}ms` : '-'" /></n-card>
      </n-grid-item>
    </n-grid>

    <n-grid cols="2" x-gap="16" class="mt-16" responsive="screen">
      <n-grid-item span="2 m:1">
        <n-card :title="t('monitor.systemInfo')">
          <template v-if="hasSysInfo()">
            <n-space vertical>
              <span>OS: {{ sysInfo.osName }}</span>
              <span>Java: {{ sysInfo.javaVersion }}</span>
              <span>{{ t('monitor.startup') }}: {{ sysInfo.startTime }}</span>
              <span>{{ t('monitor.cpuInfo') }}: {{ t('monitor.cpuCores', { n: sysInfo.availableProcessors }) }} | {{ t('monitor.cpuUsage') }} {{ sysInfo.cpuLoad || 'N/A' }}</span>
              <span>{{ t('monitor.threadInfo') }}: {{ sysInfo.threadCount || '?' }} ({{ t('monitor.peak') }} {{ sysInfo.peakThreadCount || '?' }})</span>
              <span>{{ t('monitor.gcInfo') }}: {{ t('monitor.times', { n: sysInfo.gcCount || 0 }) }} / {{ sysInfo.gcTimeMs || 0 }}ms</span>
              <span>{{ t('monitor.maxMemory') }}: {{ sysInfo.maxMemory }}</span>
              <span v-if="sysInfo.dbActive !== undefined">{{ t('monitor.dbConnectionInfo') }}: {{ t('monitor.active') }}{{ sysInfo.dbActive }} {{ t('monitor.idle') }}{{ sysInfo.dbIdle }} {{ t('monitor.waiting') }}{{ sysInfo.dbWaiting }}</span>
            </n-space>
          </template>
          <span v-else class="text-muted">{{ t('monitor.loading') }}</span>
        </n-card>
      </n-grid-item>
      <n-grid-item span="2 m:1">
        <n-card :title="t('monitor.systemStatus')">
          <n-space vertical>
            <n-space>
              <n-tag type="success" size="small">{{ t('monitor.app') }}</n-tag>
              <span>{{ sysInfo.uptime ? t('monitor.running') : t('monitor.unknown') }}</span>
            </n-space>
            <n-space>
              <n-tag :type="dbHealth.status === 'UP' ? 'success' : 'error'" size="small">{{ t('monitor.database') }}</n-tag>
              <span>{{ dbHealth.status || '?' }}</span>
            </n-space>
            <n-space>
              <n-tag :type="diskHealth.status === 'UP' ? 'success' : 'warning'" size="small">{{ t('monitor.disk') }}</n-tag>
              <span>{{ diskHealth.freeMB ? diskHealth.freeMB + t('monitor.diskAvailable') : '?' }}</span>
            </n-space>
            <n-space>
              <n-tag type="info" size="small">{{ t('monitor.cache') }}</n-tag>
              <n-button size="tiny" @click="clearSystemCache">{{ t('monitor.clearCache') }}</n-button>
              <span v-if="cacheInfo.cacheNames" class="text-xs-muted">{{ cacheInfo.cacheNames }}</span>
            </n-space>
            <n-space>
              <n-tag type="warning" size="small">{{ t('monitor.announcement') }}</n-tag>
              <span v-if="!editingAnnouncement" style="font-size:13px">{{ flags.announcement || t('monitor.noAnnouncement') }}</span>
              <n-input v-else v-model:value="announcementText" size="small" style="width:200px" :placeholder="t('monitor.announcementPlaceholder')" />
              <n-button size="tiny" @click="editingAnnouncement ? saveAnnouncement() : (editingAnnouncement = true, announcementText = flags.announcement || '')">
                {{ editingAnnouncement ? t('common.save') : t('common.edit') }}
              </n-button>
              <n-button v-if="editingAnnouncement" size="tiny" @click="editingAnnouncement = false">{{ t('common.cancel') }}</n-button>
            </n-space>
          </n-space>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card :title="t('monitor.realtimeTrend')" class="mt-16" v-if="showCharts">
      <v-chart :option="chartOption" class="chart-height" autoresize />
    </n-card>

    <n-card :title="t('monitor.sessionsCount', { n: sessions.length })" class="mt-16">
      <n-data-table
        v-if="sessions.length"
        :columns="sessionColumns"
        :data="sessions"
        size="small"
        :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }"
        :row-key="(r: any) => r.token"
      />
      <span v-else class="text-muted">{{ t('monitor.noActiveSessions') }}</span>
    </n-card>

    <n-card :title="t('monitor.benchmark')" class="mt-16" v-if="benchmark.db_ms">
      <n-grid cols="3" x-gap="16">
        <n-grid-item>
          <n-statistic :label="t('monitor.dbQuery')" :value="`${benchmark.db_ms}ms`" />
        </n-grid-item>
        <n-grid-item>
          <n-statistic :label="t('monitor.cacheRead')" :value="`${benchmark.cache_ms || '?'}ms`" />
        </n-grid-item>
        <n-grid-item>
          <n-statistic :label="t('monitor.module')" :value="benchmark.module || '-'" />
        </n-grid-item>
      </n-grid>
      <n-grid v-if="Object.keys(serviceHealth).length" cols="6" x-gap="12" class="mt-16">
        <n-grid-item v-for="(status, svc) in serviceHealth" :key="svc">
          <n-card size="small" :content-style="{padding:'12px',textAlign:'center'}">
            <div :style="{fontSize:'24px',marginBottom:'4px'}">{{ status === 'UP' ? '✅' : '❌' }}</div>
            <div style="fontSize:13px;fontWeight:600">{{ svc }}</div>
            <n-tag :type="status==='UP'?'success':'error'" size="small">{{ status }}</n-tag>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-card>
  </div>
</template>