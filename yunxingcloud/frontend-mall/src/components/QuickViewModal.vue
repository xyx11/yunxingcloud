<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import ProductRating from './ProductRating.vue'

const props = defineProps<{ product: any; show: boolean }>()
const emit = defineEmits(['close'])
const router = useRouter()
const toast = useToast()
const qty = ref(1)
const adding = ref(false)

watch(() => props.show, v => { if (!v) qty.value = 1 })

async function quickAdd() {
  adding.value = true
  try { await addToCart(props.product.id, qty.value); toast.success('已加入购物车'); emit('close') } catch { toast.error('添加失败') } finally { adding.value = false }
}

function goDetail() { router.push(`/product/${props.product.id}`); emit('close') }
</script>

<template>
  <Teleport to="body">
    <div v-if="show" @click.self="emit('close')" style="position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,.5);display:flex;align-items:center;justify-content:center;z-index:500;animation:fadeIn .2s">
      <div v-if="product" style="background:#fff;border-radius:16px;overflow:hidden;width:700px;max-width:95vw;max-height:90vh;display:flex;animation:slideUp .3s ease-out" @click.stop>
        <div style="width:320px;flex-shrink:0;background:linear-gradient(135deg,#f8f8f8,#eee);display:flex;align-items:center;justify-content:center;font-size:80px;position:relative;min-height:360px">
          <img v-if="product.imageUrl" :src="product.imageUrl" style="width:100%;height:100%;object-fit:cover" />
          <span v-else>📦</span>
          <button @click="emit('close')" style="position:absolute;top:10px;right:10px;width:28px;height:28px;border-radius:50%;background:rgba(0,0,0,.4);color:#fff;border:none;cursor:pointer;font-size:14px;display:flex;align-items:center;justify-content:center">✕</button>
        </div>
        <div style="flex:1;padding:28px;display:flex;flex-direction:column;overflow-y:auto">
          <h2 style="font-size:18px;font-weight:700;margin-bottom:8px">{{ product.name }}</h2>
          <ProductRating v-if="product.rating" :rating="product.rating" :count="product.reviewCount" style="margin-bottom:12px" />
          <p style="color:#666;font-size:13px;margin-bottom:16px;line-height:1.5;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden">{{ product.description || '暂无描述' }}</p>
          <div style="font-size:28px;color:#e4393c;font-weight:700;margin-bottom:16px">¥{{ (product.price/100).toFixed(2) }}</div>
          <div style="display:flex;align-items:center;gap:8px;margin-bottom:20px">
            <button @click="qty = Math.max(1, qty-1)" style="width:32px;height:32px;border:1px solid #ddd;background:#fff;border-radius:4px;cursor:pointer;font-size:16px">−</button>
            <span style="width:40px;text-align:center;font-weight:600">{{ qty }}</span>
            <button @click="qty = Math.min(99, qty+1)" style="width:32px;height:32px;border:1px solid #ddd;background:#fff;border-radius:4px;cursor:pointer;font-size:16px">+</button>
            <span style="color:#999;font-size:12px;margin-left:8px">库存 {{ product.stock || 0 }} 件</span>
          </div>
          <div style="display:flex;gap:10px;margin-top:auto">
            <button @click="quickAdd" :disabled="adding" style="flex:1;height:42px;background:#fff;border:2px solid #e4393c;color:#e4393c;border-radius:8px;cursor:pointer;font-size:14px;font-weight:600">{{ adding ? '添加中...' : '加入购物车' }}</button>
            <button @click="goDetail" style="flex:1;height:42px;background:#e4393c;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:14px;font-weight:600">查看详情</button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
<style scoped>
@keyframes fadeIn { from { opacity: 0 } to { opacity: 1 } }
@keyframes slideUp { from { transform: translateY(40px); opacity: 0 } to { transform: translateY(0); opacity: 1 } }
</style>
