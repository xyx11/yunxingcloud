<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { useRoute, useRouter } from 'vue-router'
import { NConfigProvider, NCard, NForm, NFormItem, NInput, NButton, NAlert } from 'naive-ui'

const route = useRoute()
const router = useRouter()

const token = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')

onMounted(() => {
  const t = route.query.token as string
  if (t) {
    token.value = t
  }
})

async function handleSubmit() {
  if (newPassword.value !== confirmPassword.value) {
    error.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await request.post('/api/password/reset', {
      token: token.value,
      newPassword: newPassword.value,
    })
    if (res.data.success) {
      success.value = res.data.message
      setTimeout(() => router.push('/login'), 2000)
    }
  } catch (e: any) {
    error.value = e.response?.data?.message || '重置失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <n-config-provider>
    <div class="page">
      <n-card class="card">
        <h1 class="title">重置密码</h1>
        <p class="subtitle">输入重置令牌和新密码</p>

        <n-alert v-if="error" type="error" :title="error" closable @close="error = ''" style="margin-bottom: 20px" />
        <n-alert v-if="success" type="success" :title="success" style="margin-bottom: 20px" />

        <n-form v-if="!success" @submit.prevent="handleSubmit">
          <n-form-item label="重置令牌">
            <n-input v-model:value="token" placeholder="输入邮箱收到的重置令牌" size="large" />
          </n-form-item>
          <n-form-item label="新密码">
            <n-input v-model:value="newPassword" type="password" placeholder="至少6位" size="large" />
          </n-form-item>
          <n-form-item label="确认密码">
            <n-input v-model:value="confirmPassword" type="password" placeholder="再次输入新密码" size="large" />
          </n-form-item>
          <n-button type="primary" block size="large" :loading="loading" attr-type="submit">
            重置密码
          </n-button>
        </n-form>

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
