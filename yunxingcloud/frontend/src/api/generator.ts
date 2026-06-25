import request from '@/api/request'

export async function fetchGeneratorTables() {
  return request.get('/api/generator/tables')
}

export async function fetchGeneratorTableColumns(tableName: string) {
  return request.get(`/api/generator/table/${tableName}`)
}

export async function generateCode(tableName: string, packageName: string) {
  return request.post(`/api/generator/generate/${tableName}`, { packageName })
}