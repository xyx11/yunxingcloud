import { ref, onMounted, onUnmounted } from 'vue'

export function useNetworkStatus(options?: { onStatusChange?: (online: boolean) => void }) {
  const isOnline = ref(navigator.onLine)
  const wasOffline = ref(!navigator.onLine)

  function onOnline() {
    isOnline.value = true
    if (wasOffline.value) {
      wasOffline.value = false
      options?.onStatusChange?.(true)
    }
  }
  function onOffline() {
    isOnline.value = false
    wasOffline.value = true
    options?.onStatusChange?.(false)
  }

  onMounted(() => {
    window.addEventListener('online', onOnline)
    window.addEventListener('offline', onOffline)
  })

  onUnmounted(() => {
    window.removeEventListener('online', onOnline)
    window.removeEventListener('offline', onOffline)
  })

  return { isOnline, wasOffline }
}
