import request from './request'

export const getBundles = () => request.get('/bundles')
export const getBundleById = (id: number) => request.get(`/bundles/${id}`)
