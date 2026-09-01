import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import { useToast, ToastInjectionKey } from './composables/useToast'
import { createLogger } from './utils/logger'
import './styles/reset.css'
import './styles/tokens.css'
import './styles/utilities.css'
import './styles/breakpoints.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)

const toast = useToast()
app.config.globalProperties.$toast = toast
app.provide(ToastInjectionKey, toast)

const logger = createLogger('app')

// Rate-limited error reporting
let errorBurstCount = 0
let errorBurstReset = 0
const MAX_BURST = 5
const BURST_WINDOW = 10000

function reportError(payload: Record<string, unknown>) {
  const now = Date.now()
  if (now > errorBurstReset) { errorBurstCount = 0; errorBurstReset = now + BURST_WINDOW }
  if (++errorBurstCount > MAX_BURST) return
  const body = JSON.stringify(payload)
  if (navigator.sendBeacon) {
    navigator.sendBeacon('/api/analytics/error', new Blob([body], { type: 'application/json' }))
  } else {
    fetch('/api/analytics/error', { method: 'POST', body, headers: { 'Content-Type': 'application/json' }, keepalive: true }).catch(() => {})
  }
}

app.config.errorHandler = (err, _vm, info) => {
  const payload = {
    message: err instanceof Error ? err.message : String(err),
    info,
    url: location.pathname,
    ts: Date.now(),
    stack: err instanceof Error ? err.stack : undefined,
  }
  logger.error('Vue error', payload)
  reportError({ type: 'vue_error', ...payload })
  toast.error('页面出现异常，请刷新重试')
}

window.addEventListener('error', (e) => {
  reportError({
    type: 'global_error',
    message: e.message, filename: e.filename, lineno: e.lineno,
    url: location.pathname, ts: Date.now(),
  })
})

window.addEventListener('unhandledrejection', (e) => {
  logger.error('Unhandled promise', {
    message: e.reason instanceof Error ? e.reason.message : String(e.reason),
    stack: e.reason instanceof Error ? e.reason.stack : undefined,
  })
  reportError({
    type: 'unhandled_rejection',
    message: e.reason instanceof Error ? e.reason.message : String(e.reason),
    url: location.pathname, ts: Date.now(),
  })
})

app.mount('#app')

if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch((err) => {
      logger.warn('SW registration failed', { message: err.message })
    })
  })
}
