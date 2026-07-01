import request from './request'

export interface GroupBuy {
  id?: number; productId: number; minMembers: number
  groupPrice: number; startTime?: string; endTime?: string; status?: string
}

export const fetchGroupBuys = () => request.get('/api/group-buy')
export const createGroupBuy = (data: GroupBuy) => request.post('/api/group-buy', data)
export const expireGroupBuys = () => request.post('/api/group-buy/expire')