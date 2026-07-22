import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'

function createMockRouter() {
  return createRouter({
    history: createWebHistory('/'),
    routes: [
      { path: '/', component: { template: '<div>Home</div>' } },
      { path: '/login', component: LoginView },
    ],
  })
}

describe('LoginView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    // Mock localStorage for jsdom
    const store: Record<string, string> = {}
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation((k: string) => store[k] ?? null)
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation((k: string, v: string) => { store[k] = v })
    vi.spyOn(Storage.prototype, 'removeItem').mockImplementation((k: string) => { delete store[k] })
    vi.spyOn(Storage.prototype, 'clear').mockImplementation(() => { Object.keys(store).forEach(k => delete store[k]) })
    // mock IntersectionObserver
    ;(globalThis as any).IntersectionObserver = class {
      observe() {}
      disconnect() {}
      unobserve() {}
    }
  })

  it('renders login form', async () => {
    const router = createMockRouter()
    router.push('/login')
    await router.isReady()
    const wrapper = mount(LoginView, {
      global: { plugins: [router, createPinia()] },
    })
    expect(wrapper.text()).toContain('YXCLOUD')
  })

  it('has username and password inputs', async () => {
    const router = createMockRouter()
    router.push('/login')
    await router.isReady()
    const wrapper = mount(LoginView, {
      global: { plugins: [router, createPinia()] },
    })
    const inputs = wrapper.findAll('input')
    expect(inputs.length).toBeGreaterThanOrEqual(2)
  })

  it('has submit button', async () => {
    const router = createMockRouter()
    router.push('/login')
    await router.isReady()
    const wrapper = mount(LoginView, {
      global: { plugins: [router, createPinia()] },
    })
    const buttons = wrapper.findAll('button')
    const submitBtn = buttons.find(b => b.text().includes('Login') || b.text().includes('登录') || b.attributes('type') === 'submit')
    expect(submitBtn).toBeDefined()
  })

  it('has link to register page', async () => {
    const router = createMockRouter()
    router.push('/login')
    await router.isReady()
    const wrapper = mount(LoginView, {
      global: { plugins: [router, createPinia()] },
    })
    // Check for register-related text or navigation link
    const text = wrapper.text().toLowerCase()
    expect(text.includes('register') || text.includes('注册') || text.includes('sign up')).toBe(true)
  })
})
