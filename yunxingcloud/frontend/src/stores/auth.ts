import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'
import { getCsrfToken } from '@/composables/useCsrf'

export const useAuthStore = defineStore('auth', () => {
  const username = ref<string | null>(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => username.value !== null)

  async function fetchUser() {
    loading.value = true
    try {
      const res = await axios.get('/api/user')
      username.value = res.data.username
    } finally {
      loading.value = false
    }
  }

  async function login(user: string, password: string): Promise<string> {
    const csrfToken = getCsrfToken()
    const res = await axios.post('/api/login',
      { username: user, password },
      { headers: { 'X-XSRF-TOKEN': csrfToken } }
    )
    if (res.data.success) {
      username.value = res.data.username
      return res.data.redirectUrl || '/'
    }
    throw new Error(res.data.message || '登录失败')
  }

  function clear() {
    username.value = null
  }

  return { username, loading, isAuthenticated, fetchUser, login, clear }
})
