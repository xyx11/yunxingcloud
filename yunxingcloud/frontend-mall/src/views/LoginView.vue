<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/locales'
import { useToast } from '@/composables/useToast'
import { forgotPassword } from '@/api/auth'
import JdButton from '@/components/JdButton.vue'
import request from '@/api/request'

const router = useRouter()
const auth = useAuthStore()
const toast = useToast()
const { t } = useI18n()

const showForgotPwd = ref(false)
const forgotForm = ref({ email: '' })
const forgotLoading = ref(false)
const forgotSent = ref(false)

async function doForgot() {
  if (!forgotForm.value.email) { toast.error(t('login.fillRequired')); return }
  forgotLoading.value = true
  try {
    await forgotPassword(forgotForm.value.email)
    forgotSent.value = true
    toast.success(t('login.resetSent'))
  } catch { toast.error(t('login.resetFail')) }
  finally { forgotLoading.value = false }
}

const form = ref({
  username: (() => { try { return localStorage.getItem('mall_remember_user') || '' } catch { return '' } })(),
  password: '',
  captcha: '',
})
const rememberMe = ref(!!form.value.username)
const showPwd = ref(false)
const error = ref('')
const loading = ref(false)
const captchaToken = ref('')
const captchaUrl = ref('')
const captchaRefreshing = ref(false)
const loginAttempts = ref(0)
const showCaptcha = computed(() => loginAttempts.value >= 2)

async function loadCaptcha() {
  try {
    const r = await request.get('/captcha')
    captchaUrl.value = r.data?.image || ''
    captchaToken.value = r.data?.captchaToken || ''
  } catch { captchaUrl.value = ''; captchaToken.value = '' }
}

async function refreshCaptcha() {
  captchaRefreshing.value = true
  loadCaptcha()
  setTimeout(() => { captchaRefreshing.value = false }, 300)
}

const shakeError = ref(false)
function triggerShake() { shakeError.value = true; setTimeout(() => shakeError.value = false, 500) }

onMounted(() => { loadCaptcha(); setTimeout(() => document.querySelector<HTMLInputElement>('.form-input')?.focus(), 200) })

async function doLogin() {
  error.value = ''
  if (!form.value.username || !form.value.password) { error.value = t('login.fillRequired'); triggerShake(); return }
  if (showCaptcha.value && !form.value.captcha) { error.value = t('login.captchaRequired'); triggerShake(); return }
  loading.value = true
  try {
    await auth.login(form.value.username, form.value.password, showCaptcha.value ? captchaToken.value : undefined, showCaptcha.value ? form.value.captcha : undefined)
    try { if (rememberMe.value) localStorage.setItem('mall_remember_user', form.value.username); else localStorage.removeItem('mall_remember_user') } catch {}
    loginAttempts.value = 0
    toast.success(t('toast.loginSuccess'))
    const redirect = router.currentRoute.value.query.redirect as string
    router.push(redirect || '/')
  } catch (e: unknown) {
    loginAttempts.value++
    loadCaptcha()
    triggerShake()
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
  { id: 'wechat', label: t('login.wechat'), color: '#07c160', bg: '#f0fff4', icon: '💬', desc: '微信一键登录' },
  { id: 'alipay', label: t('login.alipay'), color: '#1677ff', bg: '#f0f5ff', icon: '🔵', desc: '支付宝快捷登录' },
]
</script>

<template>
  <div class="login-page">
    <div class="login-card" :class="{ 'shake-anim': shakeError }">
      <div class="login-logo">
        <span class="login-emoji">🛒</span>
        <h2 class="login-title">{{ t('login.title') }}</h2>
        <p class="login-subtitle">{{ t('login.subtitle') }}</p>
      </div>

      <div v-if="error" class="login-error">{{ error }}</div>

      <div class="form-group">
        <label class="form-label">{{ t('login.username') }}</label>
        <input v-model.trim="form.username" :placeholder="t('login.placeholderUser')" class="form-input" autocomplete="username" autofocus @keyup.enter="doLogin" />
      </div>

      <div class="form-group">
        <label class="form-label">{{ t('login.password') }}</label>
        <div class="password-wrap">
          <input v-model="form.password" :type="showPwd ? 'text' : 'password'" :placeholder="t('login.placeholderPass')" class="form-input" autocomplete="current-password" @keyup.enter="doLogin" />
          <button class="pwd-toggle" type="button" @click="showPwd = !showPwd" :aria-label="showPwd ? t('login.hidePassword') : t('login.showPassword')">{{ showPwd ? '👁' : '👁‍🗨' }}</button>
        </div>
      </div>

      <!-- Captcha (shown after 2+ failed attempts) -->
      <div v-if="showCaptcha" class="form-group">
        <label class="form-label">{{ t('login.captcha') }}</label>
        <div class="captcha-row">
          <input v-model="form.captcha" :placeholder="t('login.captchaPlaceholder')" class="form-input captcha-input" maxlength="4" @keyup.enter="doLogin" />
          <div class="captcha-img-wrap" @click="refreshCaptcha" :class="{ refreshing: captchaRefreshing }">
            <img v-if="captchaUrl" :src="captchaUrl" :alt="t('login.captcha')" class="captcha-img" />
            <span class="captcha-refresh">↻ {{ t('login.captchaRefresh') }}</span>
          </div>
        </div>
      </div>

      <div class="login-options">
        <label class="remember-label">
          <input type="checkbox" v-model="rememberMe" /> {{ t('login.rememberUser') }}
        </label>
        <span class="forgot-link" @click="showForgotPwd = !showForgotPwd; forgotSent = false; forgotForm.email = ''">{{ t('login.forgotPassword') }}</span>
      </div>

      <!-- Forgot password panel -->
      <div v-if="showForgotPwd" class="forgot-panel">
        <div v-if="!forgotSent">
          <p class="forgot-desc">{{ t('login.forgotDesc') }}</p>
          <input v-model="forgotForm.email" :placeholder="t('login.emailPlaceholder')" class="form-input" type="email" @keyup.enter="doForgot" />
          <JdButton block size="sm" :loading="forgotLoading" @click="doForgot" style="margin-top:12px">{{ t('login.sendResetLink') }}</JdButton>
        </div>
        <div v-else class="forgot-success">
          <span class="forgot-check">✅</span>
          <p>{{ t('login.resetSentMsg') }}</p>
        </div>
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
          <a v-for="p in oauthProviders" :key="p.id" :href="`/oauth2/authorization/${p.id}`" class="oauth-btn" :style="{ backgroundColor: p.bg, borderColor: p.color, color: p.color }">
            <span class="oauth-icon">{{ p.icon }}</span>
            <div class="oauth-text">
              <span class="oauth-label">{{ p.label }}</span>
              <span class="oauth-desc">{{ p.desc }}</span>
            </div>
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
.password-wrap { position: relative; }
.password-wrap .form-input { padding-right: 40px; }
.pwd-toggle { position: absolute; right: 8px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; font-size: 18px; padding: 4px; line-height: 1; }

.login-options { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.remember-label { font-size: var(--font-sm); color: var(--text-tertiary); cursor: pointer; display: flex; align-items: center; gap: var(--space-xs); }
.remember-label input { accent-color: var(--jd-red); }
.forgot-link { font-size: var(--font-sm); color: var(--text-tertiary); cursor: pointer; }
.forgot-link:hover { color: var(--jd-red); }

.oauth-section { margin-top: var(--space-xl); }
.oauth-divider { display: flex; align-items: center; gap: var(--space-md); margin-bottom: var(--space-md); }
.divider-line { flex: 1; height: 1px; background: var(--border-light); }
.divider-text { font-size: var(--font-sm); color: var(--text-tertiary); white-space: nowrap; }
/* Captcha */
.captcha-row { display: flex; gap: var(--space-sm); align-items: center; }
.captcha-input { flex: 1; }
.captcha-img-wrap { flex-shrink: 0; cursor: pointer; border: 1px solid var(--border); border-radius: var(--radius-md); overflow: hidden; display: flex; flex-direction: column; align-items: center; min-width: 100px; transition: opacity var(--transition-fast); }
.captcha-img-wrap.refreshing { opacity: .5; }
.captcha-img { height: 42px; object-fit: contain; }
.captcha-refresh { font-size: 10px; color: var(--text-tertiary); padding: 2px; }

.oauth-btns { display: flex; flex-direction: column; gap: var(--space-sm); }
.oauth-btn {
  display: flex; align-items: center; gap: var(--space-md); padding: var(--space-md) var(--space-lg);
  border: 2px solid; border-radius: var(--radius-md); cursor: pointer; text-decoration: none;
  transition: all var(--transition-fast);
}
.oauth-btn:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.oauth-icon { font-size: 28px; flex-shrink: 0; }
.oauth-text { display: flex; flex-direction: column; }
.oauth-label { font-size: var(--font-md); font-weight: 700; }
.oauth-desc { font-size: 11px; opacity: .7; }

.login-footer { text-align: center; margin-top: var(--space-lg); font-size: var(--font-base); color: var(--text-tertiary); }
.login-link { color: var(--jd-red); cursor: pointer; font-weight: 600; }
.login-link:hover { text-decoration: underline; }
.demo-hint { text-align: center; margin-top: var(--space-sm); font-size: var(--font-xs); color: var(--text-placeholder); }
.forgot-panel { margin: var(--space-lg) 0; padding: var(--space-lg); background: var(--bg-hover); border-radius: var(--radius-md); }
.forgot-desc { font-size: var(--font-sm); color: var(--text-secondary); margin-bottom: var(--space-sm); }
.forgot-success { text-align: center; padding: var(--space-md) 0; }
.forgot-check { font-size: 32px; }

.shake-anim { animation: shake .5s ease; }
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 50%, 90% { transform: translateX(-6px); }
  30%, 70% { transform: translateX(6px); }
}

@media (max-width: 768px) {
  .login-page { margin: var(--space-lg) auto; padding: 0 var(--space-md); }
  .login-card { padding: var(--space-xxl) var(--space-xl); }
}
</style>
