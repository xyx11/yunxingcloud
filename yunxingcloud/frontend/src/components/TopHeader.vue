<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  NLayoutHeader, NButton, NBreadcrumb, NBreadcrumbItem, NDropdown, NAvatar, NBadge,
  NPopover, NInput,
} from 'naive-ui'
import SearchOverlay from './SearchOverlay.vue'

const props = defineProps<{
  collapsed: boolean
  isDark: boolean
  username: string
  unreadCount: number
  pageTitle: string
  locale: string
  breadcrumbs: { label: string; path: string }[]
  searchQuery: string
  showSearchResults: boolean
  searchResults: Record<string, any[]>
  searchHistory: string[]
  hasSearchResults: boolean
}>()

const emit = defineEmits<{
  'toggle-collapse': []
  'toggle-theme': []
  'toggle-fullscreen': []
  'switch-locale': []
  logout: []
  profile: []
  'open-messages': []
  'update:searchQuery': [value: string]
  search: [query: string]
  'clear-search': []
  'navigate-from-search': [type: string, item: any]
}>()

const router = useRouter()
const { t } = useI18n()

const searchQueryModel = computed({
  get: () => props.searchQuery,
  set: (val: string) => emit('update:searchQuery', val),
})

const userMenuOptions = computed(() => [
  { label: t('nav.profile'), key: 'profile' },
  { label: t('nav.logout'), key: 'logout' },
])
</script>

<template>
  <n-layout-header class="header">
    <div class="header-left">
      <n-button text class="sidebar-toggle" @click="$emit('toggle-collapse')">
        {{ collapsed ? '▶' : '◀' }}
      </n-button>
      <span v-if="collapsed" class="mobile-title">{{ pageTitle }}</span>
      <n-popover
        trigger="manual" :show="showSearchResults" placement="bottom-start"
        :width="320" @clickoutside="$emit('clear-search')"
      >
        <template #trigger>
          <n-input
            v-model:value="searchQueryModel" class="search-input-width"
            :placeholder="t('nav.searchPlaceholder')" size="small" clearable
            @keyup:enter="$emit('search', searchQueryModel)"
            @clear="$emit('clear-search')"
            @focus="searchQueryModel && $emit('search', searchQueryModel)"
          >
            <template #prefix>&#x1F50D;</template>
          </n-input>
        </template>
        <SearchOverlay
          :query="searchQueryModel"
          :results="searchResults"
          :history="searchHistory"
          :hasResults="hasSearchResults"
          @search="(q: string) => $emit('search', q)"
          @navigate="(type: string, item: any) => $emit('navigate-from-search', type, item)"
        />
      </n-popover>
      <n-breadcrumb>
        <n-breadcrumb-item v-for="b in breadcrumbs" :key="b.path" @click="router.push(b.path)">
          {{ b.label }}
        </n-breadcrumb-item>
      </n-breadcrumb>
    </div>
    <div class="header-right">
      <n-button text class="header-icon-btn" @click="$emit('toggle-fullscreen')" :title="t('nav.fullscreen')">&#x26F6;</n-button>
      <n-button text size="small" class="header-locale-btn" @click="$emit('switch-locale')">
        {{ locale === 'zh' ? 'EN' : '中' }}
      </n-button>
      <n-button text class="header-theme-btn" @click="$emit('toggle-theme')">
        {{ isDark ? '☀️' : '🌙' }}
      </n-button>
      <n-badge :value="unreadCount" :max="99" class="header-badge">
        <n-button text class="header-icon-btn" @click="$emit('open-messages')" :title="t('nav.openMessages')">&#x1F4EC;</n-button>
      </n-badge>
      <n-dropdown :options="userMenuOptions" @select="(key: string) => $emit(key as 'profile' | 'logout')">
        <div class="user-area">
          <n-avatar size="small" round class="avatar-bg">{{ username?.charAt(0)?.toUpperCase() }}</n-avatar>
          <span>{{ username }}</span>
        </div>
      </n-dropdown>
    </div>
  </n-layout-header>
</template>

<style scoped>
.header {
  height: 52px; display: flex; align-items: center; justify-content: space-between;
  padding: 0 20px; background: var(--n-color, #fff); box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  border-bottom: 1px solid var(--n-border-color, #f0f0f0);
}
.header-left { display: flex; align-items: center; gap: 16px; flex: 1; }
.header-right { display: flex; align-items: center; }
.user-area { display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 14px; }
.mobile-title { font-size: 14px; font-weight: 500; color: var(--n-text-color, #333); }
.search-input-width { width: 160px; }
.sidebar-toggle { font-size: 18px; }
.header-icon-btn { font-size: 18px; }
.header-locale-btn { margin-right: 4px; font-size: 12px; }
.header-theme-btn { font-size: 18px; margin-right: 4px; }
.header-badge { margin-right: 12px; }
.avatar-bg { background: #667eea; }
@media (max-width: 768px) {
  .header { padding: 0 10px; flex-wrap: wrap; height: auto; min-height: 44px; }
  .header-left { gap: 8px; }
  .header-right { gap: 4px; }
  .user-area span { display: none; }
}
</style>
