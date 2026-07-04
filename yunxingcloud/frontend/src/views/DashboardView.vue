<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'

let _es: EventSource | null = null
let _timer: ReturnType<typeof setInterval> | null = null
onBeforeUnmount(() => { _es?.close(); if (_timer) clearInterval(_timer); if (orderPollTimer) clearInterval(orderPollTimer) })
import { useRouter } from 'vue-router'
import { fetchDashboard, fetchRecentNotices, fetchRecentLogs } from '@/api/stats'
import { fetchSystemInfo } from '@/api/system'
import { fetchDepartments, fetchRoles } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import { useLiveStatsStore } from '@/stores/liveStats'
import { NCard, NGrid, NGridItem, NStatistic, NSpace, NTag, NSpin, NEmpty, NButton, NSelect } from 'naive-ui'

const { t, locale } = useI18n()
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const router = useRouter()
const authStore = useAuthStore()
const liveStatsStore = useLiveStatsStore()
const stats = ref({ userCount: 0, menuCount: 0, operLogCount: 0, jobCount: 0, deptCount: 0, roleCount: 0, configCount: 0, noticeCount: 0, todayLoginCount: 0, todayLoginFailCount: 0, weeklyOps: [], bizTypeDist: [] })
const bizStats = ref({ todayOrders: 0, todayRevenue: 0, pendingPay: 0, pendingShip: 0, lowStock: 0, newUsers: 0, totalProducts: 0 })
const newOrderAlert = ref(false); const newOrderCount = ref(0); let orderPollTimer: any = null
let lastOrderCount = 0
const greeting = computed(() => {
  const h = new Date().getHours()
  const name = authStore.username || ''
  if (h < 6) return t('dashboard.night', { name })
  if (h < 12) return t('dashboard.morning', { name })
  if (h < 18) return t('dashboard.afternoon', { name })
  return t('dashboard.evening', { name })
})
const sysInfo = ref<any>(null)
const loading = ref(true)
const lastUpdated = ref('')
const recentNotices = ref<any[]>([])
const showQuickEdit = ref(false)
const newQuick = ref('')
const allQuickOptions = computed(() => [
  {label:`📄 ${t('nav.swagger')}`,value:'/swagger'},{label:`⚡ ${t('nav.generator')}`,value:'/generator'},{label:`📖 ${t('nav.dict')}`,value:'/dict'},
  {label:`🖥 ${t('nav.monitor')}`,value:'/monitor'},{label:`🔧 ${t('nav.maintenance')}`,value:'/maintenance'},{label:`👥 ${t('nav.users')}`,value:'/users'},
  {label:`📋 ${t('nav.menus')}`,value:'/menus'},{label:`📊 ${t('nav.operlog')}`,value:'/operlog'},{label:`⏰ ${t('nav.jobs')}`,value:'/job'},
  {label:`🔑 ${t('nav.roles')}`,value:'/roles'},{label:`🏢 ${t('nav.departments')}`,value:'/departments'},{label:`📢 ${t('nav.notice')}`,value:'/notices'},
])
const defaultQuickLinks = computed(() => [
  {path:'/swagger',label:t('nav.swagger'),icon:'📄',type:'info'},{path:'/generator',label:t('nav.generator'),icon:'⚡',type:'success'},
  {path:'/dict',label:t('nav.dict'),icon:'📖',type:'warning'},{path:'/monitor',label:t('nav.monitor'),icon:'🖥',type:'info'},
  {path:'/maintenance',label:t('nav.maintenance'),icon:'🔧',type:'error'},
])
const quickLinks = ref<{path:string,label:string,icon:string,type:string}[]>(
  JSON.parse(localStorage.getItem('quickLinks') || 'null') || defaultQuickLinks.value
)

function addQuick() {
  if (!newQuick.value) return
  const opt = allQuickOptions.value.find(o => o.value === newQuick.value)
  if (opt && !quickLinks.value.find(q => q.path === newQuick.value)) {
    quickLinks.value.push({path:newQuick.value, label:opt.label.replace(/^[^\s]+\s/,''), icon:opt.label.substring(0,2), type:'info'})
    localStorage.setItem('quickLinks', JSON.stringify(quickLinks.value))
  }
  newQuick.value = ''
}
function removeQuick(path: string) {
  quickLinks.value = quickLinks.value.filter(q => q.path !== path)
  localStorage.setItem('quickLinks', JSON.stringify(quickLinks.value))
}
const recentLogs = ref<any[]>([])
const barOption = ref<any>({})
const pieOption = ref<any>({})
const hasWeeklyData = computed(() => (stats.value.weeklyOps || []).length > 0)
const hasBizData = computed(() => (stats.value.bizTypeDist || []).length > 0)

function updateTimestamp() {
  const loc = locale.value === 'zh' ? 'zh-CN' : 'en-US'
  lastUpdated.value = new Date().toLocaleTimeString(loc, { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

async function refreshDashboard() {
  try {
    const [res, sysRes, logRes, noticeRes] = await Promise.all([
      fetchDashboard(),
      fetchSystemInfo().catch(() => ({ data: null })),
      fetchRecentLogs({ page: 1, pageSize: 5 }).catch(() => ({ data: { items: [] } })),
      fetchRecentNotices().catch(() => ({ data: [] })),
    ])
    stats.value = res.data
    barOption.value.xAxis.data = res.data.weeklyOps.map((d: any) => d.date)
    barOption.value.series[0].data = res.data.weeklyOps.map((d: any) => d.count)
    pieOption.value.series[0].data = res.data.bizTypeDist
    sysInfo.value = sysRes.data
    recentLogs.value = logRes.data.items || []
    recentNotices.value = noticeRes.data || []
    try {
      const [d, r] = await Promise.all([fetchDepartments().catch(()=>({data:[]})), fetchRoles().catch(()=>({data:[]}))])
      stats.value.deptCount = Array.isArray(d.data) ? d.data.length : 0
      stats.value.roleCount = Array.isArray(r.data) ? r.data.length : 0
    } catch { /* ignore */ }
    try {
      const [s, o, inv] = await Promise.all([
        import('@/api/analytics').then(m => m.fetchSalesOverview().catch(()=>({data:{}}))),
        import('@/api/order').then(m => m.fetchOrders().catch(()=>({data:[]}))),
        import('@/api/inventory').then(m => m.fetchInventory().catch(()=>({data:[]}))),
      ])
      bizStats.value.todayOrders = s.data?.todayOrders || 0
      bizStats.value.todayRevenue = s.data?.todayRevenue || 0
      const orders = o.data || []
      bizStats.value.pendingPay = orders.filter((x:any) => x.status === '0').length
      bizStats.value.pendingShip = orders.filter((x:any) => x.status === '1').length
      const invData = inv.data || []
      bizStats.value.lowStock = invData.filter((x:any) => x.quantity <= (x.minQuantity || 5)).length
      bizStats.value.totalProducts = invData.length
      bizStats.value.newUsers = s.data?.newUsers || 0
    } catch { /* ignore */ }
    updateTimestamp()
  } catch { /* ignore */ }
}

function pollNewOrders() {
  import('@/api/order').then(m => m.fetchOrders().then(r => {
    const orders = r.data || []
    const count = orders.length
    if (lastOrderCount > 0 && count > lastOrderCount) {
      newOrderCount.value = count - lastOrderCount
      newOrderAlert.value = true
      setTimeout(() => { newOrderAlert.value = false }, 5000)
    }
    lastOrderCount = count
  }).catch(() => {}))
}
onMounted(async () => {
  pollNewOrders()
  orderPollTimer = setInterval(pollNewOrders, 30000)
  try {
    const res = await fetchDashboard()
    stats.value = res.data
    barOption.value = {
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: res.data.weeklyOps.map((d: any) => d.date) },
      yAxis: { type: 'value' },
      series: [{ data: res.data.weeklyOps.map((d: any) => d.count), type: 'bar', itemStyle: { color: '#667eea', borderRadius: [4,4,0,0] } }]
    }
    pieOption.value = {
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie', radius: ['40%', '70%'], center: ['50%', '45%'],
        data: res.data.bizTypeDist,
        label: { show: true, formatter: '{b}\n{d}%' },
        itemStyle: { borderRadius: 4 }
      }]
    }
    const [sysRes, logRes, noticeRes] = await Promise.all([
      fetchSystemInfo().catch(() => ({ data: null })),
      fetchRecentLogs({ page: 1, pageSize: 5 }).catch(() => ({ data: { items: [] } })),
      fetchRecentNotices().catch(() => ({ data: [] })),
    ])
    sysInfo.value = sysRes.data
    recentLogs.value = logRes.data.items || []
    recentNotices.value = noticeRes.data || []
    updateTimestamp()

    _es = new EventSource('/api/sse/dashboard')
    _es.addEventListener('stats', (e) => {
      const sseData = JSON.parse(e.data)
      if (sysInfo.value) {
        sysInfo.value.heapUsed = sseData.heapUsed
        sysInfo.value.usedMemory = sseData.usedMemory
        sysInfo.value.activeSessions = sseData.activeSessions
        sysInfo.value = { ...sysInfo.value }
      }
      if (sseData.activeSessions) liveStatsStore.setSessions(Number(sseData.activeSessions) || 0)
    })
    _timer = setInterval(refreshDashboard, 60000)
  } catch { /* ignore */ } finally { loading.value = false }
})
</script>

<template>
  <n-spin :show="loading">
    <div>
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <span style="font-size:18px;font-weight:600;color:var(--n-text-color, #333)">{{ greeting }}</span>
        <n-space>
          <n-button size="small" @click="router.push('/users')">{{ t('dashboard.addUser') }}</n-button>
          <span v-if="lastUpdated" style="font-size:12px;color:#999;margin-right:8px">{{ t('dashboard.lastUpdated', { time: lastUpdated }) }}</span>
          <n-button size="small" @click="refreshDashboard">{{ t('common.refresh') }}</n-button>
        </n-space>
      </div>

      <div v-if="newOrderAlert" style="background:linear-gradient(135deg,#fff5f5,#ffe8e8);border:1px solid #f10215;border-radius:8px;padding:12px 16px;margin-bottom:12px;display:flex;align-items:center;justify-content:space-between;animation:fadeIn .3s">
        <span>🔔 <b>{{ newOrderCount }} 笔新订单</b>，请及时处理</span>
        <n-button size="tiny" @click="newOrderAlert=false;router.push('/orders')" type="error">查看订单</n-button>
      </div>
      <!-- 业务 KPI -->
      <n-grid cols="6" x-gap="12" y-gap="12" responsive="screen" style="margin-bottom:12px">
        <n-grid-item span="6 m:3 l:2">
          <n-card size="small" style="border-left:3px solid #18a058"><n-statistic label="今日订单"><template #prefix>📦</template>{{ bizStats.todayOrders }}</n-statistic></n-card>
        </n-grid-item>
        <n-grid-item span="6 m:3 l:2">
          <n-card size="small" style="border-left:3px solid #2080f0"><n-statistic label="今日营收"><template #prefix>💰</template><template #suffix>元</template>{{ ((bizStats.todayRevenue || 0)/100).toFixed(0) }}</n-statistic></n-card>
        </n-grid-item>
        <n-grid-item span="6 m:3 l:2">
          <n-card size="small" style="border-left:3px solid #f0a020;cursor:pointer" @click="router.push('/orders')"><n-statistic label="待处理"><template #prefix>⏳</template>{{ bizStats.pendingPay + bizStats.pendingShip }}<template #suffix><span style="font-size:11px;color:#999">待付{{bizStats.pendingPay}}/待发{{bizStats.pendingShip}}</span></template></n-statistic></n-card>
        </n-grid-item>
        <n-grid-item span="6 m:3 l:2">
          <n-card size="small" style="border-left:3px solid #d03050;cursor:pointer" @click="router.push('/inventory')"><n-statistic label="库存预警"><template #prefix>⚠️</template>{{ bizStats.lowStock }}<template #suffix><span style="font-size:11px;color:#999">/{{bizStats.totalProducts}}件</span></template></n-statistic></n-card>
        </n-grid-item>
        <n-grid-item span="6 m:3 l:2">
          <n-card size="small" style="border-left:3px solid #7c3aed;cursor:pointer" @click="router.push('/products')"><n-statistic label="商品总数"><template #prefix>🏷</template>{{ bizStats.totalProducts }}</n-statistic></n-card>
        </n-grid-item>
        <n-grid-item span="6 m:3 l:2">
          <n-card size="small" style="border-left:3px solid #667eea;cursor:pointer" @click="router.push('/users')"><n-statistic label="新增用户"><template #prefix>👤</template>{{ bizStats.newUsers }}</n-statistic></n-card>
        </n-grid-item>
      </n-grid>

      <!-- 核心统计卡片 -->
      <n-grid cols="8" x-gap="12" y-gap="12" responsive="screen" item-responsive>
        <n-grid-item span="8 m:4 l:2">
          <n-card hoverable size="small" style="cursor:pointer;border-left:3px solid #667eea" @click="router.push('/users')"><n-statistic :label="t('dashboard.users')"><template #prefix><span style="background:#667eea15;padding:4px 8px;border-radius:6px;margin-right:8px">👥</span></template>{{ stats.userCount }}</n-statistic></n-card>
        </n-grid-item>
        <n-grid-item span="8 m:4 l:2">
          <n-card hoverable size="small" style="cursor:pointer;border-left:3px solid #f0a020" @click="router.push('/roles')"><n-statistic :label="t('dashboard.roles')"><template #prefix><span style="background:#f0a02015;padding:4px 8px;border-radius:6px;margin-right:8px">🛡</span></template>{{ stats.roleCount || 0 }}</n-statistic></n-card>
        </n-grid-item>
        <n-grid-item span="8 m:4 l:2">
          <n-card hoverable size="small" style="cursor:pointer;border-left:3px solid #18a058" @click="router.push('/departments')"><n-statistic :label="t('dashboard.departments')"><template #prefix><span style="background:#18a05815;padding:4px 8px;border-radius:6px;margin-right:8px">🏢</span></template>{{ stats.deptCount || 0 }}</n-statistic></n-card>
        </n-grid-item>
        <n-grid-item span="8 m:4 l:2">
          <n-card hoverable size="small" style="cursor:pointer;border-left:3px solid #2080f0" @click="router.push('/posts')"><n-statistic :label="t('dashboard.posts')"><template #prefix><span style="background:#2080f015;padding:4px 8px;border-radius:6px;margin-right:8px">👔</span></template>{{ stats.jobCount > 0 ? t('common.enabled') : '-' }}</n-statistic></n-card>
        </n-grid-item>
        <n-grid-item span="8 m:4 l:2">
          <n-card hoverable size="small" style="cursor:pointer;border-left:3px solid #d03050" @click="router.push('/menus')"><n-statistic :label="t('dashboard.menus')"><template #prefix><span style="background:#d0305015;padding:4px 8px;border-radius:6px;margin-right:8px">📋</span></template>{{ stats.menuCount }}</n-statistic></n-card>
        </n-grid-item>
        <n-grid-item span="8 m:4 l:2">
          <n-card hoverable size="small" style="cursor:pointer;border-left:3px solid #7c3aed" @click="router.push('/operlog')"><n-statistic :label="t('dashboard.operlogs')"><template #prefix><span style="background:#7c3aed15;padding:4px 8px;border-radius:6px;margin-right:8px">📊</span></template>{{ stats.operLogCount }}</n-statistic></n-card>
        </n-grid-item>
        <n-grid-item span="8 m:4 l:2">
          <n-card hoverable size="small" style="cursor:pointer" @click="router.push('/config')"><n-statistic :label="t('dashboard.configs')"><template #prefix>⚙</template>{{ stats.configCount || 0 }}</n-statistic></n-card>
        </n-grid-item>
        <n-grid-item span="8 m:4 l:2">
          <n-card hoverable size="small" style="cursor:pointer" @click="router.push('/notices')"><n-statistic :label="t('dashboard.notices')"><template #prefix>📢</template>{{ stats.noticeCount || 0 }}</n-statistic></n-card>
        </n-grid-item>
      </n-grid>

      <!-- 实时监控卡片 -->
      <n-grid v-if="sysInfo" cols="5" x-gap="12" y-gap="12" style="margin-top:12px" responsive="screen" item-responsive>
        <n-grid-item span="5 m:3 l:1">
          <n-card size="small" :bordered="false" style="background:linear-gradient(135deg,#667eea,#764ba2);color:#fff">
            <n-statistic :label="t('dashboard.onlineUsers')" :value="liveStatsStore.activeSessions || 0">
              <template #prefix>🟢</template>
            </n-statistic>
          </n-card>
        </n-grid-item>
        <n-grid-item span="5 m:3 l:1">
          <n-card size="small" :bordered="false" style="background:linear-gradient(135deg,#4facfe,#00f2fe);color:#fff">
            <n-statistic :label="t('dashboard.uptime')" :value="sysInfo?.uptime || '-'">
              <template #prefix>⏱️</template>
            </n-statistic>
          </n-card>
        </n-grid-item>
        <n-grid-item span="5 m:3 l:1">
          <n-card size="small" :bordered="false" style="background:linear-gradient(135deg,#43e97b,#38f9d7);color:#fff">
            <n-statistic :label="t('dashboard.todayLoginSuccess')" :value="stats.todayLoginCount || 0">
              <template #prefix>✅</template>
            </n-statistic>
          </n-card>
        </n-grid-item>
        <n-grid-item span="5 m:3 l:1">
          <n-card size="small" :bordered="false" style="background:linear-gradient(135deg,#f093fb,#f5576c);color:#fff">
            <n-statistic :label="t('dashboard.heapMemory')" :value="sysInfo?.heapUsed || '-'">
              <template #prefix>💾</template>
              <template #suffix>/ {{ sysInfo?.heapMax || '-' }}</template>
            </n-statistic>
          </n-card>
        </n-grid-item>
        <n-grid-item span="5 m:3 l:1">
          <n-card size="small" :bordered="false" style="background:linear-gradient(135deg,#ff9a9e,#fecfef);color:#fff">
            <n-statistic :label="t('dashboard.todayLoginFail')" :value="stats.todayLoginFailCount || 0">
              <template #prefix>⚠️</template>
            </n-statistic>
          </n-card>
        </n-grid-item>
      </n-grid>

      <!-- 图表区域 -->
      <n-grid cols="2" x-gap="12" style="margin-top:12px" responsive="screen" item-responsive>
        <n-grid-item span="2 m:1">
          <n-card :title="t('dashboard.weeklyOps')" size="small">
            <v-chart v-if="hasWeeklyData" :option="barOption" style="height:260px" autoresize />
            <n-empty v-else :description="t('dashboard.noOpsData')" style="height:260px;display:flex;align-items:center;justify-content:center" />
          </n-card>
        </n-grid-item>
        <n-grid-item span="2 m:1">
          <n-card :title="t('dashboard.bizTypeDist')" size="small">
            <v-chart v-if="hasBizData" :option="pieOption" style="height:260px" autoresize />
            <n-empty v-else :description="t('dashboard.noBizData')" style="height:260px;display:flex;align-items:center;justify-content:center" />
          </n-card>
        </n-grid-item>
      </n-grid>

      <!-- 近期公告 + 系统信息 -->
      <n-grid cols="2" x-gap="12" style="margin-top:12px" responsive="screen" item-responsive>
        <n-grid-item span="2 m:1">
          <n-card :title="t('dashboard.recentNotices')" size="small">
            <template #header-extra>
              <n-button size="small" text @click="router.push('/notices')">{{ t('common.viewAll') }} →</n-button>
            </template>
            <div v-if="recentNotices.length">
              <div v-for="notice in recentNotices" :key="notice.id" style="display:flex;align-items:center;justify-content:space-between;padding:6px 0;border-bottom:1px solid var(--n-border-color, #f0f0f0);font-size:13px">
                <span style="flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ notice.noticeTitle }}</span>
                <span style="color:#999;font-size:11px;margin-left:12px;white-space:nowrap">{{ notice.createdAt ? notice.createdAt.substring(0,10) : '' }}</span>
              </div>
            </div>
            <n-empty v-else :description="t('dashboard.noNotices')" size="small" />
          </n-card>
        </n-grid-item>
        <n-grid-item span="2 m:1">
          <n-card :title="t('monitor.systemInfo')" size="small" v-if="sysInfo">
            <n-space>
              <n-tag type="info" size="small">{{ t('dashboard.cpuCores', { n: sysInfo.availableProcessors }) }}</n-tag>
              <n-tag size="small">OS: {{ sysInfo.osName }}</n-tag>
              <n-tag size="small">Java: {{ sysInfo.javaVersion }}</n-tag>
            </n-space>
          </n-card>
        </n-grid-item>
      </n-grid>

      <!-- 近期操作 -->
      <n-card :title="t('dashboard.recentOps')" style="margin-top:12px" size="small">
        <template #header-extra>
          <n-button size="small" text @click="router.push('/operlog')">{{ t('common.viewAll') }} →</n-button>
        </template>
        <div v-if="recentLogs.length">
          <div v-for="log in recentLogs" :key="log.id" style="display:flex;align-items:center;justify-content:space-between;padding:7px 0;border-bottom:1px solid var(--n-border-color, #f0f0f0);font-size:13px">
            <div style="display:flex;align-items:center;gap:8px">
              <n-tag :type="log.status === 0 ? 'success' : 'error'" size="tiny" :bordered="false">{{ log.status === 0 ? t('operlog.success') : t('operlog.fail') }}</n-tag>
              <span>{{ log.title }}</span>
            </div>
            <span style="color:#999;font-size:12px">{{ log.operName || '-' }} · {{ log.operTime ? log.operTime.substring(0,16).replace('T',' ') : '' }}</span>
          </div>
        </div>
        <n-empty v-else :description="t('dashboard.noOpsLogs')" size="small" />
      </n-card>

      <!-- 快速入口 -->
      <n-card :title="t('dashboard.quickLinks')" style="margin-top:12px" size="small">
        <template #header-extra>
          <n-button size="tiny" @click="showQuickEdit = !showQuickEdit">{{ showQuickEdit ? t('common.done') : t('common.edit') }}</n-button>
        </template>
        <n-space>
          <n-tag v-for="q in quickLinks" :key="q.path" :type="q.type as any" size="medium" style="cursor:pointer" @click="router.push(q.path)" :closable="showQuickEdit" @close="removeQuick(q.path)">{{ q.icon }} {{ q.label }}</n-tag>
        </n-space>
        <n-space v-if="showQuickEdit" style="margin-top:8px">
          <n-select v-model:value="newQuick" :options="allQuickOptions" size="small" style="width:160px" />
          <n-button size="tiny" type="primary" @click="addQuick">{{ t('common.add') }}</n-button>
        </n-space>
      </n-card>
    </div>
  </n-spin>
</template>
