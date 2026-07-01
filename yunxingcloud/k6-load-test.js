// yunxingcloud k6 负载测试 (k6 v2.0)
// 运行: k6 run k6-load-test.js
// 远程: k6 run -e BASE_URL=http://your-server:8090 k6-load-test.js

import http from 'k6/http'
import { check, sleep } from 'k6'
import { Trend, Rate, Counter } from 'k6/metrics'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8090'
const loginDuration = new Trend('login_duration')
const apiDuration = new Trend('api_duration')
const errorRate = new Rate('error_rate')
const loginCount = new Counter('login_count')

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m',  target: 50 },
    { duration: '2m',  target: 50 },
    { duration: '1m',  target: 100 },
    { duration: '2m',  target: 100 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    'http_req_duration': ['p(95)<2000'],
    'login_duration': ['p(95)<3000'],
    'api_duration': ['p(95)<1000'],
    'error_rate': ['rate<0.05'],
  },
}

export function setup() {
  const res = http.post(`${BASE_URL}/api/login`, JSON.stringify({
    username: 'admin', password: 'admin123',
  }), { headers: { 'Content-Type': 'application/json' } })
  check(res, { 'setup login': (r) => r.status === 200 })
  console.log(`Setup login: ${res.json('success') ? 'OK' : 'FAIL'}`)
  return { token: res.json('accessToken') || '' }
}

export default function (data) {
  const h = { 'Content-Type': 'application/json', Authorization: `Bearer ${data.token}` }

  let r = http.get(`${BASE_URL}/api/stats/dashboard`, { headers: h })
  apiDuration.add(r.timings.duration); errorRate.add(r.status !== 200)

  r = http.get(`${BASE_URL}/api/users?page=1&pageSize=20`, { headers: h })
  apiDuration.add(r.timings.duration); errorRate.add(r.status !== 200)
  sleep(0.5)

  r = http.get(`${BASE_URL}/api/menus/tree`, { headers: h })
  apiDuration.add(r.timings.duration); check(r, { 'menu': (x) => x.status === 200 })
  sleep(0.3)

  r = http.get(`${BASE_URL}/api/user`, { headers: h })
  apiDuration.add(r.timings.duration)
  sleep(0.5)

  r = http.get(`${BASE_URL}/actuator/health`)
  check(r, { 'health': (x) => x.json('status') === 'UP' })

  if (__ITER % 5 === 0) {
    r = http.post(`${BASE_URL}/api/login`, JSON.stringify({
      username: 'admin', password: 'admin123',
    }), { headers: { 'Content-Type': 'application/json' } })
    loginDuration.add(r.timings.duration); loginCount.add(1); errorRate.add(r.status !== 200)
  }

  sleep(1 + Math.random() * 2)
}

export function handleSummary(data) {
  const m = data.metrics
  return {
    'k6-summary.json': JSON.stringify(data, null, 2),
    stdout: `
========================================
  yunxingcloud Performance Report
========================================
Requests:   ${m.http_reqs.values.count}
Fail Rate:  ${(m.http_req_failed.values.rate * 100).toFixed(2)}%
Avg:        ${m.http_req_duration.values.avg.toFixed(0)}ms
P50:        ${m.http_req_duration.values.med.toFixed(0)}ms
P95:        ${(m.http_req_duration.values['p(95)'] || 0).toFixed(0)}ms
P99:        ${(m.http_req_duration.values['p(99)'] || 0).toFixed(0)}ms
Max:        ${m.http_req_duration.values.max.toFixed(0)}ms
Login Avg:  ${(m.login_duration?.values.avg || 0).toFixed(0)}ms
API Avg:    ${(m.api_duration?.values.avg || 0).toFixed(0)}ms
Duration:   ${(data.state.testRunDurationMs / 1000).toFixed(0)}s
Peak VUs:   ${m.vus_max.values.max}
========================================
`,
  }
}