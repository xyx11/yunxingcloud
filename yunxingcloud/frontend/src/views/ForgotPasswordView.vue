<script setup lang="ts">
import { ref } from 'vue'
import request from '@/api/request'
import { useRouter } from 'vue-router'
import { NConfigProvider, NCard, NForm, NFormItem, NInput, NButton, NAlert } from 'naive-ui'

const router = useRouter()
const email = ref('')
const loading = ref(false)
const message = ref('')
const error = ref('')
const token = ref('')

async function handleSubmit() {
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    const res = await request.post('/api/password/forgot', { email: email.value })
    if (res.data.success) {
      message.value = res.data.message
      if (res.data.token) {
        token.value = res.data.token
      }
    }
  } catch (e: any) {
    error.value = e.response?.data?.message || '操作失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <n-config-provider>
    <div class="page">
      <n-card class="card">
        <h1 class="title">忘记密码</h1>
        <p class="subtitle">输入注册邮箱，获取重置链接</p>

        <n-alert v-if="error" type="error" :title="error" closable @close="error = ''" style="margin-bottom: 20px" />
        <n-alert v-if="message" type="success" style="margin-bottom: 20px">
          <div>{{ message }}</div>
          <div v-if="token" style="margin-top: 12px; word-break: break-all; font-size: 12px; color: #666;">
            重置令牌: {{ token }}
          </div>
        </n-alert>

        <n-form v-if="!token" @submit.prevent="handleSubmit">
          <n-form-item label="邮箱">
            <n-input v-model:value="email" placeholder="请输入注册邮箱" size="large" type="email" />
          </n-form-item>
          <n-button type="primary" block size="large" :loading="loading" attr-type="submit">
            发送重置链接
          </n-button>
        </n-form>

        <div v-if="token" style="margin-top: 20px; text-align: center;">
          <n-button type="primary" @click="router.push({ path: '/reset-password', query: { token } })">
            前往重置密码
          </n-button>
        </div>

        <div class="back-link">
          <router-link to="/login">返回登录</router-link>
        </div>
      </n-card>
    </div>
  </n-config-provider>
</template>

<style scoped>
.page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.card { width: 420px; border-radius: 12px; padding: 8px; }
.title { text-align: center; color: #333; margin-bottom: 4px; font-size: 24px; }
.subtitle { text-align: center; color: #888; margin-bottom: 28px; font-size: 14px; }
.back-link { text-align: center; margin-top: 16px; font-size: 13px; }
.back-link a { color: #667eea; text-decoration: none; }
</style>
