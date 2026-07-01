<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import CountdownTimer from '@/components/CountdownTimer.vue'

const router = useRouter()
const groups = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  try { const r = await request.get('/group-buy'); groups.value = r.data || [] } catch {}
  finally { loading.value = false }
})

function goProduct(id: number) { router.push(`/product/${id}`) }

const progress = (g: any) => Math.min(100, Math.round(((g.currentMembers || 0) / (g.minMembers || 1)) * 100))
</script>

<template>
  <div style="max-width:900px;margin:0 auto">
    <div style="background:linear-gradient(135deg,#e4393c,#ff6b6b);color:#fff;border-radius:16px;padding:32px;margin-bottom:24px;text-align:center;box-shadow:0 4px 20px rgba(228,57,60,.25)">
      <h1 style="font-size:28px;font-weight:800;margin-bottom:8px">👥 拼团专区</h1>
      <p style="font-size:15px;opacity:.9">邀请好友一起拼，享受超低拼团价</p>
    </div>
    <div v-if="loading" style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px">
      <div v-for="i in 6" :key="i" style="background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.06)">
        <div style="height:200px;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite"></div>
        <div style="padding:16px"><div style="height:16px;width:60%;background:#f0f0f0;border-radius:4px;margin-bottom:8px"></div></div>
      </div>
    </div>
    <div v-else-if="groups.length" style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px">
      <div v-for="g in groups" :key="g.id" @click="goProduct(g.productId)"
           style="background:#fff;border-radius:12px;overflow:hidden;cursor:pointer;box-shadow:0 2px 8px rgba(0,0,0,.06);transition:transform .3s"
           @mouseenter="(e:any) => e.target.style.transform='translateY(-6px)'" @mouseleave="(e:any) => e.target.style.transform=''">
        <div style="height:200px;background:linear-gradient(135deg,#f0f0ff,#e8e8ff);display:flex;align-items:center;justify-content:center;font-size:64px;position:relative">
          📦
          <span style="position:absolute;top:10px;left:10px;background:#e4393c;color:#fff;font-size:11px;padding:3px 10px;border-radius:6px;font-weight:600">{{ g.minMembers }}人团</span>
          <span style="position:absolute;top:10px;right:10px;color:#e4393c;font-size:12px;font-weight:700;background:#fff;padding:2px 8px;border-radius:4px"><CountdownTimer :end-time="g.endTime" compact /></span>
        </div>
        <div style="padding:16px">
          <h3 style="font-size:15px;font-weight:600;margin-bottom:8px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ g.productName || '拼团商品' }}</h3>
          <div style="display:flex;align-items:baseline;gap:8px;margin-bottom:10px">
            <span style="color:#e4393c;font-size:22px;font-weight:700">¥{{ (g.groupPrice/100).toFixed(2) }}</span>
            <span style="color:#999;font-size:12px;text-decoration:line-through">¥{{ (g.originalPrice/100).toFixed(2) }}</span>
            <span style="color:#e4393c;font-size:11px;margin-left:auto">{{ g.currentMembers || 0 }}/{{ g.minMembers }}人</span>
          </div>
          <div style="background:#f5f5f5;border-radius:4px;height:6px;overflow:hidden">
            <div style="height:100%;background:linear-gradient(90deg,#e4393c,#ff6b6b);border-radius:4px;transition:width .6s" :style="{width:progress(g)+'%'}"></div>
          </div>
          <button style="width:100%;margin-top:12px;padding:8px;background:#e4393c;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:13px;font-weight:600"
                  @click.stop="goProduct(g.productId)">去开团</button>
        </div>
      </div>
    </div>
    <div v-else style="background:#fff;border-radius:12px;padding:60px;text-align:center;color:#999;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <p style="font-size:48px;margin-bottom:12px">👥</p><p>暂无拼团活动</p>
    </div>
  </div>
</template>
<style scoped>@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }</style>