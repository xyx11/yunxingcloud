<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, addToCart, removeFromCart } from '@/api/cart'
import { addFavorite } from '@/api/order'
import request from '@/api/request'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'
import LazyImage from '@/components/LazyImage.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import JdButton from '@/components/JdButton.vue'
import { formatPrice } from '@/utils/format'
import type { CartItem, Product } from '@/types'

const router = useRouter()
const { t } = useI18n()
const toast = inject(ToastInjectionKey)!
const items = ref<CartItem[]>([])
const recs = ref<Product[]>([])
const loading = ref(true)
const checkingOut = ref(false)
const removingIds = ref<Set<number>>(new Set())
const updatingQty = ref<Set<number>>(new Set())
const selectedIds = ref<Set<number>>(new Set())
const showClearConfirm = ref(false)
const movingToWishlist = ref<Set<number>>(new Set())
const clearingCart = ref(false)

const lowStockItems = computed(() => items.value.filter(i => i.stock > 0 && i.stock <= 5))

async function moveToWishlist(id: number) {
  if (movingToWishlist.value.has(id)) return
  movingToWishlist.value.add(id)
  try {
    const item = items.value.find(i => i.id === id)
    if (item) { await addFavorite(item.productId); toast.success(t('toast.movedToWishlist')) }
    await removeFromCart(id); selectedIds.value.delete(id); load()
  } catch { toast.error(t('toast.favOpFail')) }
  finally { movingToWishlist.value.delete(id) }
}

async function confirmClear() {
  clearingCart.value = true
  try { await request.delete('/cart'); selectedIds.value.clear(); toast.info(t('toast.cartCleared')); load() }
  catch { toast.error(t('toast.clearFailed')) }
  finally { clearingCart.value = false; showClearConfirm.value = false }
}

const swipeOffset = ref<Record<number, number>>({})
const swipeTouching = ref<Record<number, boolean>>({})
let swipeStartX = 0

function onSwipeStart(e: TouchEvent, id: number) {
  swipeStartX = e.touches[0].clientX
  swipeTouching.value[id] = true
}
function onSwipeMove(e: TouchEvent, id: number) {
  if (!swipeTouching.value[id]) return
  const dx = e.touches[0].clientX - swipeStartX
  swipeOffset.value[id] = Math.min(0, Math.max(-80, dx))
}
function onSwipeEnd(id: number) {
  swipeTouching.value[id] = false
  if (swipeOffset.value[id] < -40) { swipeOffset.value[id] = -80 }
  else { swipeOffset.value[id] = 0 }
}

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

const lastRemoved = ref<CartItem | null>(null)
let undoTimer: ReturnType<typeof setTimeout> | null = null

async function load() {
  loading.value = true
  try { const r = await getCart(); items.value = r.data.items || []; recs.value = r.data.recommended || [] }
  catch { toast.error(t('toast.cartLoadFail')); return } // 401 → redirect, keep skeleton
  loading.value = false
}

function undoRemove() {
  if (!lastRemoved.value) return
  const item = lastRemoved.value
  lastRemoved.value = null
  if (undoTimer) { clearTimeout(undoTimer); undoTimer = null }
  addToCart(item.productId, item.quantity).then(() => load()).catch(() => toast.error(t('toast.cartRestoreFail')))
  toast.info(t('toast.cartRestoreSuccess'))
}

async function remove(id: number) {
  if (removingIds.value.has(id)) return
  const item = items.value.find(i => i.id === id)
  if (!item) return
  removingIds.value.add(id)
  // Optimistic: remove from UI immediately
  items.value = items.value.filter(i => i.id !== id)
  selectedIds.value.delete(id)
  lastRemoved.value = item
  if (undoTimer) clearTimeout(undoTimer)
  undoTimer = setTimeout(() => { lastRemoved.value = null }, 6000)
  try {
    await removeFromCart(id)
  } catch {
    // Restore on failure
    items.value = [...items.value, item].sort((a, b) => b.id - a.id)
    lastRemoved.value = null
    toast.error(t('common.updateFailed'))
  } finally {
    removingIds.value.delete(id)
  }
}

async function updateQtyDirect(item: CartItem, e: Event) {
  const v = parseInt((e.target as HTMLInputElement).value, 10)
  if (isNaN(v) || v < 1) { load(); return }
  const q = Math.min(999, v)
  const delta = q - item.quantity
  if (delta === 0) return
  updateQty(item, delta)
}

let qtyTimer: ReturnType<typeof setTimeout> | null = null
async function updateQty(item: CartItem, delta: number) {
  const n = item.quantity + delta; if (n < 1) return
  const prev = item.quantity
  item.quantity = n
  if (qtyTimer) clearTimeout(qtyTimer)
  updatingQty.value.add(item.id)
  qtyTimer = setTimeout(async () => {
    try { await removeFromCart(item.id); await addToCart(item.productId, item.quantity); load() }
    catch { item.quantity = prev; toast.error(t('toast.updateFail')); load() }
    finally { setTimeout(() => updatingQty.value.delete(item.id), 100) }
  }, 400)
}

function checkout() {
  if (selectedIds.value.size === 0) { toast.error(t('cart.noSelection')); return }
  checkingOut.value = true
  try { localStorage.setItem('checkout_selected', JSON.stringify([...selectedIds.value])) } catch {}
  router.push('/checkout')
}

onMounted(load)
onBeforeUnmount(() => { if (qtyTimer) clearTimeout(qtyTimer); if (undoTimer) clearTimeout(undoTimer) })
</script>

<template>
  <div class="cart-page">
    <h2 class="page-title">{{ t('common.cart') }}</h2>

    <!-- Skeleton -->
    <div v-if="loading" class="cart-skeleton">
      <div v-for="i in 3" :key="i" class="sk-row">
        <SkeletonBox width="80px" height="80px" rounded="8px" :count="1" />
        <div class="sk-body"><SkeletonBox height="16px" width="60%" :count="1" /></div>
      </div>
    </div>

    <!-- Cart Items -->
    <template v-else-if="items.length">
      <!-- Undo toast -->
      <div v-if="lastRemoved" class="undo-bar">
        <span>{{ t('cart.removedHint', { name: lastRemoved.productName }) }}</span>
        <button class="undo-btn" @click="undoRemove">{{ t('cart.undo') }}</button>
        <button class="undo-close" @click="lastRemoved = null" aria-label="关闭">✕</button>
      </div>
      <!-- Low stock alert -->
      <div v-if="lowStockItems.length" class="stock-alert">
        ⚠️ {{ t('cart.lowStock') }}：<span v-for="s in lowStockItems" :key="s.id" class="stock-alert-item">{{ s.productName }}({{ t('cart.stockLeft', { n: s.stock }) }})</span>
      </div>

      <div class="cart-header">
        <label class="checkbox-label">
          <input type="checkbox" v-model="allSelected" /> {{ t('cart.selectAll') }}
        </label>
        <span class="header-col header-col--price">{{ t('cart.unitPrice') }}</span>
        <span class="header-col header-col--qty">{{ t('product.quantity') }}</span>
        <span class="header-col header-col--subtotal">{{ t('cart.subTotal') }}</span>
        <span class="header-col header-col--actions">{{ t('cart.actions') }}</span>
      </div>

      <div v-for="item in items" :key="item.id" class="cart-item-wrapper">
        <div class="cart-item-swipe-bg" :class="{ reveal: (swipeOffset[item.id] || 0) < -40 }" role="button" tabindex="0" @click="remove(item.id)" @keydown.enter.prevent="remove(item.id)" @keydown.space.prevent="remove(item.id)">{{ t('common.delete') }}</div>
        <div
          class="cart-item"
          :style="{ transform: 'translateX(' + (swipeOffset[item.id] || 0) + 'px)' }"
          @touchstart.passive="(e: TouchEvent) => onSwipeStart(e, item.id)"
          @touchmove="(e: TouchEvent) => onSwipeMove(e, item.id)"
          @touchend="onSwipeEnd(item.id)"
        >
          <label class="checkbox-label cart-check">
            <input type="checkbox" :value="item.id" v-model="selectedIds" />
          </label>
          <LazyImage :src="item.imageUrl || item.productImage || ''" :alt="item.productName" height="80px" width="80px" rounded="8px" class="cart-img" />
          <div class="cart-name">
            <router-link :to="`/product/${item.productId}`">{{ item.productName }}</router-link>
          </div>
          <div class="cart-price">{{ formatPrice(item.price / 100, 2) }}</div>
          <div class="qty-control">
            <button class="qty-btn" :disabled="updatingQty.has(item.id)" @click="updateQty(item, -1)">-</button>
            <input class="qty-val" type="number" :value="item.quantity" min="1" max="999" @change="updateQtyDirect(item, $event)" @focus="($event.target as HTMLInputElement).select()" />
            <button class="qty-btn" :disabled="updatingQty.has(item.id)" @click="updateQty(item, 1)">+</button>
          </div>
          <div class="cart-subtotal">{{ formatPrice(item.price * item.quantity / 100, 2) }}</div>
          <button class="cart-move-wishlist" :disabled="movingToWishlist.has(item.id)" @click="moveToWishlist(item.id)" title="移到收藏">♥</button>
          <button class="cart-remove" :disabled="removingIds.has(item.id)" @click="remove(item.id)" aria-label="删除">{{ removingIds.has(item.id) ? '⏳' : '✕' }}</button>
        </div>
      </div>

      <!-- Bottom Bar -->
      <div class="cart-footer">
        <div class="footer-left">
          <label class="checkbox-label">
            <input type="checkbox" v-model="allSelected" /> {{ t('cart.selectAll') }}
          </label>
          <span class="selected-info">{{ t('cart.selected') }} {{ selectedIds.size }} {{ t('cart.items') }}</span>
          <button class="clear-cart-btn" @click="showClearConfirm = true">{{ t('cart.clearCart') }}</button>
        </div>
        <div class="footer-right">
          <div class="total-price">
            <span class="total-label">{{ t('cart.total') }}：</span>
            <span class="total-num">{{ formatPrice(total / 100, 2) }}</span>
          </div>
          <JdButton size="lg" :loading="checkingOut" :disabled="checkingOut" @click="checkout">{{ t('cart.checkout') }}</JdButton>
        </div>
      </div>

      <!-- Clear confirm dialog -->
      <div v-if="showClearConfirm" class="confirm-overlay" @click.self="showClearConfirm = false">
        <div class="confirm-dialog">
          <p class="confirm-msg">{{ t('common.confirmDelete') }}</p>
          <div class="confirm-btns">
            <JdButton type="outline" size="sm" @click="showClearConfirm = false">{{ t('common.cancel') }}</JdButton>
            <JdButton size="sm" :loading="clearingCart" @click="confirmClear">{{ t('common.confirm') }}</JdButton>
          </div>
        </div>
      </div>
    </template>

    <!-- Empty -->
    <div v-else>
      <JdEmpty icon="🛒" :title="t('common.emptyCart')" :description="t('cart.emptyDesc')">
        <JdButton @click="router.push('/')">{{ t('common.goShopping') }}</JdButton>
      </JdEmpty>

      <!-- Recommendations -->
      <div v-if="recs.length" class="recs-section">
        <h3 class="recs-title">{{ t('cart.recommendTitle') }}</h3>
        <div class="recs-grid">
          <div v-for="p in recs" :key="p.id" class="recs-item" role="button" tabindex="0" @click="router.push('/product/' + p.id)" @keydown.enter.prevent="router.push('/product/' + p.id)" @keydown.space.prevent="router.push('/product/' + p.id)">
            <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="120px" />
            <div class="recs-info">
              <div class="recs-name">{{ p.name }}</div>
              <span class="recs-price">{{ formatPrice(p.price / 100, 2) }}</span>
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
.sk-body { flex: 1; }

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

.cart-item-wrapper { position: relative; overflow: hidden; }
.cart-item-swipe-bg {
  position: absolute; right: 0; top: 0; bottom: 0; width: 80px;
  background: var(--jd-red); color: #fff; display: flex;
  align-items: center; justify-content: center; font-size: var(--font-md);
  font-weight: 600; cursor: pointer; border-radius: var(--radius-md);
  opacity: 0; transition: opacity var(--transition-fast);
}
.cart-item-swipe-bg.reveal { opacity: 1; }
.cart-item { display: flex; align-items: center; padding: var(--space-lg) 0; border-bottom: 1px solid var(--border-light); transition: transform .2s ease; position: relative; z-index: 1; background: var(--bg-white); }
.cart-check { margin-right: var(--space-md); }
.cart-img { flex-shrink: 0; }
.cart-name { flex: 1; min-width: 0; padding: 0 var(--space-lg); }
.cart-name a { font-weight: 600; color: var(--text-primary); text-decoration: none; font-size: 15px; }
.cart-name a:hover { color: var(--jd-red); }

.cart-price { width: 80px; text-align: center; color: var(--text-secondary); font-size: var(--font-md); }

.qty-control { display: flex; align-items: center; margin: 0 40px; }
.qty-btn { width: 32px; height: 32px; border: 1px solid var(--border); background: var(--bg-white); cursor: pointer; font-size: var(--font-md); display: flex; align-items: center; justify-content: center; }
.qty-btn:first-child { border-radius: var(--radius-sm) 0 0 var(--radius-sm); }
.qty-btn:last-child { border-radius: 0 var(--radius-sm) var(--radius-sm) 0; }
.qty-btn:hover { background: var(--bg-hover); }
.qty-val { width: 44px; text-align: center; border: 1px solid var(--border); height: 32px; font-size: var(--font-base); background: var(--bg-white); color: var(--text-primary); border-radius: 0; box-sizing: border-box; -moz-appearance: textfield; }
.qty-val::-webkit-inner-spin-button, .qty-val::-webkit-outer-spin-button { -webkit-appearance: none; margin: 0; }
.qty-val:focus { outline: none; border-color: var(--jd-red); }

.cart-subtotal { width: 80px; text-align: center; color: var(--jd-red); font-weight: 700; font-size: var(--font-lg); }
.cart-remove { width: 40px; text-align: center; border: none; background: none; cursor: pointer; color: var(--text-tertiary); font-size: var(--font-lg); transition: color var(--transition-fast); }
.cart-remove:hover { color: var(--jd-red); }
.cart-move-wishlist { width: 30px; text-align: center; border: none; background: none; cursor: pointer; color: var(--text-placeholder); font-size: var(--font-md); transition: color var(--transition-fast); }
.cart-move-wishlist:hover { color: var(--jd-red); }

/* Stock alert */
.stock-alert { background: #fff3e0; border-left: 3px solid var(--orange); padding: var(--space-md) var(--space-lg); margin-bottom: var(--space-lg); border-radius: var(--radius-md); font-size: var(--font-sm); color: var(--orange); display: flex; flex-wrap: wrap; align-items: center; gap: var(--space-sm); }
.stock-alert-item { background: #ffe0b2; padding: 2px 8px; border-radius: var(--radius-sm); font-weight: 600; white-space: nowrap; }

/* Clear cart */
.clear-cart-btn { padding: 4px 12px; border: 1px solid var(--border); background: var(--bg-white); color: var(--text-tertiary); border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-sm); margin-left: var(--space-md); transition: all var(--transition-fast); }
.clear-cart-btn:hover { color: var(--jd-red); border-color: var(--jd-red); }

/* Confirm dialog */
.confirm-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,.4); z-index: 400; display: flex; align-items: center; justify-content: center; }
.confirm-dialog { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); max-width: 360px; width: 90%; text-align: center; box-shadow: var(--shadow-xl); }
.confirm-msg { font-size: var(--font-md); margin-bottom: var(--space-xl); color: var(--text-primary); }
.confirm-btns { display: flex; gap: var(--space-md); justify-content: center; }

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
  .cart-page { padding: var(--space-md); padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px)); }
  .cart-header { display: none; }
  .cart-item { flex-wrap: wrap; gap: var(--space-sm); padding: var(--space-md) 0; position: relative; }
  .cart-check { position: absolute; top: var(--space-md); left: 0; z-index: 2; }
  .cart-img { margin-left: var(--space-xl); }
  .cart-name { flex-basis: calc(100% - 120px); padding: 0 var(--space-md); }
  .cart-price { display: none; }
  .qty-control { margin: 0; }
  .qty-btn { width: 40px; height: 40px; font-size: var(--font-lg); }
  .cart-subtotal { width: auto; margin-left: auto; font-size: var(--font-md); }
  .cart-remove { width: auto; padding: 4px 8px; }
  .cart-footer { flex-direction: column; gap: var(--space-md); }
  .footer-left { width: 100%; justify-content: space-between; }
  .footer-right { width: 100%; justify-content: space-between; }
  .recs-grid { grid-template-columns: repeat(2, 1fr); }
}

/* Undo bar */
.undo-bar {
  display: flex; align-items: center; gap: var(--space-md);
  padding: var(--space-md) var(--space-lg); margin-bottom: var(--space-md);
  background: #333; color: #fff; border-radius: var(--radius-md);
  font-size: var(--font-sm); animation: slideDown .3s ease;
}
.undo-btn {
  padding: 2px 12px; background: transparent; color: #ffd54f;
  border: 1px solid #ffd54f; border-radius: var(--radius-sm);
  cursor: pointer; font-size: var(--font-xs); font-weight: 600;
  white-space: nowrap;
}
.undo-btn:hover { background: rgba(255,213,79,.15); }
.undo-close { background: none; border: none; color: rgba(255,255,255,.6); cursor: pointer; font-size: var(--font-md); margin-left: auto; padding: 0; }
@keyframes slideDown { from { opacity: 0; transform: translateY(-8px); } to { opacity: 1; transform: translateY(0); } }
</style>
