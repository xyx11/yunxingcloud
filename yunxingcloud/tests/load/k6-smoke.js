import http from 'k6/http'
import { check, sleep, group } from 'k6'

export const options = {
  stages: [
    { duration: '30s', target: 10 },   // ramp up
    { duration: '1m', target: 50 },     // steady
    { duration: '30s', target: 0 },     // ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],  // 95% requests < 2s
    http_req_failed: ['rate<0.05'],      // < 5% error rate
  },
}

const BASE = 'http://localhost:8090'

export default function () {
  group('首页', () => {
    const r = http.get(BASE + '/api/home')
    check(r, { 'status 200': (r) => r.status === 200 })
  })

  group('搜索', () => {
    const r = http.get(BASE + '/api/products/search?q=手机&page=1&size=20')
    check(r, { 'status 200': (r) => r.status === 200 })
  })

  group('商品详情', () => {
    const r = http.get(BASE + '/api/products/1/detail')
    check(r, { 'status 200': (r) => r.status === 200 })
  })

  group('分类', () => {
    const r = http.get(BASE + '/api/categories')
    check(r, { 'status 200': (r) => r.status === 200 })
  })

  sleep(1)
}
