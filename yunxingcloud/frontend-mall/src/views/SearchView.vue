<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchProducts } from '@/api/product'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const route = useRoute()
const router = useRouter()
const toast = useToast()
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
onUnmounted(() => { clearTimeout(suggestTimer) })
watch(() => route.query.q, (q) => { searchQuery.value = (q as string) || ''; searchInput.value = searchQuery.value; page.value = 0; if (searchQuery.value) doSearch() })

async function doSearch() {
  if (!searchInput.value.trim()) return
  const q = searchInput.value.trim(); searchQuery.value = q; loading.value = true
  const h = [q, ...history.value.filter(h => h !== q)].slice(0, 10); history.value = h
  localStorage.setItem('searchHistory', JSON.stringify(h)); showHistory.value = false
  try { const r = await searchProducts(q, page.value, 20); const data = r.data; results.value = data.content || data || []; totalPages.value = data.totalPages || 0; router.replace({ query: { q } }) } catch { results.value = [] } finally { loading.value = false }
}

const sortBy = ref('')
function setSort(s: string) { sortBy.value = sortBy.value === s ? '' : s; const sorted = [...results.value]; if (s === 'price_asc') sorted.sort((a:any,b:any) => a.price - b.price); else if (s === 'price_desc') sorted.sort((a:any,b:any) => b.price - a.price); else if (s === 'sales') sorted.sort((a:any,b:any) => (b.sales||0) - (a.sales||0)); results.value = sorted }
function searchKeyword(kw: string) { searchInput.value = kw; searchQuery.value = kw; doSearch() }
function goDetail(id: number) { router.push(`/product/${id}`) }
function clearHistory() { history.value = []; localStorage.removeItem('searchHistory') }
async function quickAdd(e: Event, p: any) { e.stopPropagation(); try { await addToCart({ productId: p.id, quantity: 1 }); toast.success('已加入购物车'); p._added = true; setTimeout(() => p._added = false, 1500) } catch { toast.error('添加失败') } }
</script>

<template>
  <div>
    <div v-if="!searchQuery" style="max-width:600px;margin:0 auto 24px;text-align:center">
      <div style="display:flex;margin-bottom:24px">
        <input v-model="searchInput" :placeholder="t('search.placeholder')" @input="onInput" @keyup.enter="doSearch" @focus="showHistory=true"
               style="flex:1;height:40px;padding:0 16px;border:2px solid #f10215;border-radius:8px 0 0 8px;outline:none;font-size:15px" />
        <button @click="doSearch" style="height:40px;padding:0 28px;background:#f10215;color:#fff;border:none;border-radius:0 8px 8px 0;cursor:pointer;font-size:15px;font-weight:600">{{ t('common.search') }}</button>
      </div>
      <div v-if="suggestions.length" style="position:relative;margin-top:-16px;margin-bottom:8px;max-width:600px;margin-left:auto;margin-right:auto">
        <div style="background:#fff;border:1px solid #eee;border-radius:8px;box-shadow:0 4px 16px rgba(0,0,0,.08);overflow:hidden">
          <div v-for="s in suggestions" :key="s.id" @click="searchKeyword(s.name)"
               style="padding:10px 16px;cursor:pointer;display:flex;align-items:center;gap:10px;border-bottom:1px solid #f5f5f5;font-size:14px"
               @mouseenter="(e:any) => e.target.style.background='#f8f8f8'" @mouseleave="(e:any) => e.target.style.background=''">
            <span style="color:#999">🔍</span>
            <span>{{ s.name }}</span>
            <span style="margin-left:auto;color:#f10215;font-size:12px">¥{{ (s.price/100).toFixed(2) }}</span>
          </div>
        </div>
      </div>
      <div style="margin-bottom:24px"><h4 style="font-size:14px;color:#999;margin-bottom:12px;text-align:left">🔥 {{ t('search.hotKeywords') }}</h4>
        <div style="display:flex;flex-wrap:wrap;gap:8px">
          <span v-for="kw in hotKeywords" :key="kw" @click="searchKeyword(kw)"
                style="padding:6px 14px;background:#fff0f0;color:#f10215;border-radius:16px;cursor:pointer;font-size:13px;transition:background .2s"
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
      <div style="margin-bottom:16px;display:flex;justify-content:space-between;align-items:center">
        <h2 style="font-size:18px">"{{ searchQuery }}" 共 {{ results.length }} 件</h2>
        <div style="display:flex;gap:12px;font-size:13px">
          <span @click="setSort('')" style="cursor:pointer;padding:4px 8px;border-radius:4px" :style="{color:!sortBy?'#f10215':'#666',background:!sortBy?'#fff0f0':''}">综合</span>
          <span @click="setSort('sales')" style="cursor:pointer;padding:4px 8px;border-radius:4px" :style="{color:sortBy==='sales'?'#f10215':'#666',background:sortBy==='sales'?'#fff0f0':''}">销量</span>
          <span @click="setSort('price_asc')" style="cursor:pointer;padding:4px 8px;border-radius:4px" :style="{color:sortBy==='price_asc'?'#f10215':'#666',background:sortBy==='price_asc'?'#fff0f0':''}">价格↑</span>
          <span @click="setSort('price_desc')" style="cursor:pointer;padding:4px 8px;border-radius:4px" :style="{color:sortBy==='price_desc'?'#f10215':'#666',background:sortBy==='price_desc'?'#fff0f0':''}">价格↓</span>
        </div>
      </div>
      <div v-if="loading">
        <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px">
          <div v-for="i in 8" :key="i" style="background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.06)">
            <div style="height:180px;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite"></div>
            <div style="padding:12px"><div style="height:16px;width:70%;background:#f0f0f0;border-radius:4px;margin-bottom:8px"></div><div style="height:20px;width:40%;background:#f0f0f0;border-radius:4px"></div></div>
          </div>
        </div>
      </div>
      <div v-else-if="results.length" style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px">
        <div v-for="p in results" :key="p.id" @click="goDetail(p.id)" style="background:#fff;border-radius:8px;overflow:hidden;cursor:pointer;transition:transform .3s"
             @mouseenter="(e:any) => e.target.style.transform='translateY(-4px)'" @mouseleave="(e:any) => e.target.style.transform=''" :style="{boxShadow:'0 2px 8px rgba(0,0,0,.06)'}">
          <div style="height:180px;background:linear-gradient(135deg,#f8f8f8,#eee);display:flex;align-items:center;justify-content:center;font-size:48px;position:relative">📦</div>
          <div style="padding:12px"><h4 style="font-size:14px;margin-bottom:6px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ p.name }}</h4>
            <div style="display:flex;align-items:center;justify-content:space-between">
              <div>
                <span style="color:#f10215;font-size:18px;font-weight:700">¥{{ (p.price / 100).toFixed(2) }}</span>
                <span style="color:#999;font-size:11px;margin-left:4px">已售 {{ p.sales || 0 }}</span>
              </div>
              <button @click="(e: Event) => quickAdd(e, p)"
                      style="width:28px;height:28px;border-radius:50%;border:2px solid #f10215;background:#fff;color:#f10215;cursor:pointer;font-size:14px;display:flex;align-items:center;justify-content:center;transition:all .2s;flex-shrink:0"
                      :style="{background: (p as any)._added ? '#f10215' : '#fff', color: (p as any)._added ? '#fff' : '#f10215'}"
                      @mouseenter="(e:any) => { if(!(p as any)._added) { e.target.style.background='#f10215'; e.target.style.color='#fff' } }"
                      @mouseleave="(e:any) => { if(!(p as any)._added) { e.target.style.background='#fff'; e.target.style.color='#f10215' } }">
                {{ (p as any)._added ? '✓' : '+' }}
              </button>
            </div>
          </div>
        </div>
      </div>
      <div v-else style="text-align:center;padding:40px;color:#999">
        <p style="font-size:48px;margin-bottom:12px">🔍</p><p style="font-size:16px;margin-bottom:4px">{{ t('search.noMatch') }}</p>
        <p style="font-size:13px;margin-bottom:20px;color:#aaa">{{ t('search.tryOther') }}</p>
        <div style="display:flex;flex-wrap:wrap;gap:8px;justify-content:center;max-width:400px;margin:0 auto">
          <span v-for="kw in hotKeywords" :key="kw" @click="searchKeyword(kw)"
                style="padding:6px 16px;background:#fff;border:1px solid #f10215;color:#f10215;border-radius:20px;cursor:pointer;font-size:13px;transition:all .2s"
                @mouseenter="(e:any) => { e.target.style.background='#f10215'; e.target.style.color='#fff' }"
                @mouseleave="(e:any) => { e.target.style.background='#fff'; e.target.style.color='#f10215' }">{{ kw }}</span>
        </div>
      </div>
    </div>
  </div>
</template>