<script setup lang="ts">
import { ref, onMounted, onUnmounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderById, payOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()
const toast = inject<any>('toast')
const order = ref<any>(null)
const selectedChannel = ref<'wechat' | 'alipay'>('wechat')
const paying = ref(false)
const polling = ref(false)
const pollCount = ref(0)
let pollTimer: any = null

// 15分钟支付倒计时
const minutes = ref(15); const seconds = ref(0)
let countdownTimer: any = null
function startCountdown() {
  countdownTimer = setInterval(() => {
    if (seconds.value > 0) seconds.value--
    else if (minutes.value > 0) { minutes.value--; seconds.value = 59 }
    else { clearInterval(countdownTimer) }
  }, 1000)
}
startCountdown()

onMounted(async () => {
  try {
    const r = await getOrderById(Number(route.params.id))
    order.value = r.data.order
    if (order.value?.status !== '0') { toast.error('订单状态异常'); router.replace('/orders') }
  } catch { router.replace('/orders') }
})

function startPolling() {
  polling.value = true; pollCount.value = 0
  pollTimer = setInterval(async () => {
    pollCount.value++
    try {
      const r = await getOrderById(Number(route.params.id))
      if (r.data.order?.status === '1' || r.data.order?.status === '2' || r.data.order?.status === '3') {
        clearInterval(pollTimer); polling.value = false
        router.replace(`/order/${route.params.id}/result?status=success`)
      } else if (pollCount.value > 30) {
        clearInterval(pollTimer); polling.value = false
        router.replace(`/order/${route.params.id}/result?status=fail`)
      }
    } catch {}
  }, 2000)
}

async function confirmPay() {
  paying.value = true
  try {
    await payOrder(order.value.id, selectedChannel.value)
    startPolling()
  } catch {
    paying.value = false
    router.replace(`/order/${route.params.id}/result?status=fail`)
  }
}

onUnmounted(() => { if (pollTimer) clearInterval(pollTimer); if (countdownTimer) clearInterval(countdownTimer) })
</script>

<template>
  <div style="max-width:500px;margin:30px auto">
    <div style="background:#fff;border-radius:12px;padding:32px;box-shadow:0 2px 12px rgba(0,0,0,.06)">
      <template v-if="polling">
        <div style="text-align:center;padding:40px 0">
          <div style="font-size:48px;margin-bottom:16px;animation:spin 1s linear infinite">⏳</div>
          <h3 style="font-size:18px;margin-bottom:8px">支付处理中...</h3>
          <p style="color:#999;font-size:13px">正在确认支付结果，请稍候 ({{ pollCount }}/30)</p>
        </div>
      </template>
      <template v-else>
        <h2 style="font-size:20px;font-weight:700;margin-bottom:8px">选择支付方式</h2>
        <div v-if="order" style="color:#999;font-size:13px;margin-bottom:8px">
          订单 {{ order.orderNo }} · 应付 <b style="color:#e4393c;font-size:18px">¥{{ (order.totalAmount/100).toFixed(2) }}</b>
        </div>
        <div style="background:#fff3cd;border-radius:6px;padding:8px 12px;margin-bottom:16px;text-align:center;font-size:13px;color:#856404">
          ⏱ 请在 {{ String(minutes).padStart(2,'0') }}:{{ String(seconds).padStart(2,'0') }} 内完成支付，超时订单将自动取消
        </div>
        <div style="margin-bottom:24px">
          <div @click="selectedChannel='wechat'" style="display:flex;align-items:center;padding:16px;border-radius:8px;margin-bottom:12px;cursor:pointer;transition:all .2s"
               :style="{border:selectedChannel==='wechat'?'2px solid #e4393c':'1px solid #eee',background:selectedChannel==='wechat'?'#fff5f5':'#fff'}">
            <div style="width:32px;height:32px;background:#07c160;border-radius:8px;display:flex;align-items:center;justify-content:center;font-size:18px;margin-right:12px">💬</div>
            <div style="flex:1"><div style="font-weight:600;font-size:15px">微信支付</div><div style="color:#999;font-size:12px">微信安全支付</div></div>
            <div style="width:18px;height:18px;border-radius:50%;border:2px solid #e4393c;display:flex;align-items:center;justify-content:center"
                 :style="{background:selectedChannel==='wechat'?'#e4393c':''}"><span v-if="selectedChannel==='wechat'" style="color:#fff;font-size:11px">✓</span></div>
          </div>
          <div @click="selectedChannel='alipay'" style="display:flex;align-items:center;padding:16px;border-radius:8px;cursor:pointer;transition:all .2s"
               :style="{border:selectedChannel==='alipay'?'2px solid #e4393c':'1px solid #eee',background:selectedChannel==='alipay'?'#fff5f5':'#fff'}">
            <div style="width:32px;height:32px;background:#1677ff;border-radius:8px;display:flex;align-items:center;justify-content:center;font-size:18px;margin-right:12px">💳</div>
            <div style="flex:1"><div style="font-weight:600;font-size:15px">支付宝</div><div style="color:#999;font-size:12px">支付宝安全支付</div></div>
            <div style="width:18px;height:18px;border-radius:50%;border:2px solid #e4393c;display:flex;align-items:center;justify-content:center"
                 :style="{background:selectedChannel==='alipay'?'#e4393c':''}"><span v-if="selectedChannel==='alipay'" style="color:#fff;font-size:11px">✓</span></div>
          </div>
        </div>
        <button @click="confirmPay" :disabled="paying"
                style="width:100%;height:48px;background:#e4393c;color:#fff;border:none;border-radius:8px;font-size:16px;cursor:pointer;font-weight:700;transition:background .2s"
                @mouseenter="(e:any) => e.target.style.background='#c82930'" @mouseleave="(e:any) => e.target.style.background='#e4393c'">
          {{ paying ? '处理中...' : `确认支付 ¥${(order?.totalAmount/100).toFixed(2)}` }}
        </button>
        <p style="text-align:center;margin-top:16px;font-size:12px;color:#999">安全支付 · 数据加密传输</p>
      </template>
    </div>
  </div>
</template>
<style scoped>
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>