<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NCard, NGrid, NGridItem, NStatistic, NButton, NSpace, NPopconfirm, NInputNumber } from 'naive-ui'

const notify = useNotify()
const stats = ref<any>({})
const cleanDays = ref(90)

async function loadStats() {
  try {
    const res = await request.get('/api/maintenance/stats')
    stats.value = res.data
  } catch {}
}

async function cleanLogs() {
  try {
    await request.post(`/api/maintenance/clean-logs?days=${cleanDays.value}`)
    notify.success(`已清理 ${cleanDays.value} 天前的操作日志`)
    await loadStats()
  } catch { notify.error('清理失败') }
}

async function cleanTokens() {
  try {
    await request.post('/api/maintenance/clean-tokens')
    notify.success('已清理过期的密码重置令牌')
    await loadStats()
  } catch { notify.error('清理失败') }
}

async function vacuum() {
  try {
    await request.post('/api/maintenance/vacuum')
    notify.success('整体清理完成')
    await loadStats()
  } catch { notify.error('清理失败') }
}

onMounted(loadStats)
</script>

<template>
  <div style="padding:20px">
    <n-card title="数据维护">
      <n-grid cols="4" x-gap="16" y-gap="16" responsive="screen">
        <n-grid-item span="4 m:2 l:1">
          <n-card size="small"><n-statistic label="用户数" :value="stats.userCount ?? '-'" /></n-card>
        </n-grid-item>
        <n-grid-item span="4 m:2 l:1">
          <n-card size="small"><n-statistic label="角色数" :value="stats.roleCount ?? '-'" /></n-card>
        </n-grid-item>
        <n-grid-item span="4 m:2 l:1">
          <n-card size="small"><n-statistic label="菜单数" :value="stats.menuCount ?? '-'" /></n-card>
        </n-grid-item>
        <n-grid-item span="4 m:2 l:1">
          <n-card size="small"><n-statistic label="日志数" :value="stats.operLogCount ?? '-'" /></n-card>
        </n-grid-item>
      </n-grid>
    </n-card>

    <n-card title="日志清理" style="margin-top:16px">
      <n-space align="center">
        <span>清理</span>
        <n-input-number v-model:value="cleanDays" :min="1" :max="365" style="width:100px" size="small" />
        <span>天之前的操作日志</span>
        <n-popconfirm @positive-click="cleanLogs">
          <template #trigger><n-button type="warning" size="small">执行清理</n-button></template>
          确认删除 {{ cleanDays }} 天前的操作日志?
        </n-popconfirm>
      </n-space>
    </n-card>

    <n-card title="令牌清理" style="margin-top:16px">
      <n-space align="center">
        <span>清理所有过期的密码重置令牌</span>
        <n-popconfirm @positive-click="cleanTokens">
          <template #trigger><n-button type="warning" size="small">清理令牌</n-button></template>
          确认清理所有过期的密码重置令牌?
        </n-popconfirm>
      </n-space>
    </n-card>

    <n-card title="整体清理" style="margin-top:16px">
      <n-space align="center">
        <span>执行全部清理操作（清理90天前日志 + 过期令牌）</span>
        <n-popconfirm @positive-click="vacuum">
          <template #trigger><n-button type="error" size="small">一键清理</n-button></template>
          确认执行全部清理操作? 此操作不可撤销!
        </n-popconfirm>
      </n-space>
    </n-card>
  </div>
</template>
