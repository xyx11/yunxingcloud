<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import request from '@/api/request'
import { NCard, NGrid, NGridItem, NStatistic, NSpace, NTag } from 'naive-ui'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const stats = ref({ userCount: 0, menuCount: 0, operLogCount: 0, jobCount: 0, weeklyOps: [], bizTypeDist: [] })
const sysInfo = ref<any>(null)
const loading = ref(true)

const barOption = ref({})
const pieOption = ref({})

onMounted(async () => {
  try {
    const res = await request.get('/api/stats/dashboard')
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
    const sysRes = await request.get('/api/system/info').catch(() => ({ data: null }))
    sysInfo.value = sysRes.data
  } catch {} finally { loading.value = false }
})
</script>

<template>
  <div>
    <n-grid cols="4" x-gap="16" y-gap="16" responsive="screen" item-responsive>
      <n-grid-item span="4 m:2 l:1">
        <n-card hoverable><n-statistic label="用户数"><template #prefix>👥</template>{{ stats.userCount }}</n-statistic></n-card>
      </n-grid-item>
      <n-grid-item span="4 m:2 l:1">
        <n-card hoverable><n-statistic label="菜单数"><template #prefix>📋</template>{{ stats.menuCount }}</n-statistic></n-card>
      </n-grid-item>
      <n-grid-item span="4 m:2 l:1">
        <n-card hoverable><n-statistic label="操作日志"><template #prefix>📊</template>{{ stats.operLogCount }}</n-statistic></n-card>
      </n-grid-item>
      <n-grid-item span="4 m:2 l:1">
        <n-card hoverable><n-statistic label="定时任务"><template #prefix>⏰</template>{{ stats.jobCount }}</n-statistic></n-card>
      </n-grid-item>
    </n-grid>

    <n-grid cols="2" x-gap="16" style="margin-top:16px" responsive="screen" item-responsive>
      <n-grid-item span="2 m:1">
        <n-card title="近7天操作量">
          <v-chart :option="barOption" style="height:260px" autoresize />
        </n-card>
      </n-grid-item>
      <n-grid-item span="2 m:1">
        <n-card title="操作类型分布">
          <v-chart :option="pieOption" style="height:260px" autoresize />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="系统信息" style="margin-top:16px" v-if="sysInfo">
      <n-space>
        <n-tag type="info" size="small">运行: {{ sysInfo.uptime }}</n-tag>
        <n-tag type="info" size="small">CPU: {{ sysInfo.availableProcessors }}核</n-tag>
        <n-tag type="warning" size="small">堆内存: {{ sysInfo.heapUsed }} / {{ sysInfo.heapMax }}</n-tag>
        <n-tag size="small">OS: {{ sysInfo.osName }}</n-tag>
        <n-tag size="small">Java: {{ sysInfo.javaVersion }}</n-tag>
      </n-space>
    </n-card>

    <n-card title="快速入口" style="margin-top:16px">
      <n-space>
        <n-tag type="info" size="medium">🔄 OAuth2 / OIDC</n-tag>
        <n-tag type="success" size="medium">📄 Knife4j 文档</n-tag>
        <n-tag type="warning" size="medium">🔐 JWT Bearer</n-tag>
        <n-tag type="error" size="medium">⚡ Caffeine Cache</n-tag>
        <n-tag type="info" size="medium">🐳 Docker Ready</n-tag>
      </n-space>
    </n-card>
  </div>
</template>
