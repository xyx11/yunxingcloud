<script setup lang="ts">
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'
import JdButton from '@/components/JdButton.vue'

const router = useRouter()
const auth = useAuthStore()
const toast = inject(ToastInjectionKey)!
const { t } = useI18n()

const form = ref({
  username: (() => { try { return localStorage.getItem('mall_remember_user') || '' } catch { return '' } })(),
  password: (() => { try { return localStorage.getItem('mall_remember_pass') || '' } catch { return '' } })(),
})
const rememberMe = ref(!!form.value.username)
const rememberPassword = ref(!!form.value.password)
const error = ref('')
const loading = ref(false)

async function doLogin() {
  error.value = ''
  if (!form.value.username || !form.value.password) { error.value = t('login.fillRequired'); return }
  loading.value = true
  try {
    await auth.login(form.value.username, form.value.password)
    try { if (rememberMe.value) localStorage.setItem('mall_remember_user', form.value.username); else localStorage.removeItem('mall_remember_user'); if (rememberPassword.value) localStorage.setItem('mall_remember_pass', form.value.password); else localStorage.removeItem('mall_remember_pass') } catch {}
    toast.success(t('toast.loginSuccess'))
    const redirect = router.currentRoute.value.query.redirect as string
    router.push(redirect || '/')
  } catch (e: unknown) {
    const apiErr = e as { response?: { data?: { message?: string; code?: string } } }
    const errData = apiErr.response?.data || {}
    const errMsg: string = errData.message || ''
    if (errData.code === 'USER_NOT_FOUND' || errMsg.includes('不存在') || errMsg.includes('未注册')) {
      error.value = t('login.userNotFound')
    } else if (errData.code === 'PASSWORD_ERROR' || errMsg.includes('密码错误') || errMsg.includes('密码不正确')) {
      error.value = t('login.wrongPassword')
    } else if (errData.code === 'ACCOUNT_LOCKED' || errMsg.includes('锁定') || errMsg.includes('冻结') || errMsg.includes('禁用')) {
      error.value = t('login.accountLocked')
    } else {
      error.value = errMsg || t('login.loginError')
    }
  } finally { loading.value = false }
}

const oauthProviders = [
  { id: 'wechat', label: t('login.wechat'), color: '#07c160', bg: '#f0fff4', icon: '💬' },
  { id: 'qq', label: 'QQ', color: '#12b7f5', bg: '#f0f9ff', icon: '🐧' },
  { id: 'alipay', label: t('login.alipay'), color: '#1677ff', bg: '#f0f5ff', icon: '🔵' },
]
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-logo">
        <span class="login-emoji">🛒</span>
        <h2 class="login-title">{{ t('login.title') }}</h2>
        <p class="login-subtitle">{{ t('login.subtitle') }}</p>
      </div>

      <div v-if="error" class="login-error">{{ error }}</div>

      <div class="form-group">
        <label class="form-label">{{ t('login.username') }}</label>
        <input v-model="form.username" :placeholder="t('login.placeholderUser')" class="form-input" />
      </div>

      <div class="form-group">
        <label class="form-label">{{ t('login.password') }}</label>
        <input v-model="form.password" type="password" :placeholder="t('login.placeholderPass')" class="form-input" @keyup.enter="doLogin" />
      </div>

      <div class="login-options">
        <label class="remember-label">
          <input type="checkbox" v-model="rememberMe" /> {{ t('login.rememberUser') }}
        </label>
        <label class="remember-label">
          <input type="checkbox" v-model="rememberPassword" /> 记住密码
        </label>
        <span class="forgot-link" @click="router.push('/forgot-password')">{{ t('login.forgotPassword') }}</span>
      </div>

      <JdButton block size="lg" :loading="loading" :disabled="loading" @click="doLogin">
        {{ t('login.submit') }}
      </JdButton>

      <!-- OAuth -->
      <div class="oauth-section">
        <div class="oauth-divider">
          <span class="divider-line" />
          <span class="divider-text">{{ t('login.otherLogin') }}</span>
          <span class="divider-line" />
        </div>
        <div class="oauth-btns">
          <a v-for="p in oauthProviders" :key="p.id" :href="`/oauth2/authorization/${p.id}`" class="oauth-btn" :style="{ color: p.color, borderColor: p.color }">
            {{ p.icon }} {{ p.label }}
          </a>
        </div>
      </div>

      <p class="login-footer">
        {{ t('login.noAccount') }}<span class="login-link" @click="router.push('/register')">{{ t('login.goRegister') }}</span>
      </p>
      <p class="demo-hint">{{ t('login.demoAccount') }}</p>
    </div>
  </div>
</template>

<style scoped>
.login-page { max-width: 420px; margin: 40px auto; }
.login-card {
  background: var(--bg-white); border-radius: var(--radius-lg); padding: 40px;
  box-shadow: var(--shadow-sm);
}
.login-logo { text-align: center; margin-bottom: var(--space-xxl); }
.login-emoji { font-size: 48px; margin-bottom: var(--space-sm); display: block; }
.login-title { font-size: var(--font-title); margin-bottom: var(--space-xs); }
.login-subtitle { color: var(--text-tertiary); font-size: var(--font-base); }

.login-error {
  background: var(--jd-red-light); color: var(--jd-red); padding: var(--space-md);
  border-radius: var(--radius-md); font-size: var(--font-base); margin-bottom: var(--space-lg);
}

.form-group { margin-bottom: var(--space-lg); }
.form-label { display: block; font-size: var(--font-base); color: var(--text-secondary); margin-bottom: 6px; font-weight: 500; }
.form-input {
  width: 100%; padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md);
  font-size: var(--font-md); box-sizing: border-box; outline: none; background: var(--bg-white);
  color: var(--text-primary); transition: border-color var(--transition-fast);
}
.form-input:focus { border-color: var(--jd-red); box-shadow: 0 0 0 2px var(--jd-red-light); }

.login-options { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.remember-label { font-size: var(--font-sm); color: var(--text-tertiary); cursor: pointer; display: flex; align-items: center; gap: var(--space-xs); }
.remember-label input { accent-color: var(--jd-red); }
.forgot-link { font-size: var(--font-sm); color: var(--text-tertiary); cursor: pointer; }
.forgot-link:hover { color: var(--jd-red); }

.oauth-section { margin-top: var(--space-xl); }
.oauth-divider { display: flex; align-items: center; gap: var(--space-md); margin-bottom: var(--space-md); }
.divider-line { flex: 1; height: 1px; background: var(--border-light); }
.divider-text { font-size: var(--font-sm); color: var(--text-tertiary); white-space: nowrap; }
.oauth-btns { display: flex; gap: var(--space-md); justify-content: center; }
.oauth-btn {
  display: flex; align-items: center; gap: 4px; padding: var(--space-sm) var(--space-lg);
  border: 1px solid; border-radius: var(--radius-round); cursor: pointer; text-decoration: none;
  font-size: var(--font-sm); transition: all var(--transition-fast); background: var(--bg-white);
}
.oauth-btn:hover { filter: brightness(.95); }

.login-footer { text-align: center; margin-top: var(--space-lg); font-size: var(--font-base); color: var(--text-tertiary); }
.login-link { color: var(--jd-red); cursor: pointer; font-weight: 600; }
.login-link:hover { text-decoration: underline; }
.demo-hint { text-align: center; margin-top: var(--space-sm); font-size: var(--font-xs); color: var(--text-placeholder); }

@media (max-width: 768px) {
  .login-page { margin: var(--space-lg) auto; padding: 0 var(--space-md); }
  .login-card { padding: var(--space-xxl) var(--space-xl); }
}
</style>
