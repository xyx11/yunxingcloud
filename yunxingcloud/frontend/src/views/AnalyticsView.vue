<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NGrid, NGridItem, NStatistic, NDataTable, NSpace, NButton, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchSalesOverview, fetchOrderTrend, fetchTopProducts, fetchRevenueOverview, fetchDailyRevenue } from '@/api/analytics'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
use([BarChart, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const overview = ref<any>({})
const orderTrend = ref<any[]>([])
const topProducts = ref<any[]>([])
const dailyRevenue = ref<any[]>([])
const loading = ref(false)

const trendOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: 50, right: 20, top: 10, bottom: 30 },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value' },
  series: [
    { name: '订单数', data: [], type: 'line', smooth: true, itemStyle: { color: '#667eea' }, areaStyle: { color: 'rgba(102,126,234,0.1)' } },
  ],
})

const revenueOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: 60, right: 20, top: 10, bottom: 30 },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
  series: [
    { name: '营收', data: [], type: 'bar', itemStyle: { color: '#18a058', borderRadius: [3, 3, 0, 0] } },
  ],
})

const topColumns: DataTableColumns<any> = [
  { title: '排名', key: 'rank', width: 50, render: (_: any, i: number) => i + 1 },
  { title: '商品', key: 'productName', width: 200, ellipsis: { tooltip: true } },
  { title: '销量', key: 'sales', width: 80 },
  { title: '营收', key: 'revenue', width: 100, render: (r: any) => '¥' + ((r.revenue || 0) / 100).toFixed(2) },
]

async function load() {
  loading.value = true
  try {
    const [s, t, p, r, d] = await Promise.all([
      fetchSalesOverview().catch(() => ({ data: {} })),
      fetchOrderTrend().catch(() => ({ data: [] })),
      fetchTopProducts(10).catch(() => ({ data: [] })),
      fetchRevenueOverview().catch(() => ({ data: {} })),
      fetchDailyRevenue().catch(() => ({ data: [] })),
    ])
    overview.value = s.data || {}
    orderTrend.value = t.data || []
    topProducts.value = p.data || []
    dailyRevenue.value = d.data || {}

    // Build trend chart
    const labels = (orderTrend.value as any[]).map((x: any) => x.date || '')
    const counts = (orderTrend.value as any[]).map((x: any) => x.count || 0)
    trendOption.value.xAxis.data = labels
    trendOption.value.series[0].data = counts
    trendOption.value = { ...trendOption.value }

    // Build revenue chart
    const revData = d.data || {}
    const dates = Object.keys(revData).sort()
    const values = dates.map(k => (revData[k] || 0) / 100)
    revenueOption.value.xAxis.data = dates
    revenueOption.value.series[0].data = values
    revenueOption.value = { ...revenueOption.value }
  } catch { /* errors caught per-request */ }
  loading.value = false
}

onMounted(load)
</script>
<template>
  <div style="padding:20px">
    <n-space vertical size="large">
      <n-grid cols="4" x-gap="12">
        <n-grid-item><n-card size="small"><n-statistic label="今日订单" :value="overview.todayOrders || 0" /></n-card></n-grid-item>
        <n-grid-item><n-card size="small"><n-statistic label="今日营收"><template #suffix>元</template>{{ ((overview.todayRevenue || 0) / 100).toFixed(2) }}</n-statistic></n-card></n-grid-item>
        <n-grid-item><n-card size="small"><n-statistic label="本月订单" :value="overview.monthOrders || 0" /></n-card></n-grid-item>
        <n-grid-item><n-card size="small"><n-statistic label="本月营收"><template #suffix>元</template>{{ ((overview.monthRevenue || 0) / 100).toFixed(2) }}</n-statistic></n-card></n-grid-item>
      </n-grid>
      <n-grid cols="2" x-gap="12">
        <n-grid-item><n-card title="订单趋势" size="small"><v-chart :option="trendOption" style="height:240px" autoresize /></n-card></n-grid-item>
        <n-grid-item><n-card title="日营收" size="small"><v-chart :option="revenueOption" style="height:240px" autoresize /></n-card></n-grid-item>
      </n-grid>
      <n-card title="热销商品 TOP10" size="small">
        <n-dataTable :columns="topColumns" :data="topProducts" :loading="loading" :pagination="false" size="small" />
      </n-card>
    </n-space>
  </div>
</template>
