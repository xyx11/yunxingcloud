<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import type { FormInst, FormRules } from 'naive-ui'
import { NConfigProvider, NCard, NForm, NFormItem, NInput, NButton, NAlert, NDivider } from 'naive-ui'

const router = useRouter()

const formRef = ref<FormInst | null>(null)
const model = ref({ username: '', password: '', confirmPassword: '', email: '' })
const error = ref('')
const success = ref('')
const loading = ref(false)

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度 3-50 位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => value === model.value.password,
      message: '两次输入的密码不一致',
      trigger: 'blur',
    },
  ],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
  ],
}

onMounted(() => {})

async function handleRegister() {
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    const res = await request.post('/api/register', {
      username: model.value.username,
      password: model.value.password,
      email: model.value.email,
    })
    if (res.data.success) {
      success.value = '注册成功！正在跳转...'
      setTimeout(() => {
        window.location.href = '/'
      }, 1000)
    }
  } catch (e: any) {
    error.value = e.response?.data?.message || '注册失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <n-config-provider>
    <div class="register-page">
      <n-card class="register-card">
        <h1 class="title">创建账号</h1>
        <p class="subtitle">注册 yunxingcloud 账号</p>

        <n-alert v-if="error" type="error" :title="error" closable @close="error = ''" style="margin-bottom: 20px" />
        <n-alert v-if="success" type="success" :title="success" style="margin-bottom: 20px" />

        <n-form ref="formRef" :model="model" :rules="rules" @submit.prevent="handleRegister">
          <n-form-item label="用户名" path="username">
            <n-input v-model:value="model.username" placeholder="3-50位字符" size="large" />
          </n-form-item>
          <n-form-item label="邮箱" path="email">
            <n-input v-model:value="model.email" placeholder="选填" size="large" />
          </n-form-item>
          <n-form-item label="密码" path="password">
            <n-input v-model:value="model.password" type="password" placeholder="至少6位" size="large" />
          </n-form-item>
          <n-form-item label="确认密码" path="confirmPassword">
            <n-input v-model:value="model.confirmPassword" type="password" placeholder="再次输入密码" size="large" />
          </n-form-item>
          <n-button type="primary" block size="large" :loading="loading" attr-type="submit">
            注 册
          </n-button>
        </n-form>

        <div class="login-link">
          已有账号？
          <router-link to="/login" class="link">立即登录</router-link>
        </div>
      </n-card>
    </div>
  </n-config-provider>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 420px;
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

.login-link {
  text-align: center;
  margin-top: 16px;
  font-size: 13px;
  color: #888;
}

.link {
  color: #667eea;
  cursor: pointer;
  text-decoration: none;
}
</style>
