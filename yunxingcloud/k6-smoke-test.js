// 冒烟测试 —— 快速验证核心功能
// k6 run k6-smoke-test.js

import http from 'k6/http'
import { check } from 'k6'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080'

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    'http_req_duration': ['p(95)<5000'],
  },
}

export default function () {
  group('Health', () => {
    const r = http.get(`${BASE_URL}/actuator/health`)
    check(r, { 'health': (r) => r.json('status') === 'UP' })
  })

  group('Login', () => {
    const r = http.post(`${BASE_URL}/api/login`, JSON.stringify({
      username: 'admin', password: 'admin123',
    }), { headers: { 'Content-Type': 'application/json' } })
    check(r, {
      'status': (r) => r.status === 200,
      'success': (r) => r.json('success') === true,
      'hasToken': (r) => !!r.json('accessToken'),
    })
  })
}