<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NTag, NSpace, NModal, NInput } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchFeedback, replyFeedback } from '@/api/feedback'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<Record<string, unknown>[]>([])
const replyText = ref('')
const replyId = ref(0)
const showReply = ref(false)
const typeLabels: Record<string,string> = { suggestion: t('feedback.typeSuggestion'), bug: t('feedback.typeBug'), praise: t('feedback.typePraise'), complaint: t('feedback.typeComplaint') }

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: t('order.username'), key: 'username', width: 90 }, { title: t('feedback.type'), key: 'type', width: 60, render(r: Record<string, unknown>){ return typeLabels[r.type as string] || r.type as string } },
  { title: t('feedback.content'), key: 'content', width: 250, ellipsis:{tooltip:true} },
  { title: t('feedback.status'), key: 'status', width: 70, render(r: Record<string, unknown>){ return h(NTag,{size:'small',type:r.status==='0'?'warning':r.status==='1'?'success':'default'},{default:()=>r.status==='0'?t('feedback.statusPending'):t('feedback.statusReplied')}) } },
  { title: t('feedback.time'), key: 'createdAt', width: 140, render(r: Record<string, unknown>){ return (r.createdAt as string)?.substring(0,16) } },
  { title: t('common.actions'), key:'act', width: 80, render(r: Record<string, unknown>){ return r.status==='0'?h(NButton,{size:'small',onClick:()=>{replyId.value=r.id as number;replyText.value='';showReply.value=true}},{default:()=>t('feedback.reply')}):null } },
]

async function load() { const r = await fetchFeedback(); items.value = r.data }
async function doReply() { await replyFeedback(replyId.value, replyText.value); showReply.value=false; notify.success(t('feedback.replySuccess')); load() }
onMounted(load)
</script>
<template>
  <n-card :title="t('feedback.title')">
    <n-dataTable :columns="columns" :data="items" :pagination="{pageSize:10}" />
    <n-modal v-model:show="showReply" :title="t('feedback.replyTitle')" preset="card" class="max-w-400">
      <n-input v-model:value="replyText" type="textarea" :autosize="{minRows:3}" :placeholder="t('feedback.replyPlaceholder')" />
      <template #footer><n-button @click="showReply=false">{{ t('feedback.cancel') }}</n-button><n-button type="primary" @click="doReply">{{ t('feedback.reply') }}</n-button></template>
    </n-modal>
  </n-card>
</template>

<style scoped>
.max-w-400 { max-width: 400px; }
</style>
