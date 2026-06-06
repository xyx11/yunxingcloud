<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { NCard, NGrid, NGridItem, NTag, NButton, NSpace, NDataTable, NStatistic } from 'naive-ui'

const sysInfo = ref<any>({})
const sessions = ref<any[]>([])
const cacheInfo = ref<any>({})
const health = ref<any>({})
const dbHealth = ref<any>({})
const diskHealth = ref<any>({})
const benchmark = ref<any>({})

async function loadAll() {
  const [info, sess, cache, db, disk, bench] = await Promise.all([
    request.get('/api/system/info').catch(() => ({ data: {} })),
    request.get('/api/system/sessions').catch(() => ({ data: { sessions: [], count: 0 } })),
    request.get('/api/system/cache').catch(() => ({ data: {} })),
    request.get('/api/health/db').catch(() => ({ data: {} })),
    request.get('/api/health/disk').catch(() => ({ data: {} })),
    request.get('/api/system/benchmark').catch(() => ({ data: {} })),
  ])
  sysInfo.value = info.data; sessions.value = sess.data.sessions || []
  cacheInfo.value = cache.data; dbHealth.value = db.data; diskHealth.value = disk.data
  benchmark.value = bench.data
}

async function clearCache() {
  await request.post('/api/system/cache/clear')
  loadAll()
}

onMounted(loadAll)
</script>

<template>
  <div style="padding:24px">
    <n-grid cols="4" x-gap="16" y-gap="16" responsive="screen">
      <n-grid-item span="4 m:2 l:1"><n-card><n-statistic label="运行时长" :value="sysInfo.uptime || '-'" /></n-card></n-grid-item>
      <n-grid-item span="4 m:2 l:1"><n-card><n-statistic label="在线用户" :value="sessions.length" /></n-card></n-grid-item>
      <n-grid-item span="4 m:2 l:1"><n-card><n-statistic label="堆内存" :value="sysInfo.heapUsed || '-'" /></n-card></n-grid-item>
      <n-grid-item span="4 m:2 l:1"><n-card><n-statistic label="DB查询" :value="benchmark.db_ms ? benchmark.db_ms+'ms' : '-'" /></n-card></n-grid-item>
    </n-grid>

    <n-grid cols="2" x-gap="16" style="margin-top:16px" responsive="screen">
      <n-grid-item span="2 m:1">
        <n-card title="系统信息">
          <n-space vertical>
            <span>OS: {{ sysInfo.osName }}</span>
            <span>Java: {{ sysInfo.javaVersion }}</span>
            <span>启动: {{ sysInfo.startTime }}</span>
            <span>CPU: {{ sysInfo.availableProcessors }}核</span>
            <span>最大内存: {{ sysInfo.maxMemory }}</span>
          </n-space>
        </n-card>
      </n-grid-item>
      <n-grid-item span="2 m:1">
        <n-card title="健康状态">
          <n-space vertical>
            <n-space><n-tag :type="dbHealth.status === 'UP' ? 'success' : 'error'" size="small">数据库</n-tag><span>{{ dbHealth.status || '?' }}</span></n-space>
            <n-space><n-tag :type="diskHealth.status === 'UP' ? 'success' : 'warning'" size="small">磁盘</n-tag><span>{{ diskHealth.freeMB || '?' }}MB 可用</span></n-space>
            <n-space>
              <n-tag type="info" size="small">缓存</n-tag>
              <n-button size="tiny" @click="clearCache">清除缓存</n-button>
            </n-space>
          </n-space>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="在线会话 ({{ sessions.length }})" style="margin-top:16px">
      <n-data-table v-if="sessions.length" :columns="[{title:'用户',key:'username'},{title:'Token',key:'token'},{title:'登录时间',key:'createdAt'}]" :data="sessions" size="small" :pagination="{pageSize:10}" :row-key="(r:any)=>r.token" />
      <span v-else style="color:#999;">暂无活跃会话</span>
    </n-card>
  </div>
</template>
