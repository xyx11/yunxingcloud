<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import JdEmpty from '@/components/JdEmpty.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const notifications = ref<any[]>([])
const loading = ref(true)
const unreadCount = ref(0)
const toast = useToast()
const { t } = useI18n()

async function load() {
  loading.value = true
  try {
    const r = await request.get('/notifications')
    notifications.value = r.data || []
    const u = await request.get('/notifications/unread-count')
    unreadCount.value = u.data || 0
  } catch { toast.error(t('notifications.loadFail')) } finally { loading.value = false }
}

async function markRead(id: number) {
  try { await request.put(`/notifications/${id}/read`); load() } catch { toast.error(t('notifications.markReadFail')) }
}

async function markAllRead() {
  try { await request.put('/notifications/read-all'); load() } catch { toast.error(t('notifications.markAllFail')) }
}

onMounted(load)
</script>

<template>
  <div class="notif-page">
    <div class="notif-header">
      <h2 class="notif-title">{{ t('notifications.title') }}</h2>
      <button v-if="unreadCount > 0" class="notif-read-all" @click="markAllRead">{{ t('notifications.markAllRead') }}</button>
    </div>

    <div v-if="loading" class="notif-skel">
      <div v-for="i in 3" :key="i" class="sk-item"><div class="sk-line" /><div class="sk-line w60" /></div>
    </div>

    <div v-else-if="notifications.length" class="notif-list">
      <div v-for="n in notifications" :key="n.id" class="notif-item" :class="{ unread: !n.isRead }" role="button" tabindex="0" @click="markRead(n.id)" @keydown.enter.prevent="markRead(n.id)" @keydown.space.prevent="markRead(n.id)">
        <div class="notif-icon">{{ n.type === 'order' ? '📦' : n.type === 'promo' ? '🎉' : '🔔' }}</div>
        <div class="notif-body">
          <div class="notif-title">{{ n.title }}</div>
          <div class="notif-content">{{ n.content || n.message }}</div>
          <div class="notif-time">{{ n.createdAt?.substring(0, 10) || '' }}</div>
        </div>
      </div>
    </div>

    <JdEmpty v-else icon="🔔" :title="t('notifications.empty')" />
  </div>
</template>

<style scoped>
.notif-page { max-width: 600px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }
.notif-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-xl); }
.notif-title { font-size: var(--font-xl); font-weight: 700; }
.notif-read-all { padding: 4px 12px; border: 1px solid var(--jd-red); background: var(--bg-white); color: var(--jd-red); border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-sm); }
.notif-list { display: flex; flex-direction: column; gap: 10px; }
.notif-item { display: flex; gap: var(--space-md); padding: var(--space-lg); background: var(--bg-white); border-radius: var(--radius-md); cursor: pointer; box-shadow: var(--shadow-sm); transition: background var(--transition-fast); }
.notif-item.unread { background: var(--jd-red-light); border-left: 3px solid var(--jd-red); }
.notif-icon { font-size: 24px; flex-shrink: 0; }
.notif-body { flex: 1; min-width: 0; }
.notif-title { font-weight: 600; font-size: var(--font-md); margin-bottom: 2px; }
.notif-content { font-size: var(--font-base); color: var(--text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.notif-time { font-size: var(--font-xs); color: var(--text-tertiary); margin-top: var(--space-xs); }
.notif-skel { display: flex; flex-direction: column; gap: var(--space-md); }
.sk-item { padding: var(--space-lg); display: flex; flex-direction: column; gap: var(--space-sm); }
.sk-line { height: 16px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-sm); width: 100%; }
.sk-line.w60 { width: 60%; }
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

@media (max-width: 768px) {
  .notif-page { padding: var(--space-lg) var(--space-md) 80px; }
}
</style>
