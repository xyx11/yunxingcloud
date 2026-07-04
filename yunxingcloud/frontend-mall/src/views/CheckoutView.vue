<script setup lang="ts">
import { ref, onMounted, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getCart } from '@/api/cart'
import { submitOrder, getAddresses, getMyCoupons } from '@/api/order'
import { useI18n } from '@/locales'
import { ToastInjectionKey } from '@/composables/useToast'
import LazyImage from '@/components/LazyImage.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdButton from '@/components/JdButton.vue'

const router = useRouter()
const { t } = useI18n()
const toast = inject(ToastInjectionKey)!
const items = ref<any[]>([])
const addresses = ref<any[]>([])
const loading = ref(true)
const total = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))
const selectedAddr = ref<any>(null)
const receiver = ref({ name: '', phone: '', address: '' })
const selectedCoupon = ref<any>(null)
const couponApplied = ref(false)
const myCoupons = ref<any[]>([])
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

function selectAddress(addr: any) {
  selectedAddr.value = addr
  receiver.value = { name: addr.name, phone: addr.phone, address: [addr.province, addr.city, addr.district, addr.detail].filter(Boolean).join(' ') }
}

async function loadCoupons() { try { const r = await getMyCoupons(); myCoupons.value = (r.data || []).filter((c: any) => c.status === '0') } catch {} }

function selectCoupon(c: any) { selectedCoupon.value = c; couponApplied.value = true; showCoupons.value = false; toast.success('优惠券已选用') }

async function submit() {
  if (!receiver.value.name || !receiver.value.phone || !receiver.value.address) { toast.error(t('checkout.selectAddress')); return }
  submitting.value = true
  try { const res = await submitOrder({ ...receiver.value, couponCode: selectedCoupon.value?.couponId || '', paymentMethod: paymentMethod.value }); toast.success(t('toast.orderPlaced')); router.push('/orders') }
  catch (e: any) { toast.error(e.response?.data?.message || t('toast.orderFail')) }
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
            <div class="addr-item-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
              <span v-if="addr.isDefault" class="default-tag">[{{ t('profile.defaultAddr') }}]</span>
            </div>
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
          <span class="product-price">¥{{ (item.price * item.quantity / 100).toFixed(2) }}</span>
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
                <span>券ID:{{ c.couponId }}</span><span class="coupon-use">使用</span>
              </div>
            </div>
          </div>

          <div class="payment-row">
            <span class="payment-label">支付方式</span>
            <div class="payment-options">
              <span class="payment-option" :class="{ selected: paymentMethod === 'wechat' }" @click="paymentMethod = 'wechat'">
                <span style="color:#07c160;font-size:18px">💬</span>微信支付
              </span>
              <span class="payment-option" :class="{ selected: paymentMethod === 'alipay' }" @click="paymentMethod = 'alipay'">
                <span style="color:#1677ff;font-size:18px">🔵</span>支付宝
              </span>
            </div>
          </div>
        </div>

        <div class="price-summary">
          <div class="price-line"><span>商品总额</span><span>¥{{ (total / 100).toFixed(2) }}</span></div>
          <div class="price-line"><span>优惠</span><span class="discount">-¥{{ couponApplied ? ((selectedCoupon?.amount || total / 10) / 100).toFixed(2) : '0.00' }}</span></div>
          <div class="price-line"><span>运费</span><span class="free-shipping">免运费</span></div>
        </div>

        <div class="total-row">
          <span class="total-label">{{ t('checkout.totalItems', { n: items.length }) }}</span>
          <div><span class="total-prefix">{{ t('checkout.actualPay') }}：</span><span class="total-price">¥{{ (total / 100).toFixed(2) }}</span></div>
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
.addr-item { padding: var(--space-md); border-radius: var(--radius-md); margin-bottom: var(--space-sm); cursor: pointer; border: 1px solid var(--border-light); background: var(--bg-white); transition: all var(--transition-fast); }
.addr-item.selected { border: 2px solid var(--jd-red); background: var(--jd-red-light); }
.addr-item-name { font-weight: 600; margin-bottom: var(--space-xs); }
.addr-item-phone { color: var(--text-tertiary); font-weight: 400; }
.addr-item-detail { color: var(--text-secondary); font-size: var(--font-base); }
.default-tag { color: var(--jd-red); font-size: var(--font-xs); margin-left: var(--space-sm); }

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
.coupon-item { padding: var(--space-sm); margin-bottom: var(--space-xs); background: #fef9e7; border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-sm); display: flex; justify-content: space-between; }
.coupon-use { color: var(--jd-red); }

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
