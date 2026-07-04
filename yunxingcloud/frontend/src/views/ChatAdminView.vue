<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { NCard, NDataTable, NButton, NModal, NSpace, NTag, NInput } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'

const loading = ref(false)
const sessions = ref<any[]>([])
const showChat = ref(false); const chatMessages = ref<any[]>([])
const activeSession = ref(''); const searchKeyword = ref('')

const filtered = computed(() => {
  if (!searchKeyword.value) return sessions.value
  return sessions.value.filter(s => (s.sessionId || '').includes(searchKeyword.value))
})

const columns: DataTableColumns<any> = [
  { title: '会话ID', key: 'sessionId', width: 180, ellipsis: { tooltip: true } },
  { title: '用户', key: 'userId', width: 100 },
  { title: '消息数', key: 'msgCount', width: 70, render(r: any) { return r.messages?.length || 0 } },
  { title: '状态', key: 'status', width: 70, render(r: any) { return h(NTag, { size: 'small', type: r.status === 'active' ? 'success' : 'default' }, { default: () => r.status === 'active' ? '活跃' : '关闭' }) } },
  { title: '操作', key: 'act', width: 80, render(r: any) { return h(NButton, { size: 'tiny', onClick: () => viewChat(r.sessionId) }, { default: () => '查看' }) } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/chat/sessions'); sessions.value = r.data || [] } finally { loading.value = false } }
async function viewChat(sid: string) { activeSession.value = sid; try { const r = await request.get(`/api/chat/${sid}`); chatMessages.value = r.data || [] } catch { chatMessages.value = [] } showChat.value = true }
onMounted(load)
</script>
<template>
  <div style="padding:20px">
    <n-card title="客服会话"><template #header-extra><n-button size="small" @click="load" secondary>刷新</n-button></template>
      <n-space style="margin-bottom:12px">
        <n-input v-model:value="searchKeyword" placeholder="搜索会话ID..." size="small" clearable style="width:200px" />
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r:any)=>r.sessionId" :pagination="{pageSize:15}" size="small" />
    </n-card>
    <n-modal v-model:show="showChat" title="会话记录" preset="card" style="max-width:500px">
      <div v-if="!chatMessages.length" style="text-align:center;color:#999;padding:20px">暂无消息</div>
      <div v-for="(m,i) in chatMessages" :key="i" :style="{textAlign:m.from==='user'?'left':'right',marginBottom:'12px'}">
        <div :style="{display:'inline-block',maxWidth:'80%',padding:'8px 14px',borderRadius:'12px',background:m.from==='user'?'#e8f0fe':'#f0f0f0',fontSize:'14px'}">{{ m.content || m.text || m.message }}</div>
        <div style="font-size:11px;color:#999;margin-top:2px">{{ m.time?.substring(0,16) }}</div>
      </div>
    </n-modal>
  </div>
</template>
