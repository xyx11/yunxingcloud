<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/locales'
import JdButton from '@/components/JdButton.vue'

const router = useRouter()
const auth = useAuthStore()
const { t } = useI18n()
const form = ref({ username: '', password: '', confirmPassword: '', email: '' })
const error = ref('')
const loading = ref(false)
const showSuccess = ref(false)
const agreedToTerms = ref(false)
const showPwd = ref(false)
const shakeErr = ref(false)

function triggerShake() { shakeErr.value = true; setTimeout(() => shakeErr.value = false, 500) }

onMounted(() => setTimeout(() => document.querySelector<HTMLInputElement>('.form-input')?.focus(), 200))

const emailValid = computed(() => {
  if (!form.value.email) return true // optional field
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)
})

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
  if (!form.value.username || !form.value.password) { error.value = t('register.fillRequired'); triggerShake(); return }
  if (form.value.password !== form.value.confirmPassword) { error.value = t('register.passwordMismatch'); triggerShake(); return }
  if (!pwdChecks.value.len) { error.value = t('register.pwdLen'); triggerShake(); return }
  if (!pwdChecks.value.upper) { error.value = t('register.pwdUpper'); triggerShake(); return }
  if (!pwdChecks.value.lower) { error.value = t('register.pwdLower'); triggerShake(); return }
  if (!pwdChecks.value.digit) { error.value = t('register.pwdDigit'); triggerShake(); return }
  if (!pwdChecks.value.special) { error.value = t('register.pwdSpecial'); triggerShake(); return }
  if (!agreedToTerms.value) { error.value = t('register.agreeTerms'); triggerShake(); return }
  if (!emailValid.value) { error.value = t('register.invalidEmail'); triggerShake(); return }
  loading.value = true
  try {
    await auth.register(form.value.username, form.value.password, form.value.email || undefined)
    showSuccess.value = true
  } catch (e: unknown) { error.value = (e as { response?: { data?: { message?: string } } }).response?.data?.message || t('register.registerFail'); triggerShake() }
  finally { loading.value = false }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card" :class="{ 'shake-anim': shakeErr }">
      <div class="register-logo">
        <div class="register-emoji">🎉</div>
        <h2 class="register-title">{{ t('register.title') }}</h2>
        <p class="register-subtitle">{{ t('register.subtitle') }}</p>
      </div>

      <div v-if="error" class="register-error">{{ error }}</div>

      <div class="form-group">
        <label class="form-label">{{ t('register.username') }}</label>
        <input v-model="form.username" :placeholder="t('register.placeholderUser')" class="form-input" autocomplete="username" @keyup.enter="doRegister" />
      </div>

      <div class="form-group">
        <label class="form-label">{{ t('register.password') }} <span class="hint">({{ t('register.pwdHint') }})</span></label>
        <div class="password-wrap">
          <input v-model="form.password" :type="showPwd ? 'text' : 'password'" :placeholder="t('register.placeholderPass')" class="form-input" autocomplete="new-password" @keyup.enter="doRegister" />
          <button class="pwd-toggle" type="button" @click="showPwd = !showPwd" :aria-label="showPwd ? t('login.hidePassword') : t('login.showPassword')">{{ showPwd ? '👁' : '👁‍🗨' }}</button>
        </div>
        <div v-if="form.password" class="pwd-checks">
          <span :class="{ pass: pwdChecks.len }">{{ pwdChecks.len ? '✓' : '○' }}{{ t('register.pwdLenLabel') }}</span>
          <span :class="{ pass: pwdChecks.upper }">{{ pwdChecks.upper ? '✓' : '○' }}{{ t('register.pwdUpperLabel') }}</span>
          <span :class="{ pass: pwdChecks.digit }">{{ pwdChecks.digit ? '✓' : '○' }}{{ t('register.pwdDigitLabel') }}</span>
          <span :class="{ pass: pwdChecks.lower }">{{ pwdChecks.lower ? '✓' : '○' }}{{ t('register.pwdLowerLabel') }}</span>
          <span :class="{ pass: pwdChecks.special }">{{ pwdChecks.special ? '✓' : '○' }}{{ t('register.pwdSpecialLabel') }}</span>
          <span class="pwd-strength" :class="pwdStrength">{{ {weak: t('register.pwdWeak'), fair: t('register.pwdFair'), good: t('register.pwdGood'), strong: t('register.pwdStrong')}[pwdStrength] }}</span>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">{{ t('register.confirmPassword') }}</label>
        <input v-model="form.confirmPassword" :type="showPwd ? 'text' : 'password'" :placeholder="t('register.placeholderConfirm')" class="form-input" autocomplete="new-password" @keyup.enter="doRegister" />
      </div>

      <div class="form-group">
        <label class="form-label">{{ t('register.emailLabel') }} <span class="hint">({{ t('register.optional') }})</span></label>
        <input v-model="form.email" type="email" :placeholder="t('register.emailPlaceholder')" class="form-input" :class="{ invalid: !emailValid }" />
        <span v-if="form.email && !emailValid" class="email-error">{{ t('register.invalidEmail') }}</span>
      </div>

      <div class="benefits-box">
        <p class="benefits-title">🎁 {{ t('register.benefits') }}</p>
        <p class="benefits-item">· {{ t('register.benefit1') }}</p>
        <p class="benefits-item">· {{ t('register.benefit2') }}</p>
        <p class="benefits-item">· {{ t('register.benefit3') }}</p>
      </div>

      <!-- Agreement -->
      <div class="agreement-row">
        <label class="agreement-label">
          <input type="checkbox" v-model="agreedToTerms" />
          我已阅读并同意 <span class="agreement-link">《用户协议》</span> 和 <span class="agreement-link">《隐私政策》</span>
        </label>
      </div>

      <JdButton block size="lg" :loading="loading" :disabled="loading || !agreedToTerms" @click="doRegister">
        {{ t('register.submit') }}
      </JdButton>

      <p class="login-link">
        {{ t('register.hasAccount') }}<span class="link" @click="router.push('/login')">{{ t('register.goLogin') }}</span>
      </p>

      <!-- Success Modal -->
      <div v-if="showSuccess" class="success-overlay" @click.self="router.push('/login')">
        <div class="success-card">
          <div class="success-emoji">🎉</div>
          <h3 class="success-title">{{ t('register.successTitle') }}</h3>
          <p class="success-desc">{{ t('register.successDesc') }}</p>
          <JdButton block size="lg" @click="router.push('/login')">{{ t('register.goLoginNow') }}</JdButton>
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

.agreement-row { margin-bottom: var(--space-xl); }
.agreement-label { font-size: var(--font-sm); color: var(--text-tertiary); cursor: pointer; display: flex; align-items: center; gap: 6px; }
.agreement-label input { accent-color: var(--jd-red); }
.agreement-link { color: var(--jd-red); cursor: pointer; }
.form-input.invalid { border-color: var(--jd-red); }
.email-error { font-size: var(--font-xs); color: var(--jd-red); margin-top: 4px; display: block; }

.login-link { text-align: center; margin-top: var(--space-lg); font-size: var(--font-base); color: var(--text-tertiary); }
.login-link .link { color: var(--jd-red); cursor: pointer; font-weight: 600; }

.success-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: var(--bg-overlay); display: flex; align-items: center; justify-content: center; z-index: 200; }
.success-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: 40px; text-align: center; max-width: 360px; width: 90%; animation: slideUp .4s ease-out; }
.success-emoji { font-size: 56px; margin-bottom: var(--space-md); }
.success-title { font-size: var(--font-lg); margin-bottom: var(--space-sm); }
.success-desc { color: var(--text-secondary); font-size: var(--font-md); margin-bottom: var(--space-xxl); }

.shake-anim { animation: shake .5s ease; }
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 50%, 90% { transform: translateX(-6px); }
  30%, 70% { transform: translateX(6px); }
}

.password-wrap { position: relative; }
.password-wrap .form-input { padding-right: 40px; }
.pwd-toggle { position: absolute; right: 8px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; font-size: 18px; padding: 4px; line-height: 1; }

@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 768px) {
  .register-page { max-width: 100%; padding: 0 var(--space-md) calc(80px + env(safe-area-inset-bottom, 0px)); }
  .register-card { padding: var(--space-xl); }
  .success-card { max-width: 90%; padding: 24px; }
}
</style>
