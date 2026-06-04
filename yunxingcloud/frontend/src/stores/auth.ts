import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import request from '@/api/request'

export const useAuthStore = defineStore('auth', () => {
  const username = ref<string | null>(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => username.value !== null)

  async function fetchUser() {
    loading.value = true
    try {
      const res = await request.get('/api/user')
      username.value = res.data.username
    } finally {
      loading.value = false
    }
  }

  async function login(user: string, password: string, code?: string): Promise<string> {
    const res = await request.post('/api/login', { username: user, password, code })
    if (res.data.success) {
      username.value = res.data.username
      localStorage.setItem('accessToken', res.data.accessToken)
      localStorage.setItem('refreshToken', res.data.refreshToken)
      return '/'
    }
    throw new Error(res.data.message || '登录失败')
  }

  function restoreFromStorage() {
    const token = localStorage.getItem('accessToken')
    if (token) {
      fetchUser().catch(() => clear())
    }
  }

  function clear() {
    username.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  return { username, loading, isAuthenticated, fetchUser, login, restoreFromStorage, clear }
})
