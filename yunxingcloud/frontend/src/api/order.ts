import request from '@/api/request'

export interface OrderHead {
  id: number; orderNo: string; username: string; totalAmount: number
  status: string; paymentOrderId: number; receiverName: string
  receiverPhone: string; receiverAddress: string; remark: string
  createdAt: string; updatedAt: string
}

export function fetchOrders() { return request.get('/api/orders') }
export function getOrder(id: number) { return request.get(`/api/orders/${id}`) }
export function submitOrder(receiver?: Record<string, unknown>) { return request.post('/api/orders', receiver || {}) }
export function payOrder(id: number) { return request.post(`/api/orders/${id}/pay`) }
export function updateOrderStatus(id: number, status: string) { return request.put(`/api/orders/${id}/status`, { status }) }
export function cancelOrder(id: number) { return request.put(`/api/orders/${id}/cancel`) }
