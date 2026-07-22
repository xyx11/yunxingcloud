import request from './request'

export const getFlashSales = () => request.get('/flash-sale')
export const getFlashSaleDetail = (id: number) => request.get(`/flash-sale/${id}`)
export const buyFlashSale = (id: number, productId: number) => request.post(`/flash-sale/${id}/buy`, null, { params: { productId } })
export const setFlashRemind = (saleId: number, productId?: number) => request.post('/flash-sale/remind', { saleId, productId })