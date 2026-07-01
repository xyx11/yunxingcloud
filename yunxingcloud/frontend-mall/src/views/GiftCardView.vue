<script setup lang="ts">
import { ref } from 'vue'
import request from '@/api/request'
import { useI18n } from '@/locales'

const { t } = useI18n()
const cardNo = ref(''); const card = ref<any>(null); const msg = ref('')

async function query() { if(!cardNo.value.trim())return; try{const r=await request.get('/gift-cards/'+cardNo.value.trim());card.value=r.data;msg.value=''}catch{card.value=null;msg.value='未找到该礼品卡'} }
async function activate() { try{const r=await request.post('/gift-cards/'+cardNo.value.trim()+'/activate');card.value=r.data;msg.value='激活成功!'}catch(e:any){msg.value=e.response?.data?.message||'激活失败'} }
</script>
<template>
  <div style="max-width:400px;margin:30px auto">
    <div style="background:#fff;border-radius:12px;padding:32px;box-shadow:0 2px 8px rgba(0,0,0,.06);text-align:center">
      <h2 style="font-size:20px;margin-bottom:20px">礼品卡</h2>
      <input v-model="cardNo" placeholder="输入卡号" style="width:100%;padding:12px;border:2px solid #e4393c;border-radius:8px;font-size:15px;text-align:center;box-sizing:border-box;margin-bottom:12px" />
      <div style="display:flex;gap:8px;margin-bottom:16px">
        <button @click="query" style="flex:1;padding:10px;background:#fff;border:1px solid #e4393c;color:#e4393c;border-radius:8px;cursor:pointer;font-weight:600">查询</button>
        <button @click="activate" style="flex:1;padding:10px;background:#e4393c;color:#fff;border:none;border-radius:8px;cursor:pointer;font-weight:600">激活</button>
      </div>
      <p v-if="msg" :style="{color:msg.includes('成功')?'#4caf50':'#e4393c'}">{{ msg }}</p>
      <div v-if="card" style="background:#f5f5f5;border-radius:8px;padding:16px;margin-top:16px;text-align:left">
        <p><b>面额:</b> ¥{{ (card.amount/100).toFixed(2) }}</p>
        <p><b>余额:</b> ¥{{ (card.balance/100).toFixed(2) }}</p>
        <p><b>状态:</b> {{ {0:'未激活',1:'已激活',2:'已用完',3:'已过期'}[card.status] }}</p>
      </div>
    </div>
  </div>
</template>