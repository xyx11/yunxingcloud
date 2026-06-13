<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '@/api/request'
import JSEncrypt from 'jsencrypt'
import Cookies from 'js-cookie'
import { useAuthStore } from '@/stores/auth'
import type { FormInst, FormRules } from 'naive-ui'
import { NConfigProvider, NCard, NForm, NFormItem, NInput, NButton, NAlert, NSpace, NDivider, NCheckbox } from 'naive-ui'

const { t } = useI18n()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const formRef = ref<FormInst | null>(null)
const model = ref({ username: '', password: '', code: '', rememberMe: false })
const error = ref('')
const loading = ref(false)
const codeUrl = ref('')
const captchaEnabled = ref(true)
const appVersion = ref('')

const rules = computed<FormRules>(() => ({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: captchaEnabled.value ? [{ required: true, message: '请输入验证码', trigger: 'blur' }] : [],
}))

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
  request.get('/api/config/key/sys.version').then(r => appVersion.value = r.data.configValue || '').catch(() => {})
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
    const finalUrl = redirectUrl !== '/' ? redirectUrl : (route.query.redirect as string) || '/'
    router.push(finalUrl)
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
        <p class="subtitle">{{ t('app.subtitle') }}</p>

        <n-alert v-if="error" type="error" :title="error" closable @close="error = ''" style="margin-bottom: 20px" />

        <n-form ref="formRef" :model="model" :rules="rules" @submit.prevent="handleLogin">
          <n-form-item :label="t('login.username')" path="username">
            <n-input v-model:value="model.username" :placeholder="t('login.username')" size="large">
              <template #prefix><span style="font-size:16px;">👤</span></template>
            </n-input>
          </n-form-item>

          <n-form-item :label="t('login.password')" path="password">
            <n-input
              v-model:value="model.password" type="password" :placeholder="t('login.password')" size="large"
              show-password-on="click"
            >
              <template #prefix><span style="font-size:16px;">🔒</span></template>
            </n-input>
          </n-form-item>

          <n-form-item v-if="captchaEnabled" :label="t('login.captcha')" path="code">
            <div style="display:flex; gap:12px; width:100%;">
              <n-input v-model:value="model.code" :placeholder="t('login.captcha')" size="large" style="flex:1;" />
              <img
                v-if="codeUrl" :src="codeUrl" @click="getCaptcha"
                style="height:40px; width:120px; cursor:pointer; border-radius:4px;"
                title="点击刷新验证码"
              >
            </div>
          </n-form-item>

          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:18px;">
            <n-checkbox v-model:checked="model.rememberMe">{{ t('login.remember') }}</n-checkbox>
            <router-link to="/forgot-password" class="link">{{ t('login.forgot') }}</router-link>
          </div>

          <n-button type="primary" block size="large" :loading="loading" attr-type="submit">
            {{ t('login.title') }}
          </n-button>
        </n-form>

        <div class="register-link">
          {{ t('login.noAccount') }}<router-link to="/register" class="link">{{ t('login.register') }}</router-link>
        </div>

        <n-divider>{{ t('login.thirdParty') }}</n-divider>

        <n-space justify="center" :size="24">
          <div v-for="sp in socialProviders" :key="sp.provider" class="social-btn" @click="handleSocialLogin(sp.provider)">
            <span class="social-icon">{{ sp.icon }}</span>
            <span class="social-name">{{ sp.name }}</span>
          </div>
        </n-space>
      </n-card>
      <div v-if="appVersion" class="login-version">v{{ appVersion }}</div>
    </div>
  </n-config-provider>
</template>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  background-size: 400% 400%; animation: gradientShift 15s ease infinite;
  position: relative; overflow: hidden;
}
.login-page::before {
  content: ''; position: absolute; inset: 0;
  background: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.1) 0%, transparent 50%),
              radial-gradient(circle at 80% 20%, rgba(255,255,255,0.08) 0%, transparent 50%);
  animation: floatParticles 10s ease-in-out infinite alternate;
}
@keyframes gradientShift { 0%{background-position:0% 50%} 50%{background-position:100% 50%} 100%{background-position:0% 50%} }
@keyframes floatParticles { 0%{opacity:0.5;transform:scale(1)} 100%{opacity:1;transform:scale(1.1)} }

.login-card {
  width: 440px; border-radius: 16px; padding: 8px;
  backdrop-filter: blur(10px); background: rgba(255,255,255,0.85);
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  animation: cardIn 0.5s ease-out;
}
@keyframes cardIn { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }

.title {
  text-align: center; color: var(--n-text-color, #333);
  margin-bottom: 4px; font-size: 26px; font-weight: 600;
}

.subtitle {
  text-align: center;
  color: var(--n-text-color-3, #999);
  margin-bottom: 30px;
  font-size: 14px;
}

.register-link {
  text-align: center;
  margin-top: 16px;
  font-size: 13px;
  color: var(--n-text-color-3, #888);
}

.link {
  color: var(--primary-color, #667eea);
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
.login-version { text-align: center; margin-top: 12px; font-size: 12px; color: rgba(255,255,255,0.5); }
</style>
