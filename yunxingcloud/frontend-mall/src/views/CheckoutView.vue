<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAddresses, getMyCoupons, submitOrder, createAddress } from '@/api/order'
import { getCart } from '@/api/cart'
import { getPointsAccount } from '@/api/member'
import { formatPrice } from '@/utils/format'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import JdModal from '@/components/JdModal.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const router = useRouter()
const auth = useAuthStore()
const toast = useToast()
const { t } = useI18n()

const cartItems = ref<any[]>([])
const addresses = ref<any[]>([])
const coupons = ref<any[]>([])
const loading = ref(true)
const submitting = ref(false)

const selectedAddrId = ref<number | null>(null)
const selectedCouponId = ref<number | null>(null)
const payMethod = ref<'wechat' | 'alipay'>('wechat')
const remark = ref('')
const agreedToTerms = ref(false)

// Points
const pointsBalance = ref(0)
const pointsToUse = ref(0)
const pointsRate = 100 // 100 points = ¥1
const pointsDiscount = computed(() => Math.floor(pointsToUse.value / pointsRate) * 100)
const maxPointsDiscount = computed(() => Math.min(pointsBalance.value, Math.floor((subtotal.value - discount.value) / 100) * pointsRate))

const showAddrEditor = ref(false)
const addrForm = ref({ name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })

const selectedAddress = computed(() => addresses.value.find(a => a.id === selectedAddrId.value) || null)
const selectedCoupon = computed(() => coupons.value.find(c => c.id === selectedCouponId.value) || null)
const availableCoupons = computed(() =>
  coupons.value.filter((c: any) => !c.minAmount || subtotal.value >= c.minAmount)
)

const subtotal = computed(() =>
  cartItems.value.reduce((s, i) => s + (i.price || 0) * (i.quantity || 1), 0)
)

const discount = computed(() => {
  if (!selectedCoupon.value) return 0
  return Math.min(selectedCoupon.value.amount || 0, subtotal.value)
})

const total = computed(() => Math.max(0, subtotal.value - discount.value - pointsDiscount.value))

const canSubmit = computed(() =>
  selectedAddrId.value !== null && cartItems.value.length > 0 && agreedToTerms.value && !submitting.value
)

const isLoggedIn = computed(() => auth.isLoggedIn)

async function loadData() {
  loading.value = true
  try {
    const [cartR, addrR, couponR, pointsR] = await Promise.all([
      getCart().catch(() => ({ data: { items: [] } })),
      isLoggedIn.value ? getAddresses().catch(() => ({ data: [] })) : Promise.resolve({ data: [] }),
      isLoggedIn.value ? getMyCoupons().catch(() => ({ data: [] })) : Promise.resolve({ data: [] }),
      isLoggedIn.value ? getPointsAccount().catch(() => ({ data: { balance: 0 } })) : Promise.resolve({ data: { balance: 0 } }),
    ])
    cartItems.value = cartR.data?.items || []
    addresses.value = addrR.data || []
    coupons.value = (couponR.data || [])
    pointsBalance.value = pointsR.data?.balance || pointsR.data?.points || 0

    const def = addresses.value.find(a => a.isDefault)
    if (def) selectedAddrId.value = def.id
    else if (addresses.value.length) selectedAddrId.value = addresses.value[0].id
  } catch {
    toast.error(t('checkout.loadFail'))
  } finally {
    loading.value = false
  }
}

async function doSubmit() {
  if (!canSubmit.value || !selectedAddress.value) return
  submitting.value = true
  try {
    const addr = selectedAddress.value
    const r = await submitOrder({
      receiverName: addr.name,
      receiverPhone: addr.phone,
      receiverAddress: `${addr.province || ''}${addr.city || ''}${addr.district || ''}${addr.detail || addr.address || ''}`,
      couponId: selectedCouponId.value != null ? String(selectedCouponId.value) : '',
      payChannel: payMethod.value,
      remark: remark.value,
      points: String(pointsToUse.value || 0),
    })
    const orderId = r.data?.id || r.data?.orderId
    if (orderId) {
      router.push(`/pay/${orderId}`)
    } else {
      router.push('/orders')
    }
  } catch {
    toast.error(t('checkout.submitFail'))
  } finally {
    submitting.value = false
  }
}

const savingAddr = ref(false)
async function saveNewAddress() {
  const { name, phone, detail } = addrForm.value
  if (!name.trim() || !phone.trim() || !detail.trim()) {
    toast.error(t('checkout.addressRequired'))
    return
  }
  savingAddr.value = true
  try {
    const r = await createAddress({
      name: name.trim(),
      phone: phone.trim(),
      address: `${addrForm.value.province}${addrForm.value.city}${addrForm.value.district}${detail.trim()}`,
      isDefault: addrForm.value.isDefault,
      city: addrForm.value.city,
      district: addrForm.value.district,
    })
    const newAddr = r.data || r
    addresses.value.push(newAddr)
    selectedAddrId.value = newAddr.id
    showAddrEditor.value = false
    addrForm.value = { name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false }
  } catch {
    toast.error(t('common.updateFailed'))
  } finally {
    savingAddr.value = false
  }
}

onMounted(async () => {
  if (!isLoggedIn.value) { router.push('/login?redirect=/checkout'); return }
  await loadData()
})
</script>

<template>
  <div class="chk-page">
    <div class="chk-header">
      <button class="chk-back" @click="router.push('/cart')">← {{ t('checkout.back') }}</button>
      <h1 class="chk-title">{{ t('checkout.title') }}</h1>
    </div>

    <SkeletonBox v-if="loading" variant="list-item" :count="4" height="80px" gap="var(--space-md)" />

    <template v-else-if="cartItems.length === 0">
      <JdEmpty icon="🛒" :title="t('checkout.emptyTitle')" :description="t('checkout.emptyDesc')">
        <JdButton @click="router.push('/products')">{{ t('checkout.goBrowse') }}</JdButton>
      </JdEmpty>
    </template>

    <template v-else>
      <div class="chk-body">
        <div class="chk-main">
          <!-- Address -->
          <section class="chk-section">
            <h3 class="chk-sec-title">{{ t('checkout.address') }}</h3>
            <div v-if="addresses.length === 0" class="chk-no-addr">
              <p>{{ t('checkout.noAddress') }}</p>
              <JdButton size="sm" @click="showAddrEditor = true">{{ t('checkout.addAddress') }}</JdButton>
            </div>
            <div v-else class="chk-addr-list">
              <div
                v-for="a in addresses" :key="a.id"
                class="chk-addr-item"
                :class="{ selected: selectedAddrId === a.id }"
                role="radio"
                :aria-checked="selectedAddrId === a.id"
                tabindex="0"
                @click="selectedAddrId = a.id"
                @keydown.enter.prevent="selectedAddrId = a.id"
                @keydown.space.prevent="selectedAddrId = a.id"
              >
                <div class="chk-addr-check">{{ selectedAddrId === a.id ? '●' : '○' }}</div>
                <div class="chk-addr-body">
                  <div class="chk-addr-line1">
                    <strong>{{ a.name }}</strong>
                    <span class="chk-addr-phone">{{ a.phone }}</span>
                    <span v-if="a.isDefault" class="chk-addr-tag">{{ t('checkout.default') }}</span>
                  </div>
                  <div class="chk-addr-line2">{{ a.address || `${a.province || ''}${a.city || ''}${a.district || ''}${a.detail || ''}` }}</div>
                </div>
              </div>
              <JdButton size="sm" type="outline" @click="showAddrEditor = true">+ {{ t('checkout.newAddress') }}</JdButton>
            </div>
          </section>

          <!-- Cart items -->
          <section class="chk-section">
            <h3 class="chk-sec-title">{{ t('checkout.orderItems') }}</h3>
            <div class="chk-items">
              <div v-for="item in cartItems" :key="item.id" class="chk-item">
                <LazyImage :src="item.imageUrl || item.productImage || ''" :alt="item.productName" height="80px" width="80px" rounded="8px" />
                <div class="chk-item-info">
                  <div class="chk-item-name">{{ item.productName }}</div>
                  <div class="chk-item-qty">x{{ item.quantity || 1 }}</div>
                </div>
                <div class="chk-item-price">{{ formatPrice((item.price || 0) / 100) }}</div>
              </div>
            </div>
          </section>

          <!-- Coupon -->
          <section class="chk-section">
            <h3 class="chk-sec-title">{{ t('checkout.coupon') }}</h3>
            <div v-if="availableCoupons.length" class="chk-coupon-list">
              <div
                v-for="c in availableCoupons" :key="c.id"
                class="chk-coupon-item"
                :class="{ selected: selectedCouponId === c.id }"
                role="radio"
                :aria-checked="selectedCouponId === c.id"
                tabindex="0"
                @click="selectedCouponId = selectedCouponId === c.id ? null : c.id"
                @keydown.enter.prevent="selectedCouponId = selectedCouponId === c.id ? null : c.id"
                @keydown.space.prevent="selectedCouponId = selectedCouponId === c.id ? null : c.id"
              >
                <div class="chk-coupon-left">
                  <div class="chk-coupon-amount">{{ formatPrice(c.amount / 100) }}</div>
                  <div v-if="c.minAmount" class="chk-coupon-min">{{ t('checkout.minAmount', { n: String(Math.ceil((c.minAmount || 0) / 100)) }) }}</div>
                </div>
                <div class="chk-coupon-body">
                  <div class="chk-coupon-name">{{ c.name }}</div>
                  <div v-if="c.expireAt" class="chk-coupon-expire">{{ t('checkout.expiresAt', { d: c.expireAt }) }}</div>
                </div>
              </div>
            </div>
            <div v-else class="chk-no-coupon">{{ t('checkout.noCoupon') }}</div>
          </section>

          <!-- Remark -->
          <section class="chk-section">
            <h3 class="chk-sec-title">{{ t('checkout.remark') }}</h3>
            <textarea v-model="remark" class="chk-remark" :placeholder="t('checkout.remarkPlaceholder')" maxlength="200" />
          </section>
        </div>

        <!-- Right: order summary -->
        <aside class="chk-sidebar">
          <div class="chk-summary">
            <h3 class="chk-sum-title">{{ t('checkout.summary') }}</h3>
            <div class="chk-sum-row">
              <span>{{ t('checkout.subtotal') }}</span>
              <span>{{ formatPrice(subtotal / 100) }}</span>
            </div>
            <div v-if="discount" class="chk-sum-row chk-sum-discount">
              <span>{{ t('checkout.discount') }}</span>
              <span>-{{ formatPrice(discount / 100) }}</span>
            </div>
            <div v-if="pointsDiscount > 0" class="chk-sum-row chk-sum-discount">
              <span>{{ t('checkout.pointsDeductDetail') }}</span>
              <span>-{{ formatPrice(pointsDiscount / 100) }}</span>
            </div>
            <div class="chk-sum-row">
              <span>{{ t('checkout.shipping') }}</span>
              <span class="chk-ship-free">{{ t('checkout.freeShipping') }}</span>
            </div>
            <div class="chk-sum-divider" />
            <div class="chk-sum-row chk-sum-total">
              <span>{{ t('checkout.total') }}</span>
              <span class="chk-total-price">{{ formatPrice(total / 100) }}</span>
            </div>

            <!-- Points redeem -->
            <div v-if="pointsBalance > 0" class="chk-points">
              <p class="chk-points-info">{{ t('checkout.pointsAvailable', { n: pointsBalance }) }}</p>
              <div class="chk-points-input-row">
                <input v-model.number="pointsToUse" type="number" :max="maxPointsDiscount" :placeholder="t('checkout.pointsPlaceholder')" class="chk-points-input" />
                <span class="chk-points-hint">= {{ t('checkout.pointsValue', { n: String(Math.floor(pointsToUse / pointsRate)) }) }}</span>
              </div>
            </div>

            <div class="chk-pay-methods">
              <label class="chk-pay-method" :class="{ selected: payMethod === 'wechat' }">
                <input type="radio" v-model="payMethod" value="wechat" class="chk-pay-radio" />
                <span>💚 {{ t('checkout.wechatPay') }}</span>
              </label>
              <label class="chk-pay-method" :class="{ selected: payMethod === 'alipay' }">
                <input type="radio" v-model="payMethod" value="alipay" class="chk-pay-radio" />
                <span>💙 {{ t('checkout.alipay') }}</span>
              </label>
            </div>

            <label class="chk-agree">
              <input type="checkbox" v-model="agreedToTerms" />
              <span>{{ t('checkout.agreeTerms') }}</span>
            </label>

            <JdButton block :loading="submitting" :disabled="!canSubmit" class="chk-submit-btn" @click="doSubmit">
              {{ submitting ? t('checkout.submitting') : t('checkout.submitOrder') }}
            </JdButton>

            <p v-if="!canSubmit && !submitting" class="chk-hint">
              {{ !selectedAddrId ? t('checkout.selectAddressHint') : !agreedToTerms ? t('checkout.agreeHint') : '' }}
            </p>
          </div>
        </aside>
      </div>
    </template>

    <!-- Address editor modal -->
    <JdModal :visible="showAddrEditor" :title="t('checkout.newAddress')" @close="showAddrEditor = false">
      <div class="chk-addr-form">
        <input v-model="addrForm.name" :placeholder="t('checkout.namePlaceholder')" class="chk-addr-input" maxlength="30" aria-label="Receiver name" />
        <input v-model="addrForm.phone" :placeholder="t('checkout.phonePlaceholder')" class="chk-addr-input" maxlength="20" aria-label="Phone number" />
        <div class="chk-addr-row">
          <input v-model="addrForm.province" :placeholder="t('checkout.province')" class="chk-addr-input chk-addr-third" maxlength="20" />
          <input v-model="addrForm.city" :placeholder="t('checkout.city')" class="chk-addr-input chk-addr-third" maxlength="20" />
          <input v-model="addrForm.district" :placeholder="t('checkout.district')" class="chk-addr-input chk-addr-third" maxlength="20" />
        </div>
        <input v-model="addrForm.detail" :placeholder="t('checkout.detailPlaceholder')" class="chk-addr-input" maxlength="100" aria-label="Detail address" />
        <label class="chk-addr-default">
          <input type="checkbox" v-model="addrForm.isDefault" />
          <span>{{ t('checkout.setDefault') }}</span>
        </label>
        <div class="chk-addr-form-actions">
          <JdButton type="outline" @click="showAddrEditor = false">{{ t('common.cancel') }}</JdButton>
          <JdButton @click="saveNewAddress">{{ t('common.save') }}</JdButton>
        </div>
      </div>
    </JdModal>
  </div>
</template>

<style scoped>
.chk-page { max-width: 1000px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }
.chk-header { margin-bottom: var(--space-xl); }
.chk-back { background: none; border: none; color: var(--text-secondary); cursor: pointer; font-size: var(--font-md); padding: 0; }
.chk-back:hover { color: var(--jd-red); }
.chk-title { font-size: var(--font-xl); font-weight: 700; margin-top: var(--space-sm); }

.chk-body { display: flex; gap: var(--space-xl); align-items: flex-start; }
.chk-main { flex: 1; min-width: 0; }
.chk-sidebar { width: 340px; flex-shrink: 0; position: sticky; top: var(--space-xl); }

.chk-section { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); margin-bottom: var(--space-lg); box-shadow: var(--shadow-sm); }
.chk-sec-title { font-size: var(--font-md); font-weight: 700; margin-bottom: var(--space-lg); }

.chk-no-addr { text-align: center; padding: var(--space-lg); color: var(--text-tertiary); }
.chk-no-addr p { margin-bottom: var(--space-sm); }
.chk-addr-list { display: flex; flex-direction: column; gap: var(--space-sm); }
.chk-addr-item {
  display: flex; gap: var(--space-md); padding: var(--space-md);
  border: 2px solid var(--border); border-radius: var(--radius-md); cursor: pointer;
  transition: border-color var(--transition-fast);
}
.chk-addr-item.selected { border-color: var(--jd-red); }
.chk-addr-item:hover:not(.selected) { border-color: var(--text-tertiary); }
.chk-addr-check { color: var(--jd-red); font-size: var(--font-lg); flex-shrink: 0; padding-top: 2px; }
.chk-addr-body { flex: 1; }
.chk-addr-line1 { display: flex; gap: var(--space-sm); align-items: center; margin-bottom: 4px; font-size: var(--font-sm); }
.chk-addr-phone { color: var(--text-secondary); }
.chk-addr-tag { background: var(--jd-red); color: #fff; font-size: 11px; padding: 1px 8px; border-radius: var(--radius-round); }
.chk-addr-line2 { font-size: var(--font-sm); color: var(--text-secondary); }

.chk-items { display: flex; flex-direction: column; gap: var(--space-md); }
.chk-item { display: flex; gap: var(--space-md); align-items: center; }
.chk-item-info { flex: 1; }
.chk-item-name { font-size: var(--font-sm); font-weight: 500; margin-bottom: 4px; }
.chk-item-qty { font-size: var(--font-xs); color: var(--text-tertiary); }
.chk-item-price { color: var(--jd-red); font-weight: 700; font-size: var(--font-md); }

.chk-coupon-list { display: flex; flex-direction: column; gap: var(--space-sm); }
.chk-coupon-item {
  display: flex; gap: var(--space-md); padding: var(--space-md);
  border: 2px solid var(--border); border-radius: var(--radius-md); cursor: pointer;
  transition: border-color var(--transition-fast);
}
.chk-coupon-item.selected { border-color: var(--jd-red); }
.chk-coupon-item:hover:not(.selected) { border-color: var(--text-tertiary); }
.chk-coupon-left {
  flex-shrink: 0; width: 80px; text-align: center;
  border-right: 1px dashed var(--border); padding-right: var(--space-md);
}
.chk-coupon-amount { color: var(--jd-red); font-size: var(--font-xl); font-weight: 700; }
.chk-coupon-min { font-size: 11px; color: var(--text-tertiary); margin-top: 2px; }
.chk-coupon-body { flex: 1; }
.chk-coupon-name { font-weight: 600; font-size: var(--font-sm); }
.chk-coupon-expire { font-size: var(--font-xs); color: var(--text-tertiary); margin-top: 2px; }
.chk-no-coupon { color: var(--text-tertiary); font-size: var(--font-sm); }

.chk-remark { width: 100%; min-height: 80px; padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md); resize: vertical; font-size: var(--font-sm); font-family: inherit; color: var(--text-primary); background: var(--bg-white); box-sizing: border-box; }
.chk-remark:focus { border-color: var(--jd-red); outline: none; }

.chk-summary { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); box-shadow: var(--shadow-sm); }
.chk-sum-title { font-size: var(--font-md); font-weight: 700; margin-bottom: var(--space-lg); }
.chk-sum-row { display: flex; justify-content: space-between; margin-bottom: var(--space-sm); font-size: var(--font-sm); }
.chk-sum-discount { color: var(--jd-red); }
.chk-ship-free { color: var(--green); }
.chk-sum-divider { border-top: 1px solid var(--border); margin: var(--space-md) 0; }
.chk-sum-total { font-size: var(--font-md); font-weight: 700; }
.chk-total-price { color: var(--jd-red); font-size: var(--font-xl); }
.chk-points { margin-top: var(--space-lg); padding: var(--space-md); background: var(--bg-hover); border-radius: var(--radius-md); }
.chk-points-info { font-size: var(--font-sm); color: var(--text-secondary); margin-bottom: var(--space-xs); }
.chk-points-input-row { display: flex; align-items: center; gap: var(--space-sm); }
.chk-points-input { width: 100px; padding: 6px 10px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--font-sm); text-align: center; background: var(--bg-white); color: var(--text-primary); box-sizing: border-box; }
.chk-points-hint { font-size: var(--font-xs); color: var(--text-placeholder); }

.chk-pay-methods { margin-top: var(--space-lg); }
.chk-pay-method {
  display: flex; align-items: center; gap: var(--space-sm);
  padding: var(--space-md); border: 2px solid var(--border); border-radius: var(--radius-md);
  cursor: pointer; margin-bottom: var(--space-sm); transition: border-color var(--transition-fast);
}
.chk-pay-method.selected { border-color: var(--jd-red); }
.chk-pay-radio { display: none; }
.chk-agree { display: flex; align-items: center; gap: var(--space-sm); margin-top: var(--space-lg); font-size: var(--font-sm); color: var(--text-secondary); cursor: pointer; }
.chk-submit-btn { margin-top: var(--space-lg); }
.chk-hint { font-size: var(--font-xs); color: var(--text-tertiary); margin-top: var(--space-sm); text-align: center; }

.chk-addr-form { display: flex; flex-direction: column; gap: var(--space-md); }
.chk-addr-input { width: 100%; padding: var(--space-sm) var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md); font-size: var(--font-md); color: var(--text-primary); background: var(--bg-white); box-sizing: border-box; }
.chk-addr-input:focus { border-color: var(--jd-red); outline: none; }
.chk-addr-row { display: flex; gap: var(--space-sm); }
.chk-addr-third { flex: 1; }
.chk-addr-default { display: flex; align-items: center; gap: var(--space-sm); font-size: var(--font-sm); color: var(--text-secondary); cursor: pointer; }
.chk-addr-form-actions { display: flex; gap: var(--space-md); justify-content: flex-end; margin-top: var(--space-sm); }

@media (max-width: 768px) {
  .chk-page { padding: var(--space-lg) var(--space-md) calc(80px + env(safe-area-inset-bottom, 0px)); }
  .chk-body { flex-direction: column-reverse; }
  .chk-sidebar { width: 100%; position: static; }
}
</style>
