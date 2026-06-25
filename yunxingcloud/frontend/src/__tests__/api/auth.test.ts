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
import {
  login, fetchUserInfo, logout, fetchCaptcha, fetchPublicKey,
  register, forgotPassword, resetPassword, changePassword, refreshToken,
} from '@/api/auth'

describe('auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('login calls correct endpoint', async () => {
    await login('admin', 'pass123', 'abcd')
    expect(request.post).toHaveBeenCalledWith('/api/login', { username: 'admin', password: 'pass123', code: 'abcd' })
  })

  it('login works without captcha code', async () => {
    await login('admin', 'pass123')
    expect(request.post).toHaveBeenCalledWith('/api/login', { username: 'admin', password: 'pass123', code: undefined })
  })

  it('fetchUserInfo calls correct endpoint', async () => {
    (request.get as any).mockResolvedValue({ data: {} })
    await fetchUserInfo()
    expect(request.get).toHaveBeenCalledWith('/api/user')
  })

  it('logout calls correct endpoint', async () => {
    await logout()
    expect(request.post).toHaveBeenCalledWith('/api/logout')
  })

  it('fetchCaptcha calls correct endpoint', async () => {
    await fetchCaptcha()
    expect(request.get).toHaveBeenCalledWith('/api/captcha')
  })

  it('fetchPublicKey calls correct endpoint', async () => {
    await fetchPublicKey()
    expect(request.get).toHaveBeenCalledWith('/api/publicKey')
  })

  it('register calls correct endpoint', async () => {
    await register({ username: 'newuser', password: 'pass', email: 'a@b.com' })
    expect(request.post).toHaveBeenCalledWith('/api/register', { username: 'newuser', password: 'pass', email: 'a@b.com' })
  })

  it('forgotPassword calls correct endpoint', async () => {
    await forgotPassword('a@b.com')
    expect(request.post).toHaveBeenCalledWith('/api/password/forgot', { email: 'a@b.com' })
  })

  it('resetPassword calls correct endpoint', async () => {
    await resetPassword('token123', 'newpass')
    expect(request.post).toHaveBeenCalledWith('/api/password/reset', { token: 'token123', newPassword: 'newpass' })
  })

  it('changePassword calls correct endpoint', async () => {
    await changePassword('oldpass', 'newpass')
    expect(request.post).toHaveBeenCalledWith('/api/password/change', { oldPassword: 'oldpass', newPassword: 'newpass' })
  })

  it('refreshToken calls correct endpoint', async () => {
    (request.post as any).mockResolvedValue({ data: {} })
    await refreshToken('rt123')
    expect(request.post).toHaveBeenCalledWith('/api/refresh', { refreshToken: 'rt123' })
  })
})