import { ref } from 'vue'

interface Toast {
  id: number
  message: string
  type: 'success' | 'error' | 'info'
}

const toasts = ref<Toast[]>([])
let nextId = 1

export function useToast() {
  function show(message: string, type: 'success' | 'error' | 'info' = 'info') {
    const id = nextId++
    toasts.value.push({ id, message, type })
    setTimeout(() => {
      toasts.value = toasts.value.filter(t => t.id !== id)
    }, 3000)
  }

  function success(msg: string) { show(msg, 'success') }
  function error(msg: string) { show(msg, 'error') }
  function info(msg: string) { show(msg, 'info') }

  return { toasts, success, error, info }
}

// Singleton
const instance = ref<ReturnType<typeof useToast>>()

export function useGlobalToast() {
  if (!instance.value) instance.value = useToast()
  return instance.value
}
