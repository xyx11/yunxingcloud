<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBanners, getHotProducts, getNewProducts, getCategories, getRecommend } from '@/api/product'
import { addToCart } from '@/api/cart'
import { usePullRefresh } from '@/composables/usePullRefresh'
import { useRecentlyViewed } from '@/composables/useRecentlyViewed'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import CountdownTimer from '@/components/CountdownTimer.vue'

const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const { items: recentItems } = useRecentlyViewed()
const banners = ref<any[]>([])
const hotProducts = ref<any[]>([])
const newProducts = ref<any[]>([])
const categories = ref<any[]>([])
const recommended = ref<any[]>([])
const activeTab = ref<'hot' | 'new' | 'flash'>('hot')
const currentBanner = ref(0)
let bannerTimer: any = null

async function loadData() {
  try { const r = await getBanners(); banners.value = r.data || [] } catch {}
  try { const r = await getHotProducts(); hotProducts.value = r.data || [] } catch {}
  try { const r = await getNewProducts(); newProducts.value = r.data || [] } catch {}
  try { const r = await getCategories(); categories.value = r.data || [] } catch {}
  try { const r = await getRecommend(); recommended.value = r.data || [] } catch {}
}

const { pulling, refreshing, pullDistance } = usePullRefresh(loadData)

const flashEnd = ref(Date.now() + 6 * 3600 * 1000)
const flashPrice = (price: number) => Math.floor(price * 0.7)

onMounted(async () => {
  await loadData()
  bannerTimer = setInterval(() => {
    if (banners.value.length) currentBanner.value = (currentBanner.value + 1) % banners.value.length
  }, 4000)
})

onUnmounted(() => {
  clearInterval(bannerTimer)
})

function goDetail(id: number) { router.push(`/product/${id}`) }
function goProducts(query: Record<string, any>) { router.push({ path: '/products', query }) }
async function quickAdd(e: Event, p: any) { e.stopPropagation(); try { await addToCart({ productId: p.id, quantity: 1 }); toast.success('已加入购物车'); p._added = true; setTimeout(() => p._added = false, 1500) } catch { toast.error('添加失败') } }
</script>

<template>
  <div>
    <div v-if="pulling" class="pull-indicator" style="text-align:center;overflow:hidden;transition:height .2s" :style="{height:pullDistance+'px'}">
      <span v-if="!refreshing" style="font-size:13px;color:#999">↓ {{ t('common.loading') }}</span>
      <span v-else style="font-size:13px;color:#999">⏳ {{ t('common.loading') }}</span>
    </div>
    <div v-if="banners.length" style="position:relative;border-radius:12px;overflow:hidden;margin-bottom:24px;height:360px">
      <div v-for="(b, i) in banners" :key="b.id" :style="{position:'absolute',top:0,left:0,width:'100%',height:'100%',background:'linear-gradient(135deg,'+(i%2?'#d4000f':'#f10215')+','+(i%2?'#ff6b6b':'#f90')+')',display:'flex',alignItems:'center',justifyContent:'center',opacity:i===currentBanner?1:0,transition:'opacity .6s'}">
        <div style="text-align:center;color:#fff">
          <h2 style="font-size:42px;margin-bottom:16px">{{ b.title }}</h2>
          <p style="font-size:20px;opacity:.9">{{ t('product.hotRecommend') }}</p>
        </div>
      </div>
      <div style="position:absolute;bottom:16px;left:50%;transform:translateX(-50%);display:flex;gap:8px">
        <span v-for="(b, i) in banners" :key="b.id" @click="currentBanner=i"
              style="width:10px;height:10px;border-radius:50%;cursor:pointer;transition:all .3s"
              :style="{background:i===currentBanner?'#fff':'rgba(255,255,255,.5)',transform:i===currentBanner?'scale(1.3)':''}"></span>
      </div>
    </div>
    <div v-if="categories.length" style="display:flex;gap:24px;justify-content:center;margin-bottom:24px;flex-wrap:wrap">
      <div v-for="cat in categories.slice(0, 8)" :key="cat.id"
           @click="goProducts({ categoryId: cat.id })"
           style="text-align:center;cursor:pointer;width:80px"
           @mouseenter="(e:any) => e.target.style.transform='scale(1.08)'"
           @mouseleave="(e:any) => e.target.style.transform=''"
           :style="{transition:'transform .2s'}">
        <div style="width:56px;height:56px;border-radius:50%;background:linear-gradient(135deg,#fff0f0,#ffe8e8);display:flex;align-items:center;justify-content:center;font-size:24px;margin:0 auto 8px">{{ cat.icon || '📁' }}</div>
        <div style="font-size:12px;color:#333">{{ cat.name }}</div>
      </div>
    </div>
    <div v-if="hotProducts.length >= 3" style="background:linear-gradient(135deg,#fff5f5,#fff);border:2px solid #f10215;border-radius:12px;padding:20px 24px;margin-bottom:24px">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <div style="display:flex;align-items:center;gap:12px">
          <span style="font-size:22px;font-weight:800;color:#f10215">⏰ {{ t('product.flashSale') }}</span>
          <div style="display:flex;align-items:center;gap:4px;font-size:16px;font-weight:700;color:#333">
            <CountdownTimer :end-time="flashEnd" label="" />
          </div>
        </div>
        <span @click="goProducts({})" style="font-size:13px;color:#999;cursor:pointer">{{ t('common.allProducts') }} &gt;</span>
      </div>
      <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:12px">
        <div v-for="p in hotProducts.slice(0, 4)" :key="'fs-'+p.id" @click="goDetail(p.id)"
             style="background:#fff;border-radius:8px;overflow:hidden;cursor:pointer;text-align:center;transition:transform .3s"
             @mouseenter="(e:any) => e.target.style.transform='translateY(-4px)'"
             @mouseleave="(e:any) => e.target.style.transform=''">
          <div style="height:140px;background:linear-gradient(135deg,#f8f8f8,#eee);display:flex;align-items:center;justify-content:center;font-size:48px;position:relative">
            📦
            <span style="position:absolute;top:6px;right:6px;background:#f10215;color:#fff;font-size:10px;padding:2px 8px;border-radius:10px">7{{ t('common.discount', '折') }}</span>
          </div>
          <div style="padding:8px 12px 12px">
            <h5 style="font-size:13px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;margin-bottom:6px">{{ p.name }}</h5>
            <div style="display:flex;align-items:baseline;justify-content:center;gap:6px">
              <span style="color:#f10215;font-size:18px;font-weight:700">¥{{ (flashPrice(p.price) / 100).toFixed(2) }}</span>
              <span style="color:#999;font-size:11px;text-decoration:line-through">¥{{ (p.price / 100).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <div style="display:flex;gap:24px">
        <span @click="activeTab='hot'"
              :style="{fontSize:'20px',fontWeight:'700',cursor:'pointer',color:activeTab==='hot'?'#f10215':'#999',borderBottom:activeTab==='hot'?'3px solid #f10215':'3px solid transparent',paddingBottom:'4px'}">{{ t('product.hotRecommend') }}</span>
        <span @click="activeTab='new'"
              :style="{fontSize:'20px',fontWeight:'700',cursor:'pointer',color:activeTab==='new'?'#f10215':'#999',borderBottom:activeTab==='new'?'3px solid #f10215':'3px solid transparent',paddingBottom:'4px'}">{{ t('product.newArrival') }}</span>
      </div>
      <span @click="goProducts({})" style="font-size:13px;color:#999;cursor:pointer">{{ t('common.allProducts') }} &gt;</span>
    </div>
    <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:16px">
      <div v-for="p in (activeTab==='hot'?hotProducts:newProducts)" :key="p.id"
           @click="goDetail(p.id)"
           style="background:#fff;border-radius:8px;overflow:hidden;cursor:pointer;transition:box-shadow .3s,transform .3s"
           @mouseenter="(e:any) => { e.target.style.boxShadow='0 4px 20px rgba(0,0,0,.12)'; e.target.style.transform='translateY(-4px)' }"
           @mouseleave="(e:any) => { e.target.style.boxShadow='0 2px 8px rgba(0,0,0,.06)'; e.target.style.transform='' }"
           :style="{boxShadow:'0 2px 8px rgba(0,0,0,.06)'}">
        <div style="height:200px;background:linear-gradient(135deg,#f8f8f8,#eee);display:flex;align-items:center;justify-content:center;font-size:56px;position:relative">
          📦
          <span v-if="activeTab==='new' || p.isNew" style="position:absolute;top:6px;left:6px;background:#4caf50;color:#fff;font-size:10px;padding:1px 6px;border-radius:4px;z-index:1">新品</span>
          <span v-else-if="activeTab==='hot' || p.isHot" style="position:absolute;top:6px;left:6px;background:#f10215;color:#fff;font-size:10px;padding:1px 6px;border-radius:4px;z-index:1">热卖</span>
        </div>
        <div style="padding:12px 16px">
          <h4 style="font-size:14px;margin-bottom:8px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ p.name }}</h4>
          <div style="display:flex;align-items:center;justify-content:space-between">
            <div>
              <span style="color:#f10215;font-size:20px;font-weight:700">¥{{ (p.price / 100).toFixed(2) }}</span>
              <span style="color:#999;font-size:11px;margin-left:4px">{{ (p.sales || 0) > 1000 ? '🔥 ' + (p.sales/1000).toFixed(1) + 'k人已购' : (p.sales || 0) > 0 ? (p.sales || 0) + '人已购' : '' }}</span>
            </div>
            <button @click="(e: Event) => quickAdd(e, p)"
                    style="width:30px;height:30px;border-radius:50%;border:2px solid #f10215;background:#fff;color:#f10215;cursor:pointer;font-size:16px;display:flex;align-items:center;justify-content:center;transition:all .2s;flex-shrink:0"
                    :style="{background: (p as any)._added ? '#f10215' : '#fff', color: (p as any)._added ? '#fff' : '#f10215'}"
                    @mouseenter="(e:any) => { if(!(p as any)._added) { e.target.style.background='#f10215'; e.target.style.color='#fff' } }"
                    @mouseleave="(e:any) => { if(!(p as any)._added) { e.target.style.background='#fff'; e.target.style.color='#f10215' } }">
              {{ (p as any)._added ? '✓' : '+' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="activeTab==='hot' && !hotProducts.length && !newProducts.length" style="text-align:center;padding:60px;color:#999">
      <p style="font-size:48px;margin-bottom:16px">🛍️</p>
      <p>{{ t('common.noResults') }}</p>
    </div>
    <div v-if="recentItems.length" style="margin-top:32px">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <span style="font-size:20px;font-weight:700;color:#333">🕐 最近浏览</span>
      </div>
      <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:16px">
        <div v-for="p in recentItems.slice(0, 4)" :key="'rv-'+p.id"
             @click="goDetail(p.id)"
             style="background:#fff;border-radius:8px;overflow:hidden;cursor:pointer;box-shadow:0 2px 8px rgba(0,0,0,.06);transition:box-shadow .3s,transform .3s"
             @mouseenter="(e:any) => { e.target.style.boxShadow='0 4px 20px rgba(0,0,0,.12)'; e.target.style.transform='translateY(-4px)' }"
             @mouseleave="(e:any) => { e.target.style.boxShadow='0 2px 8px rgba(0,0,0,.06)'; e.target.style.transform='' }">
          <div style="height:200px;background:linear-gradient(135deg,#f0f0ff,#e8e8ff);display:flex;align-items:center;justify-content:center;font-size:56px">📦</div>
          <div style="padding:12px 16px">
            <h4 style="font-size:14px;margin-bottom:4px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ p.name }}</h4>
            <span style="color:#f10215;font-size:18px;font-weight:700">¥{{ (p.price / 100).toFixed(2) }}</span>
            <div style="font-size:11px;color:#aaa;margin-top:2px">{{ new Date(p.viewedAt).toLocaleString('zh-CN', {month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'}) }}</div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="recommended.length" style="margin-top:32px">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <span style="font-size:20px;font-weight:700;color:#333">🤖 为你推荐</span>
      </div>
      <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:16px">
        <div v-for="p in recommended.slice(0, 8)" :key="'rec-'+p.id"
             @click="goDetail(p.id)"
             style="background:#fff;border-radius:8px;overflow:hidden;cursor:pointer;transition:box-shadow .3s,transform .3s"
             @mouseenter="(e:any) => { e.target.style.boxShadow='0 4px 20px rgba(0,0,0,.12)'; e.target.style.transform='translateY(-4px)' }"
             @mouseleave="(e:any) => { e.target.style.boxShadow='0 2px 8px rgba(0,0,0,.06)'; e.target.style.transform='' }"
             :style="{boxShadow:'0 2px 8px rgba(0,0,0,.06)'}">
          <div style="height:200px;background:linear-gradient(135deg,#f8f0ff,#e8e0ff);display:flex;align-items:center;justify-content:center;font-size:56px;position:relative">
            🤖
          </div>
          <div style="padding:12px 16px">
            <h4 style="font-size:14px;margin-bottom:6px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ p.name }}</h4>
            <div style="display:flex;align-items:center;justify-content:space-between">
              <span style="color:#f10215;font-size:18px;font-weight:700">¥{{ (p.price / 100).toFixed(2) }}</span>
              <button @click="(e: Event) => quickAdd(e, p)" style="width:28px;height:28px;border-radius:50%;border:2px solid #f10215;background:#fff;color:#f10215;cursor:pointer;font-size:14px;display:flex;align-items:center;justify-content:center">+</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>