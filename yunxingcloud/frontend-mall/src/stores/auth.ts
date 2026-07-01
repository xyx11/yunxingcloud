import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin, register as apiRegister } from '@/api/auth'

function safeGet(k: string) { try { return localStorage.getItem(k) || '' } catch { return '' } }
function safeSet(k: string, v: string) { try { localStorage.setItem(k, v) } catch {} }
function safeRemove(k: string) { try { localStorage.removeItem(k) } catch {} }

export const useAuthStore = defineStore('auth', () => {
  const user = ref<any>(null)
  const token = ref(safeGet('accessToken'))

  async function login(username: string, password: string) {
    const res = await apiLogin({ username, password })
    token.value = res.data.accessToken
    safeSet('accessToken', token.value)
    user.value = { username }
  }

  async function register(username: string, password: string) {
    await apiRegister({ username, password })
  }

  function logout() {
    token.value = ''
    user.value = null
    safeRemove('accessToken')
  }

  const isLoggedIn = () => !!token.value

  return { user, token, login, register, logout, isLoggedIn }
})
