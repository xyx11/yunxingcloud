<script setup lang="ts">
import { ref, inject, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'

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
  <div style="max-width:420px;margin:40px auto">
    <div style="background:#fff;border-radius:12px;padding:40px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <div style="text-align:center;margin-bottom:24px">
        <div style="font-size:48px;margin-bottom:8px">🎉</div>
        <h2 style="font-size:22px;margin-bottom:4px">{{ t('register.title') }}</h2>
        <p style="color:#999;font-size:13px">{{ t('register.subtitle') }}</p>
      </div>
      <div v-if="error" style="background:#fff0f0;color:#f10215;padding:10px 12px;border-radius:6px;font-size:13px;margin-bottom:16px">{{ error }}</div>
      <div style="margin-bottom:16px">
        <label style="display:block;font-size:13px;color:#666;margin-bottom:6px">{{ t('register.username') }}</label>
        <input v-model="form.username" :placeholder="t('register.placeholderUser')" style="width:100%;padding:12px;border:1px solid #ddd;border-radius:6px;font-size:14px;box-sizing:border-box;outline:none"
               @focus="(e:any) => e.target.style.borderColor='#f10215'" @blur="(e:any) => e.target.style.borderColor='#ddd'" />
      </div>
      <div style="margin-bottom:16px">
        <label style="display:block;font-size:13px;color:#666;margin-bottom:6px">{{ t('register.password') }} <span style="color:#999;font-weight:400">(8位+大小写+特殊字符)</span></label>
        <input v-model="form.password" type="password" :placeholder="t('register.placeholderPass')" style="width:100%;padding:12px;border:1px solid #ddd;border-radius:6px;font-size:14px;box-sizing:border-box;outline:none"
               @focus="(e:any) => e.target.style.borderColor='#f10215'" @blur="(e:any) => e.target.style.borderColor='#ddd'" />
        <div v-if="form.password" style="margin-top:8px;display:flex;gap:6px;font-size:11px">
          <span :style="{color:pwdChecks.len?'#18a058':'#ccc'}">{{ pwdChecks.len ? '✓' : '○' }}8位</span>
          <span :style="{color:pwdChecks.upper?'#18a058':'#ccc'}">{{ pwdChecks.upper ? '✓' : '○' }}大写</span>
          <span :style="{color:pwdChecks.digit?'#18a058':'#ccc'}">{{ pwdChecks.digit ? '✓' : '○' }}数字</span>
          <span :style="{color:pwdChecks.lower?'#18a058':'#ccc'}">{{ pwdChecks.lower ? '✓' : '○' }}小写</span>
          <span :style="{color:pwdChecks.special?'#18a058':'#ccc'}">{{ pwdChecks.special ? '✓' : '○' }}特殊字符</span>
          <span style="margin-left:auto;font-weight:600" :style="{color:pwdStrength==='strong'?'#18a058':pwdStrength==='good'?'#2080f0':pwdStrength==='fair'?'#f0a020':'#ccc'}">{{ {weak:'弱',fair:'一般',good:'良好',strong:'强'}[pwdStrength] }}</span>
        </div>
      </div>
      <div style="margin-bottom:24px">
        <label style="display:block;font-size:13px;color:#666;margin-bottom:6px">{{ t('register.confirmPassword') }}</label>
        <input v-model="form.confirmPassword" type="password" :placeholder="t('register.placeholderConfirm')" @keyup.enter="doRegister" style="width:100%;padding:12px;border:1px solid #ddd;border-radius:6px;font-size:14px;box-sizing:border-box;outline:none"
               @focus="(e:any) => e.target.style.borderColor='#f10215'" @blur="(e:any) => e.target.style.borderColor='#ddd'" />
      </div>
      <div style="background:linear-gradient(135deg,#fff5f5,#fff0f0);border-radius:8px;padding:12px 16px;margin-bottom:20px">
        <p style="font-size:12px;font-weight:600;color:#f10215;margin-bottom:6px">🎁 {{ t('register.benefits') }}</p>
        <p style="font-size:11px;color:#666;margin:2px 0">· {{ t('register.benefit1') }}</p>
        <p style="font-size:11px;color:#666;margin:2px 0">· {{ t('register.benefit2') }}</p>
        <p style="font-size:11px;color:#666;margin:2px 0">· {{ t('register.benefit3') }}</p>
      </div>
      <button @click="doRegister" :disabled="loading" style="width:100%;height:44px;background:#f10215;color:#fff;border:none;border-radius:8px;font-size:16px;cursor:pointer;font-weight:600;transition:opacity .2s" :style="{opacity:loading?'.7':'1'}">
        {{ loading ? t('register.submitting') : t('register.submit') }}
      </button>
      <p style="text-align:center;margin-top:16px;font-size:13px;color:#999">{{ t('register.hasAccount') }}<span @click="router.push('/login')" style="color:#f10215;cursor:pointer">{{ t('register.goLogin') }}</span></p>
      <!-- 注册成功弹窗 -->
      <div v-if="showSuccess" style="position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,.4);display:flex;align-items:center;justify-content:center;z-index:200">
        <div style="background:#fff;border-radius:12px;padding:40px;text-align:center;max-width:360px;width:90%">
          <div style="font-size:56px;margin-bottom:12px">🎉</div>
          <h3 style="margin-bottom:8px">注册成功！</h3>
          <p style="color:#666;font-size:14px;margin-bottom:24px">账号已提交审核，审核通过后即可登录</p>
          <button @click="router.push('/login')" style="width:100%;height:44px;background:#f10215;color:#fff;border:none;border-radius:8px;font-size:15px;cursor:pointer;font-weight:600">前往登录</button>
        </div>
      </div>
    </div>
  </div></template>
