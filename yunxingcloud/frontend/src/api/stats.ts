import request from '@/api/request'

export async function fetchDashboard() {
  return request.get('/api/stats/dashboard')
}

export async function fetchRecentNotices() {
  return request.get('/api/stats/recent-notices')
}

export async function fetchRecentLogs(params?: { page: number; pageSize: number }) {
  return request.get('/api/operlog', { params })
}
