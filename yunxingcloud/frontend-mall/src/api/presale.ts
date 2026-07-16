import request from './request'

export const getPresales = () => request.get('/presale')
export const getPresaleById = (id: number) => request.get(`/presale/${id}`)
export const payDeposit = (id: number) => request.post(`/presale/${id}/deposit`)
export const payFinal = (id: number, orderId: number) => request.post(`/presale/${id}/final-pay`, { orderId })
