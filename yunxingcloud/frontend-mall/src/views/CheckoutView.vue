<script setup lang="ts">
import { ref, onMounted, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getCart } from '@/api/cart'
import { submitOrder, getAddresses } from '@/api/order'
import { useI18n } from '@/locales'

const router = useRouter()
const { t } = useI18n()
const toast = inject<any>('toast')
const items = ref<any[]>([])
const addresses = ref<any[]>([])
const loading = ref(true)
const total = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))
const receiver = ref({ name: '', phone: '', address: '' })
const selectedAddr = ref<any>(null)
const couponCode = ref('')
const couponApplied = ref(false)
const paymentMethod = ref('wechat')
const submitting = ref(false)

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

function applyCoupon() { if (!couponCode.value.trim()) return; couponApplied.value = true; toast.success('优惠券已使用') }
async function submit() {
  if (!receiver.value.name || !receiver.value.phone || !receiver.value.address) { toast.error(t('checkout.selectAddress')); return }
  submitting.value = true
  try { await submitOrder({ ...receiver.value, couponCode: couponCode.value, paymentMethod: paymentMethod.value }); toast.success(t('toast.orderPlaced')); router.push('/orders') }
  catch (e: any) { toast.error(e.response?.data?.message || t('toast.orderFail')) }
  finally { submitting.value = false }
}

onMounted(load)
</script>

<template>
  <div style="max-width:800px;margin:0 auto">
    <h2 style="font-size:20px;font-weight:700;margin-bottom:20px">{{ t('checkout.title') }}</h2>
    <div v-if="loading" style="background:#fff;border-radius:12px;padding:32px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <div v-for="i in 3" :key="i" style="height:20px;margin-bottom:16px;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:4px" :style="{width: (100-i*20)+'%'}"></div>
    </div>
    <template v-else>
      <div style="background:#fff;border-radius:12px;padding:24px;margin-bottom:16px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
        <h3 style="font-size:16px;font-weight:600;margin-bottom:12px">{{ t('checkout.receiverInfo') }}</h3>
        <div v-if="addresses.length" style="margin-bottom:16px">
          <div v-for="addr in addresses" :key="addr.id" @click="selectAddress(addr)"
               style="padding:12px;border-radius:8px;margin-bottom:8px;cursor:pointer;transition:all .2s"
               :style="{border:selectedAddr?.id===addr.id?'2px solid #f10215':'1px solid #eee',background:selectedAddr?.id===addr.id?'#fff5f5':'#fff'}">
            <div style="font-weight:600;margin-bottom:4px">{{ addr.name }} <span style="color:#999;font-weight:400">{{ addr.phone }}</span></div>
            <div style="color:#666;font-size:13px">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
              <span v-if="addr.isDefault" style="color:#f10215;font-size:11px;margin-left:8px">[{{ t('profile.defaultAddr') }}]</span></div>
          </div>
        </div>
        <div v-if="!selectedAddr" style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
          <input v-model="receiver.name" :placeholder="t('order.receiverName')" style="padding:10px;border:1px solid #ddd;border-radius:6px;font-size:14px" />
          <input v-model="receiver.phone" :placeholder="t('order.receiverPhone')" style="padding:10px;border:1px solid #ddd;border-radius:6px;font-size:14px" />
        </div>
        <input v-if="!selectedAddr" v-model="receiver.address" :placeholder="t('order.receiverAddress')" style="width:100%;margin-top:12px;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:14px;box-sizing:border-box" />
      </div>
      <div style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
        <h3 style="font-size:16px;font-weight:600;margin-bottom:12px">{{ t('checkout.productList') }}</h3>
        <div v-for="item in items" :key="item.id" style="display:flex;align-items:center;padding:10px 0;border-bottom:1px solid #f5f5f5">
          <div style="width:60px;height:60px;background:#f5f5f5;border-radius:6px;display:flex;align-items:center;justify-content:center;font-size:28px;margin-right:16px;flex-shrink:0">📦</div>
          <span style="flex:1;font-size:14px">{{ item.productName }} × {{ item.quantity }}</span>
          <span style="color:#f10215;font-weight:600;font-size:15px">¥{{ (item.price * item.quantity / 100).toFixed(2) }}</span>
        </div>
        <div style="margin-top:16px;padding-top:16px;border-top:1px solid #f0f0f0">
          <div style="display:flex;gap:8px;margin-bottom:12px">
            <input v-model="couponCode" placeholder="输入优惠券码" style="flex:1;padding:8px 12px;border:1px solid #ddd;border-radius:4px;font-size:13px" />
            <button @click="applyCoupon" style="padding:8px 16px;border:1px solid #f10215;color:#f10215;background:#fff;border-radius:4px;cursor:pointer;font-size:13px;white-space:nowrap" :style="couponApplied?{background:'#f10215',color:'#fff'}:{}">{{ couponApplied ? '✓ 已使用' : '使用' }}</button>
          </div>
          <div style="margin-bottom:12px">
            <span style="font-size:13px;color:#666;margin-right:12px">支付方式</span>
            <div style="display:flex;gap:12px;margin-top:8px">
              <span @click="paymentMethod='wechat'" style="cursor:pointer;padding:8px 16px;border-radius:6px;font-size:13px;transition:all .2s;display:flex;align-items:center;gap:6px"
                    :style="{border:paymentMethod==='wechat'?'2px solid #07c160':'1px solid #ddd',background:paymentMethod==='wechat'?'#f0fff4':'#fff'}">
                <span style="color:#07c160;font-size:18px">💬</span>微信支付
              </span>
              <span @click="paymentMethod='alipay'" style="cursor:pointer;padding:8px 16px;border-radius:6px;font-size:13px;transition:all .2s;display:flex;align-items:center;gap:6px"
                    :style="{border:paymentMethod==='alipay'?'2px solid #1677ff':'1px solid #ddd',background:paymentMethod==='alipay'?'#f0f5ff':'#fff'}">
                <span style="color:#1677ff;font-size:18px">🔵</span>支付宝
              </span>
            </div>
          </div>
        </div>
        <div style="display:flex;justify-content:space-between;align-items:center;padding-top:20px">
          <div style="border-top:1px solid #f0f0f0;padding-top:12px;margin-bottom:12px">
            <div style="display:flex;justify-content:space-between;font-size:13px;color:#666;margin-bottom:4px"><span>商品总额</span><span>¥{{ (total/100).toFixed(2) }}</span></div>
            <div style="display:flex;justify-content:space-between;font-size:13px;color:#666;margin-bottom:4px"><span>优惠</span><span style="color:#f10215">-¥{{ couponApplied ? (total/100*0.1).toFixed(2) : '0.00' }}</span></div>
            <div style="display:flex;justify-content:space-between;font-size:13px;color:#666"><span>运费</span><span style="color:#4caf50">免运费</span></div>
          </div>
          <div style="display:flex;justify-content:space-between;align-items:center;padding-top:12px">
            <span style="font-size:14px;color:#666">{{ t('checkout.totalItems', { n: items.length }) }}</span>
            <div><span style="font-size:16px;color:#666;margin-right:12px">{{ t('checkout.actualPay') }}：</span><span style="font-size:28px;color:#f10215;font-weight:700">¥{{ (total / 100).toFixed(2) }}</span></div>
          </div>
        </div>
        <button @click="submit" :disabled="submitting" style="width:100%;height:50px;background:#f10215;color:#fff;border:none;border-radius:8px;font-size:18px;cursor:pointer;font-weight:700;margin-top:20px;transition:background .2s"
                @mouseenter="(e:any) => { if(!submitting) e.target.style.background='#d4000f' }" @mouseleave="(e:any) => { if(!submitting) e.target.style.background='#f10215' }"
                :style="{opacity:submitting?'.7':'1',cursor:submitting?'not-allowed':'pointer'}">{{ submitting ? '提交中...' : t('order.submitOrder') }}</button>
      </div>
    </template>
  </div>
</template>
<style scoped>@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }</style>