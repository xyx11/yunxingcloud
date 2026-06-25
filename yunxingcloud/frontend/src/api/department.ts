import request from '@/api/request'

export interface Department {
  id: number
  name: string
  parentId: number | null
  sortOrder: number
  enabled: boolean
  children?: Department[]
}

export async function fetchDepartmentTree() {
  return request.get('/api/departments')
}

export async function createDepartment(data: Partial<Department>) {
  return request.post('/api/departments', data)
}

export async function updateDepartment(id: number, data: Partial<Department>) {
  return request.put(`/api/departments/${id}`, data)
}

export async function deleteDepartment(id: number) {
  return request.delete(`/api/departments/${id}`)
}

export async function moveDepartment(id: number, direction: number) {
  return request.put(`/api/departments/${id}/move`, { direction })
}
