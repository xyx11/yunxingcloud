<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const announcement = ref('')
const dismissed = ref(sessionStorage.getItem('announcement-dismissed'))

onMounted(async () => {
  try {
    const res = await request.get('/api/system/flags')
    announcement.value = res.data.announcement || ''
  } catch {}
})

function dismiss() {
  dismissed.value = 'true'
  sessionStorage.setItem('announcement-dismissed', 'true')
}
</script>

<template>
  <div v-if="announcement && !dismissed" class="banner">
    <span>{{ announcement }}</span>
    <button @click="dismiss" class="close-btn">✕</button>
  </div>
</template>

<style scoped>
.banner {
  display: flex; align-items: center; justify-content: center; gap: 12px;
  padding: 8px 16px; background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; font-size: 13px; font-weight: 500;
}
.close-btn {
  background: rgba(255,255,255,.2); border: none; color: #fff;
  cursor: pointer; border-radius: 4px; padding: 2px 6px; font-size: 12px;
}
</style>
