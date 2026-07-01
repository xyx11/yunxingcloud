import request from './request'
export interface Campaign { id?:number; name:string; type:string; threshold?:number; discount?:number; startTime?:string; endTime?:string; status?:string }
export const fetchCampaigns = () => request.get('/api/campaigns')
export const createCampaign = (data: Campaign) => request.post('/api/campaigns', data)