<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/api/request'

const route = useRoute()
const traces = ref<any[]>([])
const trackingNo = ref('')

onMounted(async () => {
  const orderId = route.query.orderId
  if (orderId) {
    try { const r = await request.get('/logistics/order/' + orderId); traces.value = r.data || [] } catch {}
  }
})

async function track() { if(!trackingNo.value.trim())return; try{const r=await request.get('/logistics/track/'+trackingNo.value.trim());traces.value=r.data||[]}catch{} }
</script>
<template>
  <div style="max-width:600px;margin:0 auto">
    <div style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <h2 style="font-size:20px;font-weight:700;margin-bottom:16px">物流追踪</h2>
      <div style="display:flex;gap:8px;margin-bottom:20px">
        <input v-model="trackingNo" placeholder="输入快递单号" style="flex:1;padding:10px;border:1px solid #ddd;border-radius:8px;font-size:14px" />
        <button @click="track" style="padding:10px 20px;background:#e4393c;color:#fff;border:none;border-radius:8px;cursor:pointer">查询</button>
      </div>
      <div v-if="traces.length">
        <div v-for="(t,i) in traces" :key="t.id" style="display:flex;gap:16px;position:relative">
          <div style="display:flex;flex-direction:column;align-items:center">
            <div style="width:12px;height:12px;border-radius:50%" :style="{background:i===0?'#e4393c':'#ddd'}"></div>
            <div v-if="i<traces.length-1" style="width:2px;flex:1;background:#eee;min-height:30px"></div>
          </div>
          <div style="padding-bottom:20px">
            <div style="font-weight:600;font-size:14px">{{ t.status }}</div>
            <div style="color:#999;font-size:12px">{{ t.location }} · {{ t.traceTime?.substring(0,16) }}</div>
            <div style="font-size:13px;margin-top:4px">{{ t.description }}</div>
          </div>
        </div>
      </div>
      <div v-else style="text-align:center;color:#999;padding:40px">暂无物流信息</div>
    </div>
  </div>
</template>