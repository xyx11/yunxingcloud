import request from './request'

export const trackByOrder = (orderId: number | string) => request.get(`/logistics/order/${orderId}`)
export const trackByNo = (trackingNo: string) => request.get(`/logistics/track/${trackingNo}`)
export const getShipments = () => request.get('/shipments')