import request from './request'
export const queryGiftCard = (cardNo: string) => request.get(`/api/gift-cards/${cardNo}`)
export const createGiftCard = (amount: number, expireDays: number) =>
  request.post('/api/gift-cards', { amount, expireDays })