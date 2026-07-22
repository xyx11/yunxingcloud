import request from './request'

export const getCompareList = () => request.get('/compare')
export const addCompare = (productId: number) => request.post(`/compare/${productId}`)
export const removeCompare = (productId: number) => request.delete(`/compare/${productId}`)
export const clearCompare = () => request.delete('/compare')
