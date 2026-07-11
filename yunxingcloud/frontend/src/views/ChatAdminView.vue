<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NModal, NSpace, NTag, NInput } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'

const loading = ref(false)
const sessions = ref<Record<string, unknown>[]>([])
const showChat = ref(false); const chatMessages = ref<Record<string, unknown>[]>([])
const activeSession = ref(''); const searchKeyword = ref('')

const filtered = computed(() => {
  if (!searchKeyword.value) return sessions.value
  return sessions.value.filter(s => (s.sessionId || '').includes(searchKeyword.value))
})

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: t('chat.sessionId'), key: 'sessionId', width: 180, ellipsis: { tooltip: true } },
  { title: t('chat.user'), key: 'userId', width: 100 },
  { title: t('chat.msgCount'), key: 'msgCount', width: 70, render(r: Record<string, unknown>) { return (r.messages as unknown[])?.length || 0 } },
  { title: t('chat.status'), key: 'status', width: 70, render(r: Record<string, unknown>) { return h(NTag, { size: 'small', type: r.status === 'active' ? 'success' : 'default' }, { default: () => r.status === 'active' ? t('chat.active') : t('chat.closed') }) } },
  { title: t('common.actions'), key: 'act', width: 80, render(r: Record<string, unknown>) { return h(NButton, { size: 'tiny', onClick: () => viewChat(r.sessionId as string) }, { default: () => t('chat.view') }) } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/chat/sessions'); sessions.value = r.data || [] } finally { loading.value = false } }
async function viewChat(sid: string) { activeSession.value = sid; try { const r = await request.get(`/api/chat/${sid}`); chatMessages.value = r.data || [] } catch { chatMessages.value = [] } showChat.value = true }
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card :title="t('chat.title')"><template #header-extra><n-button size="small" @click="load" secondary>{{ t('chat.refresh') }}</n-button></template>
      <n-space class="mb-12">
        <n-input v-model:value="searchKeyword" :placeholder="t('chat.searchPlaceholder')" size="small" clearable class="w-200" />
      </n-space>
      <n-dataTable :columns="columns" :data="filtered" :loading="loading" :row-key="(r: Record<string, unknown>)=>r.sessionId" :pagination="{pageSize:15}" size="small" />
    </n-card>
    <n-modal v-model:show="showChat" :title="t('chat.sessionRecord')" preset="card" class="max-w-500">
      <div v-if="!chatMessages.length" class="chat-empty">{{ t('chat.noMessages') }}</div>
      <div v-for="(m,i) in chatMessages" :key="i" class="chat-msg-row" :class="m.from === 'user' ? 'chat-msg--left' : 'chat-msg--right'">
        <div class="chat-bubble" :class="m.from === 'user' ? 'chat-bubble--user' : 'chat-bubble--agent'">{{ m.content || m.text || m.message }}</div>
        <div class="chat-time">{{ m.time?.substring(0, 16) }}</div>
      </div>
    </n-modal>
  </div>
</template>

<style scoped>
.w-200 { width: 200px; }
.max-w-500 { max-width: 500px; }
</style>
