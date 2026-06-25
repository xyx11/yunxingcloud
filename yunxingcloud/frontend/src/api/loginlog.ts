import request from '@/api/request'

export interface LoginInfo {
  id: number
  userName: string
  ipaddr: string
  loginLocation: string
  browser: string
  os: string
  status: string
  msg: string
  loginTime: string
}

export interface LoginStats {
  todayCount: number
  todayFailCount: number
  weekCount: number
  monthCount: number
}

export async function fetchLoginInfos(params?: { page: number; pageSize: number; keyword?: string }) {
  return request.get('/api/logininfor', { params })
}

export async function fetchLoginStats() {
  return request.get('/api/logininfor/stats')
}

export async function deleteLoginInfo(id: number) {
  return request.delete(`/api/logininfor/${id}`)
}

export async function cleanLoginInfos() {
  return request.delete('/api/logininfor/clean')
}
