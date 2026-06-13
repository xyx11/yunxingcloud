import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useLiveStatsStore = defineStore('liveStats', () => {
  const activeSessions = ref(0)

  function setSessions(n: number) { activeSessions.value = n }

  return { activeSessions, setSessions }
})
