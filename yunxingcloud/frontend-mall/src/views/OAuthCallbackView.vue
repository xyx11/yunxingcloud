<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/locales'
import JdButton from '@/components/JdButton.vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const { t } = useI18n()
const timedOut = ref(false)
const errorMsg = ref('')
let timer: ReturnType<typeof setTimeout> | null = null

async function handleCallback() {
  const token = route.query.token as string
  const username = route.query.username as string
  const nickname = route.query.nickname as string

  if (token && username) {
    try {
      auth.setToken(token, { username, nickname })
      router.replace('/')
    } catch {
      errorMsg.value = t('oauth.invalidLogin')
      if (timer) clearTimeout(timer)
    }
  } else {
    errorMsg.value = t('oauth.missingCredential')
    if (timer) clearTimeout(timer)
  }
}

function onOnline() {
  if (timedOut.value || errorMsg.value) {
    timedOut.value = false
    errorMsg.value = ''
    timer = setTimeout(() => { timedOut.value = true }, 8000)
    handleCallback()
  }
}

onMounted(() => {
  timer = setTimeout(() => { timedOut.value = true }, 8000)
  handleCallback()
  window.addEventListener('online', onOnline)
})

onUnmounted(() => {
  if (timer) clearTimeout(timer)
  window.removeEventListener('online', onOnline)
})

function goLogin() { router.replace('/login') }
function goHome() { router.replace('/') }
</script>

<template>
  <div class="oauth-callback">
    <!-- Loading -->
    <template v-if="!timedOut && !errorMsg">
      <div class="oauth-spinner" />
      <p class="oauth-status">{{ t('oauth.loggingIn') }}</p>
      <p class="oauth-hint">{{ t('oauth.hint') }}</p>
    </template>

    <!-- Timeout -->
    <template v-else-if="timedOut && !errorMsg">
      <div class="oauth-icon">⏰</div>
      <h3 class="oauth-title">{{ t('oauth.timeout') }}</h3>
      <p class="oauth-desc">{{ t('oauth.timeoutDesc') }}</p>
      <div class="oauth-actions">
        <JdButton type="outline" @click="goHome">{{ t('oauth.goHome') }}</JdButton>
        <JdButton @click="goLogin">{{ t('oauth.retryLogin') }}</JdButton>
      </div>
    </template>

    <!-- Error -->
    <template v-else>
      <div class="oauth-icon">❌</div>
      <h3 class="oauth-title">{{ t('oauth.failed') }}</h3>
      <p class="oauth-desc">{{ errorMsg }}</p>
      <div class="oauth-actions">
        <JdButton type="outline" @click="goHome">{{ t('oauth.goHome') }}</JdButton>
        <JdButton @click="goLogin">{{ t('oauth.retryLogin') }}</JdButton>
      </div>
    </template>
  </div>
</template>

<style scoped>
.oauth-callback { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 60vh; gap: var(--space-md); text-align: center; }
.oauth-spinner { width: 44px; height: 44px; border: 3px solid var(--border-light); border-top-color: var(--jd-red); border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.oauth-status { font-size: var(--font-md); color: var(--text-secondary); font-weight: 600; }
.oauth-hint { font-size: var(--font-sm); color: var(--text-placeholder); }
.oauth-icon { font-size: 52px; }
.oauth-title { font-size: var(--font-xl); font-weight: 700; }
.oauth-desc { font-size: var(--font-md); color: var(--text-secondary); }
.oauth-actions { display: flex; gap: var(--space-md); margin-top: var(--space-lg); }

@media (max-width: 768px) {
  .oauth-callback { min-height: 50vh; padding: var(--space-lg) var(--space-md); }
}
</style>
