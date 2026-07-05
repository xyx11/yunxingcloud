<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGlobalToast } from '@/composables/useToast'
import { useNotification } from '@/composables/useNotification'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import MobileNav from '@/components/MobileNav.vue'
import CompareFloatingBar from '@/views/CompareView.vue'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
useThemeStore()
const toast = useGlobalToast()
const cartCount = ref(0)
const showBackTop = ref(false)
const showWelcome = ref(false)
const welcomeDismissed = ref((() => { try { return localStorage.getItem('mall_welcome') } catch { return '1' } })())

const { supported: pushSupported, subscribed: pushSubscribed, request: requestPush, sendTest } = useNotification()
const showPushBanner = ref(pushSupported.value && !pushSubscribed.value)

function updateCartCount() {
  try { cartCount.value = JSON.parse(localStorage.getItem('cart_count') || '0') } catch { cartCount.value = 0 }
}
function onScroll() { showBackTop.value = window.scrollY > 400 }
function scrollToTop() { window.scrollTo({ top: 0, behavior: 'smooth' }) }
function dismissWelcome() { showWelcome.value = false; try { localStorage.setItem('mall_welcome', '1') } catch {} }

async function enablePush() {
  const ok = await requestPush()
  if (ok) { showPushBanner.value = false; sendTest() }
}

useThemeStore() // 初始化并应用主题

onMounted(() => {
  updateCartCount()
  window.addEventListener('scroll', onScroll)
  window.addEventListener('cart_updated', updateCartCount)
  if (!welcomeDismissed.value) setTimeout(() => showWelcome.value = true, 1500)
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('cart_updated', updateCartCount)
})
</script>

<template>
  <div>
    <AppHeader :key="'header'" :cart-count="cartCount" />

    <!-- Main Content -->
    <a href="#main-content" class="skip-link">跳转到主要内容</a>
    <main id="main-content" class="main-content" role="main" tabindex="-1">
      <router-view v-slot="{ Component, route: r }">
        <Transition name="page-fade" mode="out-in">
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
        <div class="push-banner-title">开启通知</div>
        <div class="push-banner-desc">第一时间获取促销和订单状态</div>
      </div>
      <button class="push-banner-btn" @click="enablePush">开启</button>
      <button class="push-banner-close" @click="showPushBanner = false" aria-label="关闭">✕</button>
    </div>

    <!-- Welcome Modal -->
    <div v-if="showWelcome" class="welcome-overlay" @click.self="dismissWelcome" @keydown.escape="dismissWelcome">
      <div class="welcome-card" role="dialog" aria-modal="true" aria-label="欢迎弹窗">
        <button class="welcome-close" @click="dismissWelcome" aria-label="关闭">✕</button>
        <div class="welcome-emoji">🎉</div>
        <h2 class="welcome-title">欢迎来到 YXCLOUD</h2>
        <p class="welcome-subtitle">品质生活，一站购齐</p>
        <div class="welcome-coupon">
          <div class="welcome-coupon-price">¥50</div>
          <div class="welcome-coupon-label">新用户专属优惠券礼包</div>
        </div>
        <button class="welcome-cta" @click="dismissWelcome();router.push('/coupons')">🎁 立即领取</button>
        <p class="welcome-skip" @click="dismissWelcome">暂不领取，随便逛逛</p>
      </div>
    </div>

    <!-- Back to Top -->
    <button v-if="showBackTop" class="back-to-top" @click="scrollToTop" aria-label="回到顶部">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 15l-6-6-6 6"/></svg>
    </button>

    <!-- Compare -->
    <CompareFloatingBar />

    <!-- Toast -->
    <TransitionGroup name="toast" tag="div" class="toast-container">
      <div v-for="t in toast.toasts.value" :key="t.id" class="toast-item" :class="`toast-${t.type}`">
        <span>{{ toast.icons[t.type] || 'ℹ️' }}</span>
        <span class="toast-msg">{{ t.message }}</span>
        <button class="toast-dismiss" @click="toast.dismiss(t.id)" aria-label="关闭">✕</button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style>
.page-fade-enter-active { transition: opacity .2s ease, transform .2s ease; }
.page-fade-leave-active { transition: opacity .15s ease, transform .15s ease; }
.page-fade-enter-from { opacity: 0; transform: translateY(12px); }
.page-fade-leave-to { opacity: 0; transform: translateY(-8px); }

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
.welcome-coupon {
  background: linear-gradient(135deg, var(--jd-red-light), var(--jd-red-bg));
  border-radius: var(--radius-lg); padding: var(--space-lg); margin: var(--space-lg) 0;
}
.welcome-coupon-price { font-size: var(--font-h1); font-weight: 800; color: var(--jd-red); }
.welcome-coupon-label { font-size: var(--font-sm); color: var(--text-tertiary); margin-top: 2px; }
.welcome-cta {
  width: 100%; height: 44px; background: var(--jd-red); color: #fff; border: none;
  border-radius: var(--radius-round); font-size: var(--font-lg); cursor: pointer; font-weight: 700;
  transition: background var(--transition-fast);
}
.welcome-cta:hover { background: var(--jd-red-dark); }
.welcome-skip { margin-top: var(--space-md); font-size: var(--font-sm); color: var(--text-tertiary); cursor: pointer; }

/* Back to Top */
.back-to-top {
  position: fixed; bottom: 120px; right: 16px; z-index: 150; width: 40px; height: 40px;
  border-radius: 50%; background: var(--jd-red); color: #fff; border: none; cursor: pointer;
  box-shadow: var(--shadow-md); display: flex; align-items: center; justify-content: center;
  transition: transform var(--transition), background var(--transition-fast), opacity var(--transition-fast);
  opacity: .9;
}
.back-to-top:hover { transform: scale(1.15); background: var(--jd-red-dark); opacity: 1; }

/* Toast */
.toast-container {
  position: fixed; top: 60px; right: var(--space-xl); z-index: 9999;
  display: flex; flex-direction: column; gap: var(--space-sm); pointer-events: none;
}
.toast-item {
  padding: var(--space-md) var(--space-xl); border-radius: var(--radius-md);
  font-size: var(--font-md); color: #fff; box-shadow: var(--shadow-md);
  animation: fadeIn .3s; min-width: 200px; max-width: 360px;
  display: flex; align-items: center; gap: var(--space-sm); pointer-events: auto;
}
.toast-success { background: var(--green); }
.toast-error { background: var(--jd-red); }
.toast-warning { background: var(--orange); }
.toast-info { background: var(--blue); }
.toast-msg { flex: 1; }
.toast-dismiss { background: none; border: none; color: rgba(255,255,255,.7); cursor: pointer; font-size: var(--font-md); padding: 0; line-height: 1; }

@keyframes fadeIn { from { opacity: 0; transform: translateX(20px); } to { opacity: 1; transform: translateX(0); } }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.toast-enter-active { animation: fadeIn .3s; }
.toast-leave-active { animation: fadeIn .2s reverse; }

@media (max-width: 768px) {
  .main-content { padding: var(--space-sm) var(--space-sm) calc(70px + env(safe-area-inset-bottom, 0px)); }
}
</style>
