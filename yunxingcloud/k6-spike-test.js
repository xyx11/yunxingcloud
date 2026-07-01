// 突增压力测试 (k6 v2.0)
// k6 run k6-spike-test.js

import http from 'k6/http'
import { check, sleep } from 'k6'
import { Trend, Rate } from 'k6/metrics'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8090'
const errorRate = new Rate('error_rate')
const loginTime = new Trend('login_time')

export const options = {
  stages: [
    { duration: '10s', target: 5 },
    { duration: '10s', target: 200 },
    { duration: '30s', target: 200 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    'error_rate': ['rate<0.10'],
    'http_req_duration': ['p(95)<5000'],
  },
}

export default function () {
  const h = { 'Content-Type': 'application/json' }
  const r = http.post(`${BASE_URL}/api/login`, JSON.stringify({
    username: 'admin', password: 'admin123',
  }), { headers: h })
  loginTime.add(r.timings.duration)
  errorRate.add(r.status !== 200)

  if (r.status === 200 && r.json('accessToken')) {
    const token = r.json('accessToken')
    const ah = { ...h, Authorization: `Bearer ${token}` }
    http.get(`${BASE_URL}/api/stats/dashboard`, { headers: ah })
    http.get(`${BASE_URL}/api/user`, { headers: ah })
  }
  sleep(1)
}