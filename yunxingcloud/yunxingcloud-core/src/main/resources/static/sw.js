// yunxingcloud-v3 — force refresh all clients
const CACHE_NAME = 'yunxingcloud-v3'

const urlsToCache = [
  '/',
  '/index.html',
  '/manifest.json',
  '/favicon.svg',
]

// Install
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(urlsToCache))
  )
  self.skipWaiting()
})

// Activate — wipe ALL caches, claim all clients immediately
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((names) => Promise.all(names.map((n) => caches.delete(n))))
  )
  event.waitUntil(self.clients.claim())
  // Force all open tabs to reload
  event.waitUntil(self.clients.matchAll().then((clients) => {
    clients.forEach((client) => client.navigate(client.url))
  }))
})

// Fetch — network first, cache only static assets
self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return
  const url = new URL(event.request.url)

  if (url.pathname.startsWith('/api/') ||
      url.pathname.startsWith('/oauth2/') ||
      url.pathname.startsWith('/actuator/') ||
      url.pathname.startsWith('/.well-known/') ||
      url.pathname.startsWith('/ws')) {
    return
  }

  event.respondWith(
    fetch(event.request)
      .then((response) => {
        if (response.status === 200) {
          const cloned = response.clone()
          caches.open(CACHE_NAME).then((cache) => cache.put(event.request, cloned))
        }
        return response
      })
      .catch(() => caches.match(event.request).then((cached) => cached || Response.error()))
  )
})
