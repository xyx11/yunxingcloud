import request from './request'

export interface CartItem {
  id: number; productId: number; productName: string
  price: number; quantity: number; imageUrl?: string
}

async function refreshCount() {
  try { const r = await getCart(); const items = r.data || []; const count = items.reduce((s: number, i: any) => s + i.quantity, 0); localStorage.setItem('cart_count', String(count)); window.dispatchEvent(new CustomEvent('cart_updated')) } catch (e) { console.error('[cart] refreshCount failed:', e) }
}

export const getCart = () => request.get('/cart')
export const addToCart = async (productId: number, quantity: number) => {
  const r = await request.post('/cart', { productId, quantity })
  await refreshCount(); return r
}
export const removeFromCart = async (id: number) => {
  const r = await request.delete(`/cart/${id}`)
  await refreshCount(); return r
}
export const clearCart = async () => {
  const r = await request.delete('/cart')
  await refreshCount(); return r
}
