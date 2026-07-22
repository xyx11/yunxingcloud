<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from '@/locales'

const props = defineProps<{ cartCount: number; hasUnreadNotif?: boolean }>()
const badgePulse = ref(false)
watch(() => props.cartCount, (n, o) => {
  if (n > (o || 0)) { badgePulse.value = true; setTimeout(() => badgePulse.value = false, 400) }
})

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const tabItems: { path: string; label: string; icon: string; activeIcon: string }[] = [
  { path: '/', label: '', icon: '🏠', activeIcon: '🏠' },
  { path: '/products', label: '', icon: '📂', activeIcon: '📂' },
  { path: '/live', label: '', icon: '📺', activeIcon: '📺' },
  { path: '/brands', label: '', icon: '🏷️', activeIcon: '🏷️' },
  { path: '/notifications', label: '', icon: '🔔', activeIcon: '🔔' },
  { path: '/cart', label: '', icon: '🛒', activeIcon: '🛒' },
  { path: '/profile', label: '', icon: '👤', activeIcon: '👤' },
]

const i18nKey: Record<string, string> = {
  '/': 'nav.home',
  '/products': 'nav.products',
  '/live': 'nav.live',
  '/brands': 'nav.brands',
  '/notifications': 'nav.notifications',
  '/cart': 'nav.cart',
  '/profile': 'nav.profile',
}

function goTo(path: string) { router.push(path) }
function getLabel(path: string) { return t(i18nKey[path] as any) || '' }
function isActive(path: string): boolean {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>

<template>
  <nav class="mobile-nav" role="navigation" :aria-label="t('nav.mobileNav')">
    <div class="mobile-nav-inner">
      <div
        v-for="t in tabItems" :key="t.path"
        class="nav-item"
        :class="{ active: isActive(t.path) }"
        @click="goTo(t.path)"
        @touchstart.passive=""
        @touchend.passive=""
      >
        <span class="nav-icon" :class="{ 'nav-icon--active': isActive(t.path) }">{{ t.icon }}</span>
        <span v-if="t.path==='/cart' && cartCount > 0" class="nav-badge" :class="{ 'nav-badge--pulse': badgePulse }">
          {{ cartCount > 99 ? '99+' : cartCount }}
        </span>
        <span v-if="t.path==='/notifications' && hasUnreadNotif" class="nav-dot" />
        <span class="nav-label" :class="{ 'nav-label--active': isActive(t.path) }">{{ getLabel(t.path) }}</span>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.mobile-nav {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--bg-white);
  border-top: 1px solid var(--border-light);
  padding: 6px 0 env(safe-area-inset-bottom);
  z-index: 200;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}
.mobile-nav-inner {
  display: flex;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  max-width: 500px;
  margin: 0 auto;
  justify-content: center;
  gap: 2px;
}
.mobile-nav-inner::-webkit-scrollbar { display: none; }
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  padding: 6px 10px;
  min-height: 44px;
  justify-content: center;
  position: relative;
  transition: transform var(--transition-fast);
  -webkit-tap-highlight-color: transparent;
}
.nav-item:active { transform: scale(.95); }
.nav-icon { font-size: 20px; transition: transform var(--transition); }
.nav-icon--active { transform: scale(1.2); }
.nav-badge {
  position: absolute;
  top: -4px;
  right: 2px;
  background: var(--jd-red);
  color: #fff;
  border-radius: var(--radius-round);
  font-size: 11px;
  padding: 0 5px;
  min-width: 16px;
  text-align: center;
  line-height: 17px;
  font-weight: 700;
}
.nav-badge--pulse { animation: badgePulse .4s ease; }
@keyframes badgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.4); }
}
.nav-label {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-tertiary);
}
.nav-label--active { color: var(--jd-red); }
.nav-dot { position: absolute; top: 2px; right: 8px; width: 8px; height: 8px; border-radius: 50%; background: var(--jd-red); }
.active::after {
  content: '';
  position: absolute;
  bottom: -6px;
  width: 20px;
  height: 2px;
  background: var(--jd-red);
  border-radius: 1px;
  transition: transform .2s ease, opacity .2s ease;
}
.nav-item:not(.active)::after { opacity: 0; transform: scaleX(0); }
.nav-item.active::after { opacity: 1; transform: scaleX(1); }

@media (max-width: 768px) {
  .mobile-nav { display: block !important; }
}
</style>
