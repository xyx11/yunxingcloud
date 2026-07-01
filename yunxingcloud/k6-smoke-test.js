// 冒烟测试 —— 快速验证核心功能
// k6 run k6-smoke-test.js

import http from 'k6/http'
import { check } from 'k6'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8090'

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    'http_req_duration': ['p(95)<5000'],
  },
}

export default function () {
  console.log('=== Health Check ===')
  const health = http.get(`${BASE_URL}/actuator/health`)
  check(health, { 'health UP': (r) => r.json('status') === 'UP' })
  console.log(`Health: ${health.status} ${health.json('status')}`)

  console.log('=== Login ===')
  const login = http.post(`${BASE_URL}/api/login`, JSON.stringify({
    username: 'admin', password: 'admin123',
  }), { headers: { 'Content-Type': 'application/json' } })

  check(login, {
    'login 200': (r) => r.status === 200,
    'success': (r) => r.json('success') === true,
    'hasToken': (r) => !!r.json('accessToken'),
  })

  const data = login.json()
  console.log(`Login: ${data.success ? 'PASS' : 'FAIL'} | User: ${data.username}`)

  if (data.accessToken) {
    const headers = { Authorization: `Bearer ${data.accessToken}` }
    console.log('=== Dashboard ===')
    const dash = http.get(`${BASE_URL}/api/stats/dashboard`, { headers })
    console.log(`Dashboard: ${dash.status} | Users: ${dash.json('userCount')}`)

    console.log('=== User Info ===')
    const user = http.get(`${BASE_URL}/api/user`, { headers })
    console.log(`User: ${user.status} | ${user.json('username')} (${user.json('authorities').length} perms)`)
  }
}