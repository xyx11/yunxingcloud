<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/api/request'
import { useAuthStore } from '@/stores/auth'
import {
  NConfigProvider, NLayout, NLayoutHeader, NLayoutSider, NLayoutContent, NLayoutFooter,
  NMenu, NButton, NBreadcrumb, NBreadcrumbItem, NDropdown, NAvatar, NTag,
  darkTheme, NIcon,
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { RouterLinkOutlined } from '@vicons/material'
import { useKeyboard } from '@/composables/useKeyboard'
import CommandPalette from '@/components/CommandPalette.vue'
import AnnouncementBanner from '@/components/AnnouncementBanner.vue'
import { useI18n } from 'vue-i18n'
import { switchLocale } from '@/locales'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()

const collapsed = ref(window.innerWidth < 768)
const isDark = ref(localStorage.getItem('theme') === 'dark')
const menuOptions = ref<MenuOption[]>([])
const liveStats = ref({ uptime: '', sessions: 0 })
const searchQuery = ref("")

window.addEventListener('resize', () => {
  if (window.innerWidth < 768) collapsed.value = true
})

function toggleTheme() {
  isDark.value = !isDark.value
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
}

const currentKey = computed(() => route.path.replace('/', '') || 'home')

const breadcrumbs = computed(() => {
  const path = route.path
  const parts = path.split('/').filter(Boolean)
  const items = [{ label: '首页', path: '/' }]
  const map: Record<string, string> = {
    departments: '部门管理', roles: '角色管理', users: '用户管理',
    menus: '菜单管理', operlog: '操作日志', job: '定时任务',
    config: '系统参数', generator: '代码生成', monitor: '系统监控', profile: '个人中心',
  }
  for (const p of parts) {
    items.push({ label: map[p] || p, path: `/${p}` })
  }
  return items
})

async function fetchStats() {
  try {
    const [info, sessions] = await Promise.all([
      request.get('/api/system/info'),
      request.get('/api/system/sessions'),
    ])
    liveStats.value = { uptime: info.data.uptime, sessions: sessions.data.count }
  } catch {}
}

onMounted(async () => {
  fetchStats()
  setInterval(fetchStats, 30000)
  try {
    const res = await request.get('/api/menus/tree')
    menuOptions.value = convertMenus(res.data)
  } catch {
    menuOptions.value = [
      { label: '首页', key: 'home' },
      { label: '部门管理', key: 'departments' },
      { label: '角色管理', key: 'roles' },
      { label: '用户管理', key: 'users' },
    ]
  }
})

function convertMenus(menus: any[]): MenuOption[] {
  return menus
    .filter((m: any) => m.menuType !== 'F' && m.visible)
    .sort((a: any, b: any) => (a.sortOrder || 0) - (b.sortOrder || 0))
    .map((m: any) => ({
      label: m.name,
      key: m.path ? m.path.replace('/', '') : `menu-${m.id}`,
      icon: m.icon ? () => h('span', m.icon) : undefined,
      children: m.children ? convertMenus(m.children) : undefined,
    }))
}

function handleMenuUpdate(key: string) {
  router.push(key === 'home' ? '/' : `/${key}`)
}

async function handleLogout() {
  try { await request.post('/api/logout') } catch {}
  authStore.clear()
  router.push('/login')
}

const userMenuOptions = [
  { label: '个人中心', key: 'profile' },
  { label: '退出登录', key: 'logout' },
]

function handleUserMenu(key: string) {
function globalSearch() {
  if (searchQuery.value.trim()) {
    window.open(`/api/search?q=${encodeURIComponent(searchQuery.value)}`, "_blank")
  }
}
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
  <n-config-provider :theme="isDark ? darkTheme : undefined">
    <AnnouncementBanner />
    <n-layout style="min-height: 100vh" :has-sider="true">
      <n-layout-sider bordered :collapsed="collapsed" collapse-mode="width" :width="220">
        <div class="logo">
          <span v-if="!collapsed">yunxingcloud</span>
          <span v-else style="font-size:16px;">YC</span>
        </div>
        <n-menu :value="currentKey" :options="menuOptions" @update:value="handleMenuUpdate" />
      </n-layout-sider>
      <n-layout>
        <n-layout-header class="header">
          <div class="header-left">
            <n-button text @click="collapsed = !collapsed" style="font-size:18px;">
              {{ collapsed ? '☰' : '☰' }}
            </n-button>
            <n-input v-model:value="searchQuery" placeholder="全局搜索..." size="small" clearable style="width:160px" @keyup:enter="globalSearch">
              <template #prefix>🔍</template>
            </n-input>
            <n-breadcrumb>
              <n-breadcrumb-item v-for="b in breadcrumbs" :key="b.path" @click="router.push(b.path)">
                {{ b.label }}
              </n-breadcrumb-item>
            </n-breadcrumb>
          </div>
          <div class="header-right">
            <n-button text size="small" @click="switchLocale(locale === 'zh' ? 'en' : 'zh')" style="margin-right:4px;font-size:12px;">
              {{ locale === 'zh' ? 'EN' : '中' }}
            </n-button>
            <n-button text @click="toggleTheme" style="font-size:18px;margin-right:12px;">
              {{ isDark ? '☀️' : '🌙' }}
            </n-button>
            <n-dropdown :options="userMenuOptions" @select="handleUserMenu">
              <div class="user-area">
                <n-avatar size="small" round style="background:#667eea;">{{ authStore.username?.charAt(0)?.toUpperCase() }}</n-avatar>
                <span>{{ authStore.username }}</span>
              </div>
            </n-dropdown>
          </div>
        </n-layout-header>
        <n-layout-content style="padding:16px; background:#f5f7fa; min-height:calc(100vh - 96px);" class="content-area">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </n-layout-content>
        <n-layout-footer class="footer">
          yunxingcloud {{ new Date().getFullYear() }} · {{ t('footer') }} · 运行 {{ liveStats.uptime }} · {{ liveStats.sessions }} 在线
        </n-layout-footer>
      </n-layout>
    </n-layout>
    <CommandPalette />
  </n-config-provider>
</template>

<style scoped>
.logo {
  height: 56px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff; font-size: 18px; font-weight: 600; letter-spacing: 1px;
}
.header {
  height: 52px; display: flex; align-items: center; justify-content: space-between;
  padding: 0 20px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.header-left { display: flex; align-items: center; gap: 16px; }
.header-right { display: flex; align-items: center; }
.user-area { display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 14px; }
.footer { text-align: center; font-size: 12px; color: #999; padding: 12px; background: #fff; border-top:1px solid #eee; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 768px) {
  .header { padding: 0 10px; flex-wrap: wrap; height: auto; min-height: 44px; }
  .header-left { gap: 8px; }
  .header-right { gap: 4px; }
  .user-area span { display: none; }
  .logo { font-size: 14px; height: 44px; }
  .content-area { padding: 8px !important; }
}
</style>
