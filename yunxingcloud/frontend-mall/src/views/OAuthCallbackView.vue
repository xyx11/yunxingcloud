<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/locales'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const { t } = useI18n()

onMounted(async () => {
  const token = route.query.token as string
  const username = route.query.username as string
  const nickname = route.query.nickname as string

  if (token && username) {
    try {
      auth.setToken(token, { username, nickname })
      router.replace('/')
    } catch { router.replace('/login') }
  } else {
    router.replace('/login')
  }
})
</script>

<template>
  <div class="oauth-callback">
    <div class="oauth-spinner" />
    <p>{{ t('oauth.loggingIn') }}</p>
  </div>
</template>

<style scoped>
.oauth-callback { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 60vh; gap: var(--space-lg); }
.oauth-spinner { width: 40px; height: 40px; border: 3px solid var(--border-light); border-top-color: var(--jd-red); border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .oauth-callback { min-height: 50vh; padding: var(--space-lg) var(--space-md); }
}
</style>
