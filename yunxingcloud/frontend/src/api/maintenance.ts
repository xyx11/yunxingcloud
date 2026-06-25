import request from '@/api/request'

export async function fetchMaintenanceStats() {
  return request.get('/api/maintenance/stats')
}

export async function cleanExpiredTokens() {
  return request.post('/api/maintenance/clean-tokens')
}

export async function cleanOldLogs(days: number) {
  return request.post(`/api/maintenance/clean-logs?days=${days}`)
}

export async function vacuumDatabase() {
  return request.post('/api/maintenance/vacuum')
}
