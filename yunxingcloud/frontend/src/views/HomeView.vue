<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, h, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { fetchSystemInfo, fetchSessions } from '@/api/system'
import { fetchMenuTree } from '@/api/menu'
import { fetchUnreadCount } from '@/api/message'
import { fetchConfigByKey } from '@/api/config'
import { globalSearch as searchApi } from '@/api/search'
import { logout } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useLiveStatsStore } from '@/stores/liveStats'
import { useTagsViewStore } from '@/stores/tagsView'
import { NLayout, NLayoutContent, NLayoutFooter, NIcon } from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { useKeyboard } from '@/composables/useKeyboard'
import CommandPalette from '@/components/CommandPalette.vue'
import AnnouncementBanner from '@/components/AnnouncementBanner.vue'
import SideMenu from '@/components/SideMenu.vue'
import TopHeader from '@/components/TopHeader.vue'
import TagsBar from '@/components/TagsBar.vue'
import { useI18n } from 'vue-i18n'
import { switchLocale } from '@/locales'

const router = useRouter()
const route = useRoute()

const routeLoading = ref(false)
router.beforeEach(() => { routeLoading.value = true })
router.afterEach(() => { setTimeout(() => routeLoading.value = false, 200) })

const authStore = useAuthStore()
const liveStatsStore = useLiveStatsStore()
const tagsViewStore = useTagsViewStore()
const { t, locale } = useI18n()

const collapsed = ref(localStorage.getItem('sidebarCollapsed') === 'true' ? true : window.innerWidth < 768)
const collapsedGroups = ref<Set<string>>(new Set())
function toggleGroup(key: string | number | undefined) { if (key == null) return; const k = String(key); if (collapsedGroups.value.has(k)) collapsedGroups.value.delete(k); else collapsedGroups.value.add(k) }
const isMobile = ref(window.innerWidth < 768)
const mobileOverlay = ref(false)
const isDark = ref(localStorage.getItem('theme') === 'dark')
const menuOptions = ref<MenuOption[]>([])
const liveStats = ref({ uptime: '', sessions: 0 })
const appVersion = ref('')
const unreadCount = ref(0)
const watermarkText = computed(() => {
  const name = authStore.username || 'yunxingcloud'
  const loc = locale.value === 'zh' ? 'zh-CN' : 'en-US'
  const time = new Date().toLocaleDateString(loc)
  return Array(12).fill(`${name} ${time}`).join('    ')
})

const searchQuery = ref("")
const searchResults = ref<Record<string, any[]>>({})
const showSearchResults = ref(false)
const searchHistory = ref<string[]>(JSON.parse(localStorage.getItem('searchHistory') || '[]'))

function addSearchHistory(q: string) {
  searchHistory.value = [q, ...searchHistory.value.filter(s => s !== q)].slice(0, 8)
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
}

function globalSearch() {
  const q = searchQuery.value.trim()
  if (!q) { showSearchResults.value = false; return }
  addSearchHistory(q)
  searchApi(q).then(res => {
    searchResults.value = res.data
    showSearchResults.value = true
  }).catch(() => {
    searchResults.value = {}
    showSearchResults.value = false
  })
}

function navigateFromSearch(type: string, _item: any) {
  showSearchResults.value = false
  searchQuery.value = ''
  if (type === 'users') router.push('/users')
  else if (type === 'menus') router.push('/menus')
  else if (type === 'roles') router.push('/roles')
  else if (type === 'configs') router.push('/config')
  else if (type === 'dict') router.push('/dict')
  else if (type === 'notices') router.push('/notices')
  else if (type === 'posts') router.push('/posts')
  else if (type === 'departments') router.push('/departments')
  else if (type === 'jobs') router.push('/job')
}

const hasSearchResults = computed(() => {
  return Object.values(searchResults.value).some((arr: any) => arr?.length > 0)
})

const showBackTop = ref(false)
function onResize() {
  isMobile.value = window.innerWidth < 768
  if (window.innerWidth < 768) {
    collapsed.value = true
    mobileOverlay.value = false
    localStorage.setItem('sidebarCollapsed', 'true')
  } else {
    mobileOverlay.value = false
    if (localStorage.getItem('sidebarCollapsed') === 'true') { collapsed.value = false; localStorage.removeItem('sidebarCollapsed') }
  }
}
function onScroll() { showBackTop.value = window.scrollY > 300 }
function scrollToTop() { window.scrollTo({ top: 0, behavior: 'smooth' }) }

let _statsTimer: ReturnType<typeof setInterval>
let _unreadTimer: ReturnType<typeof setInterval>
function _onThemeChange(e: Event) { isDark.value = (e as CustomEvent).detail === 'dark' }

window.addEventListener('resize', onResize)
window.addEventListener('scroll', onScroll)
onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('theme-change', _onThemeChange)
  if (_statsTimer) clearInterval(_statsTimer)
  if (_unreadTimer) clearInterval(_unreadTimer)
})

function toggleTheme() {
  isDark.value = !isDark.value
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  document.documentElement.setAttribute('theme', isDark.value ? 'dark' : 'light')
}

function toggleSidebar() {
  if (isMobile.value) { mobileOverlay.value = !mobileOverlay.value; return }
  collapsed.value = !collapsed.value
  localStorage.setItem('sidebarCollapsed', collapsed.value ? 'true' : 'false')
}

function toggleFullscreen() {
  if (!document.fullscreenElement) document.documentElement.requestFullscreen()
  else document.exitFullscreen()
}

async function handleLogout() {
  try { await logout() } catch { /* ignore */ }
  authStore.clear()
  router.push('/login')
}

const currentKey = ref('home')

const menuIcons: Record<string, () => any> = {
  home: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z' })) }),
  users: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5s-3 1.34-3 3 1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z' })) }),
  roles: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z' })) }),
  departments: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z' })) }),
  menus: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z' })) }),
  operlog: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z' })) }),
  job: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z' })) }),
  generator: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z' })) }),
  config: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.488.488 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.484.484 0 0 0-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.07.62-.07.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z' })) }),
  monitor: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M20 3H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h6v2H8v2h8v-2h-2v-2h6c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 14H4V5h16v12z' })) }),
  swagger: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm-1 2l5 5h-5V4zM8 13h8v2H8v-2zm0 4h8v2H8v-2zm0-8h3v2H8V9z' })) }),
  maintenance: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.488.488 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.484.484 0 0 0-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.07.62-.07.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z' })) }),
  dict: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M4 6h16v2H4zm0 5h16v2H4zm0 5h16v2H4z' })) }),
  notice: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z' })) }),
  posts: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z' })) }),
  tickets: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14l-5-5 1.41-1.41L12 14.17l4.59-4.58L18 11l-6 6z' })) }),
  payments: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z' })) }),
  orders: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z' })) }),
  products: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M21.41 11.58l-9-9C12.05 2.22 11.55 2 11 2H4c-1.1 0-2 .9-2 2v7c0 .55.22 1.05.59 1.42l9 9c.36.36.86.58 1.41.58.55 0 1.05-.22 1.41-.59l7-7c.37-.36.59-.86.59-1.41 0-.55-.23-1.06-.59-1.42zM5.5 7C4.67 7 4 6.33 4 5.5S4.67 4 5.5 4 7 4.67 7 5.5 6.33 7 5.5 7z' })) }),
  inventory: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M20 2H4c-1 0-2 .9-2 2v3.01c0 .72.43 1.34 1 1.69V20c0 1.1 1.1 2 2 2h14c.9 0 2-.9 2-2V8.7c.57-.35 1-.97 1-1.69V4c0-1.1-1-2-2-2zm-5 12H9v-2h6v2zm5-7H4V4h16v3z' })) }),
  warehouses: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M22 21V7L12 3 2 7v14h5v-9h10v9h5zm-11-2H9v2h2v-2zm2-3h-2v2h2v-2zm2 3h-2v2h2v-2z' })) }),
  groupbuy: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z' })) }),
  flashsale: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M13.5.67s.74 2.65.74 4.8c0 2.06-1.35 3.73-3.41 3.73-2.07 0-3.63-1.67-3.63-3.73l.03-.36C5.21 7.51 4 10.62 4 14c0 4.42 3.58 8 8 8s8-3.58 8-8C20 8.61 17.41 3.8 13.5.67zM11.71 19c-1.78 0-3.22-1.4-3.22-3.14 0-1.62 1.05-2.76 2.81-3.12 1.77-.36 3.6-1.21 4.62-2.58.39 1.29.59 2.65.59 4.04 0 2.65-2.15 4.8-4.8 4.8z' })) }),
  aftersale: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm5 11H7v-2h10v2z' })) }),
  articles: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3l6 6h-6z' })) }),
  notifications: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z' })) }),
  loginlog: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M11 7L9.6 8.4l2.6 2.6H2v2h10.2l-2.6 2.6L11 17l5-5-5-5zm9 12h-8v2h8c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-8v2h8v14z' })) }),
  online: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z' })) }),
  backup: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z' })) }),
  'register-approval': () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 24 24', width: 18, height: 18, fill: 'currentColor' }, h('path', { d: 'M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z' })) }),
}

const breadcrumbs = computed(() => {
  const path = route.path
  const parts = path.split('/').filter(Boolean)
  const items = [{ label: t('nav.home'), path: '/' }]
  const map: Record<string, string> = {
    departments: t('nav.departments'), roles: t('nav.roles'), users: t('nav.users'),
    menus: t('nav.menus'), operlog: t('nav.operlog'), job: t('nav.jobs'),
    config: t('nav.config'), generator: t('nav.generator'), monitor: t('nav.monitor'),
    swagger: t('nav.swagger'), maintenance: t('nav.maintenance'), profile: t('nav.profile'),
    dict: t('nav.dict'), notices: t('nav.notice'), posts: t('nav.posts'), tickets: t('nav.tickets'), payments: t('nav.payments'), orders: t('nav.orders'), products: t('nav.products'), inventory: t('nav.inventory'), warehouses: t('nav.warehouses'), loginlog: t('nav.loginlog'),
    online: t('nav.online'), backup: t('nav.backup'),
    approval: t('nav.approval'), 'register-approval': t('nav.registerApproval'),
    brands: t('nav.brands'), categories: t('nav.categories'), coupons: t('nav.coupons'),
    analytics: t('nav.analytics'), skus: t('nav.skus'), shipments: t('nav.shipments'),
    banners: t('nav.banners'), reviews: t('nav.reviews'), 'media-library': t('nav.mediaLibrary'),
    'export-center': t('nav.exportCenter'), 'email-templates': t('nav.emailTemplates'),
    invoices: t('nav.invoices'), seo: t('nav.seo'), audit: t('nav.audit'),
    'system-logs': t('nav.systemLogs'), 'system-config': t('nav.systemConfig'),
    'chat-admin': t('nav.chatAdmin'), 'points-admin': t('nav.pointsAdmin'),
    'recommend-config': t('nav.recommendConfig'), bundles: t('nav.bundles'),
    'activity-logs': t('nav.activityLogs'), giftcards: t('nav.giftcards'),
    notifications: t('nav.notifications'), articles: t('nav.articles'),
    aftersale: t('nav.aftersale'), flashsale: t('nav.flashsale'), groupbuy: t('nav.groupbuy'),
    'member-tiers': t('nav.memberTiers'), feedback: t('nav.feedback'), tags: t('nav.tags'),
    campaigns: t('nav.campaigns'), suppliers: t('nav.suppliers'),
  }
  for (const p of parts) {
    items.push({ label: map[p] || p, path: `/${p}` })
  }
  return items
})

const pageTitle = computed(() => {
  const p = route.path.split('/')[1]
  return breadcrumbs.value[breadcrumbs.value.length - 1]?.label || ''
})

watch(() => route.path, (path) => {
  currentKey.value = path.split('/')[1] || 'home'
  const name = path.split('/').filter(Boolean).pop() || 'home'
  const title = pageTitle.value || name
  document.title = title ? `${title} - YXCLOUD` : 'YXCLOUD 管理后台'
  if (path !== '/login') tagsViewStore.addTag({ path, name, title })
}, { immediate: true })

async function fetchStats() {
  try {
    const [info, sessions, cfg] = await Promise.all([
      fetchSystemInfo(),
      fetchSessions(),
      fetchConfigByKey('sys.version').catch(() => ({ data: { configValue: '' } })),
    ])
    liveStats.value = { uptime: info.data.uptime, sessions: sessions.data.count }
    appVersion.value = cfg.data.configValue || ''
  } catch { /* ignore */ }
}

onMounted(async () => {
  document.documentElement.setAttribute('theme', isDark.value ? 'dark' : 'light')
  if ('Notification' in window && Notification.permission === 'default') Notification.requestPermission()
  window.addEventListener('theme-change', _onThemeChange)
  fetchStats()
  fetchUnreadCount().then(r => { unreadCount.value = r.data.count; if (r.data.count > 0 && Notification.permission === 'granted') new Notification('yunxingcloud', { body: t('message.unreadNotification', { count: r.data.count }), icon: '/favicon.svg' }) }).catch(() => {})
  _statsTimer = setInterval(fetchStats, 30000)
  _unreadTimer = setInterval(() => fetchUnreadCount().then(r => unreadCount.value = r.data.count).catch(() => {}), 30000)
  try {
    const res = await fetchMenuTree()
    menuOptions.value = convertMenus(res.data)
  } catch {
    menuOptions.value = [
      { label: t('nav.home'), key: 'home', icon: menuIcons.home },
      { label: t('nav.users'), key: 'users', icon: menuIcons.users },
      { label: t('nav.roles'), key: 'roles', icon: menuIcons.roles },
      { label: t('nav.departments'), key: 'departments', icon: menuIcons.departments },
      { label: t('nav.menus'), key: 'menus', icon: menuIcons.menus },
      { label: t('nav.operlog'), key: 'operlog', icon: menuIcons.operlog },
      { label: t('nav.jobs'), key: 'job', icon: menuIcons.job },
      { label: t('nav.generator'), key: 'generator', icon: menuIcons.generator },
      { label: t('nav.config'), key: 'config', icon: menuIcons.config },
      { label: t('nav.monitor'), key: 'monitor', icon: menuIcons.monitor },
      { label: t('nav.swagger'), key: 'swagger', icon: menuIcons.swagger },
      { label: t('nav.maintenance'), key: 'maintenance', icon: menuIcons.maintenance },
      { label: t('nav.dict'), key: 'dict', icon: menuIcons.dict },
      { label: t('nav.notice'), key: 'notices', icon: menuIcons.notice },
      { label: t('nav.posts'), key: 'posts', icon: menuIcons.posts },
      { label: t('nav.tickets'), key: 'tickets', icon: menuIcons.tickets },
      { label: t('nav.payments'), key: 'payments', icon: menuIcons.payments },
      { label: t('nav.orders'), key: 'orders', icon: menuIcons.orders },
      { label: t('nav.products'), key: 'products', icon: menuIcons.products },
      { label: t('nav.inventory'), key: 'inventory', icon: menuIcons.inventory },
      { label: t('nav.warehouses'), key: 'warehouses', icon: menuIcons.warehouses },
      { label: '品牌管理', key: 'brands', icon: menuIcons.products },
      { label: '分类管理', key: 'categories', icon: menuIcons.products },
      { label: '优惠券', key: 'coupons', icon: menuIcons.giftcards },
      { label: '销售分析', key: 'analytics', icon: menuIcons.monitor },
      { label: 'SKU管理', key: 'skus', icon: menuIcons.products },
      { label: '物流发货', key: 'shipments', icon: menuIcons.tickets },
      { label: 'Banner', key: 'banners', icon: menuIcons.swagger },
      { label: '批量导入', key: 'products/import', icon: menuIcons.products },
      { label: '评价管理', key: 'reviews', icon: menuIcons.notice },
      { label: '媒体库', key: 'media-library', icon: menuIcons.swagger },
      { label: '导出中心', key: 'export-center', icon: menuIcons.generator },
      { label: '邮件模板', key: 'email-templates', icon: menuIcons.config },
      { label: '发票管理', key: 'invoices', icon: menuIcons.tickets },
      { label: 'SEO管理', key: 'seo', icon: menuIcons.config },
      { label: '审计日志', key: 'audit', icon: menuIcons.operlog },
      { label: t('nav.loginlog'), key: 'loginlog', icon: menuIcons.loginlog },
      { label: t('nav.online'), key: 'online', icon: menuIcons.online },
      { label: t('nav.backup'), key: 'backup', icon: menuIcons.backup },
    ]
  }
})

function convertMenus(menus: any[]): MenuOption[] {
  if (!Array.isArray(menus)) menus = []
  const result: MenuOption[] = [{ label: t('nav.home'), key: 'home', icon: menuIcons.home }]
  function walk(list: any[]): MenuOption[] {
    if (!Array.isArray(list)) return []
    return list
      .filter((m: any) => m && m.menuType !== 'F' && m.visible !== false)
      .sort((a: any, b: any) => (a.sortOrder || 0) - (b.sortOrder || 0))
      .map((m: any) => ({
        label: m.name || '',
        key: m.path ? m.path.replace(/\//g, '') : `menu-${m.id}`,
        icon: menuIcons[m.path ? m.path.replace(/\//g, '') : ''] || undefined,
        children: Array.isArray(m.children) && m.children.length ? walk(m.children) : undefined,
      })).map((opt: any) => {
        if (!opt.children && opt.key !== 'home') {
          opt.click = () => { router.push('/' + opt.key).catch(() => {}) }
        }
        return opt
      })
  }
  return [...result, ...walk(menus)]
}

watch(currentKey, (key) => {
  if (key.startsWith('menu-')) return
  if (window.innerWidth < 768) collapsed.value = true
  const target = key === 'home' ? '/' : '/' + key
  router.push(target).catch(() => {})
})

function handleNavigate(key: string) {
  currentKey.value = key
  router.push(key === 'home' ? '/' : '/' + key).catch(() => {})
}

function handleTagAction(action: string, path: string) {
  if (action === 'refresh') { router.go(0); return }
  else if (action === 'close') tagsViewStore.removeTag(path)
  else if (action === 'other') tagsViewStore.closeOther(path)
  else if (action === 'all') { tagsViewStore.closeAll(); return }
  else if (action === 'closeLeft') {
    const tags = tagsViewStore.tags
    const idx = tags.findIndex(t => t.path === path)
    for (let i = idx - 1; i >= 1; i--) tagsViewStore.removeTag(tags[i].path)
  }
  else if (action === 'closeRight') {
    const tags = [...tagsViewStore.tags]
    const idx = tags.findIndex(t => t.path === path)
    for (let i = tags.length - 1; i > idx; i--) tagsViewStore.removeTag(tags[i].path)
  }
  router.push(path)
}

useKeyboard({
  'Ctrl+d': () => router.push('/'),
  'Ctrl+u': () => router.push('/users'),
  'Ctrl+r': () => router.push('/roles'),
  'Ctrl+m': () => router.push('/menus'),
  '/': () => { const active = document.activeElement; if (!active || active.tagName !== 'INPUT') { const inp = document.querySelector<HTMLInputElement>('.search-input-width input, [placeholder*="搜索"]'); inp?.focus() } },
})
</script>

<template>
  <AnnouncementBanner />
  <div v-if="isMobile && mobileOverlay" class="mobile-backdrop" @click="mobileOverlay = false" />
  <n-layout class="app-layout" :has-sider="true">
    <SideMenu
      :collapsed="collapsed"
      :isMobile="isMobile"
      :mobileOverlay="mobileOverlay"
      :menuOptions="menuOptions"
      :collapsedGroups="collapsedGroups"
      :currentKey="currentKey"
      @toggle-group="toggleGroup"
      @navigate="handleNavigate"
    />
    <n-layout>
      <TopHeader
        :collapsed="collapsed"
        :isDark="isDark"
        :username="authStore.username"
        :unreadCount="unreadCount"
        :pageTitle="pageTitle"
        :locale="locale"
        :breadcrumbs="breadcrumbs"
        v-model:searchQuery="searchQuery"
        :showSearchResults="showSearchResults"
        :searchResults="searchResults"
        :searchHistory="searchHistory"
        :hasSearchResults="hasSearchResults"
        @toggle-collapse="toggleSidebar"
        @toggle-theme="toggleTheme"
        @toggle-fullscreen="toggleFullscreen"
        @switch-locale="switchLocale(locale === 'zh' ? 'en' : 'zh')"
        @logout="handleLogout"
        @profile="() => router.push('/profile')"
        @open-messages="() => router.push('/messages')"
        @search="globalSearch"
        @clear-search="showSearchResults = false"
        @navigate-from-search="navigateFromSearch"
      />
      <TagsBar
        :tags="tagsViewStore.tags"
        :activeTag="tagsViewStore.activePath"
        @tag-action="handleTagAction"
        @click-tag="(path: string) => router.push(path)"
      />
      <div v-if="routeLoading" class="loading-bar" />
      <n-layout-content :style="{ padding:'20px', background: isDark ? '#101014' : '#f0f2f5', minHeight:'calc(100vh - 96px)' }" class="content-area">
        <router-view v-slot="{ Component, route: currentRoute }">
          <transition name="page" mode="out-in">
            <component :is="Component" :key="currentRoute.fullPath" />
          </transition>
        </router-view>
      </n-layout-content>
      <n-layout-footer class="footer">
        yunxingcloud {{ new Date().getFullYear() }} · {{ t('footer') }} · {{ t('footerRunning') }} {{ liveStats.uptime }} · {{ liveStatsStore.activeSessions || liveStats.sessions }} {{ t('footerOnline') }}<template v-if="appVersion"> · v{{ appVersion }}</template>
        <div class="mt-4"><a href="https://beian.miit.gov.cn/" target="_blank" class="footer-icp">湘ICP备2026022380号-1</a></div>
      </n-layout-footer>
    </n-layout>
  </n-layout>
  <CommandPalette />
  <transition name="fade">
    <div v-if="showBackTop" class="back-top" @click="scrollToTop">▲</div>
  </transition>
  <div class="watermark" v-if="authStore.username">{{ watermarkText }}</div>
</template>

<style scoped>
.watermark {
  position:fixed;inset:0;pointer-events:none;z-index:9999;
  display:grid;grid-template-columns:repeat(4,1fr);grid-template-rows:repeat(3,1fr);
  font-size:16px;color:rgba(0,0,0,0.04);user-select:none;
  transform:rotate(-20deg) scale(1.5);white-space:nowrap;overflow:hidden;
}
.page-enter-active, .page-leave-active { transition: opacity .15s ease, transform .15s ease; }
.page-enter-from { opacity: 0; transform: translateY(8px); }
.page-leave-to { opacity: 0; transform: translateY(-8px); }
@keyframes loadingBar { 0% { width:0; opacity:1 } 70% { width:70%; opacity:1 } 100% { width:90%; opacity:0 } }
.app-layout { min-height: 100vh; }
.footer { text-align: center; font-size: 12px; color: #999; padding: 12px; background: var(--n-color, #fff); border-top:1px solid var(--n-border-color, #eee); }
.footer-icp { color: #999; text-decoration: none; }
.mt-4 { margin-top: 4px; }
.back-top {
  position: fixed; bottom: 32px; right: 32px; width: 40px; height: 40px;
  background: #667eea; color: #fff; border-radius: 50%; display: flex;
  align-items: center; justify-content: center; cursor: pointer; font-size: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15); z-index: 1000; transition: transform 0.2s;
}
.back-top:hover { transform: scale(1.1); }
.mobile-backdrop { position:fixed; inset:0; background:rgba(0,0,0,0.4); z-index:99; }
.loading-bar {
  position:fixed;top:0;left:0;right:0;z-index:9999;height:2px;
  background:linear-gradient(90deg,#667eea,#764ba2);
  animation:loadingBar 0.6s ease-out;transition:opacity 0.2s;
}
@media (max-width: 768px) {
  .content-area { padding: 8px !important; }
}
</style>
