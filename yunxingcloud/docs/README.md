# yunxingcloud 分布式微服务平台

## 概述

yunxingcloud 是基于 Spring Boot 4.0 + Vue 3 的 6 微服务分布式平台，覆盖 SSO 认证、系统管理、商城、支付、订单、库存全链路。

**生产地址：** `https://www.yunxingcloud.com`

## 微服务架构

| 服务 | 端口 | 数据库 | 职责 |
|------|------|--------|------|
| yunxingcloud-core | 8080 | yunxingcloud_core | SSO 认证 · RBAC · 系统管理 · 工单 · SPA 托管 |
| yunxingcloud-usercenter | 8081 | yunxingcloud_usercenter | 用户注册 · OAuth2/OIDC · 社交登录 |
| yunxingcloud-payment | 8083 | yunxingcloud_payment | 微信支付 · 支付宝 · 异步回调 |
| yunxingcloud-order | 8084 | yunxingcloud_order | 商品管理 · 购物车 · 下单 · Feign→Payment |
| yunxingcloud-inventory | 8085 | yunxingcloud_inventory | 仓库管理 · 入库/出库 · 预警 · SSE 推送 |
| yunxingcloud-gateway | 8090 | — | API 网关 · 路由分发 · Sentinel 限流 |

## 快速开始

### 本地开发

```bash
# 1. 启动后端（dev profile，H2 内存数据库）
./demo.sh

# 2. 访问
# 管理后台: http://localhost:5173
# 商城:     http://localhost:5174
# 后端:     http://localhost:8080
# 文档:     http://localhost:8080/doc.html
```

**默认账号：** `admin` / `admin123`

### 性能测试

```bash
k6 run k6-smoke-test.js                          # 冒烟测试
k6 run k6-load-test.js                           # 负载测试 (10 VU)
k6 run -e BASE_URL=https://www.yunxingcloud.com k6-spike-test.js  # 远程尖峰
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17 · Spring Boot 4.0 · Spring Cloud Gateway · Security · JPA · Feign · Sentinel |
| 认证 | JWT · OAuth2/OIDC · BCrypt |
| 数据库 | MySQL 8 (生产, 6 DB) · H2 (开发/测试) · Flyway 版本迁移 (22 迁移) |
| 缓存 | Redis + Caffeine |
| 前端管理 | Vue 3 · TypeScript · Vite 5 · Pinia · Naive UI · ECharts · PWA |
| 前端商城 | Vue 3 · Vite 5 · Pinia · 响应式 · 下拉刷新 |
| 文档 | Knife4j · OpenAPI 3 |
| 部署 | Docker · Docker Swarm · K8s · systemd · Nginx · Let's Encrypt |
| 监控 | Actuator · Prometheus · Sentinel · SSE 实时推送 |

## 项目结构

```
yunxingcloud/
├── yunxingcloud-common/          # 共享模块（注解/枚举/工具类）
├── yunxingcloud-api/             # Feign 接口 + DTO + 降级
├── yunxingcloud-core/            # SSO + 管理 + 工单 (8080)
├── yunxingcloud-gateway/         # API 网关 (8090)
├── yunxingcloud-usercenter/      # 用户中心 (8081)
├── yunxingcloud-payment/         # 支付 (8083)
├── yunxingcloud-order/           # 订单 + 商城 (8084)
├── yunxingcloud-inventory/       # 库存 (8085)
├── frontend/                     # Vue 3 管理后台 SPA
├── frontend-mall/                # Vue 3 商城 SPA
├── docs/                         # 文档
├── k8s/                          # Kubernetes 清单
├── deploy.sh                     # 生产部署 (12 命令)
├── restart.sh                    # 本地 6 服重启
├── demo.sh                       # 本地演示
├── Makefile                      # 快捷命令
├── k6-*.js                       # 性能测试
├── docker-compose.yml            # Docker 编排
└── docker-compose-swarm.yml      # Docker Swarm 编排
```

## 服务间调用

```
Order ──Feign──> Payment    (创建支付 → 发起支付)
Order ──Feign──> Inventory  (下单扣库存 → 取消回退)
Payment <──回调── 微信/支付宝 (异步通知更新状态)
Inventory ──SSE──> 前端     (低库存实时预警)
```

## API 文档

- Knife4j: `https://www.yunxingcloud.com/doc.html`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

## 支持

- 备案: 湘ICP备2026022380号-1