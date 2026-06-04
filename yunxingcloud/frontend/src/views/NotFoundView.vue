<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NConfigProvider, NCard, NButton } from 'naive-ui'

const router = useRouter()
</script>

<template>
  <n-config-provider>
    <div class="not-found-page">
      <n-card class="not-found-card">
        <h1>404</h1>
        <p>页面未找到</p>
        <n-button type="primary" @click="router.push('/')">返回首页</n-button>
      </n-card>
    </div>
  </n-config-provider>
</template>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.not-found-card {
  text-align: center;
  padding: 40px;
}

.not-found-card h1 {
  font-size: 64px;
  color: #ccc;
  margin-bottom: 8px;
}

.not-found-card p {
  color: #888;
  font-size: 16px;
  margin-bottom: 24px;
}
</style>
