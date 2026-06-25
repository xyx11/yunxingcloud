import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock localStorage
const lsMock: Record<string, string> = {}
vi.stubGlobal('localStorage', {
  getItem: (k: string) => lsMock[k] ?? null,
  setItem: (k: string, v: string) => { lsMock[k] = v },
  removeItem: (k: string) => { delete lsMock[k] },
  clear: () => { Object.keys(lsMock).forEach(k => delete lsMock[k]) },
})

vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  fetchUserInfo: vi.fn(),
}))

import { useAuthStore } from '@/stores/auth'
import { login, fetchUserInfo } from '@/api/auth'

describe('auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    Object.keys(lsMock).forEach(k => delete lsMock[k])
    vi.clearAllMocks()
  })

  it('starts unauthenticated', () => {
    const store = useAuthStore()
    expect(store.isAuthenticated).toBe(false)
    expect(store.username).toBeNull()
  })

  it('login sets username and stores tokens', async () => {
    const authStore = useAuthStore()
    ;(login as any).mockResolvedValue({
      data: { success: true, username: 'admin', accessToken: 'at123', refreshToken: 'rt123', redirectUrl: '/' },
    })

    const redirectUrl = await authStore.login('admin', 'pass')
    expect(redirectUrl).toBe('/')
    expect(authStore.username).toBe('admin')
    expect(authStore.isAuthenticated).toBe(true)
    expect(localStorage.getItem('accessToken')).toBe('at123')
    expect(localStorage.getItem('refreshToken')).toBe('rt123')
  })

  it('login throws on failure', async () => {
    const authStore = useAuthStore()
    ;(login as any).mockResolvedValue({ data: { success: false, message: 'error' } })

    await expect(authStore.login('admin', 'wrong')).rejects.toThrow('error')
    expect(authStore.isAuthenticated).toBe(false)
  })

  it('clear removes all state', () => {
    const authStore = useAuthStore()
    authStore.username = 'admin'
    localStorage.setItem('accessToken', 'token')
    localStorage.setItem('refreshToken', 'refresh')
    authStore.clear()

    expect(authStore.username).toBeNull()
    expect(authStore.isAuthenticated).toBe(false)
    expect(localStorage.getItem('accessToken')).toBeNull()
    expect(localStorage.getItem('refreshToken')).toBeNull()
  })

  it('fetchUser returns user data', async () => {
    const authStore = useAuthStore()
    ;(fetchUserInfo as any).mockResolvedValue({ username: 'testuser' })

    await authStore.fetchUser()
    expect(authStore.username).toBe('testuser')
    expect(authStore.isAuthenticated).toBe(true)
  })
})