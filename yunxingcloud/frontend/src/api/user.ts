import request from '@/api/request'

export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  departmentId: number
  departmentName: string
  postId: number
  registerSource: string
  enabled: boolean
  lastLoginTime?: string
  roles: { id: number; name: string; code: string }[]
}

export interface Dept {
  id: number
  name: string
  children?: Dept[]
}

export interface Role {
  id: number
  name: string
  code: string
}

export interface Post {
  id: number
  postCode: string
  postName: string
}

export interface UserListParams {
  page: number
  pageSize: number
  keyword?: string
}

export interface CreateUserPayload {
  username: string
  password: string
  nickname: string
  email: string
}

export interface UpdateUserPayload {
  nickname: string
  email: string
}

export async function fetchUsers(params: UserListParams) {
  return request.get('/api/users', { params })
}

export async function fetchDepartments() {
  return request.get('/api/departments')
}

export async function fetchRoles() {
  return request.get('/api/roles')
}

export async function fetchPosts() {
  return request.get('/api/posts')
}

export async function fetchUserSourceDict() {
  return request.get('/api/dict/data/sys_user_source')
}

export async function createUser(data: CreateUserPayload) {
  return request.post('/api/users', data)
}

export async function updateUserProfile(id: number, data: UpdateUserPayload) {
  return request.put(`/api/users/${id}/profile`, data)
}

export async function deleteUser(id: number) {
  return request.delete(`/api/users/${id}`)
}

export async function batchDeleteUsers(ids: number[]) {
  return request.delete('/api/users/batch', { data: { ids } })
}

export async function toggleUser(id: number) {
  return request.put(`/api/users/${id}/toggle`)
}

export async function setUserDepartment(id: number, departmentId: number | null) {
  return request.put(`/api/users/${id}/department`, { departmentId })
}

export async function setUserPost(id: number, postId: number | null) {
  return request.put(`/api/users/${id}/post`, { postId })
}

export async function setUserRoles(id: number, roleIds: number[]) {
  return request.put(`/api/users/${id}/roles`, { roleIds })
}

export async function resetUserPassword(id: number) {
  return request.post(`/api/users/${id}/reset-pwd`)
}

export async function fetchPendingUsers() {
  return request.get('/api/users/pending')
}

export async function fetchPendingApprovals() {
  return request.get('/api/users/pending-approval')
}

export async function approveUser(id: number) {
  return request.put(`/api/users/${id}/approve`)
}

export async function rejectUser(id: number) {
  return request.put(`/api/users/${id}/reject`)
}
