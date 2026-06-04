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

export async function fetchUserInfo(): Promise<UserInfo> {
  const res = await request.get('/api/user')
  return res.data
}

export async function refreshToken(refreshToken: string): Promise<{ accessToken: string; refreshToken: string }> {
  const res = await request.post('/api/refresh', { refreshToken })
  return res.data
}
