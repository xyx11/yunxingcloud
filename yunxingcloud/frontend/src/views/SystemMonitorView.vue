<script setup lang="ts">
import { ref, onMounted, h, onBeforeUnmount } from 'vue'
import request from '@/api/request'
import { NCard, NGrid, NGridItem, NTag, NButton, NSpace, NDataTable, NStatistic, NPopconfirm, NInput } from 'naive-ui'
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
const benchmark = ref<any>({})
const flags = ref<any>({})
const editingAnnouncement = ref(false)
const announcementText = ref('')
const chartOption = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['内存(MB)', '会话数'], bottom: 0 },
  grid: { left: 50, right: 20, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: [] as string[] },
  yAxis: { type: 'value' },
  series: [
    { name: '内存(MB)', type: 'line', data: [] as number[], smooth: true, itemStyle: { color: '#667eea' } },
    { name: '会话数', type: 'line', data: [] as number[], smooth: true, itemStyle: { color: '#43e97b' } },
  ],
})
const showCharts = ref(false)
let sseSource: EventSource | null = null
let historyTimer: ReturnType<typeof setInterval>

async function saveAnnouncement() {
  try {
    await request.post('/api/system/flags', { announcement: announcementText.value })
    notify.success('公告已更新')
    editingAnnouncement.value = false
    await loadAll()
  } catch { notify.error('更新失败') }
}

async function loadAll() {
  const [info, sess, cache, db, disk, bench, flg] = await Promise.all([
    request.get('/api/system/info').catch(() => ({ data: {} })),
    request.get('/api/system/sessions').catch(() => ({ data: { sessions: [], count: 0 } })),
    request.get('/api/system/cache').catch(() => ({ data: {} })),
    request.get('/api/health/db').catch(() => ({ data: {} })),
    request.get('/api/health/disk').catch(() => ({ data: {} })),
    request.get('/api/system/benchmark').catch(() => ({ data: {} })),
    request.get('/api/system/flags').catch(() => ({ data: {} })),
  ])
  sysInfo.value = info.data
  sessions.value = sess.data.sessions || []
  cacheInfo.value = cache.data
  dbHealth.value = db.data; diskHealth.value = disk.data
  benchmark.value = bench.data
  flags.value = flg.data
}

async function clearCache() {
  await request.post('/api/system/cache/clear')
  await loadAll()
}

const notify = useNotify()

async function revokeSession(username: string) {
  try {
    await request.post('/api/system/sessions/revoke', { username })
    notify.success(`已强制下线: ${username}`)
    await loadAll()
  } catch { notify.error('操作失败') }
}

const sessionColumns = [
  { title: '用户', key: 'username', width: 100 },
  { title: '会话ID', key: 'token', width: 180, ellipsis: { tooltip: true } },
  { title: '登录时间', key: 'createdAt', width: 150, render: (row: any) => row.createdAt ? row.createdAt.substring(0,19).replace('T',' ') : '-' },
  { title: '最后活跃', key: 'lastAccessTime', width: 150, render: (row: any) => row.lastAccessTime ? row.lastAccessTime.substring(0,19).replace('T',' ') : '-' },
  { title: '操作', key: 'actions', width: 90, render: (row: any) => h(NPopconfirm, { onPositiveClick: () => revokeSession(row.username) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error', secondary: true }, { default: () => '下线' }), default: () => `强制下线 ${row.username}?` }) },
]

const hasSysInfo = () => Object.keys(sysInfo.value).length > 0

onMounted(() => {
  loadAll()
  showCharts.value = true
  sseSource = new EventSource('/api/sse/dashboard')
  sseSource.addEventListener('stats', (e) => {
    const sse = JSON.parse(e.data)
    if (sse.heapUsed && sse.activeSessions !== undefined) {
      const time = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
      const xData = chartOption.value.xAxis.data as string[]
      const memSeries = chartOption.value.series[0].data as number[]
      const sessSeries = chartOption.value.series[1].data as number[]
      xData.push(time)
      memSeries.push(Number(sse.heapUsed.replace(/[^0-9.]/g, '')) || 0)
      sessSeries.push(Number(sse.activeSessions) || 0)
      if (xData.length > 30) { xData.shift(); memSeries.shift(); sessSeries.shift() }
    }
  })
  historyTimer = setInterval(() => { request.get('/api/system/history').then(r => {}).catch(() => {}) }, 15000)
})
onBeforeUnmount(() => { sseSource?.close(); clearInterval(historyTimer) })
</script>

<template>
  <div style="padding:20px">
    <div style="display:flex;justify-content:flex-end;margin-bottom:12px">
      <n-button size="small" @click="loadAll">刷新</n-button>
    </div>
    <n-grid cols="4" x-gap="16" y-gap="16" responsive="screen">
      <n-grid-item span="4 m:2 l:1">
        <n-card><n-statistic label="运行时长" :value="sysInfo.uptime || '-'" /></n-card>
      </n-grid-item>
      <n-grid-item span="4 m:2 l:1">
        <n-card><n-statistic label="在线用户" :value="sessions.length" /></n-card>
      </n-grid-item>
      <n-grid-item span="4 m:2 l:1">
        <n-card><n-statistic label="堆内存" :value="sysInfo.heapUsed ? `${sysInfo.heapUsed} / ${sysInfo.heapMax || '?'}` : '-'" /></n-card>
      </n-grid-item>
      <n-grid-item span="4 m:2 l:1">
        <n-card><n-statistic label="DB查询" :value="benchmark.db_ms ? `${benchmark.db_ms}ms` : '-'" /></n-card>
      </n-grid-item>
    </n-grid>

    <n-grid cols="2" x-gap="16" style="margin-top:16px" responsive="screen">
      <n-grid-item span="2 m:1">
        <n-card title="系统信息">
          <template v-if="hasSysInfo()">
            <n-space vertical>
              <span>OS: {{ sysInfo.osName }}</span>
              <span>Java: {{ sysInfo.javaVersion }}</span>
              <span>启动: {{ sysInfo.startTime }}</span>
              <span>CPU: {{ sysInfo.availableProcessors }} 核 | 使用率 {{ sysInfo.cpuLoad || 'N/A' }}</span>
              <span>线程: {{ sysInfo.threadCount || '?' }} (峰值 {{ sysInfo.peakThreadCount || '?' }})</span>
              <span>GC: {{ sysInfo.gcCount || 0 }} 次 / {{ sysInfo.gcTimeMs || 0 }}ms</span>
              <span>最大内存: {{ sysInfo.maxMemory }}</span>
              <span v-if="sysInfo.dbActive !== undefined">DB连接: 活跃{{ sysInfo.dbActive }} 空闲{{ sysInfo.dbIdle }} 等待{{ sysInfo.dbWaiting }}</span>
            </n-space>
          </template>
          <span v-else style="color:#999">加载中...</span>
        </n-card>
      </n-grid-item>
      <n-grid-item span="2 m:1">
        <n-card title="系统状态">
          <n-space vertical>
            <n-space>
              <n-tag type="success" size="small">应用</n-tag>
              <span>{{ sysInfo.uptime ? '运行中' : '未知' }}</span>
            </n-space>
            <n-space>
              <n-tag :type="dbHealth.status === 'UP' ? 'success' : 'error'" size="small">数据库</n-tag>
              <span>{{ dbHealth.status || '?' }}</span>
            </n-space>
            <n-space>
              <n-tag :type="diskHealth.status === 'UP' ? 'success' : 'warning'" size="small">磁盘</n-tag>
              <span>{{ diskHealth.freeMB ? diskHealth.freeMB + 'MB 可用' : '?' }}</span>
            </n-space>
            <n-space>
              <n-tag type="info" size="small">缓存</n-tag>
              <n-button size="tiny" @click="clearCache">清除</n-button>
              <span v-if="cacheInfo.cacheNames" style="font-size:12px;color:#999">{{ cacheInfo.cacheNames }}</span>
            </n-space>
            <n-space>
              <n-tag type="warning" size="small">公告</n-tag>
              <span v-if="!editingAnnouncement" style="font-size:13px">{{ flags.announcement || '无' }}</span>
              <n-input v-else v-model:value="announcementText" size="small" style="width:200px" placeholder="输入公告内容；[WARN]开头=警告样式" />
              <n-button size="tiny" @click="editingAnnouncement ? saveAnnouncement() : (editingAnnouncement = true, announcementText = flags.announcement || '')">
                {{ editingAnnouncement ? '保存' : '编辑' }}
              </n-button>
              <n-button v-if="editingAnnouncement" size="tiny" @click="editingAnnouncement = false">取消</n-button>
            </n-space>
          </n-space>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="实时趋势 (SSE)" style="margin-top:16px" v-if="showCharts">
      <v-chart :option="chartOption" style="height:240px" autoresize />
    </n-card>

    <n-card title="在线会话 ({{ sessions.length }})" style="margin-top:16px">
      <n-data-table
        v-if="sessions.length"
        :columns="sessionColumns"
        :data="sessions"
        size="small"
        :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }"
        :row-key="(r: any) => r.token"
      />
      <span v-else style="color:#999">暂无活跃会话</span>
    </n-card>

    <n-card title="性能基准" style="margin-top:16px" v-if="benchmark.db_ms">
      <n-grid cols="3" x-gap="16">
        <n-grid-item>
          <n-statistic label="数据库查询" :value="`${benchmark.db_ms}ms`" />
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="缓存读取" :value="`${benchmark.cache_ms || '?'}ms`" />
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="模块" :value="benchmark.module || '-'" />
        </n-grid-item>
      </n-grid>
    </n-card>
  </div>
</template>