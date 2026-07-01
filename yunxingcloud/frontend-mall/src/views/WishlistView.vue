<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getFavorites, removeFavorite } from '@/api/order'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const items = ref<any[]>([])
const loading = ref(true)

async function load() {
  loading.value = true
  try { const r = await getFavorites(); items.value = r.data || [] } catch {} finally { loading.value = false }
}

async function unfav(productId: number) {
  try { await removeFavorite(productId); items.value = items.value.filter(i => i.productId !== productId && i.id !== productId); toast.info(t('product.unfavorite')) } catch {}
}

async function quickAdd(e: Event, p: any) {
  e.stopPropagation()
  const pid = p.productId || p.id
  try { await addToCart({ productId: pid, quantity: 1 }); toast.success('已加入购物车') } catch { toast.error('添加失败') }
}

function goDetail(id: number) { router.push(`/product/${id}`) }
onMounted(load)
</script>

<template>
  <div>
    <h2 style="font-size:20px;font-weight:700;margin-bottom:16px">{{ t('profile.favorites') }}</h2>
    <div v-if="loading" style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px">
      <div v-for="i in 4" :key="i" style="background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.06)">
        <div style="height:180px;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite"></div>
        <div style="padding:12px"><div style="height:16px;width:70%;background:#f0f0f0;border-radius:4px;margin-bottom:8px"></div></div>
      </div>
    </div>
    <div v-else-if="items.length" style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px">
      <div v-for="p in items" :key="p.id" @click="goDetail(p.productId || p.id)"
           style="background:#fff;border-radius:8px;overflow:hidden;cursor:pointer;transition:transform .3s;box-shadow:0 2px 8px rgba(0,0,0,.06);position:relative"
           @mouseenter="(e:any) => e.target.style.transform='translateY(-4px)'" @mouseleave="(e:any) => e.target.style.transform=''">
        <button @click.stop="unfav(p.productId || p.id)" style="position:absolute;top:8px;right:8px;z-index:2;width:28px;height:28px;border-radius:50%;border:none;background:rgba(255,255,255,.9);cursor:pointer;font-size:14px;display:flex;align-items:center;justify-content:center;transition:all .2s"
                @mouseenter="(e:any) => e.target.style.transform='scale(1.2)'" @mouseleave="(e:any) => e.target.style.transform=''">❤️</button>
        <div style="height:180px;background:linear-gradient(135deg,#f8f8f8,#eee);display:flex;align-items:center;justify-content:center;font-size:48px">📦</div>
        <div style="padding:12px">
          <h4 style="font-size:14px;margin-bottom:6px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ p.productName || p.name || '商品' }}</h4>
          <div style="display:flex;align-items:center;justify-content:space-between">
            <span style="color:#e4393c;font-size:16px;font-weight:700">¥{{ ((p.price || 0) / 100).toFixed(2) }}</span>
            <button @click.stop="(e: Event) => quickAdd(e, p)" style="width:28px;height:28px;border-radius:50%;border:2px solid #e4393c;background:#fff;color:#e4393c;cursor:pointer;font-size:14px;display:flex;align-items:center;justify-content:center">+</button>
          </div>
        </div>
      </div>
    </div>
    <div v-else style="background:#fff;border-radius:12px;padding:60px;text-align:center;color:#999;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <p style="font-size:48px;margin-bottom:16px">💝</p><p style="font-size:16px">{{ t('profile.noFavorites') }}</p>
      <button @click="router.push('/')" style="margin-top:16px;padding:8px 24px;background:#e4393c;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:13px">{{ t('common.goShopping') }}</button>
    </div>
  </div>
</template>
<style scoped>@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }</style>