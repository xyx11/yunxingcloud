<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import request from '@/api/request'
import { useToast } from '@/composables/useToast'

const toast = useToast()
const invoices = ref<any[]>([])
const loading = ref(true)
const showForm = ref(false)
const submitting = ref(false)
const form = ref({ orderNo: '', type: 'personal', title: '', taxNo: '', email: '' })

onMounted(async () => {
  try { const r = await request.get('/invoices'); invoices.value = r.data || [] } catch {}
  finally { loading.value = false }
})

async function submit() {
  if (!form.value.orderNo) { toast.error('请输入订单号'); return }
  if (form.value.type === 'company' && !form.value.title) { toast.error('请输入公司抬头'); return }
  submitting.value = true
  try { await request.post('/invoices', form.value); toast.success('开票申请已提交'); showForm.value = false; load() } catch { toast.error('提交失败') }
  finally { submitting.value = false }
}

async function load() {
  try { const r = await request.get('/invoices'); invoices.value = r.data || [] } catch {}
}

const statusMap: Record<string, { label: string; color: string }> = {
  '0': { label: '处理中', color: '#ff9800' },
  '1': { label: '已开具', color: '#4caf50' },
  '2': { label: '已发送', color: '#1677ff' },
}
</script>

<template>
  <div style="max-width:700px;margin:0 auto">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 style="font-size:20px;font-weight:700">🧾 发票管理</h2>
      <button @click="showForm=true" style="padding:8px 20px;background:#f10215;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:13px;font-weight:600">+ 申请开票</button>
    </div>

    <div v-if="showForm" style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06);margin-bottom:16px">
      <div style="margin-bottom:12px">
        <label style="font-size:13px;color:#666;margin-bottom:6px;display:block">发票类型</label>
        <div style="display:flex;gap:8px">
          <span @click="form.type='personal'" style="cursor:pointer;padding:6px 16px;border-radius:6px;font-size:13px"
                :style="{border:form.type==='personal'?'2px solid #f10215':'1px solid #ddd',background:form.type==='personal'?'#fff5f5':'#fff'}">个人</span>
          <span @click="form.type='company'" style="cursor:pointer;padding:6px 16px;border-radius:6px;font-size:13px"
                :style="{border:form.type==='company'?'2px solid #f10215':'1px solid #ddd',background:form.type==='company'?'#fff5f5':'#fff'}">企业</span>
        </div>
      </div>
      <input v-model="form.orderNo" placeholder="订单号" style="width:100%;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;box-sizing:border-box;margin-bottom:10px" />
      <template v-if="form.type==='company'">
        <input v-model="form.title" placeholder="公司抬头" style="width:100%;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;box-sizing:border-box;margin-bottom:10px" />
        <input v-model="form.taxNo" placeholder="税号 (选填)" style="width:100%;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;box-sizing:border-box;margin-bottom:10px" />
      </template>
      <input v-model="form.email" placeholder="接收邮箱" type="email" style="width:100%;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;box-sizing:border-box;margin-bottom:12px" />
      <div style="display:flex;gap:8px;justify-content:flex-end">
        <button @click="showForm=false" style="padding:8px 20px;border:1px solid #ddd;background:#fff;border-radius:6px;cursor:pointer;font-size:13px">取消</button>
        <button @click="submit" :disabled="submitting" style="padding:8px 20px;background:#f10215;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:13px;font-weight:600">{{ submitting?'提交中...':'提交申请' }}</button>
      </div>
    </div>

    <div v-if="loading" style="display:flex;flex-direction:column;gap:12px">
      <div v-for="i in 2" :key="i" style="background:#fff;border-radius:12px;padding:20px;box-shadow:0 2px 8px rgba(0,0,0,.06);height:60px">
        <div style="height:16px;width:50%;background:linear-gradient(90deg,#f0f0f0,#e0e0e0,#f0f0f0);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:4px"></div>
      </div>
    </div>
    <div v-else-if="invoices.length">
      <div v-for="inv in invoices" :key="inv.id" style="background:#fff;border-radius:12px;padding:20px;margin-bottom:10px;box-shadow:0 2px 8px rgba(0,0,0,.06);display:flex;justify-content:space-between;align-items:center">
        <div>
          <div style="font-weight:600;font-size:14px">{{ inv.type==='company'?'企业':'个人' }}发票 · {{ inv.title || '个人' }}</div>
          <div style="color:#999;font-size:12px">订单 {{ inv.orderNo }} · {{ inv.createdAt?.substring(0,10) }}</div>
        </div>
        <span style="padding:2px 10px;border-radius:12px;font-size:12px;font-weight:600;color:#fff" :style="{background:statusMap[inv.status]?.color}">{{ statusMap[inv.status]?.label }}</span>
      </div>
    </div>
    <div v-else style="background:#fff;border-radius:12px;padding:60px;text-align:center;color:#999;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <p style="font-size:48px;margin-bottom:12px">🧾</p><p>暂无发票记录</p>
    </div>
  </div>
</template>
<style scoped>@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }</style>