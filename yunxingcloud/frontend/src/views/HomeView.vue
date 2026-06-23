<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, h, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/api/request'
import { useAuthStore } from '@/stores/auth'
import { useLiveStatsStore } from '@/stores/liveStats'
import { useTagsViewStore } from '@/stores/tagsView'
import {
  NConfigProvider, NLayout, NLayoutHeader, NLayoutSider, NLayoutContent, NLayoutFooter,
  NMenu, NButton, NBreadcrumb, NBreadcrumbItem, NDropdown, NAvatar, NTag, NBadge,
  darkTheme, lightTheme, NIcon, NPopover, NInput, NMessageProvider,
  zhCN, dateZhCN,
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'

import { useKeyboard } from '@/composables/useKeyboard'
import CommandPalette from '@/components/CommandPalette.vue'
import AnnouncementBanner from '@/components/AnnouncementBanner.vue'
import { useI18n } from 'vue-i18n'
import { switchLocale } from '@/locales'

const authStore = useAuthStore()
const liveStatsStore = useLiveStatsStore()
const tagsViewStore = useTagsViewStore()
const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()

const collapsed = ref(localStorage.getItem('sidebarCollapsed') === 'true' ? true : window.innerWidth < 768)
const isMobile = ref(window.innerWidth < 768)
const mobileOverlay = ref(false)
const isDark = ref(localStorage.getItem('theme') === 'dark')
const menuOptions = ref<MenuOption[]>([])
const liveStats = ref({ uptime: '', sessions: 0 })
const appVersion = ref('')
const unreadCount = ref(0)
const watermarkText = computed(() => {
  const name = authStore.username || 'yunxingcloud'
  const time = new Date().toLocaleDateString('zh-CN')
  return Array(12).fill(`${name} ${time}`).join('    ')
})
const ctxMenuTag = ref('')
const ctxMenuX = ref(0)
const ctxMenuY = ref(0)

function handleCtxMenu(k: string) {
  const path = ctxMenuTag.value
  if (k === 'refresh') router.go(0)
  else if (k === 'close') tagsViewStore.removeTag(path)
  else if (k === 'other') tagsViewStore.closeOther(path)
  else if (k === 'closeLeft') {
    const tags = tagsViewStore.tags; const idx = tags.findIndex(t => t.path === path)
    for (let i = idx - 1; i >= 1; i--) tagsViewStore.removeTag(tags[i].path)
  }
  else if (k === 'closeRight') {
    const tags = [...tagsViewStore.tags]; const idx = tags.findIndex(t => t.path === path)
    for (let i = tags.length - 1; i > idx; i--) tagsViewStore.removeTag(tags[i].path)
  }
  ctxMenuTag.value = ''
  router.push(path)
}
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
  request.get('/api/search', { params: { q } }).then(res => {
    searchResults.value = res.data
    showSearchResults.value = true
  }).catch(() => {
    searchResults.value = {}
    showSearchResults.value = false
  })
}

function navigateFromSearch(type: string, item: any) {
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

function onScroll() {
  showBackTop.value = window.scrollY > 300
}

window.addEventListener('resize', onResize)
window.addEventListener('scroll', onScroll)
onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('scroll', onScroll)
})

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

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
    dict: t('nav.dict'), notices: t('nav.notice'), posts: t('nav.posts'), loginlog: t('nav.loginlog'),
    online: t('nav.online'), backup: t('nav.backup'),
    approval: t('nav.approval'), 'register-approval': t('nav.registerApproval'),
  }
  for (const p of parts) {
    items.push({ label: map[p] || p, path: `/${p}` })
  }
  return items
})

const pageTitle = computed(() => {
  const p = route.path.split('/')[1]
  const map: Record<string, string> = {
    '': t('nav.dashboard'), departments: t('nav.departments'), roles: t('nav.roles'), users: t('nav.users'),
    menus: t('nav.menus'), operlog: t('nav.operlog'), job: t('nav.jobs'),
    config: t('nav.config'), generator: t('nav.generator'), monitor: t('nav.monitor'),
    swagger: t('nav.swagger'), maintenance: t('nav.maintenance'), profile: t('nav.profile'),
    dict: t('nav.dict'), notices: t('nav.notice'), posts: t('nav.posts'), loginlog: t('nav.loginlog'),
    online: t('nav.online'), backup: t('nav.backup'),
    approval: t('nav.approval'), 'register-approval': t('nav.registerApproval'),
  }
  return map[p] || ''
})

watch(() => route.path, (path) => {
  currentKey.value = path.split('/')[1] || 'home'
  const name = path.split('/').filter(Boolean).pop() || 'home'
  const title = pageTitle.value || name
  if (path !== '/login') tagsViewStore.addTag({ path, name, title })
}, { immediate: true })

async function fetchStats() {
  try {
    const [info, sessions, cfg] = await Promise.all([
      request.get('/api/system/info'),
      request.get('/api/system/sessions'),
      request.get('/api/config/key/sys.version').catch(() => ({ data: { configValue: '' } })),
    ])
    liveStats.value = { uptime: info.data.uptime, sessions: sessions.data.count }
    appVersion.value = cfg.data.configValue || ''
  } catch {}
}

onMounted(async () => {
  document.documentElement.setAttribute('theme', isDark.value ? 'dark' : 'light')
  if ('Notification' in window && Notification.permission === 'default') Notification.requestPermission()
  window.addEventListener('theme-change', ((e: CustomEvent) => { isDark.value = e.detail === 'dark' }) as EventListener)
  fetchStats()
  request.get('/api/messages/unread-count').then(r => { unreadCount.value = r.data.count; if (r.data.count > 0 && Notification.permission === 'granted') new Notification('yunxingcloud', { body: t('message.unreadNotification', { count: r.data.count }), icon: '/favicon.svg' }) }).catch(() => {})
  setInterval(fetchStats, 30000)
  setInterval(() => request.get('/api/messages/unread-count').then(r => unreadCount.value = r.data.count).catch(() => {}), 30000)
  try {
    const res = await request.get('/api/menus/tree')
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
      { label: t('nav.loginlog'), key: 'loginlog', icon: menuIcons.loginlog },
      { label: t('nav.online'), key: 'online', icon: menuIcons.online },
      { label: t('nav.backup'), key: 'backup', icon: menuIcons.backup },
      { label: t('nav.registerApproval'), key: 'register-approval', icon: menuIcons['register-approval'] },
    ]
  }
})

function convertMenus(menus: any[]): MenuOption[] {
  const result: MenuOption[] = [{ label: t('nav.home'), key: 'home', icon: menuIcons.home }]
  function walk(list: any[]): MenuOption[] {
    return list
      .filter((m: any) => m.menuType !== 'F' && m.visible)
      .sort((a: any, b: any) => (a.sortOrder || 0) - (b.sortOrder || 0))
      .map((m: any) => ({
        label: m.name,
        key: m.path ? m.path.replace(/\//g, '') : `menu-${m.id}`,
        icon: menuIcons[m.path ? m.path.replace(/\//g, '') : ''] || undefined,
        children: m.children ? walk(m.children) : undefined,
      })).map((opt: any) => {
        if (!opt.children && opt.key !== 'home') {
          opt.click = () => { console.log('[click]', opt.key); router.push('/' + opt.key).catch(() => {}); }
        }
        return opt
      })
  }
  return [...result, ...walk(menus)]
}

function handleMenuUpdate(key: string) {
  if (key.startsWith('menu-')) return
  if (window.innerWidth < 768) collapsed.value = true
  const target = key === 'home' ? '/' : '/' + key
  router.push(target).catch(() => {})
}

watch(currentKey, (key) => {
  if (key.startsWith('menu-')) return
  if (window.innerWidth < 768) collapsed.value = true
  const target = key === 'home' ? '/' : '/' + key
  router.push(target).catch(() => {})
})


async function handleLogout() {
  try { await request.post('/api/logout') } catch {}
  authStore.clear()
  router.push('/login')
}

const userMenuOptions = computed(() => [
  { label: t('nav.profile'), key: 'profile' },
  { label: t('nav.logout'), key: 'logout' },
])

const contextMenuOptions = computed(() => [
  { label: t('nav.closeCurrent'), key: 'close' },
  { label: t('nav.closeOthers'), key: 'other' },
  { label: t('nav.closeAll'), key: 'all' },
])

const ctxMenuDropdownOptions = computed(() => [
  { label: t('common.refresh'), key: 'refresh' },
  { label: t('common.close'), key: 'close' },
  { label: t('nav.closeLeft'), key: 'closeLeft' },
  { label: t('nav.closeRight'), key: 'closeRight' },
  { label: t('nav.closeOthers'), key: 'other' },
])

function handleUserMenu(key: string) {
  if (key === 'profile') router.push('/profile')
  if (key === 'logout') handleLogout()
}

useKeyboard({
  'Ctrl+d': () => router.push('/'),
  'Ctrl+u': () => router.push('/users'),
  'Ctrl+r': () => router.push('/roles'),
  'Ctrl+m': () => router.push('/menus'),
})
</script>

<template>
  <n-config-provider :theme="isDark ? darkTheme : lightTheme" :locale="zhCN" :date-locale="dateZhCN">
    <AnnouncementBanner />
    <div v-if="isMobile && mobileOverlay" class="mobile-backdrop" @click="mobileOverlay = false" />
    <n-layout style="min-height: 100vh" :has-sider="true">
      <n-layout-sider
        bordered :collapsed="isMobile ? false : collapsed" collapse-mode="width" :width="220"
        :style="isMobile ? { position: 'fixed', left: mobileOverlay ? '0' : '-220px', top: 0, bottom: 0, zIndex: 100, transition: 'left 0.3s' } : {}"
      >
        <div class="logo">
          <span v-if="!collapsed">yunxingcloud</span>
          <span v-else style="font-size:16px;">YC</span>
        </div>
        <div class="custom-menu" :class="{ collapsed }">
          <template v-for="item in menuOptions" :key="item.key">
            <template v-if="item.children">
              <div v-show="!collapsed" class="menu-group-title">{{ item.label }}</div>
              <div
                v-for="child in item.children"
                :key="child.key"
                :class="['menu-item', { active: currentKey === child.key }]"
                @click="currentKey = String(child.key); router.push(child.key === 'home' ? '/' : '/' + String(child.key)).catch(()=>{})"
              >
                <span v-if="child.icon" class="menu-icon"><component :is="child.icon" /></span>
                <span v-show="!collapsed" class="menu-label">{{ child.label }}</span>
              </div>
            </template>
          </template>
        </div>
      </n-layout-sider>
      <n-layout>
        <n-layout-header class="header">
          <div class="header-left">
            <n-button text @click="toggleSidebar" style="font-size:18px;">
              {{ collapsed ? '▶' : '◀' }}
            </n-button>
            <span v-if="collapsed" class="mobile-title">{{ pageTitle }}</span>
            <n-popover trigger="manual" :show="showSearchResults" placement="bottom-start" :width="320" @clickoutside="showSearchResults = false">
              <template #trigger>
                <n-input
                  v-model:value="searchQuery" :placeholder="t('nav.searchPlaceholder')" size="small" clearable style="width:160px"
                  @keyup:enter="globalSearch" @clear="showSearchResults = false" @focus="searchQuery && globalSearch()"
                >
                  <template #prefix>🔍</template>
                </n-input>
              </template>
              <div v-if="!searchQuery && searchHistory.length && !showSearchResults" style="max-height:200px;overflow-y:auto">
                <div style="font-size:11px;color:#999;padding:2px 8px">{{ t('nav.recentSearches') }}</div>
                <div v-for="h in searchHistory" :key="h" class="search-item" @click="searchQuery = h; globalSearch()" style="justify-content:flex-start">{{ h }}</div>
              </div>
              <div v-if="hasSearchResults" style="max-height:360px;overflow-y:auto">
                <div v-if="searchResults.users?.length" style="margin-bottom:8px">
                  <div style="font-size:12px;color:#999;padding:4px 0">{{ t('nav.users') }}</div>
                  <div v-for="u in searchResults.users" :key="u.id" class="search-item" @click="navigateFromSearch('users', u)">
                    <span>{{ u.username }}</span><span style="color:#999;font-size:12px">{{ u.nickname }} {{ u.email }}</span>
                  </div>
                </div>
                <div v-if="searchResults.roles?.length" style="margin-bottom:8px">
                  <div style="font-size:12px;color:#999;padding:4px 0">{{ t('nav.roles') }}</div>
                  <div v-for="r in searchResults.roles" :key="r.id" class="search-item" @click="navigateFromSearch('roles', r)">
                    <span>{{ r.name }}</span><span style="color:#999;font-size:12px">{{ r.code }}</span>
                  </div>
                </div>
                <div v-if="searchResults.menus?.length" style="margin-bottom:8px">
                  <div style="font-size:12px;color:#999;padding:4px 0">{{ t('nav.menus') }}</div>
                  <div v-for="m in searchResults.menus" :key="m.id" class="search-item" @click="navigateFromSearch('menus', m)">
                    <span>{{ m.name }}</span><span style="color:#999;font-size:12px">{{ m.path }}</span>
                  </div>
                </div>
                <div v-if="searchResults.configs?.length">
                  <div style="font-size:12px;color:#999;padding:4px 0">{{ t('nav.config') }}</div>
                  <div v-for="c in searchResults.configs" :key="c.id" class="search-item" @click="navigateFromSearch('configs', c)">
                    <span>{{ c.name }}</span><span style="color:#999;font-size:12px">{{ c.config_key }}</span>
                  </div>
                </div>
                <div v-if="searchResults.dict?.length">
                  <div style="font-size:12px;color:#999;padding:4px 0">{{ t('nav.dict') }}</div>
                  <div v-for="d in searchResults.dict" :key="d.id" class="search-item" @click="navigateFromSearch('dict', d)">
                    <span>{{ d.dict_name }}</span><span style="color:#999;font-size:12px">{{ d.dict_type }}</span>
                  </div>
                </div>
                <div v-if="searchResults.notices?.length">
                  <div style="font-size:12px;color:#999;padding:4px 0">{{ t('nav.notice') }}</div>
                  <div v-for="n in searchResults.notices" :key="n.id" class="search-item" @click="navigateFromSearch('notices', n)">
                    <span>{{ n.notice_title }}</span>
                  </div>
                </div>
                <div v-if="searchResults.posts?.length">
                  <div style="font-size:12px;color:#999;padding:4px 0">{{ t('nav.posts') }}</div>
                  <div v-for="p in searchResults.posts" :key="p.id" class="search-item" @click="navigateFromSearch('posts', p)">
                    <span>{{ p.post_name }}</span><span style="color:#999;font-size:12px">{{ p.post_code }}</span>
                  </div>
                </div>
                <div v-if="searchResults.departments?.length">
                  <div style="font-size:12px;color:#999;padding:4px 0">{{ t('nav.departments') }}</div>
                  <div v-for="d in searchResults.departments" :key="d.id" class="search-item" @click="navigateFromSearch('departments', d)">
                    <span>{{ d.name }}</span>
                  </div>
                </div>
                <div v-if="searchResults.total" style="font-size:11px;color:#999;text-align:center;padding:4px 0;border-top:1px solid var(--n-border-color,#eee)">{{ t('nav.searchResults', { count: searchResults.total }) }}</div>
              </div>
              <div v-else style="color:#999;padding:8px;text-align:center">{{ t('nav.noResults') }}</div>
            </n-popover>
            <n-breadcrumb>
              <n-breadcrumb-item v-for="b in breadcrumbs" :key="b.path" @click="router.push(b.path)">
                {{ b.label }}
              </n-breadcrumb-item>
            </n-breadcrumb>
          </div>
          <div class="header-right">
            <n-button text @click="toggleFullscreen" style="font-size:16px;margin-right:8px;" :title="t('nav.fullscreen')">⛶</n-button>
            <n-button text size="small" @click="switchLocale(locale === 'zh' ? 'en' : 'zh')" style="margin-right:4px;font-size:12px;">
              {{ locale === 'zh' ? 'EN' : '中' }}
            </n-button>
            <n-button text @click="toggleTheme" style="font-size:18px;margin-right:4px;">
              {{ isDark ? '☀️' : '🌙' }}
            </n-button>
            <n-badge :value="unreadCount" :max="99" style="margin-right:12px;">
              <n-button text @click="router.push('/messages')" style="font-size:18px;" :title="t('nav.openMessages')">📬</n-button>
            </n-badge>
            <n-dropdown :options="userMenuOptions" @select="handleUserMenu">
              <div class="user-area">
                <n-avatar size="small" round style="background:#667eea;">{{ authStore.username?.charAt(0)?.toUpperCase() }}</n-avatar>
                <span>{{ authStore.username }}</span>
              </div>
            </n-dropdown>
          </div>
        </n-layout-header>
        <div class="tags-view" v-if="tagsViewStore.tags.length">
          <span
            v-for="tag in tagsViewStore.tags" :key="tag.path"
            :class="['tag-item', { active: tagsViewStore.activePath === tag.path }]"
            @click="router.push(tag.path)"
            @contextmenu.prevent="ctxMenuTag = tag.path; ctxMenuX = $event.clientX; ctxMenuY = $event.clientY"
          >
            {{ tag.title }}
            <span v-if="tag.path !== '/'" class="tag-close" @click.stop="tagsViewStore.removeTag(tag.path)">×</span>
          </span>
          <n-dropdown trigger="click" :options="contextMenuOptions" @select="(k:string) => { if(k==='close') tagsViewStore.removeTag(tagsViewStore.activePath); if(k==='other') tagsViewStore.closeOther(tagsViewStore.activePath); if(k==='all') tagsViewStore.closeAll(); router.push(tagsViewStore.activePath) }">
            <span class="tag-close-all">▼</span>
          </n-dropdown>
          <n-dropdown v-if="ctxMenuTag" trigger="manual" :show="!!ctxMenuTag" :x="ctxMenuX" :y="ctxMenuY" :options="ctxMenuDropdownOptions" @select="handleCtxMenu" @clickoutside="ctxMenuTag = ''" placement="bottom-start" />
        </div>
        <n-layout-content :style="{ padding:'20px', background: isDark ? '#101014' : '#f0f2f5', minHeight:'calc(100vh - 96px)' }" class="content-area">
          <n-message-provider>
            <router-view v-slot="{ Component, route: currentRoute }">
              <transition name="page" mode="out-in">
                <component :is="Component" :key="currentRoute.fullPath" />
              </transition>
            </router-view>
          </n-message-provider>
        </n-layout-content>
        <n-layout-footer class="footer">
          yunxingcloud {{ new Date().getFullYear() }} · {{ t('footer') }} · {{ t('footerRunning') }} {{ liveStats.uptime }} · {{ liveStatsStore.activeSessions || liveStats.sessions }} {{ t('footerOnline') }}<template v-if="appVersion"> · v{{ appVersion }}</template>
          <div style="margin-top:4px"><a href="https://beian.miit.gov.cn/" target="_blank" style="color:#999;text-decoration:none">湘ICP备2026022380号-1</a></div>
        </n-layout-footer>
      </n-layout>
    </n-layout>
    <CommandPalette />
    <transition name="fade">
      <div v-if="showBackTop" class="back-top" @click="scrollToTop">▲</div>
    </transition>
    <div class="watermark" v-if="authStore.username">{{ watermarkText }}</div>
  </n-config-provider>
</template>

<style scoped>
.watermark {
  position:fixed;inset:0;pointer-events:none;z-index:9999;
  display:grid;grid-template-columns:repeat(4,1fr);grid-template-rows:repeat(3,1fr);
  font-size:16px;color:rgba(0,0,0,0.04);user-select:none;
  transform:rotate(-20deg) scale(1.5);white-space:nowrap;overflow:hidden;
}
</style>

<style scoped>
.logo {
  height: 56px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff; font-size: 18px; font-weight: 600; letter-spacing: 1px;
  user-select: none;
}
.header {
  height: 52px; display: flex; align-items: center; justify-content: space-between;
  padding: 0 20px; background: var(--n-color, #fff); box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  border-bottom: 1px solid var(--n-border-color, #f0f0f0);
}
.header-left { display: flex; align-items: center; gap: 16px; }
.header-right { display: flex; align-items: center; }
.user-area { display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 14px; }
.footer { text-align: center; font-size: 12px; color: #999; padding: 12px; background: var(--n-color, #fff); border-top:1px solid var(--n-border-color, #eee); }
.search-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 6px 8px; cursor: pointer; border-radius: 4px; font-size: 13px;
}
.search-item:hover { background: var(--n-action-color, #f0f0f0); }
.back-top {
  position: fixed; bottom: 32px; right: 32px; width: 40px; height: 40px;
  background: #667eea; color: #fff; border-radius: 50%; display: flex;
  align-items: center; justify-content: center; cursor: pointer; font-size: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15); z-index: 1000; transition: transform 0.2s;
}
.back-top:hover { transform: scale(1.1); }
.tags-view {
  display: flex; align-items: center; gap: 2px; padding: 4px 16px;
  background: var(--n-color, #fff); border-bottom: 1px solid var(--n-border-color, #eee);
  overflow-x: auto; white-space: nowrap;
}
.tag-item {
  display: inline-flex; align-items: center; gap: 4px; padding: 3px 10px;
  font-size: 12px; border-radius: 3px; cursor: pointer; color: #666;
  border: 1px solid transparent; transition: all 0.2s;
}
.tag-item:hover { background: rgba(102,126,234,0.08); }
.tag-item.active { background: rgba(102,126,234,0.12); color: var(--primary); border-color: var(--primary); }
.tag-close { font-size: 14px; line-height: 1; opacity: 0.5; }
.tag-close:hover { opacity: 1; color: #f5576c; }
.tag-close-all { font-size: 10px; cursor: pointer; padding: 4px 6px; opacity: 0.5; }
.tag-close-all:hover { opacity: 1; }
.mobile-title { font-size: 14px; font-weight: 500; color: var(--n-text-color, #333); display: none; }
@media (max-width: 768px) { .mobile-title { display: inline; } }

@media (max-width: 768px) {
  .header { padding: 0 10px; flex-wrap: wrap; height: auto; min-height: 44px; }
  .header-left { gap: 8px; }
  .header-right { gap: 4px; }
  .user-area span { display: none; }
  .logo { font-size: 14px; height: 44px; }
  .content-area { padding: 8px !important; }
}

.mobile-backdrop { position:fixed; inset:0; background:rgba(0,0,0,0.4); z-index:99; }
.custom-menu { padding: 8px 0; overflow-x: hidden; }
.custom-menu.collapsed .menu-item { padding: 10px; justify-content: center; }
.custom-menu.collapsed .menu-icon { font-size: 20px; }
.menu-group { margin-bottom: 4px; }
.menu-group-title {
  padding: 8px 20px; font-size: 11px; font-weight: 600;
  color: var(--n-text-color-3, #999); white-space: nowrap;
}
.menu-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 24px; font-size: 14px; cursor: pointer;
  color: var(--n-text-color, #333); white-space: nowrap;
  transition: all 0.15s ease; border-left: 3px solid transparent;
}
.menu-item:hover { background: rgba(102,126,234,0.08); }
.menu-item.active {
  color: #667eea; background: rgba(102,126,234,0.12);
  border-left-color: #667eea; font-weight: 500;
}
.menu-icon { font-size: 18px; display: flex; align-items: center; flex-shrink: 0; }
.menu-label { flex: 1; overflow: hidden; text-overflow: ellipsis; }
</style>
