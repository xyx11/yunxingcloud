import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const request = axios.create({
  baseURL: '',
  withCredentials: true,
  timeout: 30000,
})

// 请求拦截器：注入 JWT Bearer token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：处理 401 并自动刷新 token
let isRefreshing = false
let refreshSubscribers: ((token: string) => void)[] = []

function onRefreshed(token: string) {
  refreshSubscribers.forEach((cb) => cb(token))
  refreshSubscribers = []
}

request.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    if (error.response?.status === 401 && !originalRequest._retry) {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        if (isRefreshing) {
          return new Promise((resolve) => {
            refreshSubscribers.push((token: string) => {
              originalRequest.headers['Authorization'] = `Bearer ${token}`
              resolve(request(originalRequest))
            })
          })
        }
        originalRequest._retry = true
        isRefreshing = true
        try {
          const res = await axios.post('/api/refresh', { refreshToken })
          const { accessToken, refreshToken: newRefreshToken } = res.data
          localStorage.setItem('accessToken', accessToken)
          localStorage.setItem('refreshToken', newRefreshToken)
          onRefreshed(accessToken)
          originalRequest.headers['Authorization'] = `Bearer ${accessToken}`
          return request(originalRequest)
        } catch {
          const authStore = useAuthStore()
          authStore.clear()
          window.location.hash = `#/login?redirect=${encodeURIComponent(window.location.hash.slice(1) || '/')}`
          return Promise.reject(error)
        } finally {
          isRefreshing = false
        }
      }
      const authStore = useAuthStore()
      authStore.clear()
      window.location.hash = `#/login?redirect=${encodeURIComponent(window.location.hash.slice(1) || '/')}`
    }
    // 非401且非网络中断的错误日志
    if (error.response) {
      if (error.response.status !== 401) {
        console.error(`[${error.response.status}] ${error.response.data?.message || '服务器错误'}`)
      }
    }
    return Promise.reject(error)
  },
)

export default request
