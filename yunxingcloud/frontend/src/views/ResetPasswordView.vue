<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { resetPassword } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NCard, NForm, NFormItem, NInput, NButton, NAlert } from 'naive-ui'

const { t } = useI18n()
const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const token = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')

onMounted(() => {
  if (authStore.isAuthenticated) { router.replace('/'); return }
  const t = route.query.token as string
  if (t) {
    token.value = t
  }
})

async function handleSubmit() {
  if (newPassword.value.length < 8) {
    error.value = t('validate.passwordMinLen')
    return
  }
  if (!/[A-Z]/.test(newPassword.value) || !/[a-z]/.test(newPassword.value) || !/[0-9]/.test(newPassword.value)) {
    error.value = t('validate.passwordComplexity')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    error.value = t('validate.passwordMismatch')
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await resetPassword(token.value, newPassword.value)
    if (res.data.success) {
      success.value = res.data.message
      setTimeout(() => router.push('/login'), 2000)
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    error.value = err.response?.data?.message || t('pwd.resetFailed')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <n-card class="card">
      <h1 class="title">{{ t('pwd.reset') }}</h1>
      <p class="subtitle">{{ t('pwd.change') }}</p>

      <n-alert v-if="error" type="error" :title="error" closable @close="error = ''" class="mb-20" />
      <n-alert v-if="success" type="success" :title="success" class="mb-20" />

      <n-form v-if="!success" @submit.prevent="handleSubmit">
        <n-form-item :label="t('pwd.token')">
          <n-input v-model:value="token" :placeholder="t('pwd.tokenPlaceholder')" size="large" />
        </n-form-item>
        <n-form-item :label="t('pwd.new')">
          <n-input v-model:value="newPassword" type="password" :placeholder="t('pwd.newPasswordPlaceholder')" size="large" show-password-on="click" />
        </n-form-item>
        <n-form-item :label="t('pwd.confirm')">
          <n-input v-model:value="confirmPassword" type="password" :placeholder="t('pwd.confirmPlaceholder')" size="large" show-password-on="click" />
        </n-form-item>
        <n-button type="primary" block size="large" :loading="loading" attr-type="submit">
          {{ t('pwd.reset') }}
        </n-button>
      </n-form>

      <div class="back-link">
        <router-link to="/login">{{ t('login.backToLogin') }}</router-link>
      </div>
    </n-card>
  </div>
</template>

<style scoped>
.page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.card { width: 420px; border-radius: 12px; padding: 8px; }
.title { text-align: center; color: var(--n-text-color, #333); margin-bottom: 4px; font-size: 24px; }
.subtitle { text-align: center; color: var(--n-text-color-3, #888); margin-bottom: 28px; font-size: 14px; }
.back-link { text-align: center; margin-top: 16px; font-size: 13px; }
.back-link a { color: var(--primary-color, #667eea); text-decoration: none; }
.mb-20 { margin-bottom: 20px; }
</style>
