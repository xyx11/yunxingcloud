import http from 'k6/http'
import { check, sleep, group } from 'k6'

// 全链路压测: Gateway → Order → Payment → Inventory
// 用法: k6 run --vus 10 --duration 30s k6-fullflow-test.js

const BASE = __ENV.BASE_URL || 'http://localhost:8090'

export const options = {
  stages: [
    { duration: '30s', target: 5 },    // 预热
    { duration: '1m',  target: 10 },   // 爬升
    { duration: '2m',  target: 10 },   // 稳态
    { duration: '30s', target: 0 },    // 冷却
  ],
  thresholds: {
    'http_req_duration{name:browse}':     ['p(95)<500'],
    'http_req_duration{name:cart-add}':   ['p(95)<500'],
    'http_req_duration{name:checkout}':   ['p(95)<800'],
    'http_req_duration{name:pay}':        ['p(95)<1000'],
    'http_req_failed':                    ['rate<0.05'],
  },
}

export default function () {
  // 1. 登录获取 Token
  const loginRes = http.post(`${BASE}/api/login`, JSON.stringify({
    username: 'admin', password: 'admin123',
  }), { headers: { 'Content-Type': 'application/json' }, tags: { name: 'login' } })

  check(loginRes, { 'login 200': r => r.status === 200 })
  const token = loginRes.json('accessToken')
  const auth = { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } }

  // 2. 浏览商品列表
  group('browse products', () => {
    const res = http.get(`${BASE}/api/products?page=0&size=10`, { tags: { name: 'browse' } })
    check(res, { 'products 200': r => r.status === 200 })
  })

  // 3. 搜索商品
  group('search products', () => {
    const res = http.get(`${BASE}/api/products/search?q=test`, { tags: { name: 'search' } })
    check(res, { 'search 200': r => r.status === 200 || r.status === 404 })
  })

  // 4. 加入购物车
  group('add to cart', () => {
    const body = JSON.stringify({ productId: 1, quantity: 1 })
    const res = http.post(`${BASE}/api/cart`, body, { ...auth, tags: { name: 'cart-add' } })
    check(res, { 'cart 200': r => r.status === 200 })
  })

  // 5. 下单
  group('checkout', () => {
    const receiver = JSON.stringify({ name: 'Test User', phone: '13800138000', address: 'Test Address' })
    const res = http.post(`${BASE}/api/orders`, receiver, { ...auth, tags: { name: 'checkout' } })
    check(res, { 'order 200': r => r.status === 200 })
    const orderId = res.json('id')

    if (orderId) {
      // 6. 发起支付
      group('pay order', () => {
        const payRes = http.post(`${BASE}/api/orders/${orderId}/pay`,
          JSON.stringify({ channel: 'wechat' }),
          { ...auth, tags: { name: 'pay' } })
        check(payRes, { 'pay 200': r => r.status === 200 })
      })
    }
  })

  // 7. 查询库存
  group('check inventory', () => {
    const res = http.get(`${BASE}/api/inventory`, { ...auth, tags: { name: 'inventory' } })
    check(res, { 'inventory 200': r => r.status === 200 })
  })

  // 8. 查询支付订单
  group('check payments', () => {
    const res = http.get(`${BASE}/api/payment/orders`, { ...auth, tags: { name: 'payment-list' } })
    check(res, { 'payment 200': r => r.status === 200 })
  })

  sleep(1)
}
