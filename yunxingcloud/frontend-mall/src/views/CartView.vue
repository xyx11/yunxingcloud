<script setup lang="ts">
import { ref, onMounted, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, addToCart, removeFromCart } from '@/api/cart'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'
import LazyImage from '@/components/LazyImage.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import JdButton from '@/components/JdButton.vue'
import type { CartItem } from '@/types'

const router = useRouter()
const { t } = useI18n()
const toast = inject(ToastInjectionKey)!
const items = ref<CartItem[]>([])
const recs = ref<any[]>([])
const loading = ref(true)
const selectedIds = ref<Set<number>>(new Set())

const total = computed(() =>
  items.value.filter(i => selectedIds.value.has(i.id))
    .reduce((s, i) => s + i.price * i.quantity, 0)
)

const allSelected = computed({
  get: () => items.value.length > 0 && items.value.every(i => selectedIds.value.has(i.id)),
  set: (v: boolean) => {
    if (v) items.value.forEach(i => selectedIds.value.add(i.id))
    else selectedIds.value.clear()
  }
})

async function load() {
  loading.value = true
  try { const r = await getCart(); items.value = r.data.items || []; recs.value = r.data.recommended || [] } catch {} finally { loading.value = false }
}

async function remove(id: number) {
  try { await removeFromCart(id); selectedIds.value.delete(id); toast.info(t('toast.removed')); load() } catch { toast.error('删除失败') }
}

let qtyTimer: ReturnType<typeof setTimeout> | null = null
async function updateQty(item: CartItem, delta: number) {
  const n = item.quantity + delta; if (n < 1) return
  item.quantity = n
  if (qtyTimer) clearTimeout(qtyTimer)
  qtyTimer = setTimeout(async () => {
    try { await removeFromCart(item.id); await addToCart(item.productId, item.quantity); load() } catch { toast.error('更新失败'); load() }
  }, 400)
}

function checkout() {
  if (selectedIds.value.size === 0) { toast.error(t('checkout.selectAddress')); return }
  router.push('/checkout')
}

onMounted(load)
</script>

<template>
  <div class="cart-page">
    <h2 class="page-title">{{ t('common.cart') }}</h2>

    <!-- Skeleton -->
    <div v-if="loading" class="cart-skeleton">
      <div v-for="i in 3" :key="i" class="sk-row">
        <SkeletonBox width="80px" height="80px" rounded="8px" :count="1" />
        <div style="flex:1"><SkeletonBox height="16px" width="60%" :count="1" /></div>
      </div>
    </div>

    <!-- Cart Items -->
    <template v-else-if="items.length">
      <div class="cart-header">
        <label class="checkbox-label">
          <input type="checkbox" v-model="allSelected" /> {{ t('cart.selectAll') }}
        </label>
        <span class="header-col header-col--price">{{ t('cart.unitPrice') }}</span>
        <span class="header-col header-col--qty">{{ t('product.quantity') }}</span>
        <span class="header-col header-col--subtotal">{{ t('cart.subTotal') }}</span>
        <span class="header-col header-col--actions">{{ t('cart.actions') }}</span>
      </div>

      <div v-for="item in items" :key="item.id" class="cart-item">
        <label class="checkbox-label cart-check">
          <input type="checkbox" :value="item.id" v-model="selectedIds" />
        </label>
        <LazyImage :src="item.productImage || ''" alt="" height="80px" width="80px" rounded="8px" class="cart-img" />
        <div class="cart-name">
          <router-link :to="`/product/${item.productId}`">{{ item.productName }}</router-link>
        </div>
        <div class="cart-price">¥{{ (item.price / 100).toFixed(2) }}</div>
        <div class="qty-control">
          <button class="qty-btn" @click="updateQty(item, -1)">-</button>
          <span class="qty-val">{{ item.quantity }}</span>
          <button class="qty-btn" @click="updateQty(item, 1)">+</button>
        </div>
        <div class="cart-subtotal">¥{{ (item.price * item.quantity / 100).toFixed(2) }}</div>
        <button class="cart-remove" @click="remove(item.id)" aria-label="删除">✕</button>
      </div>

      <!-- Bottom Bar -->
      <div class="cart-footer">
        <div class="footer-left">
          <label class="checkbox-label">
            <input type="checkbox" v-model="allSelected" /> {{ t('cart.selectAll') }}
          </label>
          <span class="selected-info">{{ t('cart.selected') }} {{ selectedIds.size }} {{ t('cart.items') }}</span>
        </div>
        <div class="footer-right">
          <div class="total-price">
            <span class="total-label">{{ t('cart.total') }}：</span>
            <span class="total-num">¥{{ (total / 100).toFixed(2) }}</span>
          </div>
          <JdButton size="lg" @click="checkout">{{ t('cart.checkout') }}</JdButton>
        </div>
      </div>
    </template>

    <!-- Empty -->
    <div v-else>
      <JdEmpty icon="🛒" title="购物车是空的" description="快去挑选心仪的商品吧">
        <JdButton @click="router.push('/')">{{ t('common.goShopping') }}</JdButton>
      </JdEmpty>

      <!-- Recommendations -->
      <div v-if="recs.length" class="recs-section">
        <h3 class="recs-title">🔥 为你推荐</h3>
        <div class="recs-grid">
          <div v-for="p in recs" :key="p.id" class="recs-item" @click="router.push('/product/' + p.id)">
            <LazyImage :src="(p as any).imageUrl || ''" alt="" height="120px" />
            <div class="recs-info">
              <div class="recs-name">{{ p.name }}</div>
              <span class="recs-price">¥{{ (p.price / 100).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-page { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-sm); }
.page-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-xl); }

input[type="checkbox"] { accent-color: var(--jd-red); }

.cart-skeleton { padding: 40px 0; }
.sk-row { display: flex; align-items: center; padding: var(--space-lg) 0; border-bottom: 1px solid var(--border-light); gap: var(--space-md); }

.cart-header {
  display: flex; align-items: center; padding: var(--space-md) 0;
  border-bottom: 1px solid var(--border); font-size: var(--font-base); color: var(--text-secondary);
}
.checkbox-label { cursor: pointer; display: flex; align-items: center; gap: var(--space-sm); font-size: var(--font-base); color: var(--text-secondary); }
.header-col { text-align: center; }
.header-col--price { margin-left: auto; width: 80px; }
.header-col--qty { margin: 0 40px; }
.header-col--subtotal { width: 80px; margin-right: 60px; }
.header-col--actions { width: 40px; }

.cart-item { display: flex; align-items: center; padding: var(--space-lg) 0; border-bottom: 1px solid var(--border-light); }
.cart-check { margin-right: var(--space-md); }
.cart-img { flex-shrink: 0; }
.cart-name { flex: 1; min-width: 0; padding: 0 var(--space-lg); }
.cart-name a { font-weight: 600; color: var(--text-primary); text-decoration: none; font-size: 15px; }
.cart-name a:hover { color: var(--jd-red); }

.cart-price { width: 80px; text-align: center; color: var(--text-secondary); font-size: var(--font-md); }

.qty-control { display: flex; align-items: center; margin: 0 40px; }
.qty-btn { width: 28px; height: 28px; border: 1px solid var(--border); background: var(--bg-white); cursor: pointer; font-size: var(--font-md); display: flex; align-items: center; justify-content: center; }
.qty-btn:first-child { border-radius: var(--radius-sm) 0 0 var(--radius-sm); }
.qty-btn:last-child { border-radius: 0 var(--radius-sm) var(--radius-sm) 0; }
.qty-btn:hover { background: var(--bg-hover); }
.qty-val { width: 36px; text-align: center; border-top: 1px solid var(--border); border-bottom: 1px solid var(--border); height: 28px; line-height: 28px; font-size: var(--font-base); }

.cart-subtotal { width: 80px; text-align: center; color: var(--jd-red); font-weight: 700; font-size: var(--font-lg); }
.cart-remove { width: 40px; text-align: center; border: none; background: none; cursor: pointer; color: var(--text-tertiary); font-size: var(--font-lg); transition: color var(--transition-fast); }
.cart-remove:hover { color: var(--jd-red); }

.cart-footer {
  display: flex; align-items: center; justify-content: space-between;
  padding-top: var(--space-xl); margin-top: var(--space-sm); border-top: 1px solid var(--border-light);
}
.footer-left { display: flex; align-items: center; gap: var(--space-lg); }
.selected-info { font-size: var(--font-base); color: var(--text-tertiary); }
.footer-right { display: flex; align-items: center; gap: var(--space-xl); }
.total-price { display: flex; align-items: baseline; }
.total-label { font-size: var(--font-md); color: var(--text-secondary); }
.total-num { font-size: 26px; color: var(--jd-red); font-weight: 700; }

.recs-section { margin-top: var(--space-xxxl); text-align: left; }
.recs-title { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: var(--space-md); }
.recs-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-md); }
.recs-item { background: var(--bg-hover); border-radius: var(--radius-md); overflow: hidden; cursor: pointer; transition: transform var(--transition); }
.recs-item:hover { transform: translateY(-4px); }
.recs-info { padding: var(--space-sm) var(--space-md); }
.recs-name { font-size: var(--font-base); color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.recs-price { color: var(--jd-red); font-weight: 700; font-size: var(--font-md); }

@media (max-width: 768px) {
  .cart-page { padding: var(--space-md); }
  .header-col--qty, .header-col--subtotal, .header-col--actions { display: none; }
  .cart-header { font-size: var(--font-sm); }
  .cart-item { flex-wrap: wrap; gap: var(--space-sm); }
  .cart-name { flex-basis: calc(100% - 120px); }
  .recs-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
