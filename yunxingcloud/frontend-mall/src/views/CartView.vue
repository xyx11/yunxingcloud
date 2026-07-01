<script setup lang="ts">
import { ref, onMounted, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, addToCart, removeFromCart } from '@/api/cart'
import { getHotProducts } from '@/api/product'
import { useI18n } from '@/locales'

const router = useRouter()
const { t } = useI18n()
const toast = inject<any>('toast')
const items = ref<any[]>([])
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
  try { const r = await getCart(); items.value = r.data || []; if (!items.value.length) { const h = await getHotProducts(); recs.value = (h.data || []).slice(0, 4) } } catch {} finally { loading.value = false }
}

async function remove(id: number) { try { await removeFromCart(id); selectedIds.value.delete(id); toast.info(t('toast.removed')); load() } catch { toast.error('删除失败') } }
async function updateQty(item: any, delta: number) { const n = item.quantity + delta; if (n < 1) return; try { await removeFromCart(item.id); await addToCart(item.productId, n); load() } catch { toast.error('更新失败') } }
function checkout() { if (selectedIds.value.size === 0) { toast.error(t('checkout.selectAddress')); return }; router.push('/checkout') }
onMounted(load)
</script>

<template>
  <div style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
    <h2 style="font-size:20px;font-weight:700;margin-bottom:20px">{{ t('common.cart') }}</h2>
    <div v-if="loading" style="padding:40px 0">
      <div v-for="i in 3" :key="i" style="display:flex;align-items:center;padding:16px 0;border-bottom:1px solid #f5f5f5;gap:12px">
        <div style="width:80px;height:80px;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:8px"></div>
        <div style="flex:1"><div style="height:16px;width:60%;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:4px"></div></div>
      </div>
    </div>
    <div v-else-if="items.length">
      <div style="display:flex;align-items:center;padding:12px 0;border-bottom:1px solid #f0f0f0;font-size:13px;color:#666">
        <label style="cursor:pointer;display:flex;align-items:center;gap:8px"><input type="checkbox" v-model="allSelected" style="accent-color:#f10215" />{{ t('cart.selectAll') }}</label>
        <span style="margin-left:auto">{{ t('cart.unitPrice') }}</span><span style="margin:0 120px 0 60px">{{ t('product.quantity') }}</span><span style="margin-right:60px">{{ t('cart.subTotal') }}</span><span style="width:40px">{{ t('cart.actions') }}</span>
      </div>
      <div v-for="item in items" :key="item.id" style="display:flex;align-items:center;padding:16px 0;border-bottom:1px solid #f5f5f5">
        <label style="cursor:pointer;margin-right:12px"><input type="checkbox" :value="item.id" v-model="selectedIds" style="accent-color:#f10215" /></label>
        <div style="width:80px;height:80px;background:linear-gradient(135deg,#f8f8f8,#eee);border-radius:8px;display:flex;align-items:center;justify-content:center;font-size:32px;margin-right:16px;flex-shrink:0">📦</div>
        <div style="flex:1;min-width:0"><router-link :to="`/product/${item.productId}`" style="font-weight:600;color:#333;text-decoration:none;font-size:15px">{{ item.productName }}</router-link></div>
        <div style="width:80px;text-align:center;color:#666;font-size:14px">¥{{ (item.price / 100).toFixed(2) }}</div>
        <div style="display:flex;align-items:center;gap:0;margin:0 40px">
          <button @click="updateQty(item, -1)" style="width:28px;height:28px;border:1px solid #ddd;background:#fff;cursor:pointer;border-radius:4px 0 0 4px">-</button>
          <span style="width:36px;text-align:center;border-top:1px solid #ddd;border-bottom:1px solid #ddd;height:28px;line-height:28px;font-size:13px">{{ item.quantity }}</span>
          <button @click="updateQty(item, 1)" style="width:28px;height:28px;border:1px solid #ddd;background:#fff;cursor:pointer;border-radius:0 4px 4px 0">+</button>
        </div>
        <div style="width:80px;text-align:center;color:#f10215;font-weight:700;font-size:16px">¥{{ (item.price * item.quantity / 100).toFixed(2) }}</div>
        <button @click="remove(item.id)" style="width:40px;text-align:center;border:none;background:none;cursor:pointer;color:#ccc;font-size:18px"
                @mouseenter="(e:any) => e.target.style.color='#f10215'" @mouseleave="(e:any) => e.target.style.color='#ccc'">✕</button>
      </div>
      <div style="display:flex;align-items:center;justify-content:space-between;padding-top:20px;margin-top:8px">
        <div><label style="cursor:pointer;display:flex;align-items:center;gap:8px;font-size:13px;color:#666"><input type="checkbox" v-model="allSelected" style="accent-color:#f10215" /> {{ t('cart.selectAll') }}</label><span style="font-size:13px;color:#999;margin-left:16px">{{ t('cart.selected') }} {{ selectedIds.size }} {{ t('cart.items') }}</span></div>
        <div style="display:flex;align-items:center;gap:20px">
          <div><span style="font-size:14px;color:#666">{{ t('cart.total') }}：</span><span style="font-size:26px;color:#f10215;font-weight:700">¥{{ (total / 100).toFixed(2) }}</span></div>
          <button @click="checkout" style="padding:14px 48px;background:#f10215;color:#fff;border:none;border-radius:8px;font-size:18px;cursor:pointer;font-weight:700;transition:background .2s"
                  @mouseenter="(e:any) => e.target.style.background='#d4000f'" @mouseleave="(e:any) => e.target.style.background='#f10215'">{{ t('cart.checkout') }}</button>
        </div>
      </div>
    </div>
    <div v-else style="text-align:center;padding:40px 20px;color:#999">
      <p style="font-size:64px;margin-bottom:12px">🛒</p><p style="font-size:16px;margin-bottom:4px">{{ t('common.emptyCart') }}</p>
      <button @click="router.push('/')" style="margin-top:12px;padding:10px 32px;background:#f10215;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:14px;font-weight:600">{{ t('common.goShopping') }}</button>
      <div v-if="recs.length" style="margin-top:32px;text-align:left">
        <h3 style="font-size:15px;font-weight:600;color:#333;margin-bottom:12px">🔥 为你推荐</h3>
        <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:12px">
          <div v-for="p in recs" :key="p.id" @click="router.push('/product/'+p.id)" style="background:#fafafa;border-radius:8px;overflow:hidden;cursor:pointer;transition:transform .2s"
               @mouseenter="(e:any) => e.target.style.transform='translateY(-4px)'" @mouseleave="(e:any) => e.target.style.transform=''">
            <div style="height:120px;background:linear-gradient(135deg,#f8f8f8,#eee);display:flex;align-items:center;justify-content:center;font-size:36px">📦</div>
            <div style="padding:8px 12px"><div style="font-size:13px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:#333">{{ p.name }}</div><span style="color:#f10215;font-weight:700;font-size:14px">¥{{ (p.price/100).toFixed(2) }}</span></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
</style>