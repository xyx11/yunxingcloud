// 混合场景压测 —— 模拟真实用户行为
// k6 run k6-mixed-load.js
// 场景: 20VU 持续 2min: 50%浏览 20%搜索 15%详情 10%加购 5%下单

import http from 'k6/http'
import { check, sleep } from 'k6'
import { Rate } from 'k6/metrics'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8090'
const errorRate = new Rate('errors')

export const options = {
  stages: [
    { duration: '30s', target: 5 },   // 预热
    { duration: '30s', target: 20 },  // 爬升
    { duration: '60s', target: 20 },  // 稳定
    { duration: '30s', target: 0 },   // 回落
  ],
  thresholds: {
    'http_req_duration': ['p(95)<3000', 'p(99)<5000'],
    'errors': ['rate<0.1'],
  },
}

function login() {
  const res = http.post(`${BASE_URL}/api/login`, JSON.stringify({
    username: 'admin', password: 'admin123',
  }), { headers: { 'Content-Type': 'application/json' } })
  const ok = res.status === 200 && res.json('success') === true
  if (!ok) errorRate.add(1)
  return ok ? res.json('data.token') : null
}

function browse(token) {
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  const pages = ['/api/products?page=0', '/api/categories', '/api/banners', '/api/home', '/api/search/hot-keywords']
  const path = pages[Math.floor(Math.random() * pages.length)]
  const res = http.get(`${BASE_URL}${path}`, { headers })
  check(res, { 'browse ok': (r) => r.status === 200 }) || errorRate.add(1)
}

function search(token) {
  const keywords = ['手机', '耳机', 'T恤', '运动鞋', '笔记本']
  const kw = keywords[Math.floor(Math.random() * keywords.length)]
  const res = http.get(`${BASE_URL}/api/search?q=${kw}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  check(res, { 'search ok': (r) => r.status === 200 }) || errorRate.add(1)
}

function productDetail(token) {
  const ids = [1, 2, 3, 4, 5]
  const id = ids[Math.floor(Math.random() * ids.length)]
  const res = http.get(`${BASE_URL}/api/products/${id}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  check(res, { 'detail ok': (r) => r.status === 200 }) || errorRate.add(1)
}

function addToCart(token) {
  if (!token) return
  const res = http.post(`${BASE_URL}/api/cart`, JSON.stringify({
    productId: Math.floor(Math.random() * 5) + 1, quantity: 1,
  }), { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } })
  check(res, { 'cart ok': (r) => r.status === 200 }) || errorRate.add(1)
}

function placeOrder(token) {
  if (!token) return
  const res = http.post(`${BASE_URL}/api/orders`, JSON.stringify({
    receiver: { name: 'Test', phone: '13800138000', address: 'Test Address' },
  }), { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } })
  const ok = res.status === 200 && res.json('success') === true
  if (!ok) errorRate.add(1)
}

export default function () {
  const token = Math.random() < 0.2 ? login() : null

  const r = Math.random()
  if (r < 0.5) { browse(token); sleep(1) }          // 50% 浏览
  else if (r < 0.7) { search(token); sleep(0.8) }   // 20% 搜索
  else if (r < 0.85) { productDetail(token); sleep(0.5) } // 15% 详情
  else if (r < 0.95) { addToCart(token); sleep(0.3) }     // 10% 加购
  else { placeOrder(token); sleep(1.5) }                   // 5% 下单
}
