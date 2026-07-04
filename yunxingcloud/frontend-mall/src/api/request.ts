import axios from 'axios'

const request = axios.create({ baseURL: '/api', timeout: 15000 })

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  (res) => res,
  async (err) => {
    const config = err.config
    // Retry network errors and 5xx up to 2 times
    if (!config._retryCount) config._retryCount = 0
    if (config._retryCount < 2 && (!err.response || err.response.status >= 500)) {
      config._retryCount++
      await new Promise(r => setTimeout(r, 500 * config._retryCount))
      return request(config)
    }
    if (err.response?.status === 401) {
      localStorage.removeItem('accessToken')
      if (window.location.pathname !== '/mall/login' && !window.location.pathname.startsWith('/mall/login')) {
        window.location.href = '/mall/login'
      }
    }
    if (err.response?.status >= 500) {
      console.error('[API] Server error:', err.response?.status, err.config?.url)
    }
    return Promise.reject(err)
  }
)

export default request
