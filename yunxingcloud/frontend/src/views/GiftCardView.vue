<script setup lang="ts">
import { ref } from 'vue'
import { NCard, NForm, NFormItem, NInputNumber, NInput, NButton, NSpace, NTag } from 'naive-ui'
import { queryGiftCard, createGiftCard } from '@/api/giftcard'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const cardNo = ref(''); const queryResult = ref<any>(null)
const amount = ref(10000); const expireDays = ref(365); const creating = ref(false)

const statusLabel: Record<string,string> = { '0':'未激活', '1':'已激活', '2':'已用完', '3':'已过期' }

async function query() {
  if (!cardNo.value.trim()) return
  try { const r = await queryGiftCard(cardNo.value.trim()); queryResult.value = r.data } catch { queryResult.value = null; notify.error('未找到') }
}
async function create() { creating.value = true; try { await createGiftCard(amount.value, expireDays.value); notify.success('礼品卡已生成') } finally { creating.value = false } }
</script>
<template>
  <n-card title="礼品卡管理">
    <n-space vertical>
      <n-space><n-input v-model:value="cardNo" placeholder="输入卡号查询" style="width:240px" /><n-button type="primary" @click="query">查询</n-button></n-space>
      <div v-if="queryResult" style="background:#f5f5f5;padding:16px;border-radius:8px">
        <p><b>卡号:</b> {{ queryResult.cardNo }}</p>
        <p><b>面额:</b> ¥{{ (queryResult.amount/100).toFixed(2) }} | <b>余额:</b> ¥{{ (queryResult.balance/100).toFixed(2) }}</p>
        <p><b>状态:</b> <n-tag size="small" :type="queryResult.status==='1'?'success':'default'">{{ statusLabel[queryResult.status] }}</n-tag></p>
        <p v-if="queryResult.owner"><b>绑定用户:</b> {{ queryResult.owner }}</p>
      </div>
      <n-card title="生成礼品卡" size="small" style="max-width:400px">
        <n-form>
          <n-form-item label="面额(元)"><n-input-number v-model:value="amount" :min="1" :step="100" /></n-form-item>
          <n-form-item label="有效期(天)"><n-input-number v-model:value="expireDays" :min="1" :max="3650" /></n-form-item>
          <n-button type="primary" :loading="creating" @click="create">生成</n-button>
        </n-form>
      </n-card>
    </n-space>
  </n-card>
</template>