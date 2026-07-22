import request from './request'

export const getAfterSales = () => request.get('/after-sale')
export const createAfterSale = (data: Record<string, any>) => request.post('/after-sale', data)
export const cancelAfterSale = (id: string) => request.put(`/after-sale/${id}/cancel`)