import request from '@/api/request'

export interface SysNotice {
  id: number
  noticeTitle: string
  noticeType: string
  noticeContent: string
  status: string
  publishAt: string
  createdAt: string
}

export async function fetchNotices() {
  return request.get('/api/notices')
}

export async function fetchLatestNotices() {
  return request.get('/api/notices/latest')
}

export async function createNotice(data: Partial<SysNotice>) {
  return request.post('/api/notices', data)
}

export async function updateNotice(id: number, data: Partial<SysNotice>) {
  return request.put(`/api/notices/${id}`, data)
}

export async function deleteNotice(id: number) {
  return request.delete(`/api/notices/${id}`)
}
