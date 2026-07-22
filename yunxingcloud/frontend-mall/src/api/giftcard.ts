import request from './request'

export const getMyGiftCards = () => request.get('/gift-cards/my')
export const queryGiftCard = (cardNo: string) => request.get(`/gift-cards/${encodeURIComponent(cardNo)}`)
export const activateGiftCard = (cardNo: string) => request.post(`/gift-cards/${encodeURIComponent(cardNo)}/activate`)
export const useGiftCardPay = (cardNo: string, amount: number) => request.post(`/gift-cards/${encodeURIComponent(cardNo)}/pay`, { amount })
export const getGiftCardHistory = (cardId: number) => request.get(`/gift-cards/${cardId}/history`)