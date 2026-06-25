import request from '@/api/request'

export async function globalSearch(q: string) {
  return request.get('/api/search', { params: { q } })
}