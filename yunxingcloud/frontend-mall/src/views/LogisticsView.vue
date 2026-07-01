<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/api/request'

const route = useRoute()
const toast = inject<any>('toast')
const traces = ref<any[]>([])
const trackingNo = ref('')
const carrier = ref('')
const copying = ref(false)

onMounted(async () => {
  const orderId = route.query.orderId
  if (orderId) {
    try {
      const r = await request.get('/logistics/order/' + orderId)
      traces.value = r.data?.traces || r.data || []
      carrier.value = r.data?.carrier || ''
      trackingNo.value = r.data?.trackingNo || ''
    } catch {}
  }
})

async function track() { if(!trackingNo.value.trim())return; try{const r=await request.get('/logistics/track/'+trackingNo.value.trim());traces.value=r.data||[]}catch{} }
async function copyNo() { try { await navigator.clipboard.writeText(trackingNo.value); copying.value = true; toast.success('已复制'); setTimeout(() => copying.value = false, 1500) } catch {} }
</script>
<template>
  <div style="max-width:600px;margin:0 auto">
    <div style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <h2 style="font-size:20px;font-weight:700;margin-bottom:16px">物流追踪</h2>
      <div style="display:flex;gap:8px;margin-bottom:20px">
        <input v-model="trackingNo" placeholder="输入快递单号" style="flex:1;padding:10px;border:1px solid #ddd;border-radius:8px;font-size:14px" />
        <button @click="track" style="padding:10px 20px;background:#f10215;color:#fff;border:none;border-radius:8px;cursor:pointer">查询</button>
      </div>
      <div v-if="carrier || trackingNo" style="background:#f9f9f9;border-radius:8px;padding:12px 16px;margin-bottom:16px;display:flex;justify-content:space-between;align-items:center">
        <div>
          <span style="font-size:13px;color:#666">快递公司：<b>{{ carrier || '未知' }}</b></span>
          <span style="margin-left:16px;font-size:13px;color:#666">单号：<b>{{ trackingNo }}</b></span>
        </div>
        <button @click="copyNo" style="padding:4px 12px;border:1px solid #ddd;background:#fff;border-radius:4px;cursor:pointer;font-size:12px;white-space:nowrap"
                :style="{background:copying?'#f10215':'#fff',color:copying?'#fff':'#666'}">{{ copying ? '✓ 已复制' : '复制单号' }}</button>
      </div>
      <div v-if="traces.length">
        <div v-for="(t,i) in traces" :key="t.id || i" style="display:flex;gap:16px;position:relative">
          <div style="display:flex;flex-direction:column;align-items:center">
            <div style="width:14px;height:14px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:8px;color:#fff" :style="{background:i===0?'#f10215':'#ddd'}">{{ i===0 ? '✓' : '' }}</div>
            <div v-if="i<traces.length-1" style="width:2px;flex:1;min-height:30px" :style="{background:i===0?'#f10215':'#eee'}"></div>
          </div>
          <div style="padding-bottom:24px;flex:1">
            <div style="font-weight:600;font-size:14px" :style="{color:i===0?'#f10215':'#333'}">{{ t.status || t.description }}</div>
            <div style="color:#999;font-size:12px;margin-top:2px">{{ t.location || '' }}<span v-if="t.traceTime"> · {{ t.traceTime?.substring(0,16) }}</span></div>
          </div>
          <div style="font-size:12px;color:#aaa;white-space:nowrap">{{ t.traceTime?.substring(11,16) }}</div>
        </div>
      </div>
      <div v-else style="text-align:center;color:#999;padding:40px">暂无物流信息</div>
    </div>
  </div>
</template>