<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'

defineProps<{ cartCount: number }>()

const router = useRouter()
const route = useRoute()

const tabItems = [
  { path: '/', label: '首页', icon: '🏠' },
  { path: '/products', label: '分类', icon: '📂' },
  { path: '/cart', label: '购物车', icon: '🛒' },
  { path: '/profile', label: '我的', icon: '👤' },
]

function goTo(path: string) { router.push(path) }
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
        <span class="nav-label" :class="{ 'nav-label--active': route.path === t.path }">{{ t.label }}</span>
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
  justify-content: space-around;
  max-width: 500px;
  margin: 0 auto;
}
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  padding: 4px 14px;
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
  font-size: 9px;
  padding: 0 5px;
  min-width: 14px;
  text-align: center;
  line-height: 15px;
  font-weight: 700;
}
.nav-label {
  font-size: 10px;
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
