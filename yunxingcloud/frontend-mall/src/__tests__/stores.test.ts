import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

function mockLocalStorage() {
  const store: Record<string, string> = {}
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => { store[key] = value },
    removeItem: (key: string) => { delete store[key] },
    clear: () => { Object.keys(store).forEach(k => delete store[k]) },
  }
}

Object.defineProperty(globalThis, 'localStorage', { value: mockLocalStorage() })

describe('authStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('should initialize with no user', () => {
    const store = useAuthStore()
    expect(store.user).toBeNull()
    expect(store.token).toBe('')
    expect(store.isLoggedIn).toBe(false)
  })

  it('should restore token from localStorage', () => {
    localStorage.setItem('accessToken', 'test-token-123')
    const store = useAuthStore()
    expect(store.token).toBe('test-token-123')
    expect(store.isLoggedIn).toBe(true)
  })

  it('should clear state on logout', () => {
    const store = useAuthStore()
    store.token = 'some-token'
    store.user = { username: 'test' }
    store.logout()

    expect(store.user).toBeNull()
    expect(store.token).toBe('')
    expect(localStorage.getItem('accessToken')).toBeNull()
  })
})