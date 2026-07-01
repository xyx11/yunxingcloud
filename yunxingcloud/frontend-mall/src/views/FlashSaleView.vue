<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import CountdownTimer from '@/components/CountdownTimer.vue'

const router = useRouter()
const sales = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  try { const r = await request.get('/flash-sale'); sales.value = r.data || [] } catch {}
  finally { loading.value = false }
})

function goProduct(id: number) { router.push(`/product/${id}`) }

const isActive = (s: any) => new Date(s.startTime).getTime() <= Date.now() && new Date(s.endTime).getTime() > Date.now()
const stockPct = (s: any) => Math.max(0, Math.round(((s.stock - (s.sold||0)) / Math.max(1, s.stock)) * 100))
</script>

<template>
  <div style="max-width:900px;margin:0 auto">
    <div style="background:linear-gradient(135deg,#1a1a1a,#333);color:#fff;border-radius:16px;padding:32px;margin-bottom:24px;text-align:center;box-shadow:0 4px 20px rgba(0,0,0,.3)">
      <h1 style="font-size:28px;font-weight:800;margin-bottom:4px">⚡ 限时秒杀</h1>
      <p style="font-size:14px;opacity:.7">超值好货，手慢无！</p>
    </div>
    <div v-if="loading" style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px">
      <div v-for="i in 6" :key="i" style="background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.06)">
        <div style="height:200px;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite"></div>
        <div style="padding:16px"><div style="height:16px;width:60%;background:#f0f0f0;border-radius:4px;margin-bottom:8px"></div></div>
      </div>
    </div>
    <div v-else-if="sales.length" style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px">
      <div v-for="s in sales" :key="s.id" @click="goProduct(s.productId)"
           style="background:#fff;border-radius:12px;overflow:hidden;cursor:pointer;box-shadow:0 2px 8px rgba(0,0,0,.06);transition:transform .3s"
           @mouseenter="(e:any) => e.target.style.transform='translateY(-6px)'" @mouseleave="(e:any) => e.target.style.transform=''">
        <div style="height:200px;background:linear-gradient(135deg,#1a1a1a,#333);display:flex;align-items:center;justify-content:center;font-size:64px;position:relative">
          📦
          <div style="position:absolute;top:0;left:0;right:0;background:rgba(0,0,0,.6);color:#fff;text-align:center;padding:6px;font-size:12px;font-weight:600">
            <CountdownTimer :end-time="s.endTime" :label="new Date(s.startTime).getTime() > Date.now() ? '距开始' : '距结束'" compact />
          </div>
          <span style="position:absolute;bottom:10px;left:10px;background:#f10215;color:#fff;font-size:11px;padding:2px 8px;border-radius:4px">{{ stockPct(s) }}%剩余</span>
        </div>
        <div style="padding:16px">
          <h3 style="font-size:15px;font-weight:600;margin-bottom:8px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ s.productName || '秒杀商品' }}</h3>
          <div style="display:flex;align-items:baseline;gap:8px;margin-bottom:5px">
            <span style="color:#f10215;font-size:22px;font-weight:700">¥{{ (s.flashPrice/100).toFixed(2) }}</span>
            <span style="color:#999;font-size:12px;text-decoration:line-through">¥{{ (s.originalPrice/100).toFixed(2) }}</span>
          </div>
          <div style="background:#f5f5f5;border-radius:4px;height:6px;overflow:hidden">
            <div style="height:100%;background:linear-gradient(90deg,#f10215,#ff6b6b);border-radius:4px;transition:width 1s" :style="{width:(100-stockPct(s))+'%'}"></div>
          </div>
          <button style="width:100%;margin-top:12px;padding:8px;background:linear-gradient(90deg,#f10215,#d4000f);color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:13px;font-weight:700"
                  :style="{opacity:isActive(s)?'1':'.5'}" @click.stop="goProduct(s.productId)">
            {{ isActive(s) ? '立即抢购' : '即将开始' }}
          </button>
        </div>
      </div>
    </div>
    <div v-else style="background:#fff;border-radius:12px;padding:60px;text-align:center;color:#999;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <p style="font-size:48px;margin-bottom:12px">⚡</p><p>暂无秒杀活动，敬请期待</p>
    </div>
  </div>
</template>
<style scoped>@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }</style>
