import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

const mockGet = vi.fn()
const mockPost = vi.fn()
const mockDelete = vi.fn()

vi.mock('@/api/request', () => ({
  default: { get: (...args: any[]) => mockGet(...args), post: (...args: any[]) => mockPost(...args), delete: (...args: any[]) => mockDelete(...args) },
}))

const mockToastError = vi.fn()
vi.mock('@/composables/useToast', () => ({
  useGlobalToast: () => ({ error: mockToastError }),
}))

import { useCartStore } from '@/stores/cart'

describe('useCartStore', () => {
  let storeData: Record<string, string>

  beforeEach(() => {
    storeData = {}
    const mockLocalStorage = {
      getItem: (k: string) => storeData[k] ?? null,
      setItem: (k: string, v: string) => { storeData[k] = v },
      removeItem: (k: string) => { delete storeData[k] },
      clear: () => { storeData = {} },
      length: 0,
      key: () => null,
    }
    vi.stubGlobal('localStorage', mockLocalStorage)

    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('initializes count from localStorage', () => {
    localStorage.setItem('cart_count', '5')
    const store = useCartStore()
    expect(store.state.count).toBe(5)
  })

  it('starts with count=0 when localStorage is empty', () => {
    const store = useCartStore()
    expect(store.state.count).toBe(0)
  })

  it('fetchCart updates count and items', async () => {
    mockGet.mockResolvedValue({ data: { items: [{ quantity: 2 }, { quantity: 3 }] } })
    const store = useCartStore()
    await store.fetchCart()
    expect(store.state.count).toBe(5)
    expect(store.state.items).toHaveLength(2)
  })

  it('fetchCart shows toast on error', async () => {
    mockGet.mockRejectedValue(new Error('network'))
    const store = useCartStore()
    await store.fetchCart()
    expect(mockToastError).toHaveBeenCalled()
  })

  it('incrementLocal updates count', () => {
    const store = useCartStore()
    store.incrementLocal(3)
    expect(store.state.count).toBe(3)
    store.incrementLocal(1)
    expect(store.state.count).toBe(4)
  })

  it('addToCart calls API and refreshes', async () => {
    mockPost.mockResolvedValue({})
    mockGet.mockResolvedValue({ data: { items: [{ quantity: 1 }] } })
    const store = useCartStore()
    await store.addToCart(1, 2)
    expect(mockPost).toHaveBeenCalledWith('/cart', { productId: 1, quantity: 2 })
    expect(store.state.count).toBe(1)
  })

  it('clearCart resets state', async () => {
    mockDelete.mockResolvedValue({})
    const store = useCartStore()
    store.state.count = 10
    store.state.items = [{ id: 1 }]
    await store.clearCart()
    expect(store.state.count).toBe(0)
    expect(store.state.items).toHaveLength(0)
  })
})
