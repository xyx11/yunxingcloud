<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useCartFly } from '@/composables/useCartFly'
import { useI18n } from '@/locales'
import { formatPrice } from '@/utils/format'
import type { Product } from '@/types'
import ProductRating from './ProductRating.vue'
import LazyImage from './LazyImage.vue'

const props = defineProps<{ product: Product | null; show: boolean }>()
const emit = defineEmits(['close'])
const router = useRouter()
const toast = useToast()
const { flyToCart } = useCartFly()
const { t } = useI18n()
const qty = ref(1)
const adding = ref(false)


const modalRef = ref<HTMLElement>()

const triggerEl = ref<HTMLElement | null>(null)

watch(() => props.show, v => {
  if (v) {
    triggerEl.value = document.activeElement as HTMLElement
    setTimeout(() => modalRef.value?.querySelector<HTMLElement>('button')?.focus(), 50)
  } else {
    qty.value = 1
    setTimeout(() => triggerEl.value?.focus(), 50)
  }
})

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') { emit('close'); return }
  if (e.key === 'Tab' && modalRef.value) {
    const focusable = modalRef.value.querySelectorAll<HTMLElement>('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])')
    const first = focusable[0]; const last = focusable[focusable.length - 1]
    if (e.shiftKey && document.activeElement === first) { e.preventDefault(); last?.focus() }
    else if (!e.shiftKey && document.activeElement === last) { e.preventDefault(); first?.focus() }
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))

async function quickAdd(e: MouseEvent) { if (!props.product) return
  adding.value = true
  try { await addToCart(Number(props.product.id), qty.value); toast.success(t('toast.addedToCart')); flyToCart(e, qty.value); emit('close') } catch { toast.error(t('toast.addCartFail')) } finally { adding.value = false }
}

function goDetail() { if (!props.product) return; router.push(`/product/${props.product.id}`); emit('close') }

const isOutOfStock = () => (props.product?.stock || 0) <= 0
const maxQty = computed(() => Math.min(99, props.product?.stock || 0))
</script>

<template>
  <Teleport to="body">
    <div v-if="show" class="qv-overlay" @click.self="emit('close')">
      <div v-if="product" ref="modalRef" class="qv-modal" role="dialog" aria-modal="true" :aria-label="product.name" @click.stop>
        <div class="qv-image">
          <LazyImage :src="product.imageUrl || ''" :alt="product.name" height="100%" />
          <button class="qv-close" @click="emit('close')" aria-label="关闭">✕</button>
        </div>
        <div class="qv-body">
          <h2 class="qv-name">{{ product.name }}</h2>
          <ProductRating v-if="product.rating" :rating="product.rating" :count="product.reviewCount" class="qv-rating" />
          <p class="qv-desc">{{ product.description || t('quickView.noDescription') }}</p>
          <div class="qv-prices">
            <span class="qv-price">{{ formatPrice(product.price / 100, 2) }}</span>
            <span v-if="product.originalPrice && product.originalPrice > product.price" class="qv-original">{{ formatPrice(product.originalPrice / 100) }}</span>
            <span v-if="product.originalPrice && product.originalPrice > product.price" class="qv-discount">-{{ Math.round((1 - product.price / product.originalPrice) * 100) }}%</span>
          </div>
          <div class="qv-qty">
            <button @click="qty = Math.max(1, qty - 1)" class="qv-qty-btn" :disabled="qty <= 1 || isOutOfStock()">−</button>
            <span class="qv-qty-num">{{ qty }}</span>
            <button @click="qty = Math.min(maxQty, qty + 1)" class="qv-qty-btn" :disabled="qty >= maxQty || isOutOfStock()">+</button>
            <span class="qv-stock">{{ t('quickView.stockInfo', { n: String(product.stock || 0) }) }}</span>
          </div>
          <div class="qv-actions">
            <button v-if="isOutOfStock()" class="qv-soldout" disabled>{{ t('quickView.soldOut') }}</button>
            <button v-else class="qv-addcart" :disabled="adding" @click="quickAdd($event)">
              {{ adding ? t('quickView.adding') : t('product.addToCart') }}
            </button>
            <button class="qv-detail" @click="goDetail">{{ t('quickView.viewDetail') }}</button>
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
  width: 320px; flex-shrink: 0; position: relative; height: 360px;
  background: var(--bg-page);
}
.qv-close {
  position: absolute; top: 10px; right: 10px; width: 36px; height: 36px;
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
.qv-prices { display: flex; align-items: baseline; gap: var(--space-sm); margin-bottom: 16px; }
.qv-price { font-size: 28px; color: var(--jd-red); font-weight: 700; }
.qv-original { font-size: var(--font-sm); color: var(--text-tertiary); text-decoration: line-through; }
.qv-discount { font-size: var(--font-xs); background: var(--jd-red); color: #fff; padding: 2px 8px; border-radius: var(--radius-round); font-weight: 700; }
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
