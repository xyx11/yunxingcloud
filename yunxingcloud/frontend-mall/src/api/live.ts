import request from './request'

export const getLiveRooms = () => request.get('/live/rooms')
export const getLiveRoom = (id: number) => request.get(`/live/rooms/${id}`)
export const setLiveRemind = (roomId: number) => request.post(`/live/${roomId}/remind`)
export const reportLiveView = (roomId: number) => request.post(`/live/rooms/${roomId}/view`)
