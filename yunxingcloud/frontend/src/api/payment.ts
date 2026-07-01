import request from '@/api/request'

export interface PaymentOrder {
  id: number
  orderNo: string
  title: string
  amount: number
  channel: string
  status: string
  tradeNo: string
  buyer: string
  paidAt: string
  refundAmount: number
  refundReason: string
  refundAt: string
  createdAt: string
  updatedAt: string
}

export function fetchOrders() {
  return request.get('/api/payment/orders')
}

export function getOrder(id: number) {
  return request.get(`/api/payment/orders/${id}`)
}

export function createOrder(data: { title: string; amount: number; channel: string }) {
  return request.post('/api/payment/orders', data)
}

export function payOrder(id: number, notifyUrl?: string) {
  return request.post(`/api/payment/orders/${id}/pay`, { notifyUrl: notifyUrl || '' })
}

export function refundOrder(id: number, refundAmount: number, reason: string) {
  return request.post(`/api/payment/orders/${id}/refund`, { refundAmount, reason })
}

export function fetchRecords(orderId: number) {
  return request.get(`/api/payment/orders/${orderId}/records`)
}
