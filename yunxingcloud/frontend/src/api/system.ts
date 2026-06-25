import request from '@/api/request'

export async function fetchSystemInfo() {
  return request.get('/api/system/info')
}

export async function fetchSessions() {
  return request.get('/api/system/sessions')
}

export async function revokeSession(username: string) {
  return request.post('/api/system/sessions/revoke', { username })
}

export async function fetchCacheInfo() {
  return request.get('/api/system/cache')
}

export async function clearCache() {
  return request.post('/api/system/cache/clear')
}

export async function fetchDbHealth() {
  return request.get('/api/health/db')
}

export async function fetchDiskHealth() {
  return request.get('/api/health/disk')
}

export async function fetchBenchmark() {
  return request.get('/api/system/benchmark')
}

export async function fetchFlags() {
  return request.get('/api/system/flags')
}

export async function saveAnnouncement(announcement: string) {
  return request.post('/api/system/flags', { announcement })
}
