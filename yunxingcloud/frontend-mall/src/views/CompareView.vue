<script setup lang="ts">
import { useCompare } from '@/composables/useCompare'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useRouter } from 'vue-router'

const { items, remove, clear } = useCompare()
const toast = useToast()
const router = useRouter()

async function quickAdd(p: any) { try { await addToCart(p.id, 1); toast.success('已加入购物车') } catch { toast.error('添加失败') } }

const specs = ['price', 'sales', 'description']
const specLabel: Record<string, string> = { price: '价格', sales: '销量', description: '描述' }
</script>

<template>
  <div v-if="items.length" style="position:fixed;bottom:20px;left:50%;transform:translateX(-50%);z-index:200;background:#fff;border-radius:16px;box-shadow:0 8px 40px rgba(0,0,0,.15);padding:20px 24px;max-width:900px;width:calc(100% - 40px)">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h3 style="font-size:16px;font-weight:700">商品对比 ({{ items.length }}/3)</h3>
      <div style="display:flex;gap:8px">
        <button @click="clear" style="padding:4px 12px;border:1px solid #ddd;background:#fff;border-radius:4px;cursor:pointer;font-size:12px;color:#999">清空</button>
        <button @click="router.push('/compare')" style="padding:4px 12px;background:#f10215;color:#fff;border:none;border-radius:4px;cursor:pointer;font-size:12px">全屏对比</button>
      </div>
    </div>
    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:12px">
      <div v-for="p in items" :key="p.id" style="text-align:center;position:relative">
        <button @click="remove(p.id)" style="position:absolute;top:-6px;right:-6px;width:20px;height:20px;border-radius:50%;background:#f44336;color:#fff;border:none;cursor:pointer;font-size:11px;line-height:20px">✕</button>
        <div style="height:100px;background:linear-gradient(135deg,#f8f8f8,#eee);border-radius:8px;display:flex;align-items:center;justify-content:center;font-size:36px;margin-bottom:8px">📦</div>
        <h4 style="font-size:12px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;margin-bottom:4px">{{ p.name }}</h4>
        <span style="color:#f10215;font-size:16px;font-weight:700">¥{{ (p.price/100).toFixed(2) }}</span>
        <div style="font-size:11px;color:#999">销量 {{ p.sales || 0 }}</div>
      </div>
      <div v-for="i in (3 - items.length)" :key="'empty-'+i" style="text-align:center;border:2px dashed #eee;border-radius:8px;height:160px;display:flex;align-items:center;justify-content:center;color:#ddd;font-size:12px">
        点击商品下方"对比"添加
      </div>
    </div>
  </div>
</template>