<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useRecentlyViewed } from '@/composables/useRecentlyViewed'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const toast = useToast()
const { items, clear } = useRecentlyViewed()

function goDetail(id: number) { router.push(`/product/${id}`) }
async function quickAdd(e: Event, p: any) { e.stopPropagation(); try { await addToCart({ productId: p.id, quantity: 1 }); toast.success('已加入购物车') } catch { toast.error('添加失败') } }
</script>

<template>
  <div style="max-width:900px;margin:0 auto">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 style="font-size:20px;font-weight:700">🕐 最近浏览</h2>
      <button v-if="items.length" @click="clear" style="padding:6px 16px;border:1px solid #ddd;background:#fff;border-radius:6px;cursor:pointer;font-size:12px;color:#999">清空记录</button>
    </div>
    <div v-if="items.length" style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px">
      <div v-for="p in items" :key="p.id" @click="goDetail(p.id)"
           style="background:#fff;border-radius:8px;overflow:hidden;cursor:pointer;transition:transform .3s;box-shadow:0 2px 8px rgba(0,0,0,.06)"
           @mouseenter="(e:any) => e.target.style.transform='translateY(-4px)'" @mouseleave="(e:any) => e.target.style.transform=''">
        <div style="height:180px;background:linear-gradient(135deg,#f8f8f8,#eee);display:flex;align-items:center;justify-content:center;font-size:48px">📦</div>
        <div style="padding:12px">
          <h4 style="font-size:14px;margin-bottom:6px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ p.name }}</h4>
          <div style="display:flex;align-items:center;justify-content:space-between">
            <div>
              <span style="color:#f10215;font-size:16px;font-weight:700">¥{{ (p.price/100).toFixed(2) }}</span>
              <div style="font-size:10px;color:#aaa;margin-top:2px">{{ new Date(p.viewedAt).toLocaleString('zh-CN',{month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'}) }}</div>
            </div>
            <button @click="(e: Event) => quickAdd(e, p)" style="width:28px;height:28px;border-radius:50%;border:2px solid #f10215;background:#fff;color:#f10215;cursor:pointer;font-size:14px;display:flex;align-items:center;justify-content:center">+</button>
          </div>
        </div>
      </div>
    </div>
    <div v-else style="background:#fff;border-radius:12px;padding:60px;text-align:center;color:#999;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <p style="font-size:48px;margin-bottom:12px">🕐</p><p>暂无浏览记录</p>
      <button @click="router.push('/')" style="margin-top:12px;padding:8px 24px;background:#f10215;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:13px">去逛逛</button>
    </div>
  </div>
</template>