import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import { useGlobalToast } from './composables/useToast'

const app = createApp(App)
app.use(createPinia())
app.use(router)

// Make toast available globally via $toast
const toast = useGlobalToast()
app.config.globalProperties.$toast = toast
app.provide('toast', toast)

app.mount('#app')

// Web Vitals 监控 (需要: npm install web-vitals)
// 启用方式: localStorage.setItem('webVitals', '1') 后刷新页面
if (typeof localStorage !== 'undefined' && localStorage.getItem('webVitals')) {
  import('web-vitals').then(m => {
    m.onLCP(v => console.debug('[LCP]', (v/1000).toFixed(2)+'s'))
    m.onCLS(v => console.debug('[CLS]', v.toFixed(3)))
    m.onINP(v => console.debug('[INP]', v.toFixed(0)+'ms'))
  }).catch(() => {})
}
