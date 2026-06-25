// yunxingcloud k6 负载测试
// 安装: brew install k6
// 运行: k6 run k6-load-test.js
// 指定目标: k6 run -e BASE_URL=http://your-server:8080 k6-load-test.js

import http from 'k6/http'
import { check, sleep, group } from 'k6'
import { Trend, Rate, Counter } from 'k6/metrics'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080'

// 自定义指标
const loginDuration = new Trend('login_duration')
const apiDuration = new Trend('api_duration')
const errorRate = new Rate('error_rate')
const loginCount = new Counter('login_count')

// 测试配置
export const options = {
  stages: [
    { duration: '30s', target: 10 },   // 预热: 30s 升至 10 VUs
    { duration: '1m',  target: 50 },   // 爬坡: 1min 升至 50 VUs
    { duration: '2m',  target: 50 },   // 稳态: 2min 保持 50 VUs
    { duration: '1m',  target: 100 },  // 加压: 1min 升至 100 VUs
    { duration: '2m',  target: 100 },  // 高峰: 2min 保持 100 VUs
    { duration: '30s', target: 0 },    // 收尾: 30s 降至 0
  ],
  thresholds: {
    'http_req_duration': ['p(95)<2000'],  // 95% 请求 < 2s
    'login_duration': ['p(95)<3000'],     // 95% 登录 < 3s
    'api_duration': ['p(95)<1000'],       // 95% API < 1s
    'error_rate': ['rate<0.05'],          // 错误率 < 5%
  },
}

// 初始化 (每个 VU 只执行一次)
export function setup() {
  // 预登录一批测试账号
  const res = http.post(`${BASE_URL}/api/login`, JSON.stringify({
    username: 'admin',
    password: 'admin123',
  }), { headers: { 'Content-Type': 'application/json' } })

  const success = check(res, { 'setup login': (r) => r.status === 200 })
  if (!success) {
    console.error(`Setup login failed: ${res.status}`)
    return { token: '' }
  }

  const data = res.json()
  return { token: data.accessToken }
}

// 主测试函数 (每个 VU 循环执行)
export default function (data) {
  const headers = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${data.token}`,
  }

  group('Dashboard', () => {
    const res = http.get(`${BASE_URL}/api/stats/dashboard`, { headers })
    apiDuration.add(res.timings.duration)
    errorRate.add(res.status !== 200)
    check(res, { 'dashboard 200': (r) => r.status === 200 })
    sleep(1)
  })

  group('User Management', () => {
    // 用户列表
    const listRes = http.get(`${BASE_URL}/api/users?page=1&pageSize=20`, { headers })
    apiDuration.add(listRes.timings.duration)
    errorRate.add(listRes.status !== 200)
    check(listRes, { 'users list 200': (r) => r.status === 200 })
    sleep(0.5)
  })

  group('Menu Tree', () => {
    const res = http.get(`${BASE_URL}/api/menus/tree`, { headers })
    apiDuration.add(res.timings.duration)
    check(res, { 'menu tree 200': (r) => r.status === 200 })
    sleep(0.3)
  })

  group('Auth Info', () => {
    const res = http.get(`${BASE_URL}/api/user`, { headers })
    apiDuration.add(res.timings.duration)
    check(res, { 'user info 200': (r) => r.status === 200 })
    sleep(0.5)
  })

  group('Health Check', () => {
    const res = http.get(`${BASE_URL}/actuator/health`)
    check(res, { 'health UP': (r) => r.status === 200 && r.json('status') === 'UP' })
    sleep(0.2)
  })

  // 登录压力测试 (每 5 次迭代执行一次)
  if (__ITER % 5 === 0) {
    group('Login', () => {
      const res = http.post(`${BASE_URL}/api/login`, JSON.stringify({
        username: 'admin',
        password: 'admin123',
      }), { headers: { 'Content-Type': 'application/json' } })
      loginDuration.add(res.timings.duration)
      loginCount.add(1)
      errorRate.add(res.status !== 200)
      check(res, { 'login 200': (r) => r.status === 200 })
    })
  }

  sleep(1 + Math.random() * 2) // 模拟真实用户行为
}

// 测试结束摘要
export function handleSummary(data) {
  return {
    'k6-summary.json': JSON.stringify(data, null, 2),
    stdout: `
========================================
  yunxingcloud 性能测试报告
========================================

总请求数:     ${data.metrics.http_reqs.values.count}
失败率:       ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%
平均响应:     ${data.metrics.http_req_duration.values.avg.toFixed(0)}ms
P50 延迟:     ${data.metrics.http_req_duration.values.med.toFixed(0)}ms
P95 延迟:     ${data.metrics.http_req_duration.values['p(95)']?.toFixed(0) || 'N/A'}ms
P99 延迟:     ${data.metrics.http_req_duration.values['p(99)']?.toFixed(0) || 'N/A'}ms
最大延迟:     ${data.metrics.http_req_duration.values.max.toFixed(0)}ms

登录平均:     ${data.metrics.login_duration?.values.avg.toFixed(0) || 'N/A'}ms
API 平均:     ${data.metrics.api_duration?.values.avg.toFixed(0) || 'N/A'}ms

测试时长:     ${(data.state.testRunDurationMs / 1000).toFixed(0)}s
VUs 峰值:     ${data.metrics.vus_max.values.max}
========================================
`,
  }
}