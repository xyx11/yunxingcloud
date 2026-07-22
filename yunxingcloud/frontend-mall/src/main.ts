import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import { useGlobalToast, ToastInjectionKey } from './composables/useToast'
import { createLogger } from './utils/logger'
import './styles/reset.css'
import './styles/tokens.css'
import './styles/utilities.css'
import './styles/breakpoints.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)

const toast = useGlobalToast()
app.config.globalProperties.$toast = toast
app.provide(ToastInjectionKey, toast)

const logger = createLogger('app')

app.config.errorHandler = (err, _vm, info) => {
  const payload = {
    message: err instanceof Error ? err.message : String(err),
    info,
    url: location.pathname,
    ts: Date.now(),
    stack: err instanceof Error ? err.stack : undefined,
  }
  logger.error('Vue error', payload)
}

window.addEventListener('error', (e) => {
  if (navigator.sendBeacon) {
    navigator.sendBeacon('/api/analytics/error', new Blob([JSON.stringify({
      message: e.message, filename: e.filename, lineno: e.lineno,
      url: location.pathname, ts: Date.now(),
    })], { type: 'application/json' }))
  }
})

window.addEventListener('unhandledrejection', (e) => {
  logger.error('Unhandled promise', {
    message: e.reason instanceof Error ? e.reason.message : String(e.reason),
    stack: e.reason instanceof Error ? e.reason.stack : undefined,
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
