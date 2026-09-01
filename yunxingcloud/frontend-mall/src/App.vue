<script setup lang="ts">
import { ref, onMounted, onUnmounted, onErrorCaptured, defineAsyncComponent } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '@/composables/useToast'
import { useNotification } from '@/composables/useNotification'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import MobileNav from '@/components/MobileNav.vue'
import { useThemeStore } from '@/stores/theme'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useNetworkStatus } from '@/composables/useNetworkStatus'
import { useI18n } from '@/locales'
import { createLogger } from '@/utils/logger'

const logger = createLogger('app')

const CompareFloatingBar = defineAsyncComponent(() => import('@/views/CompareView.vue'))
const ChatWidget = defineAsyncComponent(() => import('@/components/ChatWidget.vue'))

const router = useRouter()
const auth = useAuthStore()
useCartStore()
useThemeStore()
const { t } = useI18n()
const { isOnline } = useNetworkStatus()

// PWA install prompt
interface BeforeInstallEvent extends Event { prompt(): Promise<void>; userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }> }
const installPrompt = ref<BeforeInstallEvent | null>(null)
const showInstall = ref(false)
function onBeforeInstall(e: Event) { e.preventDefault(); installPrompt.value = e as BeforeInstallEvent; showInstall.value = true }
async function doInstall() { if (installPrompt.value) { installPrompt.value.prompt(); const r = await installPrompt.value.userChoice; if (r.outcome === 'accepted') showInstall.value = false; installPrompt.value = null } }
const toast = useToast()
const cartCount = ref(0)
const cartBounce = ref(false)
const showBackTop = ref(false)
const showWelcome = ref(false)
const welcomeDismissed = ref((() => { try { return localStorage.getItem('mall_welcome') } catch { return null } })())

const pushDismissed = ref((() => { try { return localStorage.getItem('mall_push_dismissed') === '1' } catch { return false } })())
const { supported: pushSupported, subscribed: pushSubscribed, request: requestPush, sendTest } = useNotification()
const showPushBanner = ref(pushSupported.value && !pushSubscribed.value && !pushDismissed.value)
function dismissPushBanner() { showPushBanner.value = false; try { localStorage.setItem('mall_push_dismissed', '1') } catch {} }

function updateCartCount() {
  if (!auth.isLoggedIn) { cartCount.value = 0; return }
  const prev = cartCount.value
  try { cartCount.value = parseInt(localStorage.getItem('cart_count') || '0') } catch { cartCount.value = 0 }
  if (cartCount.value > prev) { cartBounce.value = true; setTimeout(() => cartBounce.value = false, 400) }
}
let scrollTicking = false
function onScroll() { if (!scrollTicking) { scrollTicking = true; requestAnimationFrame(() => { showBackTop.value = window.scrollY > 400; scrollTicking = false }) } }
function scrollToTop() { window.scrollTo({ top: 0, behavior: 'smooth' }) }
function dismissWelcome() { showWelcome.value = false; try { localStorage.setItem('mall_welcome', '1') } catch {} }

async function enablePush() {
  const ok = await requestPush()
  if (ok) { showPushBanner.value = false; try { localStorage.removeItem('mall_push_dismissed') } catch {}; sendTest() }
}

const appError = ref<string | null>(null)
const errorCount = ref(0)

onErrorCaptured((err) => {
  errorCount.value++
  appError.value = err instanceof Error ? err.message : String(err)
  logger.error('ErrorBoundary', { message: err instanceof Error ? err.message : String(err) })
  return false
})

router.afterEach(() => {
  errorCount.value = 0
  appError.value = null
})


function onNetworkError() { toast.error('网络连接异常，请检查网络后重试') }

function onGlobalKeydown(e: KeyboardEvent) {
  if ((e.key === '/' || (e.key === 'k' && e.metaKey)) && !(e.target instanceof HTMLInputElement) && !(e.target instanceof HTMLTextAreaElement)) {
    e.preventDefault()
    const searchInput = document.querySelector<HTMLInputElement>('.search-input, [data-search-input]')
    searchInput?.focus()
  }
}

onMounted(() => {
  updateCartCount()
  window.addEventListener('scroll', onScroll)
  window.addEventListener('cart_updated', updateCartCount)
  window.addEventListener('api:network-error', onNetworkError)
  window.addEventListener('beforeinstallprompt', onBeforeInstall)
  window.addEventListener('keydown', onGlobalKeydown)
  if (!welcomeDismissed.value) setTimeout(() => showWelcome.value = true, 1500)
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('cart_updated', updateCartCount)
  window.removeEventListener('api:network-error', onNetworkError)
  window.removeEventListener('beforeinstallprompt', onBeforeInstall)
  window.removeEventListener('keydown', onGlobalKeydown)
})
</script>

<template>
  <div>
    <AppHeader :key="'header'" :cart-count="cartCount" />

    <!-- Offline banner -->
    <div v-if="!isOnline" class="offline-banner">{{ t('app.offlineBanner') }}</div>

    <!-- Install prompt -->
    <div v-if="showInstall" class="install-banner">
      <span>{{ t('app.installPrompt') }}</span>
      <button class="install-btn" @click="doInstall">{{ t('app.installBtn') }}</button>
      <button class="install-close" @click="showInstall = false">✕</button>
    </div>

    <!-- Main Content -->
    <Transition name="error-bounce">
    <div v-if="appError" class="error-boundary">
      <div class="error-boundary-card">
        <h2>{{ t('app.errorTitle') }}</h2>
        <p>{{ appError }}</p>
        <p v-if="errorCount > 1" class="error-hint">{{ t('app.errorHint', { n: String(errorCount) }) }}</p>
        <div class="error-boundary-btns">
          <button class="error-boundary-btn" @click="appError = null; errorCount = 0">{{ t('app.closeBtn') }}</button>
          <button class="error-boundary-btn primary" @click="router.go(0)">{{ t('app.refreshBtn') }}</button>
          <button class="error-boundary-btn" @click="appError = null; errorCount = 0; router.push('/')">{{ t('app.homeBtn') }}</button>
        </div>
      </div>
    </div>
    </Transition>

    <a href="#main-content" class="skip-link">{{ t('app.skipLink') }}</a>
    <main id="main-content" class="main-content" role="main" tabindex="-1">
      <router-view v-slot="{ Component, route: r }">
        <Transition :name="(r.meta.transition as string) || 'page-slide-left'" mode="out-in">
          <KeepAlive v-if="r.meta.keepAlive">
            <component :is="Component" :key="r.name" />
          </KeepAlive>
          <component v-else :is="Component" :key="r.path" />
        </Transition>
      </router-view>
    </main>

    <AppFooter />

    <MobileNav :cart-count="cartCount" />

    <!-- Push Banner -->
    <div v-if="showPushBanner" class="push-banner">
      <span class="push-banner-icon">🔔</span>
      <div class="push-banner-body">
        <div class="push-banner-title">{{ t('app.pushTitle') }}</div>
        <div class="push-banner-desc">{{ t('app.pushDesc') }}</div>
      </div>
      <button class="push-banner-btn" @click="enablePush">{{ t('app.pushBtn') }}</button>
      <button class="push-banner-close" @click="dismissPushBanner" :aria-label="t('app.closeAria')">✕</button>
    </div>

    <!-- Welcome Modal -->
    <div v-if="showWelcome" class="welcome-overlay" @click.self="dismissWelcome" @keydown.escape="dismissWelcome">
      <div class="welcome-card" role="dialog" aria-modal="true" :aria-label="t('app.welcomeTitle')">
        <button class="welcome-close" @click="dismissWelcome" :aria-label="t('app.closeAria')">✕</button>
        <div class="welcome-emoji">🛍️</div>
        <h2 class="welcome-title">{{ t('app.welcomeTitle') }}</h2>
        <p class="welcome-subtitle">{{ t('app.welcomeSubtitle') }}</p>
        <div class="welcome-actions">
          <button class="welcome-cta" @click="dismissWelcome();router.push('/products')">{{ t('common.allProducts') }}</button>
        </div>
        <p class="welcome-skip" @click="dismissWelcome">{{ t('app.welcomeSkip') }}</p>
      </div>
    </div>

    <!-- Back to Top -->
    <button class="back-to-top" :class="{ 'back-to-top--visible': showBackTop }" @click="scrollToTop" aria-label="回到顶部">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 15l-6-6-6 6"/></svg>
    </button>

    <!-- Compare -->
    <CompareFloatingBar />

    <!-- Chat -->
    <ChatWidget />

    <!-- Toast -->
    <TransitionGroup name="toast" tag="div" class="toast-container" role="status" aria-live="polite" aria-atomic="true">
      <div v-for="t in toast.toasts.value" :key="t.id" class="toast-item" :class="`toast-${t.type}`">
        <span>{{ toast.icons[t.type] || 'ℹ️' }}</span>
        <span class="toast-msg">{{ t.message }}</span>
        <button class="toast-dismiss" @click="toast.dismiss(t.id)" aria-label="关闭">✕</button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style>
.page-slide-left-enter-active { transition: opacity .25s ease, transform .25s ease; }
.page-slide-left-leave-active { transition: opacity .18s ease, transform .18s ease; }
.page-slide-left-enter-from { opacity: 0; transform: translateX(30px); }
.page-slide-left-leave-to { opacity: 0; transform: translateX(-30px); }

.page-slide-right-enter-active { transition: opacity .25s ease, transform .25s ease; }
.page-slide-right-leave-active { transition: opacity .18s ease, transform .18s ease; }
.page-slide-right-enter-from { opacity: 0; transform: translateX(-30px); }
.page-slide-right-leave-to { opacity: 0; transform: translateX(30px); }

.skip-link { position: absolute; top: -100px; left: 0; background: var(--jd-red); color: #fff; padding: 8px 16px; z-index: 10000; border-radius: 0 0 var(--radius-sm) 0; text-decoration: none; font-size: var(--font-sm); }
.skip-link:focus { top: 0; }

[data-theme="dark"] input,
[data-theme="dark"] textarea,
[data-theme="dark"] select {
  background: var(--bg-white);
  color: var(--text-primary);
  border-color: var(--border);
}
</style>

<style scoped>
.main-content {
  min-height: calc(100vh - 180px);
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-lg) var(--space-xl);
}

/* Push Banner */
.push-banner {
  position: fixed; bottom: 100px; left: 50%; transform: translateX(-50%); z-index: 180;
  background: var(--bg-white); border-radius: var(--radius-lg); box-shadow: var(--shadow-lg);
  padding: 14px var(--space-xl); display: flex; align-items: center; gap: var(--space-md);
  max-width: 420px; animation: slideUp .4s ease-out;
}
.push-banner-icon { font-size: var(--font-xxl); }
.push-banner-body { flex: 1; }
.push-banner-title { font-weight: 600; font-size: var(--font-base);}
.push-banner-desc { color: var(--text-tertiary); font-size: var(--font-xs); }
.push-banner-btn {
  padding: 6px var(--space-lg); background: var(--bg-white); color: var(--text-primary);
  border: 1px solid var(--border); border-radius: var(--radius-round); cursor: pointer;
  font-size: var(--font-sm); font-weight: 600; white-space: nowrap;
  transition: all var(--transition-fast);
}
.push-banner-btn:hover { border-color: var(--jd-red); color: var(--jd-red); }
.push-banner-close {
  background: none; border: none; color: var(--text-tertiary); cursor: pointer; font-size: var(--font-md); padding: 2px;
}

/* Welcome Modal */
.welcome-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: var(--bg-overlay); display: flex; align-items: center; justify-content: center;
  z-index: 999; animation: fadeIn .3s;
}
.welcome-card {
  background: var(--bg-white); border-radius: var(--radius-xl); padding: 40px 32px 28px;
  text-align: center; max-width: 380px; width: 90%; position: relative;
  animation: slideUp .4s ease-out;
}
.welcome-close {
  position: absolute; top: var(--space-md); right: var(--space-lg);
  background: none; border: none; font-size: var(--font-xl); color: var(--text-tertiary); cursor: pointer;
}
.welcome-emoji { font-size: 56px; margin-bottom: var(--space-md); }
.welcome-title { font-size: var(--font-title); font-weight: 800; margin-bottom: var(--space-sm); }
.welcome-subtitle { color: var(--text-secondary); font-size: var(--font-md); margin-bottom: 6px; }
.welcome-actions { margin: var(--space-lg) 0; }
.welcome-cta {
  width: 100%; height: 44px; background: var(--jd-red); color: #fff; border: none;
  border-radius: var(--radius-round); font-size: var(--font-lg); cursor: pointer; font-weight: 700;
  transition: background var(--transition-fast);
}
.welcome-cta:hover { background: var(--jd-red-dark); }
.welcome-skip { margin-top: var(--space-md); font-size: var(--font-sm); color: var(--text-tertiary); cursor: pointer; }

/* Back to Top */
.back-to-top {
  position: fixed; bottom: 120px; right: 16px; z-index: 150; width: 44px; height: 44px;
  border-radius: 50%; background: rgba(241,2,21,.85); color: #fff; border: none; cursor: pointer;
  box-shadow: 0 4px 16px rgba(241,2,21,.3); display: flex; align-items: center; justify-content: center;
  transition: transform .25s cubic-bezier(.4,0,.2,1), opacity .3s, box-shadow .3s;
  opacity: 0; transform: translateY(20px);
  backdrop-filter: blur(4px); -webkit-backdrop-filter: blur(4px);
}
.back-to-top:hover { transform: translateY(-4px) scale(1.1); background: var(--jd-red); box-shadow: 0 6px 20px rgba(241,2,21,.5); opacity: 1; }
.back-to-top--visible { opacity: .85; transform: translateY(0); }

/* Toast */
.toast-container {
  position: fixed; top: 60px; right: var(--space-xl); z-index: 9999;
  display: flex; flex-direction: column-reverse; gap: var(--space-sm); pointer-events: none;
}
@media (max-width: 768px) {
  .toast-container {
    top: auto; bottom: 80px; right: 50%; transform: translateX(50%);
    align-items: center;
  }
}
.toast-item {
  padding: var(--space-md) var(--space-xl); border-radius: var(--radius-md);
  font-size: var(--font-md); color: #fff; box-shadow: var(--shadow-lg);
  min-width: 200px; max-width: 360px;
  display: flex; align-items: center; gap: var(--space-sm); pointer-events: auto;
  -webkit-backdrop-filter: blur(6px);
  backdrop-filter: blur(6px);
}
.toast-success { background: rgba(82,196,26,.92); }
.toast-error { background: rgba(241,2,21,.92); }
.toast-warning { background: rgba(255,152,0,.92); }
.toast-info { background: rgba(22,119,255,.92); }
.toast-msg { flex: 1; }
.toast-dismiss { background: none; border: none; color: rgba(255,255,255,.7); cursor: pointer; font-size: var(--font-md); padding: 0; line-height: 1; }

.toast-enter-active { animation: toastIn .35s cubic-bezier(.21,1.02,.73,1); }
.toast-leave-active { animation: toastOut .2s ease-in forwards; }
@keyframes toastIn { from { opacity: 0; transform: translateX(80px) scale(.9); } to { opacity: 1; transform: translateX(0) scale(1); } }
@keyframes toastOut { to { opacity: 0; transform: translateX(80px) scale(.9); } }

@keyframes fadeIn { from { opacity: 0; transform: translateX(20px); } to { opacity: 1; transform: translateX(0); } }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.error-bounce-enter-active { animation: errorBounceIn .4s ease-out; }
.error-bounce-leave-active { animation: errorBounceOut .2s ease-in forwards; }
@keyframes errorBounceIn { from { opacity: 0; transform: scale(.95); } to { opacity: 1; transform: scale(1); } }
@keyframes errorBounceOut { to { opacity: 0; transform: scale(.95); } }

.error-boundary { display: flex; justify-content: center; align-items: center; min-height: 60vh; padding: var(--space-xxl); }
.error-boundary-card { text-align: center; background: var(--bg-white); padding: var(--space-xxxl); border-radius: var(--radius-lg); box-shadow: var(--shadow-md); max-width: 400px; }
.error-boundary-card h2 { font-size: var(--font-xl); color: var(--jd-red); margin-bottom: var(--space-md); }
.error-boundary-card p { color: var(--text-secondary); font-size: var(--font-base); margin-bottom: var(--space-xl); word-break: break-word; }
.error-hint { color: var(--text-tertiary) !important; font-size: var(--font-sm) !important; }
.error-boundary-btns { display: flex; gap: var(--space-sm); justify-content: center; flex-wrap: wrap; }
.error-boundary-btn { padding: 10px 24px; border: 1px solid var(--border); background: var(--bg-white); color: var(--text-primary); border-radius: var(--radius-md); cursor: pointer; font-size: var(--font-md); }
.error-boundary-btn.primary { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }

/* Screen reader only utility */
.sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0,0,0,0); white-space: nowrap; border: 0; }

/* Offline banner */
.offline-banner {
  text-align: center; padding: var(--space-sm); background: #fff3cd;
  color: #856404; font-size: 13px; font-weight: 600;
}

/* Install banner */
.install-banner {
  display: flex; align-items: center; gap: var(--space-md); padding: var(--space-sm) var(--space-lg);
  background: var(--jd-red); color: #fff; font-size: 14px;
}
.install-banner span { flex: 1; }
.install-btn {
  padding: 4px 16px; background: #fff; color: var(--jd-red); border: none;
  border-radius: var(--radius-round); font-size: 13px; font-weight: 600; cursor: pointer;
}
.install-close {
  background: none; border: none; color: rgba(255,255,255,.7); cursor: pointer; font-size: 16px;
}

/* 平板竖屏 */
@media (min-width: 769px) and (max-width: 1024px) {
  .main-content { padding: var(--space-md) var(--space-xl); }
}
/* 手机 */
@media (max-width: 768px) {
  .main-content { padding: var(--space-sm) var(--space-sm) calc(70px + env(safe-area-inset-bottom, 0px)); }
  .push-banner { bottom: 80px; max-width: 90%; }
}
/* 大屏 */
@media (min-width: 1400px) {
  .main-content { max-width: 1320px; }
}
.router-loading { display: flex; justify-content: center; align-items: center; min-height: 50vh; }
.router-loading-spinner { width: 32px; height: 32px; border: 3px solid var(--border-light); border-top-color: var(--jd-red); border-radius: 50%; animation: spin .6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
