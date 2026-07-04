<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NTag, NSpace, NModal, NInput } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchFeedback, replyFeedback } from '@/api/feedback'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<any[]>([])
const replyText = ref('')
const replyId = ref(0)
const showReply = ref(false)
const typeLabels: Record<string,string> = { suggestion:'建议', bug:'Bug', praise:'好评', complaint:'投诉' }

const columns: DataTableColumns<any> = [
  { title: '用户', key: 'username', width: 90 }, { title: '类型', key: 'type', width: 60, render(r:any){ return typeLabels[r.type] } },
  { title: '内容', key: 'content', width: 250, ellipsis:{tooltip:true} },
  { title: '状态', key: 'status', width: 70, render(r:any){ return h(NTag,{size:'small',type:r.status==='0'?'warning':r.status==='1'?'success':'default'},{default:()=>r.status==='0'?'待处理':'已回复'}) } },
  { title: '时间', key: 'createdAt', width: 140, render(r:any){ return r.createdAt?.substring(0,16) } },
  { title: '操作', key:'act', width: 80, render(r:any){ return r.status==='0'?h(NButton,{size:'small',onClick:()=>{replyId.value=r.id;replyText.value='';showReply.value=true}},{default:()=>'回复'}):null } },
]

async function load() { const r = await fetchFeedback(); items.value = r.data }
async function doReply() { await replyFeedback(replyId.value, replyText.value); showReply.value=false; notify.success('已回复'); load() }
onMounted(load)
</script>
<template>
  <n-card title="客户反馈">
    <n-dataTable :columns="columns" :data="items" :pagination="{pageSize:10}" />
    <n-modal v-model:show="showReply" title="回复反馈" preset="card" style="max-width:400px">
      <n-input v-model:value="replyText" type="textarea" :autosize="{minRows:3}" placeholder="回复内容" />
      <template #footer><n-button @click="showReply=false">取消</n-button><n-button type="primary" @click="doReply">回复</n-button></template>
    </n-modal>
  </n-card>
</template>