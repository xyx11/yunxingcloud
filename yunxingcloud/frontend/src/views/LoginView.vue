<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { fetchCsrfToken } from '@/api/auth'
import type { FormInst, FormRules } from 'naive-ui'
import { NConfigProvider, NCard, NForm, NFormItem, NInput, NButton, NAlert, darkTheme } from 'naive-ui'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const formRef = ref<FormInst | null>(null)
const model = ref({ username: '', password: '' })
const error = ref('')
const loading = ref(false)

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

onMounted(async () => {
  await fetchCsrfToken()
  if (route.query.error !== undefined) {
    error.value = '用户名或密码错误，请重试。'
  }
})

async function handleLogin() {
  loading.value = true
  error.value = ''
  try {
    const redirectUrl = await authStore.login(model.value.username, model.value.password)
    window.location.href = redirectUrl
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || '用户名或密码错误'
    await fetchCsrfToken()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <n-config-provider>
    <div class="login-page">
      <n-card class="login-card">
        <h1 class="title">yunxingcloud</h1>
        <p class="subtitle">SSO 单点登录认证中心</p>

        <n-alert v-if="error" type="error" :title="error" closable @close="error = ''" style="margin-bottom: 20px" />

        <n-form ref="formRef" :model="model" :rules="rules" @submit.prevent="handleLogin">
          <n-form-item label="用户名" path="username">
            <n-input v-model:value="model.username" placeholder="请输入用户名" size="large" />
          </n-form-item>
          <n-form-item label="密码" path="password">
            <n-input v-model:value="model.password" type="password" placeholder="请输入密码" size="large" />
          </n-form-item>
          <n-button type="primary" block size="large" :loading="loading" attr-type="submit">
            登 录
          </n-button>
        </n-form>
      </n-card>
    </div>
  </n-config-provider>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  border-radius: 12px;
  padding: 8px;
}

.title {
  text-align: center;
  color: #333;
  margin-bottom: 4px;
  font-size: 24px;
}

.subtitle {
  text-align: center;
  color: #888;
  margin-bottom: 28px;
  font-size: 14px;
}
</style>
