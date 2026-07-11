<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { forgotPassword } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NCard, NForm, NFormItem, NInput, NButton, NAlert } from 'naive-ui'

const { t } = useI18n()
const authStore = useAuthStore()
const router = useRouter()
const email = ref('')

onMounted(() => { if (authStore.isAuthenticated) router.replace('/') })
const loading = ref(false)
const message = ref('')
const error = ref('')
const token = ref('')

async function handleSubmit() {
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    const res = await forgotPassword(email.value)
    if (res.data.success) {
      message.value = res.data.message
      if (res.data.token) {
        token.value = res.data.token
      }
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    error.value = err.response?.data?.message || t('common.error')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <n-card class="card">
      <h1 class="title">{{ t('login.forgot') }}</h1>
      <p class="subtitle">{{ t('pwd.forgotInstruction') }}</p>

      <n-alert v-if="error" type="error" :title="error" closable @close="error = ''" class="mb-20" />
      <n-alert v-if="message" type="success" class="mb-20">
        <div>{{ message }}</div>
        <div v-if="token" class="token-info">
          {{ t('pwd.tokenLabel') }}: {{ token }}
        </div>
      </n-alert>

      <n-form v-if="!token" @submit.prevent="handleSubmit">
        <n-form-item :label="t('register.email')">
          <n-input v-model:value="email" :placeholder="t('register.email')" size="large" />
        </n-form-item>
        <n-button type="primary" block size="large" :loading="loading" attr-type="submit">
          {{ t('pwd.sendEmail') }}
        </n-button>
      </n-form>

      <div v-if="token" class="action-center">
        <n-button type="primary" @click="router.push({ path: '/reset-password', query: { token } })">
          {{ t('pwd.goReset') }}
        </n-button>
      </div>

      <div class="back-link">
        <router-link to="/login">{{ t('common.back') }}</router-link>
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
.token-info { margin-top: 12px; word-break: break-all; font-size: 12px; color: #666; }
.action-center { margin-top: 20px; text-align: center; }
</style>
