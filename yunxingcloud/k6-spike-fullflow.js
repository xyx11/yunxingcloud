import http from 'k6/http'
import { check, sleep } from 'k6'

// 全链路尖峰压测: 短时间内大量并发冲击完整下单链路
// 用法: k6 run k6-spike-fullflow.js

const BASE = __ENV.BASE_URL || 'http://localhost:8090'

export const options = {
  stages: [
    { duration: '20s', target: 5 },
    { duration: '10s', target: 50 },   // 尖峰
    { duration: '30s', target: 50 },   // 持续高压
    { duration: '10s', target: 5 },    // 回落
    { duration: '20s', target: 0 },
  ],
  thresholds: {
    'http_req_duration':           ['p(95)<2000'],
    'http_req_failed':             ['rate<0.1'],
  },
}

export default function () {
  // 登录
  let loginRes = http.post(`${BASE}/api/login`, JSON.stringify({
    username: 'admin', password: 'admin123',
  }), { headers: { 'Content-Type': 'application/json' } })
  const token = loginRes.json('accessToken')
  if (!token) { sleep(1); return }
  const auth = { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } }

  // 快速浏览 → 下单 → 支付 全链路
  http.get(`${BASE}/api/products?size=5`)
  http.get(`${BASE}/api/products/hot`)

  let cartRes = http.post(`${BASE}/api/cart`, JSON.stringify({ productId: 1, quantity: 1 }), auth)
  if (cartRes.status !== 200) { sleep(1); return }

  let orderRes = http.post(`${BASE}/api/orders`, JSON.stringify({
    name: 'Spike', phone: '13900000000', address: 'Addr'
  }), auth)

  if (orderRes.status === 200) {
    let oid = orderRes.json('id')
    if (oid) http.post(`${BASE}/api/orders/${oid}/pay`, JSON.stringify({ channel: 'wechat' }), auth)
  }

  // 库存/支付状态查询
  http.get(`${BASE}/api/inventory`, auth)
  http.get(`${BASE}/api/payment/orders`, auth)

  sleep(1)
}
