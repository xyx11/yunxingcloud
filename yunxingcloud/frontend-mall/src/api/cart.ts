import request from './request'

export interface CartItem {
  id: number; productId: number; productName: string
  price: number; quantity: number; imageUrl?: string
}

export const getCart = () => request.get('/cart')
export const addToCart = (productId: number, quantity: number) =>
  request.post('/cart', { productId, quantity })
export const removeFromCart = (id: number) => request.delete(`/cart/${id}`)
export const clearCart = () => request.delete('/cart')
