<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchProducts } from '@/api/product'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import { formatPrice } from '@/utils/format'
import type { Product } from '@/types'
import LazyImage from '@/components/LazyImage.vue'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const results = ref<Product[]>([])
const totalPages = ref(0)
const page = ref(0)
const loading = ref(false)
const searchQuery = ref('')
const searchInput = ref('')
const showHistory = ref(false)
const sortBy = ref('')
const suggestIndex = ref(-1)
const searchFocused = ref(false)

const hotKeywords = ref(['华为手机', 'MacBook', 'Nike', '茅台', '空调', '耳机', '运动鞋', '洗发水'])
const history = ref<string[]>(JSON.parse(localStorage.getItem('searchHistory') || '[]'))
const suggestions = ref<Product[]>([])
let suggestTimer: ReturnType<typeof setTimeout> | null = null

function onInput() {
  if (suggestTimer) clearTimeout(suggestTimer)
  const q = searchInput.value.trim()
  if (!q) { suggestions.value = []; suggestIndex.value = -1; return }
  suggestTimer = setTimeout(async () => {
    try { const r = await searchProducts(q, 0, 5); suggestions.value = (r.data.content || r.data || []).slice(0, 5); suggestIndex.value = -1 } catch { suggestions.value = [] }
  }, 200)
}

function onKeydown(e: KeyboardEvent) {
  if (!suggestions.value.length) return
  if (e.key === 'ArrowDown') { e.preventDefault(); suggestIndex.value = Math.min(suggestIndex.value + 1, suggestions.value.length - 1) }
  else if (e.key === 'ArrowUp') { e.preventDefault(); suggestIndex.value = Math.max(suggestIndex.value - 1, -1) }
  else if (e.key === 'Enter' && suggestIndex.value >= 0) { e.preventDefault(); searchKeyword(suggestions.value[suggestIndex.value].name) }
}

onMounted(() => { searchQuery.value = (route.query.q as string) || ''; searchInput.value = searchQuery.value; if (searchQuery.value) doSearch() })
onUnmounted(() => { if (suggestTimer) clearTimeout(suggestTimer) })
watch(() => route.query.q, (q) => { searchQuery.value = (q as string) || ''; searchInput.value = searchQuery.value; page.value = 0; if (searchQuery.value) doSearch() })

async function doSearch() {
  if (!searchInput.value.trim()) return
  const q = searchInput.value.trim(); searchQuery.value = q; loading.value = true; suggestions.value = []
  const h = [q, ...history.value.filter(h => h !== q)].slice(0, 10); history.value = h
  localStorage.setItem('searchHistory', JSON.stringify(h)); showHistory.value = false
  try { const r = await searchProducts(q, page.value, 20); const data = r.data; results.value = data.content || data || []; totalPages.value = data.totalPages || 0; router.replace({ query: { q } }) } catch { results.value = [] } finally { loading.value = false }
}

function loadMore() { page.value++; doSearch() }
function clearInput() { searchInput.value = ''; suggestions.value = []; searchFocused.value = false }
function cancelSearch() { searchQuery.value = ''; searchInput.value = ''; results.value = []; router.replace({ query: {} }) }

function setSort(s: string) {
  sortBy.value = sortBy.value === s ? '' : s
  const sorted = [...results.value]
  if (s === 'price_asc') sorted.sort((a: Product, b: Product) => a.price - b.price)
  else if (s === 'price_desc') sorted.sort((a: Product, b: Product) => b.price - a.price)
  else if (s === 'sales') sorted.sort((a: Product, b: Product) => (b.sales || 0) - (a.sales || 0))
  results.value = sorted
}

function highlightName(name: string): string {
  if (!searchQuery.value) return name
  return name.replace(new RegExp(`(${searchQuery.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi'), '<mark>$1</mark>')
}

function searchKeyword(kw: string) { searchInput.value = kw; searchQuery.value = kw; doSearch() }
function goDetail(id: number) { router.push(`/product/${id}`) }
function clearHistory() { history.value = []; localStorage.removeItem('searchHistory') }
function removeHistoryItem(kw: string) { history.value = history.value.filter(h => h !== kw); localStorage.setItem('searchHistory', JSON.stringify(history.value)) }

async function quickAdd(e: Event, p: Product) {
  e.stopPropagation()
  try { await addToCart(p.id, 1); toast.success('已加入购物车'); (p as Product)._added = true; setTimeout(() => (p as Product)._added = false, 1500) } catch { toast.error('添加失败') }
}
</script>

<template>
  <div>
    <!-- Empty Search -->
    <div v-if="!searchQuery" class="search-empty">
      <div class="search-bar">
        <input v-model="searchInput" :placeholder="t('search.placeholder')" @input="onInput" @keyup.enter="doSearch" @keydown="onKeydown" @focus="showHistory = true; searchFocused = true" class="search-input" />
        <button v-if="searchInput" class="search-clear" @click="clearInput" aria-label="清除">✕</button>
        <button class="search-btn" @click="doSearch">{{ t('common.search') }}</button>
      </div>

      <!-- Suggestions -->
      <div v-if="suggestions.length" class="suggestions">
        <div v-for="(s, i) in suggestions" :key="s.id" class="suggest-item" :class="{ active: i === suggestIndex }" @click="searchKeyword(s.name)">
          <span class="suggest-icon">🔍</span>
          <span v-html="highlightName(s.name)" />
          <span class="suggest-price">{{ formatPrice(s.price / 100, 2) }}</span>
        </div>
      </div>

      <!-- Hot Keywords -->
      <div class="hot-section">
        <h4 class="hot-title">🔥 {{ t('search.hotKeywords') }}</h4>
        <div class="hot-grid">
          <span v-for="kw in hotKeywords" :key="kw" class="hot-tag" @click="searchKeyword(kw)">{{ kw }}</span>
        </div>
      </div>

      <!-- History -->
      <div v-if="history.length && showHistory" class="history-section">
        <div class="history-header">
          <h4 class="history-title">📋 {{ t('search.history') }}</h4>
          <button class="clear-btn" @click="clearHistory">{{ t('search.clearHistory') }}</button>
        </div>
        <div class="history-grid">
          <span v-for="kw in history.slice(0, 10)" :key="kw" class="history-tag" @click="searchKeyword(kw)">
            {{ kw }}
            <button class="hist-del" @click.stop="removeHistoryItem(kw)" aria-label="删除">✕</button>
          </span>
        </div>
      </div>
    </div>

    <!-- Search Results -->
    <div v-if="searchQuery">
      <div class="results-header">
        <h2 class="results-title">"{{ searchQuery }}" 共 {{ results.length }} 件</h2>
        <div class="sort-row">
          <span class="sort-opt" :class="{ active: !sortBy }" @click="setSort('')">综合</span>
          <span class="sort-opt" :class="{ active: sortBy === 'sales' }" @click="setSort('sales')">销量</span>
          <span class="sort-opt" :class="{ active: sortBy === 'price_asc' }" @click="setSort('price_asc')">价格↑</span>
          <span class="sort-opt" :class="{ active: sortBy === 'price_desc' }" @click="setSort('price_desc')">价格↓</span>
        </div>
      </div>

      <!-- Skeleton -->
      <div v-if="loading" class="results-grid">
        <div v-for="i in 8" :key="i" class="sk-card">
          <div class="sk-img" />
          <div class="sk-body"><div class="sk-line sk-line-w70" /><div class="sk-line sk-line-w40" /></div>
        </div>
      </div>

      <!-- Results -->
      <div v-else-if="results.length" class="results-grid">
        <div v-for="p in results" :key="p.id" class="result-card" @click="goDetail(p.id)">
          <LazyImage :src="p.imageUrl || (p.images?.[0]) || ''" :alt="p.name" height="180px" />
          <div class="result-info">
            <h4 class="result-name" v-html="highlightName(p.name)" />
            <div class="result-bottom">
              <div>
                <span class="result-price">{{ formatPrice(p.price / 100, 2) }}</span>
                <span class="result-sales">已售 {{ p.sales || 0 }}</span>
              </div>
              <button class="add-btn" :class="{ added: (p as Product)._added }" @click="(e: Event) => quickAdd(e, p)">
                {{ (p as Product)._added ? '✓' : '+' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty Results -->
      <div v-else class="no-results">
        <p class="no-results-icon">🔍</p>
        <p class="no-results-text">{{ t('search.noMatch') }}</p>
        <p class="no-results-hint">{{ t('search.tryOther') }}</p>
        <div class="no-results-tags">
          <span v-for="kw in hotKeywords" :key="kw" class="retry-tag" @click="searchKeyword(kw)">{{ kw }}</span>
        </div>
      </div>

      <!-- Load More -->
      <div v-if="results.length && page < totalPages - 1" class="load-more">
        <button class="load-more-btn" @click="loadMore">加载更多</button>
      </div>
    </div>

    <!-- Mobile Cancel -->
    <button v-if="searchQuery" class="mobile-cancel" @click="cancelSearch">取消</button>
  </div>
</template>

<style scoped>
.search-empty { max-width: 600px; margin: 0 auto var(--space-xxl); text-align: center; }

.search-bar { display: flex; position: relative; margin-bottom: var(--space-xxl); }
.search-input { flex: 1; height: 40px; padding: 0 var(--space-xl) 0 var(--space-lg); border: 2px solid var(--jd-red); border-radius: var(--radius-md) 0 0 var(--radius-md); outline: none; font-size: 15px; background: var(--bg-white); color: var(--text-primary); }
.search-input:focus { box-shadow: 0 0 0 3px var(--jd-red-light); }
.search-clear { position: absolute; right: 110px; top: 50%; transform: translateY(-50%); background: none; border: none; color: var(--text-tertiary); cursor: pointer; font-size: 14px; padding: 4px; }
.search-clear:hover { color: var(--text-primary); }
.search-btn { height: 40px; padding: 0 28px; background: var(--jd-red); color: #fff; border: none; border-radius: 0 var(--radius-md) var(--radius-md) 0; cursor: pointer; font-size: 15px; font-weight: 600; transition: background var(--transition-fast); }
.search-btn:hover { background: var(--jd-red-dark); }

.suggestions { background: var(--bg-white); border: 1px solid var(--border-light); border-radius: var(--radius-md); box-shadow: var(--shadow-lg); overflow: hidden; margin-top: -16px; margin-bottom: var(--space-sm); }
.suggest-item { padding: var(--space-md) var(--space-lg); cursor: pointer; display: flex; align-items: center; gap: var(--space-md); border-bottom: 1px solid var(--border-light); font-size: var(--font-md); transition: background var(--transition-fast); }
.suggest-item:hover, .suggest-item.active { background: var(--bg-hover); }
.suggest-icon { color: var(--text-tertiary); }
.suggest-price { margin-left: auto; color: var(--jd-red); font-size: var(--font-sm); }

.hot-section { margin-bottom: var(--space-xxl); }
.hot-title { font-size: var(--font-md); color: var(--text-tertiary); margin-bottom: var(--space-md); text-align: left; }
.hot-grid { display: flex; flex-wrap: wrap; gap: var(--space-sm); }
.hot-tag { padding: 6px 14px; background: var(--jd-red-light); color: var(--jd-red); border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-base); transition: background var(--transition-fast); }
.hot-tag:hover { background: #ffe0e0; }

.history-section { text-align: left; }
.history-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-sm); }
.history-title { font-size: var(--font-md); color: var(--text-tertiary); }
.clear-btn { border: none; background: none; color: var(--text-tertiary); cursor: pointer; font-size: var(--font-sm); }
.history-grid { display: flex; flex-wrap: wrap; gap: var(--space-sm); }
.history-tag { padding: 5px 12px; background: var(--bg-hover); border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-sm); color: var(--text-secondary); transition: background var(--transition-fast); display: flex; align-items: center; gap: 4px; }
.history-tag:hover { background: var(--border); }
.hist-del { background: none; border: none; color: var(--text-tertiary); cursor: pointer; font-size: 10px; padding: 0; }
.hist-del:hover { color: var(--jd-red); }

/* Results */
.results-header { margin-bottom: var(--space-lg); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--space-sm); }
.results-title { font-size: var(--font-lg); color: var(--text-primary); }
.sort-row { display: flex; gap: var(--space-md); font-size: var(--font-base); }
.sort-opt { cursor: pointer; padding: 4px 8px; border-radius: var(--radius-sm); color: var(--text-secondary); transition: all var(--transition-fast); }
.sort-opt.active { color: var(--jd-red); background: var(--jd-red-light); }

.results-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }

.sk-card { background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.sk-img { height: 180px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; }
.sk-body { padding: var(--space-md); }
.sk-line { background: var(--border-light); border-radius: var(--radius-sm); margin-bottom: var(--space-sm); }

.result-card { background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition); }
.result-card:hover { transform: translateY(-4px); }
.result-info { padding: var(--space-md); }
.result-name { font-size: var(--font-md); color: var(--text-primary); margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.result-name :deep(mark) { background: #fff3cd; color: var(--jd-red); padding: 0 1px; border-radius: 2px; }
.result-bottom { display: flex; align-items: center; justify-content: space-between; }
.result-price { color: var(--jd-red); font-size: var(--font-lg); font-weight: 700; }
.result-sales { color: var(--text-tertiary); font-size: var(--font-xs); margin-left: var(--space-xs); }

.add-btn { width: 28px; height: 28px; border-radius: 50%; border: 2px solid var(--jd-red); background: var(--bg-white); color: var(--jd-red); cursor: pointer; font-size: var(--font-md); display: flex; align-items: center; justify-content: center; transition: all var(--transition-fast); flex-shrink: 0; }
.add-btn:hover { background: var(--jd-red); color: #fff; }
.add-btn.added { background: var(--jd-red); color: #fff; }

.load-more { text-align: center; margin-top: var(--space-xxl); }
.load-more-btn { padding: var(--space-md) 48px; border: 1px solid var(--border); background: var(--bg-white); border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-md); color: var(--text-secondary); transition: all var(--transition-fast); }
.load-more-btn:hover { border-color: var(--jd-red); color: var(--jd-red); }

.no-results { text-align: center; padding: 40px; color: var(--text-tertiary); }
.no-results-icon { font-size: 48px; margin-bottom: var(--space-md); }
.no-results-text { font-size: var(--font-lg); margin-bottom: var(--space-xs); }
.no-results-hint { font-size: var(--font-base); color: var(--text-placeholder); margin-bottom: var(--space-xl); }
.no-results-tags { display: flex; flex-wrap: wrap; gap: var(--space-sm); justify-content: center; max-width: 400px; margin: 0 auto; }
.retry-tag { padding: 6px var(--space-lg); background: var(--bg-white); border: 1px solid var(--jd-red); color: var(--jd-red); border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-base); transition: all var(--transition-fast); }
.retry-tag:hover { background: var(--jd-red); color: #fff; }

.mobile-cancel { display: none; }

.sk-line-w70 { width: 70%; height: 16px; }
.sk-line-w40 { width: 40%; height: 20px; }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

@media (max-width: 768px) {
  .results-grid { grid-template-columns: repeat(2, 1fr); }
  .mobile-cancel { display: inline-block; margin-top: var(--space-sm); padding: var(--space-sm) var(--space-lg); border: 1px solid var(--border); background: var(--bg-white); border-radius: var(--radius-md); cursor: pointer; font-size: var(--font-base); color: var(--text-secondary); }
}
</style>
