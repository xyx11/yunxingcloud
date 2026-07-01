<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/api/request'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const records = ref<any[]>([])
const loading = ref(true)
const showForm = ref(false)
const submitting = ref(false)
const form = ref({ orderNo: '', type: 'refund', reason: '', amount: '', description: '' })

const types = [
  { value: 'refund', label: '仅退款', icon: '💰', desc: '未收到货或与商家协商退款' },
  { value: 'return', label: '退货退款', icon: '📦', desc: '已收到货，需要退货并退款' },
  { value: 'exchange', label: '换货', icon: '🔄', desc: '商品有问题，需要更换' },
]

const statusMap: Record<string, { label: string; color: string }> = {
  '0': { label: '待审核', color: '#ff9800' },
  '1': { label: '已通过', color: '#4caf50' },
  '2': { label: '已拒绝', color: '#f44336' },
  '3': { label: '退款中', color: '#1677ff' },
  '4': { label: '已完成', color: '#4caf50' },
}

onMounted(async () => {
  try { const r = await request.get('/after-sale'); records.value = r.data || [] } catch {}
  finally { loading.value = false }
})

async function submit() {
  if (!form.value.orderNo || !form.value.reason) { toast.error('请填写完整信息'); return }
  submitting.value = true
  try { await request.post('/after-sale', form.value); toast.success('售后申请已提交'); showForm.value = false; records.value.unshift({ ...form.value, status: '0', createdAt: new Date().toISOString() }) }
  catch { toast.error('提交失败') }
  finally { submitting.value = false }
}
</script>

<template>
  <div style="max-width:700px;margin:0 auto">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 style="font-size:20px;font-weight:700">🛡️ 售后申请</h2>
      <button @click="showForm=true" style="padding:8px 20px;background:#e4393c;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:13px;font-weight:600">+ 新建申请</button>
    </div>

    <div v-if="showForm" style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06);margin-bottom:16px">
      <div style="margin-bottom:16px">
        <label style="font-size:13px;color:#666;margin-bottom:8px;display:block">售后类型</label>
        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:8px">
          <div v-for="t in types" :key="t.value" @click="form.type=t.value"
               style="padding:12px;border-radius:8px;cursor:pointer;text-align:center;transition:all .2s"
               :style="{border:form.type===t.value?'2px solid #e4393c':'1px solid #eee',background:form.type===t.value?'#fff5f5':'#fff'}">
            <div style="font-size:24px;margin-bottom:4px">{{ t.icon }}</div>
            <div style="font-size:13px;font-weight:600;margin-bottom:2px">{{ t.label }}</div>
            <div style="font-size:10px;color:#999">{{ t.desc }}</div>
          </div>
        </div>
      </div>
      <input v-model="form.orderNo" placeholder="订单号" style="width:100%;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;box-sizing:border-box;margin-bottom:12px" />
      <input v-model="form.amount" placeholder="退款金额 (¥)" type="number" style="width:100%;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;box-sizing:border-box;margin-bottom:12px" />
      <textarea v-model="form.reason" placeholder="请描述售后原因..." style="width:100%;height:80px;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;resize:none;box-sizing:border-box;margin-bottom:12px"></textarea>
      <div style="display:flex;gap:8px;justify-content:flex-end">
        <button @click="showForm=false" style="padding:8px 20px;border:1px solid #ddd;background:#fff;border-radius:6px;cursor:pointer;font-size:13px">取消</button>
        <button @click="submit" :disabled="submitting" style="padding:8px 20px;background:#e4393c;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:13px;font-weight:600">{{ submitting?'提交中...':'提交申请' }}</button>
      </div>
    </div>

    <div v-if="loading" style="display:flex;flex-direction:column;gap:12px">
      <div v-for="i in 3" :key="i" style="background:#fff;border-radius:12px;padding:20px;box-shadow:0 2px 8px rgba(0,0,0,.06);height:80px">
        <div style="height:16px;width:50%;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:4px;margin-bottom:12px"></div>
      </div>
    </div>
    <div v-else-if="records.length">
      <div v-for="r in records" :key="r.id || r.orderNo" style="background:#fff;border-radius:12px;padding:20px;margin-bottom:12px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
        <div style="display:flex;justify-content:space-between;align-items:start;margin-bottom:10px">
          <div>
            <span style="font-size:13px;color:#999">订单 {{ r.orderNo }}</span>
            <span style="margin-left:12px;font-size:13px;font-weight:600">{{ ({refund:'仅退款',return:'退货退款',exchange:'换货'} as any)[r.type] }}</span>
          </div>
          <span style="padding:2px 10px;border-radius:12px;font-size:12px;font-weight:600;color:#fff" :style="{background:statusMap[r.status]?.color}">{{ statusMap[r.status]?.label }}</span>
        </div>
        <div style="color:#666;font-size:13px">{{ r.reason }}</div>
        <div v-if="r.amount" style="color:#e4393c;font-weight:700;margin-top:4px">¥{{ (Number(r.amount)/100).toFixed(2) }}</div>
        <div style="color:#aaa;font-size:11px;margin-top:6px">{{ r.createdAt?.substring(0,16) }}</div>
      </div>
    </div>
    <div v-else style="background:#fff;border-radius:12px;padding:60px;text-align:center;color:#999;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <p style="font-size:48px;margin-bottom:12px">🛡️</p><p>暂无售后记录</p>
    </div>
  </div>
</template>
<style scoped>@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }</style>