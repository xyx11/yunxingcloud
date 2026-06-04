<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/api/request'
import JSEncrypt from 'jsencrypt'
import Cookies from 'js-cookie'
import { useAuthStore } from '@/stores/auth'
import type { FormInst, FormRules } from 'naive-ui'
import { NConfigProvider, NCard, NForm, NFormItem, NInput, NButton, NAlert, NSpace, NDivider, NCheckbox } from 'naive-ui'

const authStore = useAuthStore()
const route = useRoute()

const formRef = ref<FormInst | null>(null)
const model = ref({ username: 'admin', password: 'admin123', code: '', rememberMe: false })
const error = ref('')
const loading = ref(false)
const codeUrl = ref('')
const captchaEnabled = ref(true)

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: captchaEnabled.value ? [{ required: true, message: '请输入验证码', trigger: 'blur' }] : [],
}

const socialProviders = [
  { name: '微信', icon: '💬', color: '#07C160', provider: 'wechat' },
  { name: 'QQ', icon: '🐧', color: '#12B7F5', provider: 'qq' },
  { name: '支付宝', icon: '💙', color: '#1677FF', provider: 'alipay' },
]

onMounted(async () => {
  // load saved credentials
  const savedUser = Cookies.get('username')
  const savedPass = Cookies.get('password')
  const savedRemember = Cookies.get('rememberMe')
  if (savedUser) model.value.username = savedUser
  if (savedRemember === 'true') {
    model.value.rememberMe = true
    const encPwd = Cookies.get('encPassword')
    if (encPwd) model.value.password = encPwd
  }
  await getCaptcha()
  if (route.query.error !== undefined) {
    error.value = '用户名或密码错误'
  }
})

async function getCaptcha() {
  try {
    const res = await request.get('/api/captcha')
    captchaEnabled.value = res.data.captchaEnabled !== false
    if (captchaEnabled.value) {
      codeUrl.value = 'data:image/png;base64,' + res.data.img
    }
  } catch { /* captcha unavailable */ }
}

async function getPublicKey(): Promise<string> {
  try {
    const res = await request.get('/api/publicKey')
    return res.data.publicKey
  } catch { return '' }
}

async function handleLogin() {
  loading.value = true
  error.value = ''
  try {
    let password = model.value.password
    const publicKey = await getPublicKey()
    if (publicKey) {
      const encrypt = new JSEncrypt()
      encrypt.setPublicKey(publicKey)
      const encrypted = encrypt.encrypt(password)
      if (encrypted) password = encrypted
    }

    if (model.value.rememberMe) {
      Cookies.set('username', model.value.username, { expires: 30 })
      Cookies.set('encPassword', model.value.password, { expires: 30 })
      Cookies.set('rememberMe', 'true', { expires: 30 })
    } else {
      Cookies.remove('username'); Cookies.remove('encPassword'); Cookies.remove('rememberMe')
    }

    const redirectUrl = await authStore.login(
      model.value.username,
      password,
      model.value.code || undefined,
    )
    window.location.href = redirectUrl
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || '用户名或密码错误'
    await getCaptcha()
  } finally {
    loading.value = false
  }
}

function handleSocialLogin(provider: string) {
  window.location.href = `/oauth2/authorization/${provider}`
}
</script>

<template>
  <n-config-provider>
    <div class="login-page">
      <n-card class="login-card">
        <h1 class="title">yunxingcloud</h1>
        <p class="subtitle">分布式微服务架构平台</p>

        <n-alert v-if="error" type="error" :title="error" closable @close="error = ''" style="margin-bottom: 20px" />

        <n-form ref="formRef" :model="model" :rules="rules" @submit.prevent="handleLogin">
          <n-form-item label="用户名" path="username">
            <n-input v-model:value="model.username" placeholder="请输入用户名" size="large">
              <template #prefix><span style="font-size:16px;">👤</span></template>
            </n-input>
          </n-form-item>

          <n-form-item label="密码" path="password">
            <n-input v-model:value="model.password" type="password" placeholder="请输入密码" size="large"
              show-password-on="click">
              <template #prefix><span style="font-size:16px;">🔒</span></template>
            </n-input>
          </n-form-item>

          <n-form-item v-if="captchaEnabled" label="验证码" path="code">
            <div style="display:flex; gap:12px; width:100%;">
              <n-input v-model:value="model.code" placeholder="验证码" size="large" style="flex:1;" />
              <img v-if="codeUrl" :src="codeUrl" @click="getCaptcha"
                style="height:40px; width:120px; cursor:pointer; border-radius:4px;"
                title="点击刷新验证码" />
            </div>
          </n-form-item>

          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:18px;">
            <n-checkbox v-model:checked="model.rememberMe">记住密码</n-checkbox>
            <router-link to="/forgot-password" class="link">忘记密码?</router-link>
          </div>

          <n-button type="primary" block size="large" :loading="loading" attr-type="submit">
            登 录
          </n-button>
        </n-form>

        <div class="register-link">
          还没有账号？<router-link to="/register" class="link">立即注册</router-link>
        </div>

        <n-divider>或使用第三方登录</n-divider>

        <n-space justify="center" :size="24">
          <div v-for="sp in socialProviders" :key="sp.provider" class="social-btn" @click="handleSocialLogin(sp.provider)">
            <span class="social-icon">{{ sp.icon }}</span>
            <span class="social-name">{{ sp.name }}</span>
          </div>
        </n-space>
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
  width: 440px;
  border-radius: 12px;
  padding: 8px;
}

.title {
  text-align: center;
  color: #333;
  margin-bottom: 4px;
  font-size: 26px;
  font-weight: 600;
}

.subtitle {
  text-align: center;
  color: #999;
  margin-bottom: 30px;
  font-size: 14px;
}

.register-link {
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

.social-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.social-btn:hover { background: #f5f5f5; }
.social-icon { font-size: 28px; margin-bottom: 4px; }
.social-name { font-size: 12px; color: #666; }
</style>
