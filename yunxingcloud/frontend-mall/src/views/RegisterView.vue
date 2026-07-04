<script setup lang="ts">
import { ref, inject, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'
import JdButton from '@/components/JdButton.vue'

const router = useRouter()
const auth = useAuthStore()
const toast = inject(ToastInjectionKey)!
const { t } = useI18n()
const form = ref({ username: '', password: '', confirmPassword: '', email: '' })
const error = ref('')
const loading = ref(false)
const showSuccess = ref(false)

const pwdChecks = computed(() => ({
  len: form.value.password.length >= 8,
  upper: /[A-Z]/.test(form.value.password),
  lower: /[a-z]/.test(form.value.password),
  digit: /[0-9]/.test(form.value.password),
  special: /[!@#$%^&*(),.?":{}|<>]/.test(form.value.password),
}))
const pwdStrength = computed(() => {
  const c = pwdChecks.value; const score = [c.len,c.upper,c.lower,c.digit,c.special].filter(Boolean).length
  return score <= 1 ? 'weak' : score <= 2 ? 'fair' : score <= 3 ? 'good' : 'strong'
})

async function doRegister() {
  error.value = ''
  if (!form.value.username || !form.value.password) { error.value = t('register.fillRequired'); return }
  if (form.value.password !== form.value.confirmPassword) { error.value = t('register.passwordMismatch'); return }
  if (!pwdChecks.value.len) { error.value = '密码至少8位'; return }
  if (!pwdChecks.value.upper) { error.value = '密码需包含大写字母'; return }
  if (!pwdChecks.value.lower) { error.value = '密码需包含小写字母'; return }
  if (!pwdChecks.value.digit) { error.value = '密码需包含数字'; return }
  if (!pwdChecks.value.special) { error.value = '密码需包含特殊字符(!@#$%等)'; return }
  loading.value = true
  try {
    await auth.register(form.value.username, form.value.password)
    showSuccess.value = true
  } catch (e: any) { error.value = e.response?.data?.message || t('register.registerFail') }
  finally { loading.value = false }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-logo">
        <div class="register-emoji">🎉</div>
        <h2 class="register-title">{{ t('register.title') }}</h2>
        <p class="register-subtitle">{{ t('register.subtitle') }}</p>
      </div>

      <div v-if="error" class="register-error">{{ error }}</div>

      <div class="form-group">
        <label class="form-label">{{ t('register.username') }}</label>
        <input v-model="form.username" :placeholder="t('register.placeholderUser')" class="form-input" />
      </div>

      <div class="form-group">
        <label class="form-label">{{ t('register.password') }} <span class="hint">(8位+大小写+数字+特殊字符)</span></label>
        <input v-model="form.password" type="password" :placeholder="t('register.placeholderPass')" class="form-input" />
        <div v-if="form.password" class="pwd-checks">
          <span :class="{ pass: pwdChecks.len }">{{ pwdChecks.len ? '✓' : '○' }}8位</span>
          <span :class="{ pass: pwdChecks.upper }">{{ pwdChecks.upper ? '✓' : '○' }}大写</span>
          <span :class="{ pass: pwdChecks.digit }">{{ pwdChecks.digit ? '✓' : '○' }}数字</span>
          <span :class="{ pass: pwdChecks.lower }">{{ pwdChecks.lower ? '✓' : '○' }}小写</span>
          <span :class="{ pass: pwdChecks.special }">{{ pwdChecks.special ? '✓' : '○' }}特殊字符</span>
          <span class="pwd-strength" :class="pwdStrength">{{ {weak:'弱',fair:'一般',good:'良好',strong:'强'}[pwdStrength] }}</span>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">{{ t('register.confirmPassword') }}</label>
        <input v-model="form.confirmPassword" type="password" :placeholder="t('register.placeholderConfirm')" class="form-input" @keyup.enter="doRegister" />
      </div>

      <div class="benefits-box">
        <p class="benefits-title">🎁 {{ t('register.benefits') }}</p>
        <p class="benefits-item">· {{ t('register.benefit1') }}</p>
        <p class="benefits-item">· {{ t('register.benefit2') }}</p>
        <p class="benefits-item">· {{ t('register.benefit3') }}</p>
      </div>

      <JdButton block size="lg" :loading="loading" @click="doRegister">
        {{ loading ? t('register.submitting') : t('register.submit') }}
      </JdButton>

      <p class="login-link">
        {{ t('register.hasAccount') }}<span class="link" @click="router.push('/login')">{{ t('register.goLogin') }}</span>
      </p>

      <!-- Success Modal -->
      <div v-if="showSuccess" class="success-overlay" @click.self="router.push('/login')">
        <div class="success-card">
          <div class="success-emoji">🎉</div>
          <h3 class="success-title">注册成功！</h3>
          <p class="success-desc">账号已提交审核，审核通过后即可登录</p>
          <JdButton block size="lg" @click="router.push('/login')">前往登录</JdButton>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page { max-width: 420px; margin: 40px auto; }
.register-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: 40px; box-shadow: var(--shadow-sm); }
.register-logo { text-align: center; margin-bottom: var(--space-xxl); }
.register-emoji { font-size: 48px; margin-bottom: var(--space-sm); }
.register-title { font-size: var(--font-title); margin-bottom: var(--space-xs); }
.register-subtitle { color: var(--text-tertiary); font-size: var(--font-base); }
.register-error { background: var(--jd-red-light); color: var(--jd-red); padding: var(--space-md); border-radius: var(--radius-md); font-size: var(--font-base); margin-bottom: var(--space-lg); }

.form-group { margin-bottom: var(--space-lg); }
.form-label { display: block; font-size: var(--font-base); color: var(--text-secondary); margin-bottom: 6px; font-weight: 500; }
.form-label .hint { color: var(--text-tertiary); font-weight: 400; }
.form-input { width: 100%; padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md); font-size: var(--font-md); box-sizing: border-box; outline: none; background: var(--bg-white); color: var(--text-primary); transition: border-color var(--transition-fast); }
.form-input:focus { border-color: var(--jd-red); box-shadow: 0 0 0 2px var(--jd-red-light); }

.pwd-checks { margin-top: var(--space-sm); display: flex; gap: 6px; font-size: var(--font-xs); flex-wrap: wrap; }
.pwd-checks span { color: var(--text-placeholder); }
.pwd-checks span.pass { color: var(--green); }
.pwd-strength { margin-left: auto; font-weight: 600; }
.pwd-strength.weak { color: var(--text-placeholder); }
.pwd-strength.fair { color: var(--orange); }
.pwd-strength.good { color: var(--blue); }
.pwd-strength.strong { color: var(--green); }

.benefits-box { background: linear-gradient(135deg, var(--jd-red-light), var(--jd-red-bg)); border-radius: var(--radius-md); padding: var(--space-md) var(--space-lg); margin-bottom: var(--space-xl); }
.benefits-title { font-size: var(--font-sm); font-weight: 600; color: var(--jd-red); margin-bottom: 6px; }
.benefits-item { font-size: var(--font-xs); color: var(--text-secondary); margin: 2px 0; }

.login-link { text-align: center; margin-top: var(--space-lg); font-size: var(--font-base); color: var(--text-tertiary); }
.login-link .link { color: var(--jd-red); cursor: pointer; font-weight: 600; }

.success-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: var(--bg-overlay); display: flex; align-items: center; justify-content: center; z-index: 200; }
.success-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: 40px; text-align: center; max-width: 360px; width: 90%; animation: slideUp .4s ease-out; }
.success-emoji { font-size: 56px; margin-bottom: var(--space-md); }
.success-title { font-size: var(--font-lg); margin-bottom: var(--space-sm); }
.success-desc { color: var(--text-secondary); font-size: var(--font-md); margin-bottom: var(--space-xxl); }

@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>
