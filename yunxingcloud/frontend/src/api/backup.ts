import request from '@/api/request'

export async function fetchBackups() {
  return request.get('/api/maintenance/backups')
}

export async function createBackup() {
  return request.post('/api/maintenance/backup')
}

export async function restoreBackup(filename: string) {
  return request.post(`/api/maintenance/restore/${filename}`)
}

export async function deleteBackup(filename: string) {
  return request.delete(`/api/maintenance/backups/${filename}`)
}
