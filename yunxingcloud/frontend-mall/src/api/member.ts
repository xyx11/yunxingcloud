import request from './request'

export const getMemberTiers = () => request.get('/member/tiers')
export const getMemberBenefits = () => request.get('/member/benefits')
export const getPointsAccount = () => request.get('/points/account')
export const checkin = () => request.post('/points/checkin')
export const getCheckinStatus = () => request.get('/points/checkin/status')
export const getPointsRecords = (page = 1, size = 10) => request.get('/points/records', { params: { page, size } })
export const redeemPoints = (amount: number) => request.post('/points/redeem', { amount })
