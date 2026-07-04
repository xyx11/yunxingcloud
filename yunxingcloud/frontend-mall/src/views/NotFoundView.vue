<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>
<template>
  <div style="text-align:center;padding:80px 20px">
    <div style="font-size:80px;font-weight:800;color:#f10215;margin-bottom:16px;line-height:1">404</div>
    <h2 style="font-size:20px;color:#333;margin-bottom:8px">页面未找到</h2>
    <p style="color:#999;font-size:14px;margin-bottom:24px">您访问的页面不存在或已被移除</p>
    <button @click="router.push('/')"
            style="padding:10px 36px;background:#f10215;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:14px;font-weight:600">
      返回首页
    </button>
  </div>
</template>
