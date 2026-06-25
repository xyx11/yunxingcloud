import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/api/request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

import request from '@/api/request'
import { fetchUsers, createUser, updateUserProfile, batchDeleteUsers, toggleUser, resetUserPassword } from '@/api/user'

describe('user API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('fetchUsers calls correct endpoint with params', async () => {
    const params = { page: 1, pageSize: 10, keyword: 'test' }
    await fetchUsers(params)
    expect(request.get).toHaveBeenCalledWith('/api/users', { params })
  })

  it('createUser calls correct endpoint', async () => {
    const data = { username: 'newuser', password: 'pass', nickname: 'New', email: 'new@test.com' }
    await createUser(data)
    expect(request.post).toHaveBeenCalledWith('/api/users', data)
  })

  it('updateUserProfile calls correct endpoint', async () => {
    await updateUserProfile(1, { nickname: 'Updated', email: 'updated@test.com' })
    expect(request.put).toHaveBeenCalledWith('/api/users/1/profile', { nickname: 'Updated', email: 'updated@test.com' })
  })

  it('batchDeleteUsers calls correct endpoint', async () => {
    await batchDeleteUsers([1, 2, 3])
    expect(request.delete).toHaveBeenCalledWith('/api/users/batch', { data: { ids: [1, 2, 3] } })
  })

  it('toggleUser calls correct endpoint', async () => {
    await toggleUser(5)
    expect(request.put).toHaveBeenCalledWith('/api/users/5/toggle')
  })

  it('resetUserPassword calls correct endpoint', async () => {
    await resetUserPassword(3)
    expect(request.post).toHaveBeenCalledWith('/api/users/3/reset-pwd')
  })
})
