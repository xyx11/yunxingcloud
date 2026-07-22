<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatPrice } from '@/utils/format'
const { t } = useI18n()
import { NCard, NForm, NFormItem, NInputNumber, NInput, NButton, NSpace, NTag, NDataTable } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { queryGiftCard, createGiftCard } from '@/api/giftcard'
import { useNotify } from '@/composables/useNotify'
import request from '@/api/request'

const notify = useNotify()
const cardNo = ref(''); const queryResult = ref<any>(null)
const amount = ref(10000); const expireDays = ref(365); const creating = ref(false)
const cards = ref<any[]>([])
const batchCount = ref(10); const batchAmount = ref(10000)

const statusLabel: Record<string,string> = { '0':t('common.inactive'), '1':t('common.active2'), '2':t('common.used'), '3':t('common.expired') }

const columns: DataTableColumns<any> = [
  { title: t('giftCard.cardNo'), key: 'cardNo', width: 200 },
  { title: t('giftCard.amount'), key: 'amount', width: 80, render(r:any){ return formatPrice(r.amount/100, 2) } },
  { title: t('giftCard.balance'), key: 'balance', width: 80, render(r:any){ return formatPrice(r.balance/100, 2) } },
  { title: t('common.status'), key:'status', width:80, render(r:any){ return h(NTag,{size:'small',type:r.status==='1'?'success':'default'},{default:()=>statusLabel[r.status]}) } },
  { title: t('common.bindUser'), key:'owner', width:100, render(r:any){ return r.owner||'-' } },
]

async function query() {
  if (!cardNo.value.trim()) return
  try { const r = await queryGiftCard(cardNo.value.trim()); queryResult.value = r.data } catch { queryResult.value = null; notify.error(t('common.notFound')) }
}
async function create() { creating.value = true; try { await createGiftCard(amount.value, expireDays.value); notify.success(t('giftCard.createSuccess')); loadCards() } finally { creating.value = false } }
async function batchCreate() {
  creating.value = true
  try {
    for (let i = 0; i < batchCount.value; i++) { await createGiftCard(batchAmount.value, expireDays.value) }
    notify.success(t('giftCard.batchSuccess', { n: String(batchCount.value) })); loadCards()
  } finally { creating.value = false }
}
async function loadCards() { try { const r = await request.get('/api/giftcards'); cards.value = r.data || [] } catch(e) { console.warn('Failed to load gift cards:', e) } }
onMounted(loadCards)
</script>
<template>
  <n-card :title="t('nav.giftcards')">
    <n-space vertical>
      <n-space><n-input v-model:value="cardNo" :placeholder="t('giftCard.queryPlaceholder')" class="w-240" /><n-button type="primary" @click="query">{{ t('common.search') }}</n-button></n-space>
      <div v-if="queryResult" class="query-result">
        <p><b>{{ t('giftCard.cardNoLabel') }}</b> {{ queryResult.cardNo }}</p>
        <p><b>{{ t('giftCard.amountLabel') }}</b> {{ formatPrice(queryResult.amount/100, 2) }} | <b>{{ t('giftCard.balanceLabel') }}</b> {{ formatPrice(queryResult.balance/100, 2) }}</p>
        <p><b>{{ t('giftCard.statusLabel') }}</b> <n-tag size="small" :type="queryResult.status==='1'?'success':'default'">{{ statusLabel[queryResult.status] }}</n-tag></p>
        <p v-if="queryResult.owner"><b>{{ t('giftCard.bindUserLabel') }}</b> {{ queryResult.owner }}</p>
      </div>
      <n-space>
        <n-card :title="t('giftCard.generateCard')" size="small" class="max-w-300">
          <n-form>
            <n-form-item :label="t('giftCard.amountYuan')"><n-input-number v-model:value="amount" :min="1" :step="100" /></n-form-item>
            <n-form-item :label="t('giftCard.expireDays')"><n-input-number v-model:value="expireDays" :min="1" :max="3650" /></n-form-item>
            <n-button type="primary" :loading="creating" @click="create">{{ t('giftCard.generate') }}</n-button>
          </n-form>
        </n-card>
        <n-card :title="t('giftCard.batchGenerate')" size="small" class="max-w-300">
          <n-form>
            <n-form-item :label="t('giftCard.count')"><n-input-number v-model:value="batchCount" :min="1" :max="100" /></n-form-item>
            <n-form-item :label="t('giftCard.amountYuan')"><n-input-number v-model:value="batchAmount" :min="1" :step="100" /></n-form-item>
            <n-button type="primary" :loading="creating" @click="batchCreate">{{ t('giftCard.batchGenerate') }}</n-button>
          </n-form>
        </n-card>
      </n-space>
      <n-card :title="t('giftCard.cardList')" size="small">
        <n-dataTable :columns="columns" :data="cards" :pagination="{pageSize:10}" size="small" />
      </n-card>
    </n-space>
  </n-card>
</template>

<style scoped>
.w-240 { width: 240px; }
.query-result { background: #f5f5f5; padding: 16px; border-radius: 8px; }
.max-w-300 { max-width: 300px; }
</style>
