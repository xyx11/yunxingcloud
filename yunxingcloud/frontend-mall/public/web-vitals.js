// Core Web Vitals tracking - lightweight inline collector
(function () {
  var METRIC_ENDPOINT = '/api/analytics/vitals'
  var queue = []
  var timer = null

  function send(data) {
    queue.push(data)
    if (!timer) {
      timer = setTimeout(function () {
        var payload = JSON.stringify({ metrics: queue, url: location.pathname, ts: Date.now() })
        if (navigator.sendBeacon) {
          navigator.sendBeacon(METRIC_ENDPOINT, new Blob([payload], { type: 'application/json' }))
        } else {
          fetch(METRIC_ENDPOINT, { method: 'POST', body: payload, headers: { 'Content-Type': 'application/json' }, keepalive: true }).catch(function () {})
        }
        queue = []
        timer = null
      }, 3000)
    }
  }

  // LCP
  try {
    new PerformanceObserver(function (list) {
      var entries = list.getEntries()
      var last = entries[entries.length - 1]
      if (last) send({ name: 'LCP', value: Math.round(last.startTime), rating: last.startTime <= 2500 ? 'good' : last.startTime <= 4000 ? 'needs-improvement' : 'poor' })
    }).observe({ type: 'largest-contentful-paint', buffered: true })
  } catch (_) {}

  // CLS
  try {
    var cls = 0
    new PerformanceObserver(function (list) {
      for (var i = 0; i < list.getEntries().length; i++) {
        if (!list.getEntries()[i].hadRecentInput) cls += list.getEntries()[i].value
      }
    }).observe({ type: 'layout-shift', buffered: true })
    window.addEventListener('visibilitychange', function () {
      if (document.visibilityState === 'hidden') {
        send({ name: 'CLS', value: Math.round(cls * 1000) / 1000, rating: cls <= 0.1 ? 'good' : cls <= 0.25 ? 'needs-improvement' : 'poor' })
      }
    })
  } catch (_) {}

  // FCP
  try {
    new PerformanceObserver(function (list) {
      var entries = list.getEntries()
      if (entries.length) send({ name: 'FCP', value: Math.round(entries[0].startTime) })
    }).observe({ type: 'paint', buffered: true })
  } catch (_) {}

  // TTFB
  try {
    var nav = performance.getEntriesByType('navigation')[0]
    if (nav) send({ name: 'TTFB', value: Math.round(nav.responseStart) })
  } catch (_) {}

  // INP (Interaction to Next Paint)
  try {
    var maxINP = 0
    new PerformanceObserver(function (list) {
      for (var i = 0; i < list.getEntries().length; i++) {
        var entry = list.getEntries()[i]
        var duration = entry.duration
        if (duration > maxINP) maxINP = duration
      }
    }).observe({ type: 'event', buffered: true, durationThreshold: 16 })
    window.addEventListener('visibilitychange', function () {
      if (document.visibilityState === 'hidden' && maxINP > 0) {
        send({ name: 'INP', value: Math.round(maxINP), rating: maxINP <= 200 ? 'good' : maxINP <= 500 ? 'needs-improvement' : 'poor' })
      }
    })
  } catch (_) {}
})()
