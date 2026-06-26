# yunxingcloud 分布式微服务平台

## 概述

yunxingcloud 是一个基于 Spring Boot 3 + Vue 3 的分布式微服务平台，提供 SSO 认证中心、系统管理、用户中心和 API 网关。

**生产地址：** `https://www.yunxingcloud.com`

## 快速开始

### 本地开发

```bash
# 1. 启动后端（dev profile，使用 H2 内存数据库）
./demo.sh

# 2. 访问
# 前端: http://localhost:5173
# 后端: http://localhost:8080
# 文档: http://localhost:8080/doc.html
```

**默认账号：** `admin` / `admin123`

### 性能测试

```bash
k6 run k6-smoke-test.js                          # 冒烟测试
k6 run k6-load-test.js                           # 负载测试
k6 run -e BASE_URL=https://www.yunxingcloud.com k6-spike-test.js  # 远程
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17 · Spring Boot 4.0 · Spring Security · JPA · Flyway |
| 认证 | JWT · OAuth2 · BCrypt · RSA |
| 数据库 | MySQL 8 (生产) · H2 (开发) |
| 缓存 | Redis + Caffeine |
| 前端 | Vue 3 · TypeScript · Vite 5 · Naive UI · ECharts |
| 文档 | Knife4j · OpenAPI 3 |
| 部署 | Docker · systemd · Nginx · Let's Encrypt |
| 监控 | Actuator · K6 · 健康检查脚本 |

## 项目结构

```
yunxingcloud/
├── yunxingcloud-common/       # 共享模块
├── yunxingcloud-core/         # SSO + 管理 (8080)
├── yunxingcloud-gateway/      # API 网关 (8090)
├── yunxingcloud-usercenter/   # 用户中心 (8081)
├── frontend/                  # Vue 3 SPA
├── docs/                      # 文档
├── deploy.sh                  # 生产部署脚本
├── demo.sh                    # 本地演示
├── k6-*.js                    # 性能测试
└── docker-compose.yml         # Docker 部署
```

## API 文档

访问 `https://www.yunxingcloud.com/doc.html` 查看完整 API 文档。

## 支持

- 邮件: admin@yunxingcloud.com
- 备案: 湘ICP备2026022380号-1
