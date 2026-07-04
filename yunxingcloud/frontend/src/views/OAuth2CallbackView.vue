<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NConfigProvider, NResult, NButton, NSpin } from 'naive-ui'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const status = ref<'loading' | 'success' | 'error'>('loading')
const message = ref('')

onMounted(() => {
  const token = route.query.token as string
  const username = route.query.username as string
  const nickname = route.query.nickname as string

  if (token && username) {
    localStorage.setItem('accessToken', token)
    if (nickname) localStorage.setItem('nickname', nickname)
    status.value = 'success'
    message.value = nickname || username
    setTimeout(() => router.push('/'), 1500)
  } else {
    status.value = 'error'
    message.value = t('auth.oauthFailed')
  }
})
</script>

<template>
  <n-config-provider>
    <div style="min-height:100vh;display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,#667eea,#764ba2)">
      <n-result
        v-if="status === 'loading'"
        :title="t('auth.loggingIn')"
        :description="t('auth.verifying')"
      >
        <template #icon>
          <n-spin size="large" />
        </template>
      </n-result>
      <n-result
        v-else-if="status === 'success'"
        status="success"
        :title="t('auth.welcomeBack', {name: message})"
        :description="t('auth.loginRedirect')"
      />
      <n-result
        v-else
        status="error"
        :title="t('auth.loginFailed')"
        :description="message"
      >
        <template #footer>
          <n-button type="primary" @click="router.push('/login')">{{ t('auth.backToLogin') }}</n-button>
        </template>
      </n-result>
    </div>
  </n-config-provider>
</template>
