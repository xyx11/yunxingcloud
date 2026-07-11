<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, onBeforeUnmount } from 'vue'
const { t, locale } = useI18n()
import { fetchDashboard } from '@/api/stats'
import { fetchSystemInfo, fetchSessions } from '@/api/system'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart, PieChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
use([BarChart, PieChart, LineChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer])

const stats = ref<any>({})
const time = ref('')
const barOption = ref<any>({})
const pieOption = ref<any>({})
const lineOption = ref<any>({})

let timer: ReturnType<typeof setInterval>

function updateTime() {
  const loc = locale.value === 'zh' ? 'zh-CN' : 'en-US'
  time.value = new Date().toLocaleString(loc, { hour12: false })
}

async function fetchData() {
  try {
    const [dash, info, sess] = await Promise.all([
      fetchDashboard(),
      fetchSystemInfo().catch(()=>({data:{}})),
      fetchSessions().catch(()=>({data:{count:0}})),
    ])
    stats.value = { ...dash.data, sessions: sess.data.count, ...info.data }
    barOption.value = {
      tooltip: { trigger: 'axis' }, grid: { left: 50, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: dash.data.weeklyOps?.map((d:any)=>d.date)||[] },
      yAxis: { type: 'value' },
      series: [{ data: dash.data.weeklyOps?.map((d:any)=>d.count)||[], type: 'bar', itemStyle: { color: '#667eea', borderRadius: [6,6,0,0] } }]
    }
    pieOption.value = {
      tooltip: { trigger: 'item' }, legend: { bottom: 0, textStyle: { color: '#ccc' } },
      series: [{
        type: 'pie', radius: ['45%','75%'], center: ['50%','45%'],
        data: dash.data.bizTypeDist||[], label: { show: true, formatter: '{b}\n{d}%', color: '#999' },
        itemStyle: { borderRadius: 4 }
      }]
    }
    if (dash.data.loginTrend) {
      lineOption.value = {
        tooltip: { trigger: 'axis' }, grid: { left: 50, right: 20, top: 20, bottom: 30 },
        xAxis: { type: 'category', data: dash.data.loginTrend.map((d:any)=>d.date) },
        yAxis: { type: 'value' },
        series: [{ data: dash.data.loginTrend.map((d:any)=>d.count), type: 'line', smooth: true, itemStyle: { color: '#43e97b' }, areaStyle: { color: 'rgba(67,233,123,0.15)' } }]
      }
    }
  } catch { /* ignore */ }
  updateTime()
}

onMounted(() => { fetchData(); timer = setInterval(fetchData, 10000) })
onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <div class="screen">
    <header class="screen-header">
      <span class="screen-title">yunxingcloud {{ t('dataScreen.title') }}</span>
      <span class="screen-time">{{ time }}</span>
    </header>

    <div class="stat-row">
      <div class="stat-card" style="--c:#667eea"><span class="stat-val">{{ stats.userCount||0 }}</span><span class="stat-label">{{ t('dataScreen.totalUsers') }}</span></div>
      <div class="stat-card" style="--c:#43e97b"><span class="stat-val">{{ stats.sessions||0 }}</span><span class="stat-label">{{ t('dashboard.onlineUsers') }}</span></div>
      <div class="stat-card" style="--c:#f093fb"><span class="stat-val">{{ stats.todayLoginCount||0 }}</span><span class="stat-label">{{ t('dashboard.todayLoginSuccess') }}</span></div>
      <div class="stat-card" style="--c:#4facfe"><span class="stat-val">{{ stats.operLogCount||0 }}</span><span class="stat-label">{{ t('dashboard.totalOps') }}</span></div>
      <div class="stat-card" style="--c:#ff9a9e"><span class="stat-val">{{ stats.deptCount||0 }}</span><span class="stat-label">{{ t('dataScreen.totalDepts') }}</span></div>
      <div class="stat-card" style="--c:#ffd700"><span class="stat-val">{{ stats.noticeCount||0 }}</span><span class="stat-label">{{ t('dataScreen.totalJobs') }}</span></div>
    </div>

    <div class="chart-row">
      <div class="chart-box"><v-chart :option="barOption" class="chart-h-280" autoresize /></div>
      <div class="chart-box"><v-chart :option="pieOption" class="chart-h-280" autoresize /></div>
    </div>

    <div class="chart-row" v-if="lineOption.series">
      <div class="chart-box full"><v-chart :option="lineOption" class="chart-h-200" autoresize /></div>
    </div>
  </div>
</template>

<style scoped>
.screen {
  min-height: 100vh; background: #0a0e27; color: #fff;
  padding: 20px 40px; font-family: 'PingFang SC', sans-serif;
}
.screen-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 0; border-bottom: 1px solid rgba(255,255,255,0.1); margin-bottom: 24px;
}
.screen-title { font-size: 28px; font-weight: 700; letter-spacing: 2px; background: linear-gradient(90deg,#667eea,#43e97b); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.screen-time { font-size: 16px; color: #8894b0; }
.stat-row { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card {
  background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06);
  border-radius: 12px; padding: 20px; text-align: center;
  border-top: 3px solid var(--c);
}
.stat-val { display: block; font-size: 36px; font-weight: 700; color: var(--c); }
.stat-label { display: block; font-size: 13px; color: #8894b0; margin-top: 4px; }
.chart-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
.chart-box { background: rgba(255,255,255,0.02); border-radius: 12px; padding: 16px; border: 1px solid rgba(255,255,255,0.04); }
.chart-box.full { grid-column: 1/-1; }
@media (max-width: 1024px) { .stat-row { grid-template-columns: repeat(3, 1fr); } .chart-row { grid-template-columns: 1fr; } }
.chart-h-280 { height: 280px; }
.chart-h-200 { height: 200px; }
</style>