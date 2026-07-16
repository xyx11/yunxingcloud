const CACHE_NAME = 'yxcloud-mall-v3'
const STATIC_ASSETS = ['/', '/index.html', '/manifest.json']

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(STATIC_ASSETS)))
  self.skipWaiting()
})

self.addEventListener('activate', (e) => {
  e.waitUntil(caches.keys().then((keys) => Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))))
  self.clients.claim()
})

self.addEventListener('fetch', (e) => {
  if (e.request.method !== 'GET') return

  // API: network first, cache fallback
  if (e.request.url.includes('/api/')) {
    e.respondWith(networkFirst(e.request))
    return
  }

  // Static assets: cache first
  if (e.request.destination === 'script' || e.request.destination === 'style' ||
      e.request.destination === 'image' || e.request.destination === 'font' ||
      e.request.url.match(/\.(js|css|png|jpg|jpeg|gif|svg|ico|woff2?)$/)) {
    e.respondWith(cacheFirst(e.request))
    return
  }

  // Navigation: network first
  e.respondWith(networkFirst(e.request))
})

async function cacheFirst(req) {
  const cached = await caches.match(req)
  if (cached) return cached
  try {
    const resp = await fetch(req)
    if (resp.ok) {
      const cache = await caches.open(CACHE_NAME)
      cache.put(req, resp.clone())
    }
    return resp
  } catch {
    return new Response('Offline', { status: 503 })
  }
}

async function networkFirst(req) {
  try {
    const resp = await fetch(req)
    if (resp.ok) {
      const cache = await caches.open(CACHE_NAME)
      cache.put(req, resp.clone())
    }
    return resp
  } catch {
    const cached = await caches.match(req)
    return cached || new Response(JSON.stringify({ error: 'offline' }), {
      headers: { 'Content-Type': 'application/json' },
    })
  }
}
