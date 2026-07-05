<script setup lang="ts">
import { useRouter } from 'vue-router'
import JdButton from '@/components/JdButton.vue'
const router = useRouter()
</script>
<template>
  <div class="nf-page">
    <div class="nf-code">404</div>
    <h2 class="nf-title">页面未找到</h2>
    <p class="nf-desc">您访问的页面不存在或已被移除</p>
    <JdButton size="lg" @click="router.push('/')">返回首页</JdButton>
  </div>
</template>
<style scoped>
.nf-page { text-align: center; padding: 80px 20px; }
.nf-code { font-size: 80px; font-weight: 800; color: var(--jd-red); margin-bottom: 16px; line-height: 1; }
.nf-title { font-size: var(--font-xl); color: var(--text-primary); margin-bottom: 8px; }
.nf-desc { color: var(--text-tertiary); font-size: var(--font-md); margin-bottom: 24px; }
</style>
