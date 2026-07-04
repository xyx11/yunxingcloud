<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/api/request'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const records = ref<any[]>([])
const loading = ref(true)
const showForm = ref(false)
const submitting = ref(false)
const form = ref({ orderNo: '', type: 'refund', reason: '', amount: '', description: '' })

const types = [
  { value: 'refund', label: () => t('afterSale.refundOnly'), icon: '💰', desc: () => t('afterSale.refundDesc') },
  { value: 'return', label: () => t('afterSale.returnRefund'), icon: '📦', desc: () => t('afterSale.returnDesc') },
  { value: 'exchange', label: () => t('afterSale.exchange'), icon: '🔄', desc: () => t('afterSale.exchangeDesc') },
]

const typeLabels: Record<string, () => string> = {
  refund: () => t('afterSale.refundOnly'),
  return: () => t('afterSale.returnRefund'),
  exchange: () => t('afterSale.exchange'),
}

const statusMap: Record<string, { label: string; color: string }> = {
  '0': { label: t('afterSale.statusPending'), color: '#ff9800' },
  '1': { label: t('afterSale.statusApproved'), color: '#4caf50' },
  '2': { label: t('afterSale.statusRejected'), color: '#f44336' },
  '3': { label: t('afterSale.statusRefunding'), color: '#1677ff' },
  '4': { label: t('afterSale.statusCompleted'), color: '#4caf50' },
}

onMounted(async () => {
  try { const r = await request.get('/after-sale'); records.value = r.data || [] } catch {}
  finally { loading.value = false }
})

async function submit() {
  if (!form.value.orderNo || !form.value.reason) { toast.error(t('afterSale.fillComplete')); return }
  submitting.value = true
  try { await request.post('/after-sale', form.value); toast.success(t('afterSale.submitSuccess')); showForm.value = false; records.value.unshift({ ...form.value, status: '0', createdAt: new Date().toISOString() }) }
  catch { toast.error(t('afterSale.submitFail')) }
  finally { submitting.value = false }
}
</script>

<template>
  <div style="max-width:700px;margin:0 auto">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 style="font-size:20px;font-weight:700">🛡️ {{ t('afterSale.title') }}</h2>
      <button @click="showForm=true" style="padding:8px 20px;background:#f10215;color:#fff;border:none;border-radius:8px;cursor:pointer;font-size:13px;font-weight:600">+ {{ t('afterSale.newRequest') }}</button>
    </div>

    <div v-if="showForm" style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06);margin-bottom:16px">
      <div style="margin-bottom:16px">
        <label style="font-size:13px;color:#666;margin-bottom:8px;display:block">{{ t('afterSale.type') }}</label>
        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:8px">
          <div v-for="tp in types" :key="tp.value" @click="form.type=tp.value"
               style="padding:12px;border-radius:8px;cursor:pointer;text-align:center;transition:all .2s"
               :style="{border:form.type===tp.value?'2px solid #f10215':'1px solid #eee',background:form.type===tp.value?'#fff5f5':'#fff'}">
            <div style="font-size:24px;margin-bottom:4px">{{ tp.icon }}</div>
            <div style="font-size:13px;font-weight:600;margin-bottom:2px">{{ tp.label() }}</div>
            <div style="font-size:10px;color:#999">{{ tp.desc() }}</div>
          </div>
        </div>
      </div>
      <input v-model="form.orderNo" :placeholder="t('afterSale.orderNo')" style="width:100%;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;box-sizing:border-box;margin-bottom:12px" />
      <input v-model="form.amount" :placeholder="t('afterSale.amount') + ' (¥)'" type="number" style="width:100%;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;box-sizing:border-box;margin-bottom:12px" />
      <textarea v-model="form.reason" :placeholder="t('afterSale.reason')" style="width:100%;height:80px;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;resize:none;box-sizing:border-box;margin-bottom:12px"></textarea>
      <div style="display:flex;gap:8px;justify-content:flex-end">
        <button @click="showForm=false" style="padding:8px 20px;border:1px solid #ddd;background:#fff;border-radius:6px;cursor:pointer;font-size:13px">{{ t('common.cancel') }}</button>
        <button @click="submit" :disabled="submitting" style="padding:8px 20px;background:#f10215;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:13px;font-weight:600">{{ submitting ? t('afterSale.submitting') : t('afterSale.submitRequest') }}</button>
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
            <span style="font-size:13px;color:#999">{{ t('afterSale.orderPrefix') }} {{ r.orderNo }}</span>
            <span style="margin-left:12px;font-size:13px;font-weight:600">{{ typeLabels[r.type]?.() || r.type }}</span>
          </div>
          <span style="padding:2px 10px;border-radius:12px;font-size:12px;font-weight:600;color:#fff" :style="{background:statusMap[r.status]?.color}">{{ statusMap[r.status]?.label }}</span>
        </div>
        <div style="color:#666;font-size:13px">{{ r.reason }}</div>
        <div v-if="r.amount" style="color:#f10215;font-weight:700;margin-top:4px">¥{{ (Number(r.amount)/100).toFixed(2) }}</div>
        <div style="color:#aaa;font-size:11px;margin-top:6px">{{ r.createdAt?.substring(0,16) }}</div>
      </div>
    </div>
    <div v-else style="background:#fff;border-radius:12px;padding:60px;text-align:center;color:#999;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <p style="font-size:48px;margin-bottom:12px">🛡️</p><p>{{ t('afterSale.noRecords') }}</p>
    </div>
  </div>
</template>
<style scoped>@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }</style>
