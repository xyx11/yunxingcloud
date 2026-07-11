<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  query: string
  results: Record<string, any[]>
  history: string[]
  hasResults: boolean
}>()

const emit = defineEmits<{
  search: [query: string]
  navigate: [type: string, item: any]
}>()

const { t } = useI18n()
</script>

<template>
  <div v-if="!query && history.length && !hasResults" class="search-history">
    <div class="search-section-title">{{ t('nav.recentSearches') }}</div>
    <div v-for="item in history" :key="item" class="search-item search-item-start" @click="emit('search', item)">{{ item }}</div>
  </div>
  <div v-if="hasResults" class="search-results">
    <div v-if="results.users?.length" class="search-section">
      <div class="search-section-label">{{ t('nav.users') }}</div>
      <div v-for="u in results.users" :key="u.id" class="search-item" @click="emit('navigate', 'users', u)">
        <span>{{ u.username }}</span><span class="search-item-detail">{{ u.nickname }} {{ u.email }}</span>
      </div>
    </div>
    <div v-if="results.roles?.length" class="search-section">
      <div class="search-section-label">{{ t('nav.roles') }}</div>
      <div v-for="r in results.roles" :key="r.id" class="search-item" @click="emit('navigate', 'roles', r)">
        <span>{{ r.name }}</span><span class="search-item-detail">{{ r.code }}</span>
      </div>
    </div>
    <div v-if="results.menus?.length" class="search-section">
      <div class="search-section-label">{{ t('nav.menus') }}</div>
      <div v-for="m in results.menus" :key="m.id" class="search-item" @click="emit('navigate', 'menus', m)">
        <span>{{ m.name }}</span><span class="search-item-detail">{{ m.path }}</span>
      </div>
    </div>
    <div v-if="results.configs?.length">
      <div class="search-section-label">{{ t('nav.config') }}</div>
      <div v-for="c in results.configs" :key="c.id" class="search-item" @click="emit('navigate', 'configs', c)">
        <span>{{ c.name }}</span><span class="search-item-detail">{{ c.config_key }}</span>
      </div>
    </div>
    <div v-if="results.dict?.length">
      <div class="search-section-label">{{ t('nav.dict') }}</div>
      <div v-for="d in results.dict" :key="d.id" class="search-item" @click="emit('navigate', 'dict', d)">
        <span>{{ d.dict_name }}</span><span class="search-item-detail">{{ d.dict_type }}</span>
      </div>
    </div>
    <div v-if="results.notices?.length">
      <div class="search-section-label">{{ t('nav.notice') }}</div>
      <div v-for="n in results.notices" :key="n.id" class="search-item" @click="emit('navigate', 'notices', n)">
        <span>{{ n.notice_title }}</span>
      </div>
    </div>
    <div v-if="results.posts?.length">
      <div class="search-section-label">{{ t('nav.posts') }}</div>
      <div v-for="p in results.posts" :key="p.id" class="search-item" @click="emit('navigate', 'posts', p)">
        <span>{{ p.post_name }}</span><span class="search-item-detail">{{ p.post_code }}</span>
      </div>
    </div>
    <div v-if="results.departments?.length">
      <div class="search-section-label">{{ t('nav.departments') }}</div>
      <div v-for="d in results.departments" :key="d.id" class="search-item" @click="emit('navigate', 'departments', d)">
        <span>{{ d.name }}</span>
      </div>
    </div>
    <div v-if="results.jobs?.length">
      <div class="search-section-label">{{ t('nav.jobs') }}</div>
      <div v-for="j in results.jobs" :key="j.id" class="search-item" @click="emit('navigate', 'jobs', j)">
        <span>{{ j.job_name }}</span>
      </div>
    </div>
    <div v-if="results.total" class="search-result-total">{{ t('nav.searchResults', { count: results.total }) }}</div>
  </div>
  <div v-else-if="!hasResults && query" class="search-no-results">{{ t('nav.noResults') }}</div>
</template>

<style scoped>
.search-history { max-height: 200px; overflow-y: auto; }
.search-results { max-height: 360px; overflow-y: auto; }
.search-section { margin-bottom: 8px; }
.search-section-title { font-size: 11px; color: #999; padding: 2px 8px; }
.search-section-label { font-size: 12px; color: #999; padding: 4px 0; }
.search-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 6px 8px; cursor: pointer; border-radius: 4px; font-size: 13px;
}
.search-item:hover { background: var(--n-action-color, #f0f0f0); }
.search-item-detail { color: #999; font-size: 12px; }
.search-item-start { justify-content: flex-start; }
.search-result-total { font-size: 11px; color: #999; text-align: center; padding: 4px 0; border-top: 1px solid var(--n-border-color, #eee); }
.search-no-results { color: #999; padding: 8px; text-align: center; }
</style>
