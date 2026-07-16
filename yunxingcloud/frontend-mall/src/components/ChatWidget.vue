<script setup lang="ts">
import { ref, nextTick } from 'vue'
import request from '@/api/request'
import { useI18n } from '@/locales'

const { t } = useI18n()
const open = ref(false)
const messages = ref<{ role: string; content: string }[]>([])
const input = ref('')
const sending = ref(false)
const enabled = ref(false)

async function checkHealth() {
  try { await request.get('/chat/health'); enabled.value = true } catch {}
}

function toggle() {
  open.value = !open.value
  if (open.value && messages.value.length === 0) {
    messages.value.push({ role: 'assistant', content: t('chat.greeting') })
  }
  if (open.value) nextTick(() => { const el = document.getElementById('chat-list'); if (el) el.scrollTop = el.scrollHeight })
}

async function send() {
  const text = input.value.trim(); if (!text || sending.value) return
  messages.value.push({ role: 'user', content: text }); input.value = ''
  sending.value = true
  nextTick(() => { const el = document.getElementById('chat-list'); if (el) el.scrollTop = el.scrollHeight })
  try {
    const r = await request.post('/chat', { message: text })
    const reply = r.data?.reply || r.data?.message || t('chat.fallbackReply')
    messages.value.push({ role: 'assistant', content: reply })
  } catch { messages.value.push({ role: 'assistant', content: t('chat.serviceDown') }) } finally { sending.value = false }
  nextTick(() => { const el = document.getElementById('chat-list'); if (el) el.scrollTop = el.scrollHeight })
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
      <div id="chat-list" class="chat-list">
        <div v-for="(m, i) in messages" :key="i" class="chat-msg" :class="m.role">
          {{ m.content }}
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
    </button>
  </div>
</template>

<style scoped>
.chat-widget { position: fixed; bottom: 80px; right: 20px; z-index: 300; }
.chat-bubble { width: 52px; height: 52px; border-radius: 50%; background: var(--jd-red); color: #fff; border: none; cursor: pointer; font-size: 24px; box-shadow: 0 4px 16px rgba(241,2,21,.3); display: flex; align-items: center; justify-content: center; }
.chat-panel { width: 340px; max-height: 480px; background: var(--bg-white); border-radius: var(--radius-lg); box-shadow: 0 8px 32px rgba(0,0,0,.15); display: flex; flex-direction: column; overflow: hidden; }
.chat-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: var(--jd-red); color: #fff; font-weight: 600; }
.chat-close { background: none; border: none; color: #fff; cursor: pointer; font-size: 16px; }
.chat-list { flex: 1; overflow-y: auto; padding: 12px; max-height: 320px; display: flex; flex-direction: column; gap: 8px; }
.chat-msg { padding: 8px 12px; border-radius: var(--radius-md); font-size: 13px; line-height: 1.5; max-width: 85%; word-break: break-word; }
.chat-msg.user { background: var(--jd-red); color: #fff; align-self: flex-end; }
.chat-msg.assistant { background: var(--bg-hover); color: var(--text-primary); align-self: flex-start; }
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
