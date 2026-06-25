import request from '@/api/request'

export interface SysRole {
  id: number
  name: string
  code: string
  description: string
  permissions: string
  enabled: boolean
  user_count?: number
  createdAt?: string
}

export async function fetchSysRoles() {
  return request.get('/api/roles')
}

export async function createRole(data: Partial<SysRole>) {
  return request.post('/api/roles', data)
}

export async function updateRole(id: number, data: Partial<SysRole>) {
  return request.put(`/api/roles/${id}`, data)
}

export async function deleteRole(id: number) {
  return request.delete(`/api/roles/${id}`)
}
