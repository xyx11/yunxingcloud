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
