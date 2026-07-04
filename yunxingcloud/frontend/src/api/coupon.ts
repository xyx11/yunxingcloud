import request from './request'

export interface Coupon {
  id?: number; name: string; type: string; threshold: number; amount: number
  totalQty?: number; usedQty?: number; startTime?: string; endTime?: string; status?: string
  createdAt?: string
}

export function fetchCoupons() { return request.get('/api/coupons') }
export function createCoupon(data: Coupon) { return request.post('/api/coupons', data) }
export function updateCoupon(id: number, data: Coupon) { return request.put(`/api/coupons/${id}`, data) }
export function deleteCoupon(id: number) { return request.delete(`/api/coupons/${id}`) }
