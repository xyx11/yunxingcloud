import request from '@/api/request'

export interface SysMenu {
  id: number
  name: string
  parentId: number | null
  sortOrder: number
  path: string
  component: string
  menuType: string
  perms: string
  icon: string
  visible: boolean
  status: string
  children?: SysMenu[]
}

export async function fetchMenuTree() {
  return request.get('/api/menus/tree')
}

export async function fetchAllMenus() {
  return request.get('/api/menus')
}

export async function createMenu(data: Partial<SysMenu>) {
  return request.post('/api/menus', data)
}

export async function updateMenu(id: number, data: Partial<SysMenu>) {
  return request.put(`/api/menus/${id}`, data)
}

export async function deleteMenu(id: number) {
  return request.delete(`/api/menus/${id}`)
}

export async function moveMenu(id: number, direction: number) {
  return request.put(`/api/menus/${id}/move`, { direction })
}
