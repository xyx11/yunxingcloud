<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useGlobalToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import { useCompare } from '@/composables/useCompare'
import { useNotification } from '@/composables/useNotification'
import request from '@/api/request'
import CompareFloatingBar from '@/views/CompareView.vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const toast = useGlobalToast()
const { t, locale, setLocale } = useI18n()
const searchText = ref('')
const categories = ref<any[]>([])
const cartCount = ref(0)
const showBackTop = ref(false)
const isDark = ref(localStorage.getItem('mall_theme') === 'dark')
const voiceSearching = ref(false)

function updateCartCount() {
  try { const r = JSON.parse(localStorage.getItem('cart_count') || '0'); cartCount.value = r } catch { cartCount.value = 0 }
}
function scrollToTop() { window.scrollTo({ top: 0, behavior: 'smooth' }) }
function toggleTheme() {
  isDark.value = !isDark.value
  localStorage.setItem('mall_theme', isDark.value ? 'dark' : 'light')
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
}
function startVoiceSearch() {
  if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) return
  const SR = (window as any).webkitSpeechRecognition || (window as any).SpeechRecognition
  const rec = new SR(); rec.lang = 'zh-CN'; rec.interimResults = false
  voiceSearching.value = true; rec.start()
  rec.onresult = (e: any) => { searchText.value = e.results[0][0].transcript; doSearch(); voiceSearching.value = false }
  rec.onerror = () => { voiceSearching.value = false }
}

if (isDark.value) document.documentElement.setAttribute('data-theme', 'dark')

const { supported: pushSupported, subscribed: pushSubscribed, request: requestPush, sendTest } = useNotification()
const showPushBanner = ref(pushSupported.value && !pushSubscribed.value)
async function enablePush() { const ok = await requestPush(); if (ok) { showPushBanner.value = false; sendTest() } }

onMounted(async () => {
  try { const r = await request.get('/categories'); categories.value = r.data || [] } catch {}
  updateCartCount()
  window.addEventListener('scroll', () => { showBackTop.value = window.scrollY > 400 })
  window.addEventListener('cart_updated', updateCartCount)
})

function doSearch() {
  if (!searchText.value.trim()) return
  router.push({ path: '/search', query: { q: searchText.value.trim() } })
}

function goCategory(catId: number) {
  router.push({ path: '/products', query: { categoryId: catId } })
}

function goTo(path: string) { router.push(path) }

const tabItems = [
  { path: '/', label: '首页', icon: '🏠' },
  { path: '/products', label: '分类', icon: '📂' },
  { path: '/cart', label: '购物车', icon: '🛒' },
  { path: '/profile', label: '我的', icon: '👤' },
]
</script>

<template>
  <div>
    <!-- Top Bar (desktop) -->
    <div class="top-bar" style="background:#f5f5f5;font-size:12px;color:#666;padding:6px 20px;display:flex;justify-content:space-between">
      <span>{{ t('common.welcome') }}</span>
      <div style="display:flex;gap:16px;align-items:center">
        <span @click="setLocale(locale==='zh'?'en':'zh')" style="cursor:pointer;font-size:11px" :title="locale==='zh'?'Switch to English':'切换到中文'">{{ locale === 'zh' ? 'EN' : '中' }}</span>
        <span v-if="auth.isLoggedIn()" style="cursor:pointer" @click="goTo('/profile')">{{ auth.user?.username }} | {{ t('common.profile') }}</span>
        <span v-else style="cursor:pointer" @click="goTo('/login')">{{ t('common.login') }}</span>
        <span v-if="!auth.isLoggedIn()" style="cursor:pointer" @click="goTo('/register')">{{ t('common.register') }}</span>
        <span v-if="auth.isLoggedIn()" style="cursor:pointer" @click="auth.logout();goTo('/login')">{{ t('common.logout') }}</span>
      </div>
    </div>

    <!-- Main Header -->
    <header class="main-header" style="background:#e4393c;color:#fff;padding:12px 20px;display:flex;align-items:center;justify-content:space-between;gap:20px;position:sticky;top:0;z-index:100;box-shadow:0 2px 8px rgba(0,0,0,.15)">
      <h1 style="font-size:22px;font-weight:700;cursor:pointer;white-space:nowrap;margin:0" @click="router.push('/')">YXCLOUD 商城</h1>

      <!-- Search (desktop) -->
      <div class="search-box" role="search" style="flex:1;max-width:600px;display:flex">
        <input v-model="searchText" placeholder="搜索商品..." aria-label="搜索商品" @keyup.enter="doSearch"
               style="flex:1;height:36px;padding:0 12px;border:none;border-radius:4px 0 0 4px;outline:none;font-size:14px" />
        <button @click="doSearch" aria-label="搜索按钮" style="height:36px;padding:0 20px;background:#c82930;color:#fff;border:none;border-radius:0 4px 4px 0;cursor:pointer;font-size:14px;font-weight:600">搜索</button>
      </div>

      <div class="header-links" role="navigation" aria-label="用户导航" style="display:flex;align-items:center;gap:16px;font-size:13px;white-space:nowrap">
        <button @click="startVoiceSearch" :disabled="voiceSearching" style="background:none;border:none;color:#fff;cursor:pointer;font-size:16px;padding:2px 6px;opacity:.8" :style="{opacity:voiceSearching?'.5':'.8'}" :title="voiceSearching?'正在监听...':'语音搜索'">{{ voiceSearching ? '🎙️' : '🎤' }}</button>
        <button @click="toggleTheme" style="background:none;border:none;color:#fff;cursor:pointer;font-size:16px;padding:2px 6px;opacity:.8" :title="isDark?'浅色模式':'深色模式'">{{ isDark ? '☀️' : '🌙' }}</button>
        <span @click="goTo('/orders')" style="cursor:pointer">我的订单</span>
        <span @click="goTo('/cart')" class="cart-icon" style="cursor:pointer;position:relative">
          🛒 购物车
          <span v-if="cartCount > 0" style="position:absolute;top:-8px;right:-12px;background:#fff;color:#e4393c;border-radius:10px;font-size:10px;padding:1px 5px;font-weight:700;min-width:16px;text-align:center;line-height:14px">{{ cartCount > 99 ? '99+' : cartCount }}</span>
        </span>
      </div>
    </header>

    <!-- Category Nav (desktop) -->
    <nav class="cat-nav" style="background:#fff;border-bottom:2px solid #e4393c;padding:0 20px;display:flex;gap:0;overflow-x:auto">
      <span @click="goTo('/products')" style="display:inline-block;padding:10px 16px;cursor:pointer;font-size:14px;white-space:nowrap;color:#333;transition:color .2s"
            @mouseenter="(e:any) => e.target.style.color='#e4393c'" @mouseleave="(e:any) => e.target.style.color='#333'">全部商品</span>
      <span v-for="cat in categories.slice(0, 8)" :key="cat.id"
            @click="goCategory(cat.id)"
            style="display:inline-block;padding:10px 16px;cursor:pointer;font-size:14px;white-space:nowrap;color:#333;transition:color .2s"
            @mouseenter="(e:any) => e.target.style.color='#e4393c'" @mouseleave="(e:any) => e.target.style.color='#333'">{{ cat.name }}</span>
    </nav>

    <!-- Mobile Search -->
    <div class="mobile-search" style="display:none;padding:8px 12px;background:#e4393c">
      <div style="display:flex;border-radius:20px;overflow:hidden">
        <input v-model="searchText" placeholder="搜索商品..." aria-label="搜索商品" @keyup.enter="doSearch"
               style="flex:1;height:32px;padding:0 12px;border:none;outline:none;font-size:13px" />
        <button @click="doSearch" style="height:32px;padding:0 16px;background:#c82930;color:#fff;border:none;cursor:pointer;font-size:12px">搜索</button>
      </div>
    </div>

    <!-- Main Content -->
    <main class="main-content" role="main" style="min-height:calc(100vh - 180px);max-width:1200px;margin:0 auto;padding:16px 20px">
      <router-view />
    </main>

    <!-- Push Notification Banner -->
    <div v-if="showPushBanner" style="position:fixed;bottom:100px;left:50%;transform:translateX(-50%);z-index:180;background:#fff;border-radius:12px;box-shadow:0 4px 24px rgba(0,0,0,.12);padding:14px 20px;display:flex;align-items:center;gap:12px;max-width:400px;animation:slideUp .4s ease-out">
      <span style="font-size:24px">🔔</span>
      <div style="flex:1"><div style="font-weight:600;font-size:13px">开启通知</div><div style="color:#999;font-size:11px">第一时间获取促销和订单状态</div></div>
      <button @click="enablePush" style="padding:6px 16px;background:#e4393c;color:#fff;border:none;border-radius:16px;cursor:pointer;font-size:12px;font-weight:600;white-space:nowrap">开启</button>
      <button @click="showPushBanner = false" style="background:none;border:none;color:#ccc;cursor:pointer;font-size:14px;padding:2px">✕</button>
    </div>

    <!-- Footer (desktop) -->
    <footer class="footer-desk" style="background:#333;color:#999;padding:32px 20px 20px">
      <div style="max-width:1200px;margin:0 auto;display:grid;grid-template-columns:repeat(4,1fr);gap:24px;font-size:13px">
        <div><h4 style="color:#fff;margin-bottom:12px">购物指南</h4><p style="margin:6px 0;cursor:pointer">购物流程</p><p style="margin:6px 0;cursor:pointer">会员介绍</p><p style="margin:6px 0;cursor:pointer">常见问题</p></div>
        <div><h4 style="color:#fff;margin-bottom:12px">配送方式</h4><p style="margin:6px 0;cursor:pointer">配送范围</p><p style="margin:6px 0;cursor:pointer">配送时效</p><p style="margin:6px 0;cursor:pointer">验货签收</p></div>
        <div><h4 style="color:#fff;margin-bottom:12px">售后服务</h4><p style="margin:6px 0;cursor:pointer">退货政策</p><p style="margin:6px 0;cursor:pointer">退款说明</p><p style="margin:6px 0;cursor:pointer">联系客服</p></div>
        <div><h4 style="color:#fff;margin-bottom:12px">关于我们</h4><p style="margin:6px 0;cursor:pointer">公司介绍</p><p style="margin:6px 0;cursor:pointer">联系我们</p><p style="margin:6px 0;cursor:pointer">加入我们</p></div>
      </div>
      <div style="text-align:center;padding-top:20px;margin-top:20px;border-top:1px solid #444;font-size:12px">© 2026 yunxingcloud 商城</div>
    </footer>

    <!-- Mobile Bottom Nav -->
    <nav class="mobile-nav" role="navigation" aria-label="底部导航" style="display:none;position:fixed;bottom:0;left:0;right:0;background:#fff;border-top:1px solid #eee;padding:6px 0 env(safe-area-inset-bottom);z-index:200">
      <div style="display:flex;justify-content:space-around">
        <div v-for="t in tabItems" :key="t.path" @click="goTo(t.path)"
             style="display:flex;flex-direction:column;align-items:center;gap:2px;cursor:pointer;padding:4px 12px;position:relative">
          <span style="font-size:20px">{{ t.icon }}</span>
          <span v-if="t.path==='/cart' && cartCount > 0" style="position:absolute;top:0;right:2px;background:#e4393c;color:#fff;border-radius:10px;font-size:9px;padding:0 4px;min-width:14px;text-align:center;line-height:14px;font-weight:700">{{ cartCount > 99 ? '99+' : cartCount }}</span>
          <span style="font-size:10px" :style="{color:route.path===t.path?'#e4393c':'#999'}">{{ t.label }}</span>
        </div>
      </div>
    </nav>

    <!-- Back to Top -->
    <button v-if="showBackTop" @click="scrollToTop"
            style="position:fixed;bottom:80px;right:20px;z-index:150;width:40px;height:40px;border-radius:50%;background:#e4393c;color:#fff;border:none;cursor:pointer;font-size:18px;box-shadow:0 2px 12px rgba(228,57,60,.4);transition:transform .2s;display:flex;align-items:center;justify-content:center"
            @mouseenter="(e:any) => e.target.style.transform='scale(1.1)'" @mouseleave="(e:any) => e.target.style.transform=''">
      ↑
    </button>

    <!-- Compare Floating Bar -->
    <CompareFloatingBar />

    <!-- Toast Container -->
    <div style="position:fixed;top:60px;right:20px;z-index:9999;display:flex;flex-direction:column;gap:8px">
      <div v-for="t in toast.toasts.value" :key="t.id"
           style="padding:12px 20px;border-radius:8px;font-size:14px;color:#fff;box-shadow:0 4px 12px rgba(0,0,0,.15);animation:fadeIn .3s;min-width:200px;max-width:360px"
           :style="{background:t.type==='success'?'#52c41a':t.type==='error'?'#e4393c':'#1677ff'}">
        {{ t.message }}
      </div>
    </div>
  </div>
</template>

<style>
[data-theme="dark"] {
  --bg: #1a1a2e; --card-bg: #16213e; --text: #e0e0e0; --text-secondary: #999;
  --border: #333; --header-bg: #0f3460; --footer-bg: #0a0a1a;
}
[data-theme="dark"] body { background: var(--bg); color: var(--text); }
[data-theme="dark"] .main-content { background: var(--bg); }
[data-theme="dark"] button { color: var(--text); }

@keyframes fadeIn {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}

/* Mobile: < 768px */
@media (max-width: 768px) {
  .top-bar { display: none !important; }
  .main-header { padding: 10px 12px !important; }
  .main-header h1 { font-size: 16px !important; }
  .search-box { display: none !important; }
  .header-links { font-size: 12px !important; gap: 12px !important; }
  .cat-nav { display: none !important; }
  .mobile-search { display: flex !important; }
  .main-content { padding: 8px 10px 70px !important; }
  .footer-desk { display: none !important; }
  .mobile-nav { display: block !important; }
}
</style>
