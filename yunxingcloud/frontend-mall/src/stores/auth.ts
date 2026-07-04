import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, register as apiRegister } from '@/api/auth'
import type { UserInfo } from '@/types'

function safeGet(k: string) { try { return localStorage.getItem(k) || '' } catch { return '' } }
function safeSet(k: string, v: string) { try { localStorage.setItem(k, v) } catch {} }
function safeRemove(k: string) { try { localStorage.removeItem(k) } catch {} }
function safeParseJSON(k: string, d: any = null) { try { const v = safeGet(k); return v ? JSON.parse(v) : d } catch { return d } }

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserInfo | null>(safeParseJSON('user'))
  const token = ref(safeGet('accessToken'))

  async function login(username: string, password: string) {
    const res = await apiLogin({ username, password })
    token.value = res.data.accessToken
    safeSet('accessToken', token.value)
    user.value = { username }
    safeSet('user', JSON.stringify({ username }))
  }

  async function register(username: string, password: string) {
    await apiRegister({ username, password })
  }

  function logout() {
    token.value = ''
    user.value = null
    safeRemove('accessToken')
    safeRemove('user')
  }

  const isLoggedIn = computed(() => !!token.value)

  return { user, token, login, register, logout, isLoggedIn }
})
