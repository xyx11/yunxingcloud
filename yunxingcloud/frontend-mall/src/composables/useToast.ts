import { ref, type InjectionKey } from 'vue'

export interface ToastItem {
  id: number
  message: string
  type: 'success' | 'error' | 'info' | 'warning'
}

export const ToastInjectionKey: InjectionKey<ReturnType<typeof useToast>> = Symbol('toast')

const toasts = ref<ToastItem[]>([])
let nextId = 1

const icons: Record<string, string> = {
  success: '✅',
  error: '❌',
  info: 'ℹ️',
  warning: '⚠️',
}

export function useToast() {
  function show(message: string, type: 'success' | 'error' | 'info' | 'warning' = 'info', duration = 3000) {
    const id = nextId++
    toasts.value.push({ id, message, type })
    if (duration > 0) {
      setTimeout(() => {
        toasts.value = toasts.value.filter(t => t.id !== id)
      }, duration)
    }
  }

  function success(msg: string) { show(msg, 'success') }
  function error(msg: string) { show(msg, 'error', 4000) }
  function info(msg: string) { show(msg, 'info') }
  function warning(msg: string) { show(msg, 'warning', 4000) }
  function dismiss(id: number) { toasts.value = toasts.value.filter(t => t.id !== id) }

  return { toasts, icons, success, error, info, warning, dismiss }
}

const instance = ref<ReturnType<typeof useToast>>()

export function useGlobalToast() {
  if (!instance.value) instance.value = useToast()
  return instance.value
}