<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { formatPrice } from '@/utils/format'
import type { Product } from '@/types'
import ProductRating from './ProductRating.vue'
import LazyImage from './LazyImage.vue'

const props = defineProps<{ product: Product | null; show: boolean }>()
const emit = defineEmits(['close'])
const router = useRouter()
const toast = useToast()
const qty = ref(1)
const adding = ref(false)

watch(() => props.show, v => { if (!v) qty.value = 1 })

function onKeydown(e: KeyboardEvent) { if (e.key === 'Escape') emit('close') }

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))

async function quickAdd() { if (!props.product) return
  adding.value = true
  try { await addToCart(Number(props.product.id), qty.value); toast.success('已加入购物车'); emit('close') } catch { toast.error('添加失败') } finally { adding.value = false }
}

function goDetail() { router.push(`/product/${props.product.id}`); emit('close') }

const isOutOfStock = () => (props.product.stock || 0) <= 0
</script>

<template>
  <Teleport to="body">
    <div v-if="show" class="qv-overlay" @click.self="emit('close')">
      <div v-if="product" class="qv-modal" @click.stop>
        <div class="qv-image">
          <LazyImage :src="product.imageUrl || ''" :alt="product.name" height="100%" />
          <button class="qv-close" @click="emit('close')" aria-label="关闭">✕</button>
        </div>
        <div class="qv-body">
          <h2 class="qv-name">{{ product.name }}</h2>
          <ProductRating v-if="product.rating" :rating="product.rating" :count="product.reviewCount" class="qv-rating" />
          <p class="qv-desc">{{ product.description || '暂无描述' }}</p>
          <div class="qv-price">{{ formatPrice(product.price / 100, 2) }}</div>
          <div class="qv-qty">
            <button @click="qty = Math.max(1, qty - 1)" class="qv-qty-btn" :disabled="qty <= 1">−</button>
            <span class="qv-qty-num">{{ qty }}</span>
            <button @click="qty = Math.min(99, qty + 1)" class="qv-qty-btn" :disabled="qty >= 99">+</button>
            <span class="qv-stock">库存 {{ product.stock || 0 }} 件</span>
          </div>
          <div class="qv-actions">
            <button v-if="isOutOfStock()" class="qv-soldout" disabled>已售罄</button>
            <button v-else class="qv-addcart" :disabled="adding" @click="quickAdd">
              {{ adding ? '添加中...' : '加入购物车' }}
            </button>
            <button class="qv-detail" @click="goDetail">查看详情</button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.qv-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: var(--bg-overlay); display: flex; align-items: center;
  justify-content: center; z-index: 500; animation: fadeIn .2s;
}
.qv-modal {
  background: var(--bg-white); border-radius: var(--radius-xl); overflow: hidden;
  width: 700px; max-width: 95vw; max-height: 90vh; display: flex;
  animation: slideUp .3s ease-out;
}
.qv-image {
  width: 320px; flex-shrink: 0; position: relative; min-height: 360px;
  background: var(--bg-page);
}
.qv-close {
  position: absolute; top: 10px; right: 10px; width: 28px; height: 28px;
  border-radius: 50%; background: rgba(0,0,0,.4); color: #fff; border: none;
  cursor: pointer; font-size: 14px; display: flex; align-items: center;
  justify-content: center; z-index: 1;
}
.qv-close:hover { background: rgba(0,0,0,.6); }
.qv-body {
  flex: 1; padding: 28px; display: flex; flex-direction: column; overflow-y: auto;
}
.qv-name { font-size: 18px; font-weight: 700; margin-bottom: 8px; }
.qv-rating { margin-bottom: 12px; }
.qv-desc {
  color: var(--text-secondary); font-size: 13px; margin-bottom: 16px;
  line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden;
}
.qv-price { font-size: 28px; color: var(--jd-red); font-weight: 700; margin-bottom: 16px; }
.qv-qty { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
.qv-qty-btn {
  width: 32px; height: 32px; border: 1px solid var(--border); background: var(--bg-white);
  border-radius: var(--radius-sm); cursor: pointer; font-size: 16px; display: flex;
  align-items: center; justify-content: center; transition: all var(--transition-fast);
}
.qv-qty-btn:hover:not(:disabled) { border-color: var(--jd-red); color: var(--jd-red); }
.qv-qty-btn:disabled { opacity: .4; cursor: not-allowed; }
.qv-qty-num { width: 40px; text-align: center; font-weight: 600; }
.qv-stock { color: var(--text-tertiary); font-size: 12px; margin-left: 8px; }
.qv-actions { display: flex; gap: 10px; margin-top: auto; }
.qv-addcart, .qv-detail, .qv-soldout {
  flex: 1; height: 42px; border-radius: var(--radius-md); cursor: pointer;
  font-size: 14px; font-weight: 600; transition: all var(--transition-fast);
}
.qv-addcart { background: var(--bg-white); border: 2px solid var(--jd-red); color: var(--jd-red); }
.qv-addcart:hover:not(:disabled) { background: var(--jd-red-light); }
.qv-addcart:disabled { opacity: .6; cursor: not-allowed; }
.qv-detail { background: var(--jd-red); color: #fff; border: none; }
.qv-detail:hover { background: var(--jd-red-dark); }
.qv-soldout {
  flex: 2; background: var(--border-light); color: var(--text-tertiary);
  border: none; cursor: not-allowed;
}

@keyframes fadeIn { from { opacity: 0 } to { opacity: 1 } }
@keyframes slideUp { from { transform: translateY(40px); opacity: 0 } to { transform: translateY(0); opacity: 1 } }

@media (max-width: 700px) {
  .qv-modal { flex-direction: column; }
  .qv-image { width: 100%; min-height: 240px; }
  .qv-body { padding: 20px; }
  .qv-name { font-size: 16px; }
  .qv-price { font-size: 24px; }
}
</style>
