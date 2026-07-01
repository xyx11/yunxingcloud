import { ref } from 'vue'

const supported = ref('Notification' in window && 'serviceWorker' in navigator)
const permission = ref(Notification.permission)
const subscribed = ref(localStorage.getItem('mall_push_subscribed') === 'true')

export function useNotification() {
  async function request() {
    if (!supported.value) return false
    try {
      const result = await Notification.requestPermission()
      permission.value = result
      if (result === 'granted') {
        const reg = await navigator.serviceWorker.ready
        const sub = await reg.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey: urlBase64ToUint8Array('BNcEz3WmQHq6WE-D9oJg5E9xRm0KZkPqJfTyHuVxLwYgAsNdFbGnHjMkLpQwErTyUiOpPaSdFgHjKlZxCvBnM')
        })
        localStorage.setItem('mall_push_subscribed', 'true')
        subscribed.value = true
        return true
      }
    } catch { /* not supported or blocked */ }
    return false
  }

  function unsubscribe() {
    localStorage.removeItem('mall_push_subscribed')
    subscribed.value = false
  }

  function sendTest() {
    if (permission.value === 'granted') {
      new Notification('YXCLOUD 商城', { body: '🎉 订阅成功！您将收到促销和订单状态通知', icon: '/mall/favicon.svg' })
    }
  }

  return { supported, permission, subscribed, request, unsubscribe, sendTest }
}

function urlBase64ToUint8Array(base64: string) {
  const padding = '='.repeat((4 - (base64.length % 4)) % 4)
  const raw = atob((base64 + padding).replace(/-/g, '+').replace(/_/g, '/'))
  return new Uint8Array([...raw].map(c => c.charCodeAt(0)))
}