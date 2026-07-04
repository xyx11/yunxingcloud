<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from '@/locales'
const { t } = useI18n()
import LazyImage from '@/components/LazyImage.vue'
import request from '@/api/request'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const toast = useToast()
const ranks = ref<any[]>([])
const loading = ref(true)
const tab = ref<'sales' | 'new' | 'price'>('sales')

const tabs = [
  { key: 'sales', label: '热卖榜', icon: '🔥' },
  { key: 'new', label: '新品榜', icon: '✨' },
  { key: 'price', label: '好价榜', icon: '💰' },
]

onMounted(async () => {
  try { const r = await request.get('/products/hot'); ranks.value = (r.data || []).slice(0, 20) } catch {}
  finally { loading.value = false }
})

function goDetail(id: number) { router.push(`/product/${id}`) }
async function quickAdd(e: Event, p: any) { e.stopPropagation(); try { await addToCart({ productId: p.id, quantity: 1 }); toast.success('已加入购物车') } catch { toast.error('添加失败') } }

const medalColor = (i: number) => ['#ff6b00','#999','#b87333'][i] || '#666'
</script>

<template>
  <div style="max-width:900px;margin:0 auto">
    <div style="background:linear-gradient(135deg,#f10215,#ff4444);color:#fff;border-radius:16px;padding:32px;margin-bottom:24px;text-align:center;box-shadow:0 4px 20px rgba(241,2,21,.25)">
      <h1 style="font-size:28px;font-weight:800;margin-bottom:4px">🏆 商品排行榜</h1>
      <p style="font-size:14px;opacity:.85">实时热卖 · 品质好物</p>
    </div>
    <div style="display:flex;gap:0;margin-bottom:16px;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <span v-for="t in tabs" :key="t.key" @click="tab = t.key"
            style="flex:1;text-align:center;padding:12px;cursor:pointer;font-size:14px;transition:all .2s;font-weight:600"
            :style="{background:tab===t.key?'#f10215':'#fff',color:tab===t.key?'#fff':'#666'}">{{ t.icon }} {{ t.label }}</span>
    </div>
    <div v-if="loading" style="display:flex;flex-direction:column;gap:10px">
      <div v-for="i in 10" :key="i" style="background:#fff;border-radius:8px;padding:16px;box-shadow:0 1px 4px rgba(0,0,0,.04);height:60px">
        <div style="height:16px;width:50%;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:4px"></div>
      </div>
    </div>
    <div v-else style="display:flex;flex-direction:column;gap:10px">
      <div v-for="(p, i) in ranks" :key="p.id" @click="goDetail(p.id)"
           style="background:#fff;border-radius:8px;padding:14px 20px;cursor:pointer;box-shadow:0 1px 4px rgba(0,0,0,.04);display:flex;align-items:center;gap:16px;transition:transform .2s,box-shadow .2s"
           @mouseenter="(e:any) => { e.target.style.transform='translateX(4px)'; e.target.style.boxShadow='0 4px 16px rgba(0,0,0,.08)' }"
           @mouseleave="(e:any) => { e.target.style.transform=''; e.target.style.boxShadow='0 1px 4px rgba(0,0,0,.04)' }">
        <span style="font-size:22px;font-weight:800;width:32px;text-align:center;flex-shrink:0" :style="{color:medalColor(i)}">
          {{ i < 3 ? ['🥇','🥈','🥉'][i] : i + 1 }}
        </span>
        <LazyImage :src="p.imageUrl || ''" alt="" height="60px" width="60px" rounded="6px" />
        <div style="flex:1">
          <div style="font-weight:600;font-size:15px;margin-bottom:2px">{{ p.name }}</div>
          <div style="color:#999;font-size:12px">已售 {{ (p.sales || 0).toLocaleString() }} 件</div>
        </div>
        <div style="text-align:right;flex-shrink:0">
          <div style="color:#f10215;font-size:20px;font-weight:700">¥{{ (p.price/100).toFixed(2) }}</div>
          <button @click="(e:Event) => quickAdd(e, p)" style="margin-top:4px;padding:2px 12px;border:1px solid #f10215;background:#fff;color:#f10215;border-radius:12px;cursor:pointer;font-size:11px">+ 加购</button>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }</style>