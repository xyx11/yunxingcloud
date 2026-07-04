<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { fetchCaptcha, fetchPublicKey } from '@/api/auth'
import JSEncrypt from 'jsencrypt'
import Cookies from 'js-cookie'
import { useAuthStore } from '@/stores/auth'
import {
  NForm, NFormItem, NInput, NButton,
  NCheckbox, NDivider, NAlert,
} from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'

const { t } = useI18n()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const formRef = ref<FormInst>()
const model = ref({ username: '', password: '', code: '', rememberMe: false })
const error = ref('')
const loading = ref(false)
const codeUrl = ref('')
const captchaEnabled = ref(true)
const appVersion = ref('')

const rules: FormRules = {
  username: [{ required: true, message: () => t('login.usernameRequired'), trigger: 'blur' }],
  password: [{ required: true, message: () => t('login.passwordRequired'), trigger: 'blur' }],
}

const socialProviders = computed(() => [
  { name: t('login.wechat'), color: '#07C160', provider: 'wechat', svg: 'M8.5 11.5c-1.5 0-2.7-.5-3.5-1.5l.7-2.3c.5 1 1.5 1.5 2.8 1.5 1.7 0 2.8-1 2.8-2.3 0-1.2-.9-2-2.5-2-.8 0-1.5.2-2 .5l.6-5.5h5.5l-.3 1.5h-4l-.3 2.3c.5-.2 1.2-.3 2-.3 2.5 0 4 1.3 4 3.3 0 2.2-1.8 3.8-4.5 3.8z' },
  { name: t('login.qq'), color: '#12B7F5', provider: 'qq', svg: 'M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm1 14c-2.5 0-4.5-2-4.5-4.5S10.5 7 13 7s4.5 2 4.5 4.5S15.5 16 13 16zm0-1.5c-1.7 0-3-1.3-3-3s1.3-3 3-3 3 1.3 3 3-1.3 3-3 3z' },
  { name: t('login.alipay'), color: '#1677FF', provider: 'alipay', svg: 'M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm-1 6.5c-1.5 0-3 .5-4 1.5l.5-1c.5-.8 1.3-1.3 2.2-1.3 1.2 0 2 .5 2.5 1.3.3-.2.8-.5 1.3-.5 1 0 1.8.5 2.3 1.3h-2.5c-.5 0-1 .3-1.3.7.5.5 1 1 1.8 1 1.2 0 2.2-.5 3-1.3-.2 1.5-1 3-2.5 3-1 0-2-.5-2.5-1.5-.5.5-1.2.8-2 .8-1.3 0-2.3-.8-2.5-1.8.5.3 1 .5 1.5.5.8 0 1.5-.5 2-1-.3-.5-.5-1.2-.5-2 0-1 .5-2 1.5-2.5.3.5.8 1 1.5 1 .7 0 1.2-.3 1.5-.8-.3-.5-.8-1-1.5-1zM16 15.5c1 0 1.8-.5 2.3-1.3-.3 1-1 2.3-2.3 2.3-.5 0-1-.3-1.3-.7.3-.2.8-.3 1.3-.3z' },
])

onMounted(async () => {
  if (authStore.isAuthenticated) { router.replace('/'); return }
  const savedUser = Cookies.get('username')
  const savedRemember = Cookies.get('rememberMe')
  if (savedUser) model.value.username = savedUser
  if (savedRemember === 'true') {
    model.value.rememberMe = true
    const encPwd = Cookies.get('encPassword')
    if (encPwd) model.value.password = encPwd
  }
  appVersion.value = '1.0.0'
  await getCaptcha()
  if (route.query.error !== undefined) error.value = t('login.badCredentials')
})

async function getCaptcha() {
  try {
    const res = await fetchCaptcha()
    captchaEnabled.value = res.data.captchaEnabled !== false
    if (captchaEnabled.value) codeUrl.value = 'data:image/png;base64,' + res.data.img
  } catch { /* captcha unavailable */ }
}

async function getPublicKey(): Promise<string> {
  try { const res = await fetchPublicKey(); return res.data.publicKey } catch { return '' }
}

async function handleLogin() {
  loading.value = true
  error.value = ''
  try {
    if (formRef.value) {
      try { await formRef.value.validate() } catch { loading.value = false; return }
    }
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
    const redirectUrl = await authStore.login(model.value.username, password, model.value.code || undefined)
    const finalUrl = redirectUrl !== '/' ? redirectUrl : (route.query.redirect as string) || '/'
    router.push(finalUrl)
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || t('login.badCredentials')
    await getCaptcha()
  } finally { loading.value = false }
}

function handleSocialLogin(provider: string) {
  window.location.href = `/oauth2/authorization/${provider}`
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <h1 class="title">yunxingcloud</h1>
        <span class="version-badge">v1.0.0</span>
      </div>
      <p class="subtitle">{{ t('app.subtitle') }}</p>

      <n-alert v-if="error" type="error" closable @close="error = ''" style="margin-bottom:20px">
        {{ error }}
      </n-alert>

      <n-form ref="formRef" :model="model" :rules="rules" label-placement="top">
        <n-form-item path="username">
          <n-input v-model:value="model.username" :placeholder="t('login.username')" size="large">
            <template #prefix>👤</template>
          </n-input>
        </n-form-item>

        <n-form-item path="password">
          <n-input v-model:value="model.password" type="password" :placeholder="t('login.password')" size="large" show-password-on="click" @keyup.enter="handleLogin">
            <template #prefix>🔒</template>
          </n-input>
        </n-form-item>

        <n-form-item v-if="captchaEnabled" path="code">
          <div style="display:flex; gap:12px; width:100%;">
            <n-input v-model:value="model.code" :placeholder="t('login.captcha')" size="large" style="flex:1;">
              <template #prefix>🔑</template>
            </n-input>
            <img v-if="codeUrl" :src="codeUrl" @click="getCaptcha" :alt="t('login.captcha')" :title="t('login.refreshCaptcha')" style="height:40px;width:120px;cursor:pointer;border-radius:4px;">
          </div>
        </n-form-item>

        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:18px;">
          <n-checkbox v-model:checked="model.rememberMe">{{ t('login.remember') }}</n-checkbox>
          <router-link to="/forgot-password" class="link">{{ t('login.forgot') }}</router-link>
        </div>

        <n-button type="primary" size="large" :loading="loading" block @click="handleLogin">
          {{ t('login.title') }}
        </n-button>
      </n-form>

      <div class="register-link">
        {{ t('login.noAccount') }}<router-link to="/register" class="link">{{ t('login.register') }}</router-link>
      </div>

      <n-divider>{{ t('login.thirdParty') }}</n-divider>

      <div style="display:flex;justify-content:center;gap:24px;">
        <div v-for="sp in socialProviders" :key="sp.provider" class="social-btn" @click="handleSocialLogin(sp.provider)">
          <svg class="social-icon" viewBox="0 0 24 24" width="28" height="28" :fill="sp.color">
            <path :d="sp.svg" />
          </svg>
          <span class="social-name">{{ sp.name }}</span>
        </div>
      </div>
    </div>
  </div>
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
  pointer-events: none;
  background: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.1) 0%, transparent 50%),
              radial-gradient(circle at 80% 20%, rgba(255,255,255,0.08) 0%, transparent 50%);
  animation: floatParticles 10s ease-in-out infinite alternate;
}
@keyframes gradientShift { 0%{background-position:0% 50%} 50%{background-position:100% 50%} 100%{background-position:0% 50%} }
@keyframes floatParticles { 0%{opacity:0.5;transform:scale(1)} 100%{opacity:1;transform:scale(1.1)} }

.login-card {
  width: 440px; max-width: calc(100vw - 32px); border-radius: 16px; padding: 32px;
  background: rgba(255,255,255,0.92);
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  animation: cardIn 0.5s ease-out;
}
@keyframes cardIn { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }

.title { text-align: center; color: #333; margin-bottom: 4px; font-size: 26px; font-weight: 600; }
.subtitle { text-align: center; color: #999; margin-bottom: 30px; font-size: 14px; }
.register-link { text-align: center; margin-top: 16px; font-size: 13px; color: #888; }
.link { color: #667eea; cursor: pointer; text-decoration: none; }
.social-btn { display: flex; flex-direction: column; align-items: center; cursor: pointer; padding: 8px; border-radius: 8px; transition: background 0.2s; }
.social-btn:hover { background: #f5f5f5; }
.social-icon { font-size: 28px; margin-bottom: 4px; }
.social-name { font-size: 12px; color: #666; }
.brand { display: flex; align-items: baseline; justify-content: center; gap: 10px; }
.version-badge { font-size: 13px; color: #667eea; font-weight: 500; background: rgba(102,126,234,0.1); padding: 2px 10px; border-radius: 10px; }
</style>