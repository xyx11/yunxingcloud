<script setup lang="ts">
import { ref, onMounted, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getCart } from '@/api/cart'
import { submitOrder, getAddresses, getMyCoupons } from '@/api/order'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'
import LazyImage from '@/components/LazyImage.vue'
import { formatPrice } from '@/utils/format'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdButton from '@/components/JdButton.vue'
import type { Address, Coupon, CartItem } from '@/types'

const router = useRouter()
const { t } = useI18n()
const toast = inject(ToastInjectionKey)!
const items = ref<CartItem[]>([])
const addresses = ref<Address[]>([])
const loading = ref(true)
const total = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))
const selectedAddr = ref<Address | null>(null)
const receiver = ref({ name: '', phone: '', address: '' })
const selectedCoupon = ref<Coupon | null>(null)
const couponApplied = ref(false)
const myCoupons = ref<Coupon[]>([])
const showCoupons = ref(false)
const paymentMethod = ref('wechat')
const submitting = ref(false)
const showInvoice = ref(false)
const invoice = ref({ type: 'personal', title: '', taxNo: '', email: '' })

async function load() {
  loading.value = true
  try { const r = await getCart(); items.value = r.data || [] } catch {}
  try { const r = await getAddresses(); addresses.value = r.data || [] } catch {}
  finally { loading.value = false }
}

function selectAddress(addr: Address) {
  selectedAddr.value = addr
  receiver.value = { name: addr.name, phone: addr.phone, address: [addr.province, addr.city, addr.district, addr.detail].filter(Boolean).join(' ') }
}

async function loadCoupons() { try { const r = await getMyCoupons(); myCoupons.value = (r.data || []).filter((c: Coupon) => c.status === '0' || c.status === 'available') } catch {} }

function selectCoupon(c: Coupon) { selectedCoupon.value = c; couponApplied.value = true; showCoupons.value = false; toast.success('优惠券已选用') }

async function submit() {
  if (!receiver.value.name || !receiver.value.phone || !receiver.value.address) { toast.error(t('checkout.selectAddress')); return }
  submitting.value = true
  try { const res = await submitOrder({ ...receiver.value, couponCode: String(selectedCoupon.value?.couponId ?? ''), paymentMethod: paymentMethod.value }); toast.success(t('toast.orderPlaced')); const resData = res.data as { id?: number; data?: { id?: number } }; const orderId = resData.id || resData.data?.id; router.push(orderId ? '/order/' + orderId : '/orders') }
  catch (e: unknown) { toast.error((e as { response?: { data?: { message?: string } } }).response?.data?.message || t('toast.orderFail')) }
  finally { submitting.value = false }
}

onMounted(() => { load(); loadCoupons() })
</script>

<template>
  <div class="checkout-page">
    <h2 class="page-title">{{ t('checkout.title') }}</h2>

    <div v-if="loading" class="card">
      <SkeletonBox height="20px" width="80%" />
      <SkeletonBox height="20px" width="50%" />
      <SkeletonBox height="20px" width="60%" />
    </div>

    <template v-else>
      <!-- Receiver -->
      <div class="card">
        <h3 class="section-title">{{ t('checkout.receiverInfo') }}</h3>
        <div v-if="addresses.length" class="addr-list">
          <div v-for="addr in addresses" :key="addr.id" class="addr-item" :class="{ selected: selectedAddr?.id === addr.id }" @click="selectAddress(addr)">
            <div class="addr-item-name">{{ addr.name }} <span class="addr-item-phone">{{ addr.phone }}</span></div>
            <div class="addr-item-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</div>
            <span v-if="addr.isDefault" class="default-addr-badge">默认地址</span>
          </div>
        </div>
        <div v-if="!selectedAddr" class="receiver-form">
          <div class="receiver-grid">
            <input v-model="receiver.name" :placeholder="t('order.receiverName')" class="field-input" />
            <input v-model="receiver.phone" :placeholder="t('order.receiverPhone')" class="field-input" />
          </div>
          <input v-model="receiver.address" :placeholder="t('order.receiverAddress')" class="field-input field-full" />
        </div>
      </div>

      <!-- Products -->
      <div class="card">
        <h3 class="section-title">{{ t('checkout.productList') }}</h3>
        <div v-for="item in items" :key="item.id" class="product-row">
          <LazyImage :src="item.imageUrl || item.productImage || ''" alt="" height="60px" width="60px" rounded="6px" />
          <span class="product-name">{{ item.productName }} × {{ item.quantity }}</span>
          <span class="product-price">{{ formatPrice(item.price * item.quantity / 100, 2) }}</span>
        </div>

        <div class="summary-section">
          <div class="coupon-row">
            <button v-if="!couponApplied" class="coupon-btn" @click="showCoupons = !showCoupons; if (showCoupons) loadCoupons()">
              {{ myCoupons.length ? myCoupons.length + '张可用' : '选择优惠券' }}
            </button>
            <div v-else class="coupon-selected">
              <span>已选优惠券</span>
              <button class="coupon-remove" @click="selectedCoupon = null; couponApplied = false">移除</button>
            </div>
            <div v-if="showCoupons && myCoupons.length" class="coupon-dropdown">
              <div v-for="c in myCoupons" :key="c.id" class="coupon-item" @click="selectCoupon(c)">
                <span class="coupon-amount">{{ formatPrice((c.amount || 0) / 100) }}</span>
                <span class="coupon-cond" v-if="c.minAmount">满{{ formatPrice(c.minAmount / 100) }}可用</span>
                <span class="coupon-use">使用</span>
              </div>
            </div>
          </div>

          <div class="payment-row">
            <span class="payment-label">支付方式</span>
            <div class="payment-options">
              <span class="payment-option" :class="{ selected: paymentMethod === 'wechat' }" @click="paymentMethod = 'wechat'">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="#07c160"><path d="M8.5 13.5c-.83 0-1.5.67-1.5 1.5s.67 1.5 1.5 1.5S10 15.83 10 15s-.67-1.5-1.5-1.5zm5 0c-.83 0-1.5.67-1.5 1.5s.67 1.5 1.5 1.5S15 15.83 15 15s-.67-1.5-1.5-1.5zM12 2C6.48 2 2 6.04 2 11c0 2.57 1.2 4.88 3.11 6.44l-.86 2.56 2.82-1.53c.9.3 1.86.48 2.87.53l.06-.58A5 5 0 0 1 12 6a5 5 0 0 1 5 5 5 5 0 0 1-3.3 4.73l.3 1.08c.33.04.66.08.99.08 1.72 0 3.34-.44 4.75-1.2l2.86 1.46-.82-2.44C21.24 15.59 22 13.38 22 11c0-4.96-4.48-9-10-9z"/></svg>
                微信支付
              </span>
              <span class="payment-option" :class="{ selected: paymentMethod === 'alipay' }" @click="paymentMethod = 'alipay'">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="#1677ff"><path d="M21.422 15.358c-3.22-1.386-6.847-2.408-10.604-2.996-1.04-.165-2.104-.284-3.18-.336-1.09-.052-2.19-.064-3.29-.006-.402.02-.805.05-1.205.108-.086.012-.22.04-.3.06-.08.02-.154.05-.194.09-.064.082-.02.258.03.382.074.19.242.368.398.508.69.62 1.51 1.07 2.35 1.41 1.69.68 3.49 1.07 5.24 1.51 1.08.27 2.15.55 3.21.88.42.13.83.28 1.23.45.87.36 1.46.86 1.47 1.48.01.56-.35 1.06-.88 1.24-.34.11-.71.14-1.07.14-1.04.02-2.05-.22-3.03-.5-1.82-.52-3.57-1.2-5.2-2.14-.14-.08-.29-.19-.44-.27-.02-.01-.06-.02-.08-.01-.01.02-.01.05-.01.08l-.01.47c0 .74.07 1.47.25 2.19.17.68.47 1.3.88 1.88.42.59.94 1.09 1.53 1.48.6.4 1.24.71 1.93.91 1.08.32 2.2.48 3.33.5 1.28.03 2.56-.09 3.8-.42.61-.16 1.2-.37 1.76-.65.55-.28 1.05-.62 1.47-1.06.32-.34.6-.72.78-1.16.2-.5.28-1.04.19-1.57-.15-.86-.68-1.58-1.54-2.16-.13-.09-.26-.16-.41-.24l-.04-.02zM3.904 8.158c.34-.11.69-.19 1.04-.24.78-.13 1.57-.12 2.35-.02.72.1 1.42.29 2.12.5.68.2 1.36.42 2.02.69 1.29.52 2.46 1.17 3.47 1.98.2.16.43.31.62.47.01 0 .06.04.07.04 0-.24-.01-.48-.01-.72 0-.86-.01-1.73-.01-2.59 0-.52-.01-1.04 0-1.56.01-.36.04-.72.13-1.07.08-.32.21-.61.4-.88.22-.31.5-.57.83-.76.33-.2.68-.35 1.05-.43.41-.1.82-.14 1.24-.12.4.02.8.07 1.2.13 0-.53-.02-1.06-.06-1.59-.02-.3-.07-.6-.15-.9-.06-.24-.16-.47-.3-.68-.2-.31-.47-.58-.79-.76-.56-.32-1.19-.47-1.83-.51-1.52-.12-3.04.12-4.46.77-.56.26-1.08.6-1.56 1-.57.47-1.05 1.02-1.43 1.65-.18.29-.23.62-.23.96.01.34.03.68.04 1.02.01.36 0 .72 0 1.08.01.78 0 1.56 0 2.34l-.01.33c-.04.03-.08.05-.12.07-1.37.93-2.55 2.09-3.46 3.46-.44.67-.79 1.39-1.05 2.15-.01.02-.02.05-.04.08-.12.29-.12.29-.24-.01-.23-.58-.53-1.14-.8-1.7-.25-.52-.57-1.01-.86-1.52-.22-.39-.46-.77-.65-1.17-.12-.26-.23-.52-.3-.8-.06-.23-.09-.47-.08-.71.02-.5.18-.96.5-1.36.21-.26.47-.48.76-.63z"/></svg>
                支付宝
              </span>
            </div>
          </div>
        </div>

        <div class="price-summary">
          <div class="price-line"><span>商品总额</span><span>{{ formatPrice(total / 100, 2) }}</span></div>
          <div class="price-line"><span>优惠</span><span class="discount">{{ couponApplied ? '-' + formatPrice((selectedCoupon?.amount || total / 10) / 100, 2) : formatPrice(0, 2) }}</span></div>
          <div class="price-line"><span>运费</span><span class="free-shipping">免运费</span></div>
        </div>

        <div class="total-row">
          <span class="total-label">{{ t('checkout.totalItems', { n: items.length }) }}</span>
          <div><span class="total-prefix">{{ t('checkout.actualPay') }}：</span><span class="total-price">{{ formatPrice(total / 100, 2) }}</span></div>
        </div>

        <!-- Invoice -->
        <div class="invoice-section">
          <div class="invoice-toggle" @click="showInvoice = !showInvoice">{{ showInvoice ? '▼' : '▶' }} 发票信息</div>
          <div v-if="showInvoice" class="invoice-form">
            <div class="invoice-type">
              <label class="radio-label"><input type="radio" v-model="invoice.type" value="personal" /> 个人</label>
              <label class="radio-label"><input type="radio" v-model="invoice.type" value="company" /> 企业</label>
            </div>
            <input v-if="invoice.type === 'company'" v-model="invoice.title" placeholder="发票抬头" class="field-input field-full" />
            <input v-if="invoice.type === 'company'" v-model="invoice.taxNo" placeholder="税号" class="field-input field-full" />
            <input v-model="invoice.email" placeholder="接收邮箱" class="field-input field-full" />
          </div>
        </div>

        <JdButton block size="lg" :loading="submitting" @click="submit">
          {{ submitting ? '提交中...' : t('order.submitOrder') }}
        </JdButton>
      </div>
    </template>
  </div>
</template>

<style scoped>
.checkout-page { max-width: 800px; margin: 0 auto; }
.page-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-xl); }
.card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); margin-bottom: var(--space-lg); box-shadow: var(--shadow-sm); }
.section-title { font-size: var(--font-lg); font-weight: 600; margin-bottom: var(--space-md); }

.addr-list { margin-bottom: var(--space-lg); }
.addr-item { padding: var(--space-lg); border-radius: var(--radius-md); margin-bottom: var(--space-sm); cursor: pointer; border: 1px solid var(--border-light); background: var(--bg-white); transition: all var(--transition-fast); box-shadow: var(--shadow-sm); }
.addr-item:hover { box-shadow: var(--shadow-md); border-color: var(--border); }
.addr-item.selected { border: 2px solid var(--jd-red); background: var(--jd-red-light); box-shadow: 0 0 0 4px rgba(241,2,21,.08); }
.addr-item-name { font-weight: 600; margin-bottom: var(--space-xs); }
.addr-item-phone { color: var(--text-tertiary); font-weight: 400; }
.addr-item-detail { color: var(--text-secondary); font-size: var(--font-base); }
.default-tag { color: var(--jd-red); font-size: var(--font-xs); margin-left: var(--space-sm); }
.default-addr-badge { display: inline-block; background: #e8f5e9; color: var(--green); font-size: var(--font-xs); padding: 1px 8px; border-radius: var(--radius-sm); font-weight: 600; margin-top: var(--space-xs); }

.receiver-form { margin-bottom: var(--space-lg); }
.receiver-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-md); margin-bottom: var(--space-md); }

.field-input { padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md); font-size: var(--font-md); background: var(--bg-white); color: var(--text-primary); }
.field-input:focus { border-color: var(--jd-red); outline: none; }
.field-full { width: 100%; box-sizing: border-box; margin-top: var(--space-md); }

.product-row { display: flex; align-items: center; padding: var(--space-md) 0; border-bottom: 1px solid var(--border-light); gap: var(--space-lg); }
.product-name { flex: 1; font-size: var(--font-md); }
.product-price { color: var(--jd-red); font-weight: 600; font-size: 15px; }

.summary-section { margin-top: var(--space-lg); }
.coupon-row { display: flex; gap: var(--space-sm); margin-bottom: var(--space-md); flex-wrap: wrap; }
.coupon-btn { padding: var(--space-sm) var(--space-lg); border: 1px dashed var(--jd-red); color: var(--jd-red); background: var(--bg-white); border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-base); }
.coupon-selected { display: flex; justify-content: space-between; align-items: center; background: #fff9f0; padding: var(--space-sm) var(--space-md); border-radius: var(--radius-md); font-size: var(--font-base); flex: 1; }
.coupon-remove { background: none; border: none; color: var(--text-tertiary); cursor: pointer; font-size: var(--font-sm); }
.coupon-dropdown { width: 100%; max-height: 150px; overflow: auto; margin-top: var(--space-sm); }
.coupon-item { padding: var(--space-sm) var(--space-md); margin-bottom: var(--space-xs); background: #fef9e7; border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-sm); display: flex; align-items: center; gap: var(--space-sm); }
.coupon-item:hover { background: #fef3c7; }
.coupon-amount { color: var(--jd-red); font-weight: 700; font-size: var(--font-md); }
.coupon-cond { color: var(--text-tertiary); font-size: var(--font-xs); flex: 1; }
.coupon-use { color: var(--jd-red); font-weight: 600; }

.payment-row { margin-bottom: var(--space-md); }
.payment-label { font-size: var(--font-base); color: var(--text-secondary); margin-right: var(--space-md); }
.payment-options { display: flex; gap: var(--space-md); margin-top: var(--space-sm); }
.payment-option { cursor: pointer; padding: var(--space-sm) var(--space-lg); border-radius: var(--radius-md); font-size: var(--font-base); display: flex; align-items: center; gap: 6px; border: 1px solid var(--border); background: var(--bg-white); transition: all var(--transition-fast); }
.payment-option.selected { border: 2px solid var(--jd-red); background: var(--jd-red-light); }

.price-summary { margin-top: var(--space-lg); padding-top: var(--space-md); border-top: 1px solid var(--border-light); }
.price-line { display: flex; justify-content: space-between; font-size: var(--font-base); color: var(--text-secondary); margin-bottom: var(--space-xs); }
.discount { color: var(--jd-red); }
.free-shipping { color: var(--green); }

.total-row { display: flex; justify-content: space-between; align-items: center; padding-top: var(--space-md); margin-top: var(--space-md); border-top: 1px solid var(--border-light); }
.total-label { font-size: var(--font-md); color: var(--text-secondary); }
.total-prefix { font-size: var(--font-lg); color: var(--text-secondary); }
.total-price { font-size: var(--font-h1); color: var(--jd-red); font-weight: 700; }

.invoice-section { margin-top: var(--space-lg); padding: var(--space-md); background: var(--bg-hover); border-radius: var(--radius-md); }
.invoice-toggle { cursor: pointer; font-size: var(--font-md); font-weight: 600; }
.invoice-form { margin-top: var(--space-sm); }
.invoice-type { margin-bottom: var(--space-sm); display: flex; gap: var(--space-lg); }
.radio-label { font-size: var(--font-base); cursor: pointer; display: flex; align-items: center; gap: 4px; }
.radio-label input { accent-color: var(--jd-red); }
.invoice-form .field-full { margin-top: var(--space-sm); }
</style>
