import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin, register as apiRegister } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<any>(null)
  const token = ref(localStorage.getItem('accessToken') || '')

  async function login(username: string, password: string) {
    const res = await apiLogin({ username, password })
    token.value = res.data.accessToken
    localStorage.setItem('accessToken', token.value)
    user.value = { username }
  }

  async function register(username: string, password: string) {
    await apiRegister({ username, password })
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('accessToken')
  }

  const isLoggedIn = () => !!token.value

  return { user, token, login, register, logout, isLoggedIn }
})
