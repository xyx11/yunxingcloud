<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { useI18n } from '@/locales'

const { t } = useI18n()
const account = ref<any>(null)
const records = ref<any[]>([])

onMounted(async () => {
  try { const r = await request.get('/points/account'); account.value = r.data } catch {}
  try { const r = await request.get('/points/records'); records.value = r.data || [] } catch {}
})
</script>
<template>
  <div style="max-width:600px;margin:0 auto">
    <div style="background:linear-gradient(135deg,#e4393c,#ff6b6b);color:#fff;border-radius:12px;padding:32px;margin-bottom:24px;text-align:center">
      <p style="font-size:14px;opacity:.8">可用积分</p>
      <p style="font-size:48px;font-weight:700;margin:8px 0">{{ account?.balance || 0 }}</p>
      <p style="font-size:12px;opacity:.7">累计获得 {{ account?.totalEarned || 0 }} · 已用 {{ account?.totalSpent || 0 }}</p>
    </div>
    <div style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <h3 style="font-size:16px;font-weight:600;margin-bottom:16px">积分明细</h3>
      <div v-if="records.length">
        <div v-for="r in records" :key="r.id" style="display:flex;justify-content:space-between;padding:12px 0;border-bottom:1px solid #f5f5f5">
          <div><div style="font-size:14px">{{ r.remark || r.type }}</div><div style="color:#999;font-size:12px">{{ r.createdAt?.substring(0,10) }}</div></div>
          <span :style="{fontSize:'16px',fontWeight:'600',color:r.amount>0?'#4caf50':'#e4393c'}">{{ r.amount > 0 ? '+' : '' }}{{ r.amount }}</span>
        </div>
      </div>
      <div v-else style="text-align:center;color:#999;padding:20px">暂无积分记录</div>
    </div>
  </div>
</template>