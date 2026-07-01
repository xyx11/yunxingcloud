import request from './request'

export interface Notification {
  id?: number; username: string; title: string; content?: string; type?: string
}
export const fetchNotifications = () => request.get('/api/notifications')
export const unreadCount = () => request.get('/api/notifications/unread-count')
export const markRead = (id: number) => request.put(`/api/notifications/${id}/read`)
export const markAllRead = () => request.put('/api/notifications/read-all')
export const sendNotification = (data: Notification) => request.post('/api/notifications', data)