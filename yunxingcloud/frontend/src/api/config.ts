import request from '@/api/request'

export interface SysConfig {
  id: number
  name: string
  configKey: string
  configValue: string
  configType: string
  createdAt: string
}

export async function fetchConfigs() {
  return request.get('/api/config')
}

export async function createConfig(data: Partial<SysConfig>) {
  return request.post('/api/config', data)
}

export async function updateConfig(id: number, data: Partial<SysConfig>) {
  return request.put(`/api/config/${id}`, data)
}

export async function deleteConfig(id: number) {
  return request.delete(`/api/config/${id}`)
}

export async function fetchConfigByKey(key: string) {
  return request.get(`/api/config/key/${key}`)
}

export async function refreshFeatureFlags() {
  return request.post('/api/config/refresh-flags')
}
