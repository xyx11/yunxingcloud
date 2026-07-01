import request from './request'

export interface FlashSale {
  id?: number; productId: number; flashPrice: number
  stock: number; limitPerUser?: number; startTime: string; endTime: string; status?: string
}

export const fetchFlashSales = () => request.get('/api/flash-sale')
export const getFlashSale = (id: number) => request.get(`/api/flash-sale/${id}`)
export const createFlashSale = (data: FlashSale) => request.post('/api/flash-sale', data)
export const preheatFlashSale = (id: number) => request.post(`/api/flash-sale/${id}/preheat`)