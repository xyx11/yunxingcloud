import axios from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'

// Track in-flight requests for deduplication
const pending = new Map<string, AbortController>()

function getKey(config: InternalAxiosRequestConfig): string {
  return `${config.method}:${config.url}:${JSON.stringify(config.params || {})}:${JSON.stringify(config.data || {})}`
}

function addPending(config: InternalAxiosRequestConfig) {
  const key = getKey(config)
  if (pending.has(key)) {
    pending.get(key)!.abort()
  }
  const controller = new AbortController()
  config.signal = controller.signal
  pending.set(key, controller)
}

function removePending(config: InternalAxiosRequestConfig) {
  const key = getKey(config)
  pending.delete(key)
}

// Batch dedup: merge identical GET requests within a short window
const inFlight = new Map<string, Promise<any>>()
function dedupGet(config: InternalAxiosRequestConfig): InternalAxiosRequestConfig | Promise<InternalAxiosRequestConfig> {
  if (config.method !== 'get') return config
  const key = getKey(config)
  const existing = inFlight.get(key)
  if (existing) return existing.then(() => config)
  return config
}
function trackGet(config: InternalAxiosRequestConfig, promise: Promise<any>) {
  if (config.method !== 'get') return
  const key = getKey(config)
  inFlight.set(key, promise)
  promise.finally(() => { inFlight.delete(key) })
}

const request = axios.create({ baseURL: '/api', timeout: 15000 })

request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('accessToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  if (config.method === 'get') addPending(config)
  return dedupGet(config)
})

request.interceptors.response.use(
  (res) => { removePending(res.config); trackGet(res.config, Promise.resolve(res)); return res },
  async (err) => {
    removePending(err.config || {})
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
      localStorage.removeItem('user')
      const currentPath = window.location.pathname
      if (currentPath !== '/login') {
        import('@/router').then(({ default: router }) => {
          router.replace('/login?redirect=' + encodeURIComponent(currentPath + window.location.search))
        })
      }
    }
    if (err.response?.status >= 500) {
      console.error('[API] Server error:', err.response?.status, err.config?.url)
    }
    // Network error (no response) — show toast if available
    if (!err.response) {
      console.error('[API] Network error:', err.config?.url)
      // Fire a custom event that the app can listen to for showing a toast
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('api:network-error', { detail: { url: err.config?.url } }))
      }
    }
    return Promise.reject(err)
  }
)

export default request
