import { ref } from 'vue'
import { useI18n } from '@/locales'

const { t } = useI18n()

const supported = ref('Notification' in window && 'serviceWorker' in navigator)
const permission = ref('Notification' in window ? Notification.permission : 'default')
const subscribed = ref(localStorage.getItem('mall_push_subscribed') === 'true')

export function useNotification() {
  async function request() {
    if (!supported.value) return false
    try {
      const result = await Notification.requestPermission()
      permission.value = result
      if (result === 'granted') {
        const reg = await navigator.serviceWorker.ready
        await reg.pushManager.subscribe({
          userVisibleOnly: true,
          // Set VITE_VAPID_PUBLIC_KEY in your .env file for production
          applicationServerKey: urlBase64ToUint8Array(
            import.meta.env.VITE_VAPID_PUBLIC_KEY || 'BNcEz3WmQHq6WE-D9oJg5E9xRm0KZkPqJfTyHuVxLwYgAsNdFbGnHjMkLpQwErTyUiOpPaSdFgHjKlZxCvBnM'
          )
        })
        localStorage.setItem('mall_push_subscribed', 'true')
        subscribed.value = true
        return true
      }
    } catch { /* not supported or blocked */ }
    return false
  }

  function unsubscribe() {
    navigator.serviceWorker.ready
      .then(reg => reg.pushManager.getSubscription())
      .then(sub => sub?.unsubscribe())
      .catch(() => {})
    localStorage.removeItem('mall_push_subscribed')
    subscribed.value = false
  }

  function sendTest() {
    if (permission.value === 'granted') {
      new Notification(t('notif.title'), { body: t('notif.subscribed'), icon: '/favicon.svg' })
    }
  }

  return { supported, permission, subscribed, request, unsubscribe, sendTest }
}

function urlBase64ToUint8Array(base64: string) {
  const padding = '='.repeat((4 - (base64.length % 4)) % 4)
  const raw = atob((base64 + padding).replace(/-/g, '+').replace(/_/g, '/'))
  return new Uint8Array([...raw].map(c => c.charCodeAt(0)))
}