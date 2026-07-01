import request from './request'

export interface OrderHead {
  id: number; orderNo: string; username: string
  totalAmount: number; status: string; receiverName?: string
  receiverPhone?: string; receiverAddress?: string; createdAt?: string
}
export interface Address {
  id: number; name: string; phone: string; address: string
  isDefault?: boolean; city?: string; district?: string; zipCode?: string
}
export interface Coupon {
  id: number; name: string; amount: number; minAmount?: number
  expireAt?: string; status?: string
}

// Orders
export const getOrders = () => request.get('/orders')
export const getOrderById = (id: number) => request.get(`/orders/${id}`)
export const submitOrder = (receiver: Record<string, string>) =>
  request.post('/orders', receiver)
export const payOrder = (id: number, channel?: string) =>
  request.post(`/orders/${id}/pay`, { channel: channel || 'wechat' })
export const cancelOrder = (id: number) => request.put(`/orders/${id}/cancel`)

// Addresses
export const getAddresses = () => request.get('/addresses')
export const createAddress = (data: Partial<Address>) => request.post('/addresses', data)
export const updateAddress = (id: number, data: Partial<Address>) =>
  request.put(`/addresses/${id}`, data)
export const deleteAddress = (id: number) => request.delete(`/addresses/${id}`)
export const setDefaultAddress = (id: number, isDefault: boolean) =>
  request.put(`/addresses/${id}`, { isDefault })

// Favorites
export const getFavorites = () => request.get('/favorites')
export const checkFavorite = (productId: number) =>
  request.get(`/favorites/${productId}/check`)
export const addFavorite = (productId: number) => request.post(`/favorites/${productId}`)
export const removeFavorite = (productId: number) => request.delete(`/favorites/${productId}`)

// Coupons
export const getMyCoupons = () => request.get('/coupons/my')

// Shipments
export const getShipments = () => request.get('/shipments')
export const getOrderShipments = (orderId: number) => request.get(`/shipments?orderId=${orderId}`)
