import axios from 'axios'
import { getCsrfToken } from '@/composables/useCsrf'

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
  const res = await axios.get('/api/user')
  return res.data
}

export async function fetchCsrfToken(): Promise<void> {
  await axios.get('/api/csrf')
}
