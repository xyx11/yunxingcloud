import request from './request'

export interface AfterSale {
  id?: number; orderId: number; type: string; reason?: string
  refundAmount?: number; evidenceUrls?: string; status?: string
}

export const fetchAfterSales = () => request.get('/api/after-sale')
export const approveAfterSale = (id: number, remark?: string) =>
  request.put(`/api/after-sale/${id}/approve`, { remark })
export const rejectAfterSale = (id: number, remark: string) =>
  request.put(`/api/after-sale/${id}/reject`, { remark })