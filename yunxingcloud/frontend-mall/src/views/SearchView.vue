<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchProducts } from '@/api/product'
import { useI18n } from '@/locales'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const results = ref<any[]>([])
const totalPages = ref(0)
const page = ref(0)
const loading = ref(false)
const searchQuery = ref('')
const searchInput = ref('')
const showHistory = ref(false)

const hotKeywords = ref(['华为手机', 'MacBook', 'Nike', '茅台', '空调', '耳机', '运动鞋', '洗发水'])
const history = ref<string[]>(JSON.parse(localStorage.getItem('searchHistory') || '[]'))
const suggestions = ref<any[]>([])
let suggestTimer: any = null

function onInput() {
  clearTimeout(suggestTimer)
  const q = searchInput.value.trim()
  if (!q) { suggestions.value = []; return }
  suggestTimer = setTimeout(async () => {
    try { const r = await searchProducts(q, 0, 5); suggestions.value = (r.data.content || r.data || []).slice(0, 5) } catch { suggestions.value = [] }
  }, 300)
}

onMounted(() => { searchQuery.value = (route.query.q as string) || ''; searchInput.value = searchQuery.value; if (searchQuery.value) doSearch() })
watch(() => route.query.q, (q) => { searchQuery.value = (q as string) || ''; searchInput.value = searchQuery.value; page.value = 0; if (searchQuery.value) doSearch() })

async function doSearch() {
  if (!searchInput.value.trim()) return
  const q = searchInput.value.trim(); searchQuery.value = q; loading.value = true
  const h = [q, ...history.value.filter(h => h !== q)].slice(0, 10); history.value = h
  localStorage.setItem('searchHistory', JSON.stringify(h)); showHistory.value = false
  try { const r = await searchProducts(q, page.value, 20); const data = r.data; results.value = data.content || data || []; totalPages.value = data.totalPages || 0; router.replace({ query: { q } }) } catch { results.value = [] } finally { loading.value = false }
}

function searchKeyword(kw: string) { searchInput.value = kw; searchQuery.value = kw; doSearch() }
function goDetail(id: number) { router.push(`/product/${id}`) }
function clearHistory() { history.value = []; localStorage.removeItem('searchHistory') }
</script>

<template>
  <div>
    <div v-if="!searchQuery" style="max-width:600px;margin:0 auto 24px;text-align:center">
      <div style="display:flex;margin-bottom:24px">
        <input v-model="searchInput" :placeholder="t('search.placeholder')" @input="onInput" @keyup.enter="doSearch" @focus="showHistory=true"
               style="flex:1;height:40px;padding:0 16px;border:2px solid #e4393c;border-radius:8px 0 0 8px;outline:none;font-size:15px" />
        <button @click="doSearch" style="height:40px;padding:0 28px;background:#e4393c;color:#fff;border:none;border-radius:0 8px 8px 0;cursor:pointer;font-size:15px;font-weight:600">{{ t('common.search') }}</button>
      </div>
      <div v-if="suggestions.length" style="position:relative;margin-top:-16px;margin-bottom:8px;max-width:600px;margin-left:auto;margin-right:auto">
        <div style="background:#fff;border:1px solid #eee;border-radius:8px;box-shadow:0 4px 16px rgba(0,0,0,.08);overflow:hidden">
          <div v-for="s in suggestions" :key="s.id" @click="searchKeyword(s.name)"
               style="padding:10px 16px;cursor:pointer;display:flex;align-items:center;gap:10px;border-bottom:1px solid #f5f5f5;font-size:14px"
               @mouseenter="(e:any) => e.target.style.background='#f8f8f8'" @mouseleave="(e:any) => e.target.style.background=''">
            <span style="color:#999">🔍</span>
            <span>{{ s.name }}</span>
            <span style="margin-left:auto;color:#e4393c;font-size:12px">¥{{ (s.price/100).toFixed(2) }}</span>
          </div>
        </div>
      </div>
      <div style="margin-bottom:24px"><h4 style="font-size:14px;color:#999;margin-bottom:12px;text-align:left">🔥 {{ t('search.hotKeywords') }}</h4>
        <div style="display:flex;flex-wrap:wrap;gap:8px">
          <span v-for="kw in hotKeywords" :key="kw" @click="searchKeyword(kw)"
                style="padding:6px 14px;background:#fff0f0;color:#e4393c;border-radius:16px;cursor:pointer;font-size:13px;transition:background .2s"
                @mouseenter="(e:any) => e.target.style.background='#ffe0e0'" @mouseleave="(e:any) => e.target.style.background='#fff0f0'">{{ kw }}</span>
        </div>
      </div>
      <div v-if="history.length && showHistory" style="text-align:left">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px">
          <h4 style="font-size:14px;color:#999">📋 {{ t('search.history') }}</h4>
          <button @click="clearHistory" style="border:none;background:none;color:#999;cursor:pointer;font-size:12px">{{ t('search.clearHistory') }}</button>
        </div>
        <div style="display:flex;flex-wrap:wrap;gap:8px">
          <span v-for="kw in history.slice(0, 10)" :key="kw" @click="searchKeyword(kw)"
                style="padding:5px 12px;background:#f5f5f5;border-radius:14px;cursor:pointer;font-size:12px;color:#666;transition:background .2s"
                @mouseenter="(e:any) => e.target.style.background='#eee'" @mouseleave="(e:any) => e.target.style.background='#f5f5f5'">{{ kw }}</span>
        </div>
      </div>
    </div>
    <div v-if="searchQuery">
      <div style="margin-bottom:16px"><h2 style="font-size:18px">{{ t('search.resultCount', { n: results.length }) }}</h2></div>
      <div v-if="loading" style="text-align:center;padding:40px;color:#999">{{ t('search.searching') }}</div>
      <div v-else-if="results.length" style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px">
        <div v-for="p in results" :key="p.id" @click="goDetail(p.id)" style="background:#fff;border-radius:8px;overflow:hidden;cursor:pointer;transition:transform .3s"
             @mouseenter="(e:any) => e.target.style.transform='translateY(-4px)'" @mouseleave="(e:any) => e.target.style.transform=''" :style="{boxShadow:'0 2px 8px rgba(0,0,0,.06)'}">
          <div style="height:180px;background:linear-gradient(135deg,#f8f8f8,#eee);display:flex;align-items:center;justify-content:center;font-size:48px">📦</div>
          <div style="padding:12px"><h4 style="font-size:14px;margin-bottom:6px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ p.name }}</h4>
            <div style="display:flex;align-items:baseline;gap:8px">
              <span style="color:#e4393c;font-size:18px;font-weight:700">¥{{ (p.price / 100).toFixed(2) }}</span>
              <span style="color:#999;font-size:11px">{{ t('product.salesCount') }} {{ p.sales || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else style="text-align:center;padding:60px;color:#999">
        <p style="font-size:48px;margin-bottom:16px">🔍</p><p>{{ t('search.noMatch') }}</p><p style="font-size:13px;margin-top:8px;color:#aaa">{{ t('search.tryOther') }}</p>
      </div>
    </div>
  </div>
</template>