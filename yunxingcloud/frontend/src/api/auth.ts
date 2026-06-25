import request from '@/api/request'

export interface LoginResponse {
  success: boolean
  username?: string
  redirectUrl?: string
  message?: string
}

export interface UserInfo {
  username: string
}

export async function login(user: string, password: string, code?: string) {
  return request.post('/api/login', { username: user, password, code })
}

export async function fetchUserInfo(): Promise<UserInfo> {
  const res = await request.get('/api/user')
  return res.data
}

export async function refreshToken(refreshToken: string): Promise<{ accessToken: string; refreshToken: string }> {
  const res = await request.post('/api/refresh', { refreshToken })
  return res.data
}

export async function logout() {
  return request.post('/api/logout')
}

export async function fetchCaptcha() {
  return request.get('/api/captcha')
}

export async function fetchPublicKey() {
  return request.get('/api/publicKey')
}

export async function register(data: { username: string; password: string; email: string }) {
  return request.post('/api/register', data)
}

export async function forgotPassword(email: string) {
  return request.post('/api/password/forgot', { email })
}

export async function resetPassword(token: string, newPassword: string) {
  return request.post('/api/password/reset', { token, newPassword })
}

export async function changePassword(oldPassword: string, newPassword: string) {
  return request.post('/api/password/change', { oldPassword, newPassword })
}
