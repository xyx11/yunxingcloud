<script setup lang="ts">
import { h } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getCsrfToken } from '@/composables/useCsrf'
import {
  NConfigProvider, NLayout, NLayoutHeader, NLayoutContent,
  NCard, NTable, NButton, NCode,
} from 'naive-ui'

const authStore = useAuthStore()

const endpoints = [
  { key: 1, name: '授权端点', path: '/oauth2/authorize', desc: 'OAuth2 授权码流程入口' },
  { key: 2, name: 'Token 端点', path: '/oauth2/token', desc: '换取 access_token / refresh_token' },
  { key: 3, name: 'JWKS 端点', path: '/oauth2/jwks', desc: 'RSA 公钥，验证 JWT 签名' },
  { key: 4, name: 'UserInfo', path: '/userinfo', desc: 'OIDC 用户信息端点' },
  { key: 5, name: '吊销端点', path: '/oauth2/revoke', desc: '吊销 Token' },
  { key: 6, name: 'Discovery', path: '/.well-known/openid-configuration', desc: 'OIDC 自动发现' },
]

const columns = [
  { title: '端点', key: 'name', width: 120 },
  { title: '路径', key: 'path', width: 240, render: (row: any) => h(NCode, {}, { default: () => row.path }) },
  { title: '说明', key: 'desc' },
]

function handleLogout() {
  const form = document.createElement('form')
  form.method = 'POST'
  form.action = '/logout'
  const csrfInput = document.createElement('input')
  csrfInput.type = 'hidden'
  csrfInput.name = '_csrf'
  csrfInput.value = getCsrfToken()
  form.appendChild(csrfInput)
  document.body.appendChild(form)
  form.submit()
}
</script>

<template>
  <n-config-provider>
    <n-layout>
      <n-layout-header class="header">
        <h1>yunxingcloud SSO 认证中心</h1>
        <div class="user-info">
          <span>{{ authStore.username }}</span>
          <n-button text type="warning" @click="handleLogout">退出</n-button>
        </div>
      </n-layout-header>
      <n-layout-content class="content">
        <n-card title="欢迎使用 SSO 认证中心" class="card">
          <p>您已成功登录。本系统提供 OAuth 2.0 / OpenID Connect 单点登录服务，子服务可通过标准 OIDC 协议接入认证。</p>
        </n-card>
        <n-card title="OAuth2 / OIDC 端点" class="card">
          <n-table :columns="columns" :data="endpoints" />
        </n-card>
      </n-layout-content>
    </n-layout>
  </n-config-provider>
</template>

<style scoped>
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 0 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 56px;
}

.header h1 {
  font-size: 18px;
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
}

.content {
  max-width: 860px;
  margin: 32px auto;
  padding: 0 20px;
}

.card {
  margin-bottom: 20px;
}

.card p {
  color: #666;
  font-size: 14px;
  line-height: 1.8;
}

:deep(.n-data-table-th) {
  font-weight: 500;
}
</style>
