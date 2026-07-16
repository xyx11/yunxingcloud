<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from '@/locales'

defineProps<{ cartCount: number }>()

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const tabItems: { path: string; label: string; icon: string }[] = [
  { path: '/', label: '', icon: '🏠' },
  { path: '/products', label: '', icon: '📂' },
  { path: '/live', label: '', icon: '📺' },
  { path: '/brands', label: '', icon: '🏷️' },
  { path: '/notifications', label: '', icon: '🔔' },
  { path: '/cart', label: '', icon: '🛒' },
  { path: '/profile', label: '', icon: '👤' },
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
</script>

<template>
  <nav class="mobile-nav" role="navigation" aria-label="底部导航">
    <div class="mobile-nav-inner">
      <div
        v-for="t in tabItems" :key="t.path"
        class="nav-item"
        :class="{ active: route.path === t.path }"
        @click="goTo(t.path)"
        @touchstart.passive=""
        @touchend.passive=""
      >
        <span class="nav-icon" :class="{ 'nav-icon--active': route.path === t.path }">{{ t.icon }}</span>
        <span v-if="t.path==='/cart' && cartCount > 0" class="nav-badge">
          {{ cartCount > 99 ? '99+' : cartCount }}
        </span>
        <span class="nav-label" :class="{ 'nav-label--active': route.path === t.path }">{{ getLabel(t.path) }}</span>
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
.nav-label {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-tertiary);
}
.nav-label--active { color: var(--jd-red); }
.active::after {
  content: '';
  position: absolute;
  bottom: -6px;
  width: 20px;
  height: 2px;
  background: var(--jd-red);
  border-radius: 1px;
}

@media (max-width: 768px) {
  .mobile-nav { display: block !important; }
}
</style>
