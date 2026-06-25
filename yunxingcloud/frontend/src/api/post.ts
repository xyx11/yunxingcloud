import request from '@/api/request'

export interface SysPost {
  id: number
  postCode: string
  postName: string
  postSort: number
  status: string
  remark: string
  createdAt: string
}

export async function fetchPosts() {
  return request.get('/api/posts')
}

export async function createPost(data: Partial<SysPost>) {
  return request.post('/api/posts', data)
}

export async function updatePost(id: number, data: Partial<SysPost>) {
  return request.put(`/api/posts/${id}`, data)
}

export async function deletePost(id: number) {
  return request.delete(`/api/posts/${id}`)
}
