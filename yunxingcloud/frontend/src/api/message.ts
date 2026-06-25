import request from '@/api/request'

export interface Message {
  id: number
  title: string
  content: string
  senderName: string
  receiverName: string
  isRead: boolean
  createdAt: string
}

export async function fetchInbox() {
  return request.get('/api/messages/inbox')
}

export async function fetchSent() {
  return request.get('/api/messages/sent')
}

export async function sendMessage(data: { toUser?: string; receiverName?: string; title: string; content: string }) {
  return request.post('/api/messages', data)
}

export async function markAsRead(id: number) {
  return request.put(`/api/messages/${id}/read`)
}

export async function fetchUnreadCount() {
  return request.get('/api/messages/unread-count')
}

export async function deleteMessage(id: number) {
  return request.delete(`/api/messages/${id}`)
}
