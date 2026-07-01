const CACHE_NAME = 'yxcloud-mall-v2.4.0'
const ASSETS = ['/mall/', '/mall/index.html']

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE_NAME).then(cache => cache.addAll(ASSETS)))
  self.skipWaiting()
})

self.addEventListener('activate', e => {
  e.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))))
  self.clients.claim()
})

self.addEventListener('fetch', e => {
  if (e.request.method !== 'GET') return
  if (e.request.url.includes('/api/')) {
    return e.respondWith(networkFirst(e.request))
  }
  e.respondWith(cacheFirst(e.request))
})

async function cacheFirst(req) {
  const cached = await caches.match(req)
  if (cached) return cached
  try {
    const resp = await fetch(req)
    if (resp.ok) { const cache = await caches.open(CACHE_NAME); cache.put(req, resp.clone()) }
    return resp
  } catch { return new Response('Offline', { status: 503 }) }
}

async function networkFirst(req) {
  try {
    const resp = await fetch(req)
    const cache = await caches.open(CACHE_NAME)
    cache.put(req, resp.clone())
    return resp
  } catch {
    const cached = await caches.match(req)
    return cached || new Response(JSON.stringify({ error: 'offline' }), { headers: { 'Content-Type': 'application/json' } })
  }
}