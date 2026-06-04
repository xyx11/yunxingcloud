<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import request from '@/api/request'
import { NCard, NGrid, NGridItem, NStatistic, NTable, NCode, NSpace } from 'naive-ui'

const stats = ref([
  { label: '用户数', value: 0, color: '#667eea', icon: '👥' },
  { label: '部门数', value: 0, color: '#07C160', icon: '🏢' },
  { label: '角色数', value: 0, color: '#FF9800', icon: '🔑' },
  { label: '菜单数', value: 0, color: '#F44336', icon: '📋' },
])

const endpoints = [
  { key: 1, name: '授权端点', path: '/oauth2/authorize', desc: 'OAuth2 授权码流程' },
  { key: 2, name: 'Token 端点', path: '/oauth2/token', desc: '获取 access_token' },
  { key: 3, name: 'JWKS 端点', path: '/oauth2/jwks', desc: 'RSA 公钥' },
  { key: 4, name: 'UserInfo', path: '/userinfo', desc: 'OIDC 用户信息' },
  { key: 5, name: 'Discovery', path: '/.well-known/openid-configuration', desc: 'OIDC 自动发现' },
]

const columns = [
  { title: '端点', key: 'name', width: 110 },
  { title: '路径', key: 'path', width: 250, render: (row: any) => h(NCode, {}, { default: () => row.path }) },
  { title: '说明', key: 'desc' },
]

onMounted(async () => {
  try {
    const [users, depts, roles, menus] = await Promise.all([
      request.get('/api/users').catch(() => ({ data: [] })),
      request.get('/api/departments').catch(() => ({ data: [] })),
      request.get('/api/roles').catch(() => ({ data: [] })),
      request.get('/api/menus').catch(() => ({ data: [] })),
    ])
    stats.value[0].value = Array.isArray(users.data) ? users.data.length : 0
    stats.value[1].value = countTree(depts.data)
    stats.value[2].value = Array.isArray(roles.data) ? roles.data.length : 0
    stats.value[3].value = Array.isArray(menus.data) ? menus.data.length : 0
  } catch {}
})

function countTree(nodes: any[]): number {
  let count = 0
  for (const n of nodes) { count++; if (n.children) count += countTree(n.children) }
  return count
}
</script>

<template>
  <div>
    <n-grid cols="4" x-gap="16" y-gap="16" responsive="screen" item-responsive>
      <n-grid-item span="4 m:2 l:1">
        <n-card hoverable>
          <n-statistic :label="stats[0].label">
            <template #prefix><span style="font-size:24px;">👥</span></template>
            {{ stats[0].value }}
          </n-statistic>
        </n-card>
      </n-grid-item>
      <n-grid-item span="4 m:2 l:1">
        <n-card hoverable>
          <n-statistic :label="stats[1].label">
            <template #prefix><span style="font-size:24px;">🏢</span></template>
            {{ stats[1].value }}
          </n-statistic>
        </n-card>
      </n-grid-item>
      <n-grid-item span="4 m:2 l:1">
        <n-card hoverable>
          <n-statistic :label="stats[2].label">
            <template #prefix><span style="font-size:24px;">🔑</span></template>
            {{ stats[2].value }}
          </n-statistic>
        </n-card>
      </n-grid-item>
      <n-grid-item span="4 m:2 l:1">
        <n-card hoverable>
          <n-statistic :label="stats[3].label">
            <template #prefix><span style="font-size:24px;">📋</span></template>
            {{ stats[3].value }}
          </n-statistic>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="系统信息" style="margin-top:16px;">
      <n-space vertical>
        <div style="color:#666;font-size:13px;">版本: yunxingcloud v1.0.0</div>
        <div style="color:#666;font-size:13px;">架构: Spring Boot 4.0 + OAuth2 + JPA + Vue 3</div>
        <div style="color:#666;font-size:13px;">OAuth2 / OIDC 端点列表:</div>
      </n-space>
      <n-table :columns="columns" :data="endpoints" style="margin-top:8px;" size="small" />
    </n-card>
  </div>
</template>
