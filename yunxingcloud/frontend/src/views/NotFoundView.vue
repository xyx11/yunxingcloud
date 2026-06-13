<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NConfigProvider, NButton, NSpace, NTag } from 'naive-ui'

const router = useRouter()
const links = [
  { label: '首页', path: '/' },
  { label: '用户管理', path: '/users' },
  { label: '角色管理', path: '/roles' },
  { label: '菜单管理', path: '/menus' },
  { label: '系统监控', path: '/operlog' },
]
</script>

<template>
  <n-config-provider>
    <div class="page">
      <div class="container">
        <div class="code">404</div>
        <div class="divider" />
        <div class="title">页面未找到</div>
        <div class="subtitle">您访问的页面不存在或已被移除</div>
        <div style="margin: 28px 0 20px;">
          <n-space justify="center" size="small">
            <n-tag
              v-for="link in links" :key="link.path"
              type="info" size="medium" style="cursor:pointer"
              @click="router.push(link.path)"
            >
              {{ link.label }}
            </n-tag>
          </n-space>
        </div>
        <n-button type="primary" size="large" @click="router.push('/')">
          返回首页
        </n-button>
      </div>
    </div>
  </n-config-provider>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.container { text-align: center; }
.code {
  font-size: 120px;
  font-weight: 700;
  color: rgba(255,255,255,0.9);
  line-height: 1;
}
.divider {
  width: 60px;
  height: 3px;
  background: rgba(255,255,255,0.5);
  margin: 16px auto;
  border-radius: 2px;
}
.title { font-size: 22px; color: #fff; font-weight: 500; }
.subtitle { font-size: 14px; color: rgba(255,255,255,0.7); margin-top: 8px; }
</style>
