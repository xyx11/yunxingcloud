import { reactive } from 'vue'
import { defineStore } from 'pinia'
import request from '@/api/request'

interface CartState {
  count: number
  items: any[]
}

export const useCartStore = defineStore('cart', () => {
  const state = reactive<CartState>({
    count: parseInt(localStorage.getItem('cart_count') || '0'),
    items: [],
  })

  async function fetchCart() {
    try {
      const r = await request.get('/cart')
      const items = r.data?.items || []
      state.items = items
      state.count = items.reduce((sum: number, i: any) => sum + (i.quantity || 1), 0)
      persist()
    } catch {
      /* fallback toast — view should handle errors */
    }
  }

  async function addToCart(productId: number, quantity = 1) {
    try {
      await request.post('/cart', { productId, quantity })
      await fetchCart()
      window.dispatchEvent(new CustomEvent('cart_updated'))
    } catch (e) {
      throw e
    }
  }

  async function removeFromCart(id: number) {
    try {
      await request.delete(`/cart/${id}`)
      await fetchCart()
      window.dispatchEvent(new CustomEvent('cart_updated'))
    } catch (e) {
      throw e
    }
  }

  async function clearCart() {
    try {
      await request.delete('/cart')
      state.items = []
      state.count = 0
      persist()
      window.dispatchEvent(new CustomEvent('cart_updated'))
    } catch (e) {
      throw e
    }
  }

  function persist() {
    localStorage.setItem('cart_count', String(state.count))
  }

  function incrementLocal(delta = 1) {
    state.count += delta
    persist()
  }

  return { state, fetchCart, addToCart, removeFromCart, clearCart, incrementLocal }
})