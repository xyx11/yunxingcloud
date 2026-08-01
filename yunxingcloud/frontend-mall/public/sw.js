const CACHE_NAME = 'yxcloud-mall-v7'
const API_CACHE = 'yxcloud-api-v2'
const READONLY_API = ['/api/products', '/api/categories', '/api/brands', '/api/banners', '/api/home', '/api/flash-sale']

self.addEventListener('install', () => {
  self.skipWaiting()
})

self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(
        keys
          .filter((k) => k.startsWith('yxcloud-'))
          .map((k) => caches.delete(k))
      )
    )
  )
  self.clients.claim()
})

self.addEventListener('fetch', (e) => {
  if (e.request.method !== 'GET') return

  // Navigation: always network first
  if (e.request.mode === 'navigate') {
    e.respondWith(fetch(e.request).catch(() => caches.match(e.request)))
    return
  }

  // API: stale-while-revalidate for read-only, network-first for others
  if (e.request.url.includes('/api/')) {
    const isReadOnly = READONLY_API.some(p => e.request.url.includes(p))
    e.respondWith(isReadOnly ? staleWhileRevalidate(e.request) : fetch(e.request).catch(() => caches.match(e.request)))
    return
  }

  // Static assets: network first, cache fallback
  if (e.request.destination === 'script' || e.request.destination === 'style' ||
      e.request.destination === 'image' || e.request.destination === 'font' ||
      e.request.url.match(/\.(js|css|png|jpg|jpeg|gif|svg|ico|woff2?)$/)) {
    e.respondWith(networkFirstCache(e.request))
    return
  }

  e.respondWith(fetch(e.request).catch(() => caches.match(e.request)))
})

async function staleWhileRevalidate(req) {
  const cache = await caches.open(API_CACHE)
  const cached = await cache.match(req)
  const p = fetch(req).then(r => { if (r.ok) cache.put(req, r.clone()); return r }).catch(() => cached)
  return cached || p
}

async function networkFirstCache(req) {
  try {
    const resp = await fetch(req)
    if (resp.ok) {
      const cache = await caches.open(CACHE_NAME)
      cache.put(req, resp.clone())
    }
    return resp
  } catch {
    return caches.match(req)
  }
}
