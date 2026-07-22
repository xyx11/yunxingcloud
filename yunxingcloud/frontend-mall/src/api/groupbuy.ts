import request from './request'

export const getGroupBuys = () => request.get('/group-buy')
export const getMyGroupBuys = () => request.get('/group-buy/my')
export const openGroup = (id: number) => request.post(`/group-buy/${id}/open`)
export const joinGroup = (id: number) => request.post(`/group-buy/${id}/join`)