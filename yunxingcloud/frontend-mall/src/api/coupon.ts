import request from './request'

export const getAvailableCoupons = () => request.get('/coupons/available')
export const getMyCoupons = () => request.get('/coupons/my')
export const claimCoupon = (id: number) => request.post(`/coupons/${id}/claim`)
export const getCouponsByBrand = (brandId: number) => request.get('/coupons', { params: { brandId } })