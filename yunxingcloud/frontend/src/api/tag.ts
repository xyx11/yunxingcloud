import request from './request'
export interface Tag { id?:number; name:string; color?:string; sortOrder?:number }
export const fetchTags = () => request.get('/api/tags')
export const createTag = (data: Tag) => request.post('/api/tags', data)
export const getProductTags = (productId: number) => request.get(`/api/tags/product/${productId}`)
export const addProductTag = (productId: number, tagId: number) => request.post(`/api/tags/product/${productId}/${tagId}`)
export const removeProductTag = (productId: number, tagId: number) => request.delete(`/api/tags/product/${productId}/${tagId}`)