import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import { useGlobalToast, ToastInjectionKey } from './composables/useToast'
import './styles/reset.css'
import './styles/utilities.css'
import './styles/breakpoints.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)

const toast = useGlobalToast()
app.config.globalProperties.$toast = toast
app.provide(ToastInjectionKey, toast)

app.config.errorHandler = (err, _vm, info) => {
  console.error('[Vue Error]', info, err)
}

app.mount('#app')

// PWA: register service worker
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => {})
  })
}
