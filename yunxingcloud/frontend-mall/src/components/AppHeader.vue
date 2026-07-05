<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { useI18n } from '@/locales'
import request from '@/api/request'

const router = useRouter()
const auth = useAuthStore()
const theme = useThemeStore()
const { locale, setLocale } = useI18n()
const searchText = ref('')
const categories = ref<any[]>([])
const showMega = ref(false)
const voiceSearching = ref(false)
let megaTimer: ReturnType<typeof setTimeout> | null = null

const hotKeywords = ['iPhone 17', 'MacBook Pro', '华为Mate 70', '茅台飞天', 'Nike Dunk', '戴森V16']

onMounted(async () => {
  try { const r = await request.get('/categories'); categories.value = r.data || [] } catch { /* noop */ }
})



function doSearch() {
  if (!searchText.value.trim()) return
  router.push({ path: '/search', query: { q: searchText.value.trim() } })
}

async function startVoiceSearch() {
  if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) return
  voiceSearching.value = true
  try {
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    const recognition = new SpeechRecognition()
    recognition.lang = 'zh-CN'
    recognition.onresult = (e: any) => { searchText.value = e.results[0][0].transcript; doSearch() }
    recognition.onend = () => { voiceSearching.value = false }
    recognition.start()
  } catch { voiceSearching.value = false }
}

function goTo(path: string) { router.push(path) }

function goCategory(catId: number) {
  router.push({ path: '/products', query: { categoryId: catId } })
  showMega.value = false
}

function openMega() {
  if (megaTimer) clearTimeout(megaTimer)
  showMega.value = true
}
function closeMega() {
  megaTimer = setTimeout(() => { showMega.value = false }, 200)
}

defineProps<{ cartCount?: number }>()
</script>

<template>
  <!-- Top Bar -->
  <div class="top-bar">
    <span>欢迎来到 YXCLOUD 商城</span>
    <div class="top-bar-links">
      <button class="lang-btn" @click="setLocale(locale==='zh'?'en':'zh')" :title="locale==='zh'?'Switch to English':'切换到中文'">
        {{ locale === 'zh' ? 'EN' : '中' }}
      </button>
      <template v-if="auth.isLoggedIn">
        <span class="top-link" @click="goTo('/profile')">{{ auth.user?.username }}</span>
        <span class="top-link" @click="goTo('/orders')">我的订单</span>
        <span class="top-link" @click="auth.logout();goTo('/login')">退出</span>
      </template>
      <template v-else>
        <span class="top-link" @click="goTo('/login')">你好，请登录</span>
        <span class="top-link top-link--highlight" @click="goTo('/register')">免费注册</span>
      </template>
    </div>
  </div>

  <!-- Main Header -->
  <header class="main-header">
    <h1 class="logo" @click="router.push('/')" role="button" tabindex="0" aria-label="YXCLOUD 商城首页" @keydown.enter="router.push('/')">YXCLOUD</h1>

    <!-- Search Box -->
    <div class="search-box" role="search">
      <input v-model="searchText" placeholder="搜索华为手机、茅台飞天..." @keyup.enter="doSearch" aria-label="搜索商品" />
      <button class="search-btn" @click="doSearch" aria-label="搜索">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
      </button>
    </div>

    <!-- Hot Keywords -->
    <div class="hot-tags">
      <span v-for="kw in hotKeywords" :key="kw" class="hot-tag" @click="searchText=kw;doSearch()">{{ kw }}</span>
    </div>

    <!-- Header Actions -->
    <div class="header-actions" role="navigation" aria-label="用户导航">
      <button class="header-btn" @click="startVoiceSearch" :disabled="voiceSearching" :title="voiceSearching?'正在监听...':'语音搜索'">
        {{ voiceSearching ? '🎙️' : '🎤' }}
      </button>
      <button class="header-btn" @click="theme.toggle" :title="theme.isDark?'浅色模式':'深色模式'">
        {{ theme.isDark ? '☀️' : '🌙' }}
      </button>
      <span class="header-link" @click="goTo('/orders')">我的订单</span>
      <span class="header-link cart-link" @click="goTo('/cart')">
        🛒<span class="cart-label">购物车</span>
        <span v-if="cartCount && cartCount > 0" class="cart-badge">{{ cartCount > 99 ? '99+' : cartCount }}</span>
      </span>
    </div>
  </header>

  <!-- Category Nav -->
  <nav class="cat-nav">
    <div class="mega-menu-wrapper" @mouseenter="openMega" @mouseleave="closeMega">
      <span class="all-categories" @click="goTo('/products')">📂 全部商品分类</span>
      <div v-if="showMega && categories.length" class="mega-dropdown">
        <div v-for="cat in categories" :key="cat.id" class="mega-item" @click="goCategory(cat.id)">
          <span>{{ cat.icon || '📁' }}</span>
          <span>{{ cat.name }}</span>
        </div>
      </div>
    </div>
    <span v-for="cat in categories.slice(0, 8)" :key="cat.id" class="nav-link" @click="goCategory(cat.id)">
      {{ cat.name }}
    </span>
  </nav>

  <!-- Mobile Search -->
  <div class="mobile-search">
    <div class="mobile-search-inner">
      <input v-model="searchText" placeholder="搜索商品" aria-label="搜索商品" @keyup.enter="doSearch" />
      <button @click="doSearch">搜索</button>
    </div>
  </div>
</template>

<style scoped>
/* Top Bar */
.top-bar {
  background: var(--bg-hover);
  font-size: var(--font-sm);
  color: var(--text-tertiary);
  padding: 6px var(--space-xl);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.top-bar-links { display: flex; gap: var(--space-lg); align-items: center; }
.lang-btn {
  background: none; border: 1px solid var(--border); color: var(--text-tertiary);
  font-size: var(--font-xs); cursor: pointer; padding: 1px 6px; border-radius: var(--radius-sm);
  transition: color var(--transition-fast), border-color var(--transition-fast);
}
.lang-btn:hover { color: var(--jd-red); border-color: var(--jd-red); }
.top-link { cursor: pointer; transition: color var(--transition-fast); }
.top-link:hover { color: var(--jd-red); }
.top-link--highlight { color: var(--jd-red); font-weight: 600; }

/* Header */
.main-header {
  background: linear-gradient(135deg, var(--jd-red-dark), var(--jd-red));
  color: var(--text-white);
  padding: var(--space-md) var(--space-xl);
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow-md);
  flex-wrap: wrap;
}
.logo {
  font-size: var(--font-xxl);
  font-weight: 800;
  cursor: pointer;
  white-space: nowrap;
  letter-spacing: -0.5px;
}
.logo:hover { opacity: .85; }

.search-box {
  flex: 1;
  max-width: 500px;
  display: flex;
  border-radius: var(--radius-round);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,.15);
}
.search-box input {
  flex: 1; height: 38px; padding: 0 var(--space-lg); border: none; outline: none;
  font-size: var(--font-md); color: var(--text-primary);
}
.search-btn {
  width: 60px; height: 38px; background: var(--jd-red); color: #fff; border: none;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: background var(--transition-fast);
}
.search-btn:hover { background: var(--jd-red-dark); }

.hot-tags {
  display: flex; gap: var(--space-md); font-size: var(--font-xs); color: rgba(255,255,255,.7);
  width: 100%; padding-left: 0; margin-top: -4px;
}
.hot-tag { cursor: pointer; transition: color var(--transition-fast); }
.hot-tag:hover { color: #fff; }

.header-actions { display: flex; align-items: center; gap: var(--space-lg); font-size: var(--font-base); white-space: nowrap; }
.header-btn {
  background: none; border: none; color: #fff; cursor: pointer;
  font-size: var(--font-lg); padding: 2px 6px; opacity: .8; transition: opacity var(--transition-fast);
}
.header-btn:hover { opacity: 1; }
.header-link { cursor: pointer; transition: opacity var(--transition-fast); }
.header-link:hover { opacity: .8; }
.cart-link { position: relative; }
.cart-label { margin-left: 2px; }
.cart-badge {
  position: absolute; top: -8px; right: -12px; background: #fff; color: var(--jd-red);
  border-radius: var(--radius-round); font-size: 10px; padding: 1px 5px;
  font-weight: 700; min-width: 16px; text-align: center; line-height: 14px;
}

/* Category Nav */
.cat-nav {
  background: var(--bg-white);
  border-bottom: 2px solid var(--jd-red);
  padding: 0 var(--space-xl);
  display: flex;
  gap: 0;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}
.cat-nav::-webkit-scrollbar { display: none; }
.nav-link {
  display: inline-block; padding: var(--space-md) var(--space-lg); cursor: pointer;
  font-size: var(--font-md); white-space: nowrap; color: var(--text-primary);
  transition: color var(--transition-fast);
}
.nav-link:hover { color: var(--jd-red); }

.mega-menu-wrapper { position: relative; flex-shrink: 0; }
.all-categories {
  display: inline-block; padding: var(--space-md) var(--space-xl); cursor: pointer;
  font-size: var(--font-md); white-space: nowrap; color: #fff; background: var(--jd-red);
  font-weight: 600;
}
.mega-dropdown {
  position: absolute; top: 100%; left: 0; z-index: 300;
  background: var(--bg-white); border: 1px solid var(--border-light);
  box-shadow: var(--shadow-xl); border-radius: 0 0 var(--radius-md) var(--radius-md);
  min-width: 200px; padding: var(--space-sm) 0;
  animation: slideDown var(--transition);
}
.mega-item {
  padding: var(--space-sm) var(--space-xl); cursor: pointer; font-size: var(--font-base);
  color: var(--text-primary); display: flex; align-items: center; gap: var(--space-sm);
  transition: all var(--transition-fast);
}
.mega-item:hover { background: var(--jd-red-light); color: var(--jd-red); }

/* Mobile Search */
.mobile-search { display: none; padding: var(--space-sm) var(--space-md); background: var(--jd-red); }
.mobile-search-inner { display: flex; border-radius: var(--radius-round); overflow: hidden; }
.mobile-search-inner input {
  flex: 1; height: 34px; padding: 0 var(--space-md); border: none; outline: none; font-size: var(--font-base);
}
.mobile-search-inner button {
  height: 34px; padding: 0 var(--space-lg); background: var(--jd-red); color: #fff; border: none;
  cursor: pointer; font-size: var(--font-sm); font-weight: 600;
}

@keyframes slideDown { from { opacity: 0; transform: translateY(-8px); } to { opacity: 1; transform: translateY(0); } }

/* Responsive */
@media (max-width: 768px) {
  .top-bar { display: none !important; }
  .main-header { padding: var(--space-sm) var(--space-md); flex-wrap: nowrap; }
  .logo { font-size: var(--font-lg); }
  .search-box { display: none !important; }
  .hot-tags { display: none !important; }
  .header-actions { font-size: var(--font-sm); gap: var(--space-sm); }
  .cart-label { display: none; }
  .cat-nav { display: none !important; }
  .mobile-search { display: flex !important; }
  .header-actions .header-link:first-of-type { display: none; }
}
</style>
