<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from '@/locales'

const { t } = useI18n()
const show = ref(false)
const dismissed = ref(false)
const isIos = ref(false)
const installEvent = ref<any>(null)

onMounted(() => {
  isIos.value = /iphone|ipad|ipod/.test(navigator.userAgent.toLowerCase())

  // Check if already installed (standalone mode)
  if (window.matchMedia('(display-mode: standalone)').matches) return

  // Listen for beforeinstallprompt
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault()
    installEvent.value = e
    // Only show if not recently dismissed
    const lastDismiss = localStorage.getItem('app_banner_dismissed')
    if (!lastDismiss || Date.now() - parseInt(lastDismiss) > 7 * 86400000) {
      show.value = true
    }
  })

  // Show for iOS after delay if not in standalone
  if (isIos.value) {
    const lastDismiss = localStorage.getItem('app_banner_dismissed')
    if (!lastDismiss || Date.now() - parseInt(lastDismiss) > 7 * 86400000) {
      setTimeout(() => { show.value = true }, 3000)
    }
  }
})

async function install() {
  if (!installEvent.value) return
  try {
    await installEvent.value.prompt()
    const result = await installEvent.value.userChoice
    if (result.outcome === 'accepted') show.value = false
  } catch {}
}

function dismiss() {
  show.value = false
  dismissed.value = true
  localStorage.setItem('app_banner_dismissed', String(Date.now()))
}
</script>

<template>
  <div v-if="show" class="ab-banner">
    <button class="ab-close" @click="dismiss" aria-label="关闭">✕</button>
    <span class="ab-icon">📱</span>
    <div class="ab-text">
      <strong>{{ t('app.installTitle') }}</strong>
      <span v-if="isIos" v-html="t('app.installIosTip')" />
      <span v-else>{{ t('app.installDesc') }}</span>
    </div>
    <button v-if="!isIos" class="ab-install-btn" @click="install">{{ t('app.installBtn') }}</button>
  </div>
</template>

<style scoped>
.ab-banner {
  position: fixed; bottom: 70px; left: 16px; right: 16px; z-index: 299;
  background: var(--bg-white); border-radius: var(--radius-lg);
  box-shadow: 0 4px 24px rgba(0,0,0,.15); padding: var(--space-md) var(--space-lg);
  display: flex; align-items: center; gap: var(--space-md);
  animation: slideUp .4s ease-out;
}
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
.ab-close { position: absolute; top: 6px; right: 10px; background: none; border: none; color: var(--text-tertiary); cursor: pointer; font-size: 14px; }
.ab-icon { font-size: 32px; flex-shrink: 0; }
.ab-text { flex: 1; display: flex; flex-direction: column; gap: 2px; font-size: var(--font-sm); color: var(--text-secondary); }
.ab-text strong { font-size: var(--font-md); color: var(--text-primary); }
.ab-install-btn {
  padding: 8px 20px; background: var(--jd-red); color: #fff; border: none;
  border-radius: var(--radius-round); cursor: pointer; font-weight: 600;
  font-size: var(--font-sm); white-space: nowrap; flex-shrink: 0;
}
.ab-install-btn:hover { background: #d63434; }

@media (max-width: 768px) {
  .ab-banner { bottom: calc(60px + env(safe-area-inset-bottom, 0px)); left: 10px; right: 10px; }
}
</style>