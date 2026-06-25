import request from '@/api/request'

export interface OperLog {
  id: number
  title: string
  businessType: string
  method: string
  requestMethod: string
  operatorType: string
  operName: string
  operUrl: string
  operIp: string
  operLocation: string
  operParam: string
  jsonResult: string
  status: string
  errorMsg: string
  operTime: string
}

export interface OperLogParams {
  page: number
  pageSize: number
  keyword?: string
  status?: string
  businessType?: string
}

export async function fetchOperLogs(params: OperLogParams) {
  return request.get('/api/operlog', { params })
}

export async function deleteOperLog(id: number) {
  return request.delete(`/api/operlog/${id}`)
}

export async function batchDeleteOperLogs(ids: number[]) {
  return request.delete('/api/operlog/batch', { data: { ids } })
}

export async function cleanOperLogs() {
  return request.delete('/api/operlog/clean')
}

export async function exportOperLogs(params?: Record<string, string>) {
  return request.get('/api/operlog/export', { params, responseType: 'blob' })
}
