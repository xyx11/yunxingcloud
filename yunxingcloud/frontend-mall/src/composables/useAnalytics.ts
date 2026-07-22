// 数据分析埋点 — 使用 sendBeacon 发送到 /api/analytics
export function useAnalytics() {
  function track(event: string, data?: Record<string, unknown>) {
    try {
      const payload = JSON.stringify({ event, data, url: location.pathname, ts: Date.now() })
      if (navigator.sendBeacon) {
        navigator.sendBeacon('/api/analytics/track', new Blob([payload], { type: 'application/json' }))
      } else {
        fetch('/api/analytics/track', { method: 'POST', body: payload, keepalive: true }).catch(() => {})
      }
    } catch { /* silent */ }
  }

  function pageView() { track('page_view') }
  function productView(id: number) { track('product_view', { productId: id }) }
  function addToCart(id: number, qty: number) { track('add_to_cart', { productId: id, quantity: qty }) }
  function checkout(total: number) { track('begin_checkout', { total }) }
  function purchase(orderNo: string, amount: number) { track('purchase', { orderNo, amount }) }
  function search(kw: string, results: number) { track('search', { keyword: kw, results }) }

  return { track, pageView, productView, addToCart, checkout, purchase, search }
}
