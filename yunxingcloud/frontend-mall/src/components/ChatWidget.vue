<script setup lang="ts">
import { ref, nextTick } from 'vue'
import request from '@/api/request'
import { useI18n } from '@/locales'

const { t } = useI18n()
const open = ref(false)
const messages = ref<{ role: string; content: string; time: number }[]>([])
const input = ref('')
const sending = ref(false)
const enabled = ref(false)
const CHAT_KEY = 'mall_chat_messages'
const MAX_MSGS = 50

function loadMsgs() { try { const raw = localStorage.getItem(CHAT_KEY); messages.value = raw ? JSON.parse(raw) : [] } catch { messages.value = [] } }
function saveMsgs() { try { localStorage.setItem(CHAT_KEY, JSON.stringify(messages.value.slice(-MAX_MSGS))) } catch {} }
loadMsgs()

function fmtTime(ts: number): string { const d = new Date(ts); return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0') }

async function checkHealth() {
  try { await request.get('/chat/health'); enabled.value = true } catch {}
}

function toggle() {
  open.value = !open.value
  if (open.value) {
    unread.value = 0
    if (messages.value.length === 0) {
      messages.value.push({ role: 'assistant', content: t('chat.greeting'), time: Date.now() })
      saveMsgs()
    }
    nextTick(() => { const el = document.getElementById('chat-list'); if (el) el.scrollTop = el.scrollHeight })
  }
}

async function send() {
  const text = input.value.trim(); if (!text || sending.value) return
  messages.value.push({ role: 'user', content: text, time: Date.now() }); input.value = ''
  saveMsgs()
  sending.value = true
  nextTick(() => { const el = document.getElementById('chat-list'); if (el) el.scrollTop = el.scrollHeight })
  try {
    const r = await request.post('/chat', { message: text })
    const reply = r.data?.reply || r.data?.message || t('chat.fallbackReply')
    messages.value.push({ role: 'assistant', content: reply, time: Date.now() })
    if (!open.value) unread.value++
  } catch { messages.value.push({ role: 'assistant', content: t('chat.serviceDown'), time: Date.now() }) } finally { sending.value = false }
  saveMsgs()
  nextTick(() => { const el = document.getElementById('chat-list'); if (el) el.scrollTop = el.scrollHeight })
}

const unread = ref(0)

const quickQuestions = [t('chat.q1'), t('chat.q2'), t('chat.q3'), t('chat.q4')]

async function sendQuick(q: string) {
  input.value = q
  await send()
}

checkHealth()
</script>

<template>
  <div v-if="enabled" class="chat-widget">
    <div v-if="open" class="chat-panel">
      <div class="chat-header">
        <span>{{ t('chat.title') }}</span>
        <button class="chat-close" @click="open = false">✕</button>
      </div>
      <!-- Quick reply buttons -->
      <div v-if="messages.length <= 1" class="chat-quick-replies">
        <button v-for="q in quickQuestions" :key="q" class="chat-quick-btn" @click="sendQuick(q)">{{ q }}</button>
      </div>
      <div id="chat-list" class="chat-list">
        <div v-for="(m, i) in messages" :key="i" class="chat-msg-wrap" :class="m.role">
          <div class="chat-msg" :class="m.role">{{ m.content }}</div>
          <div class="chat-time" :class="m.role">{{ fmtTime(m.time) }}</div>
        </div>
        <div v-if="sending" class="chat-msg assistant chat-typing">...</div>
      </div>
      <div class="chat-input-row">
        <input v-model="input" class="chat-input" :placeholder="t('chat.placeholder')" @keyup.enter="send" :disabled="sending" />
        <button class="chat-send" @click="send" :disabled="sending">{{ t('chat.send') }}</button>
      </div>
    </div>
    <button v-else class="chat-bubble" @click="toggle" :aria-label="t('chat.ariaLabel')">
      {{ t('chat.toggle') }}
      <span v-if="unread > 0" class="chat-unread">{{ unread > 9 ? '9+' : unread }}</span>
    </button>
  </div>
</template>

<style scoped>
.chat-widget { position: fixed; bottom: 80px; right: 20px; z-index: 300; }
.chat-bubble { width: 52px; height: 52px; border-radius: 50%; background: var(--jd-red); color: #fff; border: none; cursor: pointer; font-size: 24px; box-shadow: 0 4px 16px rgba(241,2,21,.3); display: flex; align-items: center; justify-content: center; position: relative; }
.chat-unread { position: absolute; top: -4px; right: -4px; background: #fff; color: var(--jd-red); border-radius: var(--radius-round); font-size: 11px; font-weight: 700; min-width: 18px; height: 18px; line-height: 18px; text-align: center; padding: 0 4px; box-shadow: 0 2px 6px rgba(0,0,0,.15); }
.chat-panel { width: 340px; max-height: 480px; background: var(--bg-white); border-radius: var(--radius-lg); box-shadow: 0 8px 32px rgba(0,0,0,.15); display: flex; flex-direction: column; overflow: hidden; }
.chat-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: var(--jd-red); color: #fff; font-weight: 600; }
.chat-close { background: none; border: none; color: #fff; cursor: pointer; font-size: 16px; }
.chat-quick-replies { display: flex; flex-wrap: wrap; gap: 6px; padding: 8px 12px; border-bottom: 1px solid var(--border-light); }
.chat-quick-btn { padding: 5px 12px; border: 1px solid var(--jd-red); background: var(--bg-white); color: var(--jd-red); border-radius: var(--radius-round); font-size: 12px; cursor: pointer; transition: all var(--transition-fast); }
.chat-quick-btn:hover { background: var(--jd-red); color: #fff; }

.chat-list { flex: 1; overflow-y: auto; padding: 12px; max-height: 320px; display: flex; flex-direction: column; gap: 8px; }
.chat-msg { padding: 8px 12px; border-radius: var(--radius-md); font-size: 13px; line-height: 1.5; max-width: 85%; word-break: break-word; }
.chat-msg-wrap { display: flex; flex-direction: column; max-width: 85%; }
.chat-msg-wrap.user { align-self: flex-end; }
.chat-msg-wrap.assistant { align-self: flex-start; }
.chat-msg { padding: 8px 12px; border-radius: var(--radius-md); font-size: 13px; line-height: 1.5; word-break: break-word; }
.chat-msg.user { background: var(--jd-red); color: #fff; }
.chat-msg.assistant { background: var(--bg-hover); color: var(--text-primary); }
.chat-time { font-size: 10px; color: var(--text-placeholder); margin-top: 2px; }
.chat-time.user { text-align: right; }
.chat-time.assistant { text-align: left; }
.chat-typing { opacity: .6; }
.chat-input-row { display: flex; padding: 10px; border-top: 1px solid var(--border-light); gap: 8px; }
.chat-input { flex: 1; padding: 8px 12px; border: 1px solid var(--border); border-radius: var(--radius-round); font-size: 13px; outline: none; }
.chat-send { padding: 6px 14px; background: var(--jd-red); color: #fff; border: none; border-radius: var(--radius-round); cursor: pointer; font-size: 13px; }
.chat-send:disabled { opacity: .5; }

@media (max-width: 768px) {
  .chat-widget { bottom: 70px; right: 12px; }
  .chat-panel { width: calc(100vw - 24px); max-width: 340px; }
}
</style>
