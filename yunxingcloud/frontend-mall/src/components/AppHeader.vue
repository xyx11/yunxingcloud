<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { useI18n } from '@/locales'
import request from '@/api/request'
import { useToast } from '@/composables/useToast'
import type { Category } from '@/types'

const router = useRouter()
const toast = useToast()
const auth = useAuthStore()
const theme = useThemeStore()
const { t, locale, setLocale } = useI18n()
const searchText = ref('')
const categories = ref<Category[]>([])
const showMega = ref(false)
const voiceSearching = ref(false)
let megaTimer: ReturnType<typeof setTimeout> | null = null

const hotKeywords = ['iPhone 17', 'MacBook Pro', '华为Mate 70', '茅台飞天', 'Nike Dunk', '戴森V16']

// Instant search suggestions
const suggestions = ref<string[]>([])
const showSuggestions = ref(false)
let suggestTimer: ReturnType<typeof setTimeout> | null = null

async function fetchSuggestions(q: string) {
  if (!q.trim()) { suggestions.value = []; return }
  try {
    const r = await request.get('/search/suggest', { params: { q, limit: 6 } })
    suggestions.value = r.data || []
  } catch { suggestions.value = [] }
}

function onSearchInput() {
  showSuggestions.value = true
  if (suggestTimer) clearTimeout(suggestTimer)
  suggestTimer = setTimeout(() => fetchSuggestions(searchText.value), 200)
}

function onSearchFocus() { if (suggestions.value.length) showSuggestions.value = true }
function onSearchBlur() { setTimeout(() => showSuggestions.value = false, 200) }
function selectSuggestion(kw: string) { searchText.value = kw; showSuggestions.value = false; doSearch() }

onMounted(async () => {
  try { const r = await request.get('/categories'); categories.value = r.data || [] } catch { /* noop */ }
})

function doSearch() {
  if (!searchText.value.trim()) return
  showSuggestions.value = false
  router.push({ path: '/search', query: { q: searchText.value.trim() } })
}

async function startVoiceSearch() {
  if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
    toast.info(t('header.voiceNotSupported'))
    return
  }
  voiceSearching.value = true
  try {
    const win = window as unknown as Record<string, unknown>
    const SRClass = (win.SpeechRecognition || win.webkitSpeechRecognition) as new () => { lang: string; start(): void; onresult: (e: { results: { transcript: string }[][] }) => void; onend: () => void }
    if (!SRClass) { voiceSearching.value = false; toast.info(t('header.voiceUnavailable')); return }
    const recognition = new SRClass()
    recognition.lang = 'zh-CN'
    recognition.onresult = (e) => { searchText.value = e.results[0][0].transcript; doSearch() }
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
  megaTimer = setTimeout(() => { showMega.value = false }, 350)
}

const props = defineProps<{ cartCount?: number }>()
const badgePulse = ref(false)
watch(() => props.cartCount, (n, o) => {
  if (n && n > (o || 0)) { badgePulse.value = true; setTimeout(() => badgePulse.value = false, 400) }
})
</script>

<template>
  <!-- Top Bar -->
  <div class="top-bar">
    <span>{{ t('common.welcome') }}</span>
    <div class="top-bar-links">
      <button class="lang-btn" @click="theme.toggle()" :title="theme.isDark ? t('header.switchLight') : t('header.switchDark')">
        {{ theme.isDark ? '☀️' : '🌙' }}
      </button>
      <button class="lang-btn" @click="setLocale(locale==='zh'?'en':'zh')" :title="locale==='zh'?t('header.switchLang'):'Switch to English'">
        {{ locale === 'zh' ? 'EN' : '中' }}
      </button>
      <template v-if="auth.isLoggedIn">
        <span class="top-link" @click="goTo('/profile')">{{ auth.user?.username }}</span>
        <span class="top-link" @click="goTo('/orders')">{{ t('common.orders') }}</span>
        <span class="top-link" @click="auth.logout();goTo('/login')">{{ t('common.logout') }}</span>
      </template>
      <template v-else>
        <span class="top-link" @click="goTo('/login')">{{ t('header.loginPrompt') }}</span>
        <span class="top-link top-link--highlight" @click="goTo('/register')">{{ t('header.freeRegister') }}</span>
      </template>
    </div>
  </div>

  <!-- Main Header -->
  <header class="main-header">
    <h1 class="logo" @click="router.push('/')" role="button" tabindex="0" :aria-label="t('header.logoAria')" @keydown.enter="router.push('/')">YXCLOUD</h1>

    <!-- Search Box -->
    <div class="search-box" role="search">
      <input v-model="searchText" :placeholder="t('search.placeholder')" @keyup.enter="doSearch" @input="onSearchInput" @focus="onSearchFocus" @blur="onSearchBlur" :aria-label="t('header.searchAria')" />
      <button class="search-btn" @click="doSearch" :aria-label="t('common.search')">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
      </button>
      <!-- Suggestions dropdown -->
      <ul v-if="showSuggestions && suggestions.length" class="search-suggestions">
        <li v-for="s in suggestions" :key="s" class="suggest-item" @mousedown.prevent="selectSuggestion(s)">
          <span class="suggest-icon">🔍</span>
          <span class="suggest-text">{{ s }}</span>
        </li>
      </ul>
    </div>

    <!-- Hot Keywords -->
    <div class="hot-tags">
      <span v-for="kw in hotKeywords" :key="kw" class="hot-tag" @click="searchText=kw;doSearch()">{{ kw }}</span>
    </div>

    <!-- Header Actions -->
    <div class="header-actions" role="navigation" aria-label="用户导航">
      <button class="header-btn" @click="startVoiceSearch" :disabled="voiceSearching" :title="voiceSearching?t('header.listening'):t('header.voiceSearch')">
        {{ voiceSearching ? '🎙️' : '🎤' }}
      </button>
      <button class="header-btn" @click="theme.toggle" :title="theme.isDark?t('header.themeLight'):t('header.themeDark')">
        {{ theme.isDark ? '☀️' : '🌙' }}
      </button>
      <span class="header-link" @click="goTo('/orders')">{{ t('common.orders') }}</span>
      <span class="header-link cart-link" data-cart-target="cart-fly" @click="goTo('/cart')">
        🛒<span class="cart-label">{{ t('common.cart') }}</span>
        <span v-if="cartCount && cartCount > 0" class="cart-badge" :class="{ 'cart-badge--pulse': badgePulse }">{{ cartCount > 99 ? '99+' : cartCount }}</span>
      </span>
    </div>
  </header>

  <!-- Category Nav -->
  <nav class="cat-nav">
    <div class="mega-menu-wrapper" @mouseenter="openMega" @mouseleave="closeMega">
      <span class="all-categories" @click="goTo('/products')">📂 {{ t('header.allCategories') }}</span>
      <div v-if="showMega && categories.length" class="mega-dropdown">
        <div v-for="cat in categories" :key="cat.id" class="mega-item" role="button" tabindex="0" @click="goCategory(cat.id)" @keydown.enter.prevent="goCategory(cat.id)" @keydown.space.prevent="goCategory(cat.id)">
          <span>{{ cat.icon || '📁' }}</span>
          <span>{{ cat.name }}</span>
        </div>
      </div>
    </div>
    <span v-for="cat in categories" :key="cat.id" class="nav-link" @click="goCategory(cat.id)">
      {{ cat.name }}
    </span>
  </nav>

  <!-- Mobile Search -->
  <div class="mobile-search">
    <div class="mobile-search-inner">
      <input v-model="searchText" :placeholder="t('search.placeholder')" :aria-label="t('header.searchAria')" @keyup.enter="doSearch" />
      <button @click="doSearch">{{ t('common.search') }}</button>
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
  transition: transform .3s cubic-bezier(.34,1.56,.64,1), opacity .2s;
  display: inline-block;
}
.logo:hover { opacity: .85; transform: scale(1.05) rotate(-1deg); }

.search-box {
  flex: 1;
  max-width: 500px;
  display: flex;
  position: relative;
  border-radius: var(--radius-round);
  overflow: visible;
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

.search-suggestions {
  position: absolute; top: 100%; left: 0; right: 0; z-index: 100;
  background: var(--bg-white); border-radius: 0 0 var(--radius-md) var(--radius-md);
  box-shadow: var(--shadow-md); list-style: none; margin: 0; padding: var(--space-xs) 0;
  max-height: 280px; overflow-y: auto;
}
.suggest-item {
  display: flex; align-items: center; gap: var(--space-sm);
  padding: var(--space-sm) var(--space-lg); cursor: pointer; font-size: var(--font-sm);
  color: var(--text-primary); transition: background var(--transition-fast);
}
.suggest-item:hover { background: var(--bg-hover); }
.suggest-icon { opacity: .4; font-size: var(--font-xs); }
.suggest-text { flex: 1; }

.hot-tags {
  display: flex; gap: var(--space-sm); font-size: 12px; color: rgba(255,255,255,.75);
  width: 100%; padding-left: 0; margin-top: 4px; max-width: 500px;
  flex-wrap: nowrap; overflow: hidden;
}
.hot-tag { cursor: pointer; transition: color var(--transition-fast); white-space: nowrap; flex-shrink: 0; overflow: hidden; text-overflow: ellipsis; max-width: 90px; }
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
.cart-badge--pulse { animation: badgePulse .4s ease; }
@keyframes badgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.4); }
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
  position: relative;
}
.cat-nav::after {
  content: ''; position: sticky; right: 0; top: 0; bottom: 0;
  width: 40px; flex-shrink: 0;
  background: linear-gradient(to right, transparent, var(--bg-white));
  pointer-events: none;
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
