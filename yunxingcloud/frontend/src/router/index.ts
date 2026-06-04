import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/LoginView.vue'
import HomeView from '@/views/HomeView.vue'
import ConsentView from '@/views/ConsentView.vue'
import NotFoundView from '@/views/NotFoundView.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/login', name: 'Login', component: LoginView },
    { path: '/', name: 'Home', component: HomeView },
    { path: '/oauth2/consent', name: 'Consent', component: ConsentView },
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView },
  ],
})

router.beforeEach(async (to, _from, next) => {
  if (to.path === '/login') {
    next()
    return
  }

  const authStore = useAuthStore()
  if (!authStore.isAuthenticated) {
    try {
      await authStore.fetchUser()
      next()
    } catch {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      return
    }
  }
  next()
})

export default router
