<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { register } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import type { FormInst, FormItemRule, FormRules } from 'naive-ui'
import { NCard, NForm, NFormItem, NInput, NButton, NAlert } from 'naive-ui'

const { t } = useI18n()
const authStore = useAuthStore()
const router = useRouter()

const formRef = ref<FormInst | null>(null)
const model = ref({ username: '', password: '', confirmPassword: '', email: '' })
const error = ref('')
const success = ref('')
const loading = ref(false)
const passwordStrength = computed(() => {
  const p = model.value.password
  let score = 0
  if (p.length >= 8) score++
  if (p.length >= 12) score++
  if (/[A-Z]/.test(p)) score++
  if (/[a-z]/.test(p)) score++
  if (/[0-9]/.test(p)) score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  return { score, pct: Math.min(100, score * 17), color: score <= 2 ? '#f5576c' : score <= 4 ? '#f093fb' : '#43e97b', label: score <= 2 ? t('passwordStrength.weak') : score <= 4 ? t('passwordStrength.medium') : t('passwordStrength.strong') }
})

const rules: FormRules = {
  username: [
    { required: true, message: () => t('login.usernameRequired'), trigger: 'blur' },
    { min: 3, max: 50, message: () => t('validate.usernameLen'), trigger: 'blur' },
  ],
  password: [
    { required: true, message: () => t('login.passwordRequired'), trigger: 'blur' },
    { min: 8, message: () => t('validate.passwordMinLen'), trigger: 'blur' },
    {
      validator: (_rule: FormItemRule, value: string) => /[A-Z]/.test(value),
      message: () => t('validate.passwordNeedUpper'), trigger: 'blur',
    },
    {
      validator: (_rule: FormItemRule, value: string) => /[a-z]/.test(value),
      message: () => t('validate.passwordNeedLower'), trigger: 'blur',
    },
    {
      validator: (_rule: FormItemRule, value: string) => /[0-9]/.test(value),
      message: () => t('validate.passwordNeedDigit'), trigger: 'blur',
    },
    {
      validator: (_rule: FormItemRule, value: string) => /[!@#$%^&*(),.?":{}|<>]/.test(value),
      message: () => t('validate.passwordNeedSpecial'), trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: () => t('validate.confirmRequired'), trigger: 'blur' },
    {
      validator: (_rule: FormItemRule, value: string) => value === model.value.password,
      message: () => t('validate.passwordMismatch'),
      trigger: 'blur',
    },
  ],
  email: [
    { type: 'email', message: () => t('validate.emailValid'), trigger: 'blur' },
  ],
}

onMounted(() => { if (authStore.isAuthenticated) router.replace('/') })

async function handleRegister() {
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    const res = await register({
      username: model.value.username,
      password: model.value.password,
      email: model.value.email,
    })
    if (res.data.success) {
      if (res.data.approved === false) {
        success.value = t('register.pendingApproval')
      } else {
        success.value = t('register.success')
        setTimeout(() => { router.push('/') }, 1000)
      }
    } else {
      error.value = res.data.message || t('validate.registerFailed')
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    error.value = err.response?.data?.message || t('validate.registerFailed')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <n-card class="register-card">
      <h1 class="title">{{ t('register.title') }}</h1>
      <p class="subtitle">{{ t('app.subtitle') }}</p>

      <n-alert v-if="error" type="error" :title="error" closable @close="error = ''" class="mb-20" />
      <n-alert v-if="success" type="success" :title="success" class="mb-20" />

      <n-form ref="formRef" :model="model" :rules="rules" @submit.prevent="handleRegister">
        <n-form-item :label="t('register.username')" path="username">
          <n-input v-model:value="model.username" placeholder="3-50" size="large" />
        </n-form-item>
        <n-form-item :label="t('register.email')" path="email">
          <n-input v-model:value="model.email" :placeholder="t('register.email')" size="large" />
        </n-form-item>
        <n-form-item :label="t('register.password')" path="password">
          <n-input v-model:value="model.password" type="password" placeholder="8+ chars" size="large" show-password-on="click" />
          <div v-if="model.password" class="mt-4">
            <div class="password-meter">
              <div :style="{width:passwordStrength.pct+'%',height:'100%',background:passwordStrength.color,transition:'all .3s'}" />
            </div>
            <span :style="{fontSize:'11px',color:passwordStrength.color,float:'right'}">{{ passwordStrength.label }}</span>
          </div>
        </n-form-item>
        <n-form-item :label="t('register.confirmPassword')" path="confirmPassword">
          <n-input v-model:value="model.confirmPassword" type="password" placeholder="8+ chars" size="large" show-password-on="click" />
        </n-form-item>
        <n-button type="primary" block size="large" :loading="loading" attr-type="submit">
          {{ t('register.submit') }}
        </n-button>
      </n-form>

      <div class="login-link">
        {{ t('register.hasAccount') }}
        <router-link to="/login" class="link">{{ t('login.title') }}</router-link>
      </div>
    </n-card>
  </div>
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
  color: var(--n-text-color, #333);
  margin-bottom: 4px;
  font-size: 24px;
}

.subtitle {
  text-align: center;
  color: var(--n-text-color-3, #888);
  margin-bottom: 28px;
  font-size: 14px;
}

.login-link {
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
.mb-20 { margin-bottom: 20px; }
.mt-4 { margin-top: 4px; }
.password-meter { height: 4px; border-radius: 2px; background: var(--n-action-color, #eee); overflow: hidden; }
</style>
