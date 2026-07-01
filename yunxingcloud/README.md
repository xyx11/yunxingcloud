# yunxingcloud

[![CI](https://github.com/xyx11/yunxingcloud/actions/workflows/ci.yml/badge.svg)](https://github.com/xyx11/yunxingcloud/actions)
[![CodeQL](https://github.com/xyx11/yunxingcloud/actions/workflows/codeql.yml/badge.svg)](https://github.com/xyx11/yunxingcloud/actions)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-2.3.0-brightgreen.svg)](https://github.com/xyx11/yunxingcloud/releases)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen.svg)](https://spring.io/)

分布式微服务平台 — 6 微服务架构：SSO + 管理 + 网关 + 用户中心 + 支付 + 订单 + 库存

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17 · Spring Boot 4.0 · Spring Cloud Gateway · Spring Security · OAuth2/OIDC · JPA · Feign |
| 认证 | JWT (JJWT 0.12) · OAuth2 Authorization Server · Nimbus JOSE · Sentinel 限流熔断 |
| 数据库 | MySQL 8 (生产) · H2 (开发/测试) · Flyway 版本迁移 |
| 缓存 | Redis + Caffeine · Spring Cache |
| 前端 | Vue 3 · TypeScript · Vite 5 · Pinia · Naive UI 2 · ECharts · PWA |
| 前端商城 | Vue 3 · Vite 5 · Pinia · 响应式布局 · 下拉刷新 |
| 文档 | Knife4j 4.5 · Swagger UI · OpenAPI 3 |
| 部署 | Docker · Docker Compose · Docker Swarm · K8s · systemd · Nginx · Let's Encrypt |
| 监控 | Actuator · Prometheus (6 服务) · Sentinel Dashboard · SSE 实时推送 |

## 项目结构

```
yunxingcloud/
├── yunxingcloud-common/          # 共享模块（注解/枚举/工具类）
├── yunxingcloud-api/             # Feign 接口 + DTO + 降级实现
├── yunxingcloud-core/            # SSO 认证中心 + 系统管理 + 工单 (8080)
├── yunxingcloud-gateway/         # WebFlux API 网关 (8090)
├── yunxingcloud-usercenter/      # 用户注册 + OAuth2/OIDC 授权 (8081)
├── yunxingcloud-payment/         # 支付服务 — 微信/支付宝 (8083)
├── yunxingcloud-order/           # 订单服务 — 商品+购物车+下单 (8084)
├── yunxingcloud-inventory/       # 库存服务 — 仓库+入库/出库+预警 (8085)
├── frontend/                     # Vue 3 管理后台 SPA
├── frontend-mall/                # Vue 3 商城 SPA（移动端适配）
├── deploy.sh                     # 生产一键部署 (12 命令)
├── restart.sh                    # 本地 6 服重启
├── docker-compose.yml            # Docker 9 服务编排
├── docker-compose-swarm.yml      # Docker Swarm 编排
├── k8s/                          # Kubernetes 部署清单
├── nginx.conf                    # Nginx HTTPS 反向代理
├── Makefile                      # 构建/测试/Docker 快捷命令
└── .github/workflows/            # CI/CD
```

## 架构图

```mermaid
graph TB
    subgraph 客户端
        Admin["管理后台 (Vue 3)"]
        Mall["商城 (Vue 3)"]
    end

    subgraph 网关
        Gateway["API 网关 :8090<br/>限流 · 路由 · 日志"]
    end

    subgraph 服务层
        Core["SSO 认证中心 :8080<br/>JWT · OAuth2 · RBAC · 工单"]
        UC["用户中心 :8081<br/>注册 · 角色 · 部门 · 社交登录"]
        Pay["支付服务 :8083<br/>微信支付 · 支付宝"]
        Ord["订单服务 :8084<br/>商品 · 购物车 · 下单"]
        Inv["库存服务 :8085<br/>仓库 · 入库/出库 · 预警"]
    end

    subgraph 数据层
        MySQL["MySQL 8 (6 DB)"]
        Redis["Redis"]
        Nacos["Nacos"]
        Sentinel["Sentinel Dashboard"]
    end

    Admin --> Gateway
    Mall --> Gateway
    Gateway --> Core
    Gateway --> UC
    Gateway --> Pay
    Gateway --> Ord
    Gateway --> Inv
    Ord -- Feign --> Pay
    Ord -- Feign --> Inv
    Core --> MySQL
    Core --> Redis
    UC --> MySQL
    Pay --> MySQL
    Ord --> MySQL
    Inv --> MySQL
```

## 快速开始

### 一键演示

```bash
./demo.sh
# 自动构建 → 启动 → 冒烟测试 → 打开浏览器
```

### 本地开发

```bash
# 启动全部 6 服务
make dev              # Core (8080)
make dev-usercenter   # Usercenter (8081)
make dev-payment      # Payment (8083)
make dev-order        # Order (8084)
make dev-inventory    # Inventory (8085)
make dev-gateway      # Gateway (8090)

# 前端开发
cd frontend && npm run dev         # 管理后台 :5173
cd frontend-mall && npm run dev    # 商城 :5174

# 默认账号: admin / admin123
```

### 使用 Makefile

```bash
make dev              # 启动 Core 开发服务器
make dev-payment      # 启动 Payment 开发服务器
make test             # 运行全部测试 (46+ tests)
make build            # 编译后端 + 构建前端
make package          # Maven 打包 6 模块
make docker-build-all # 构建全部 6 个 Docker 镜像
make docker-up        # Docker Compose 启动
make deploy           # 一键部署
make deploy-quick     # 增量构建快速部署
```

## API 端点

### SSO 认证中心 (core) — 端口 8080

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/login` | POST | 用户登录（JWT） |
| `/api/logout` | POST | 登出（Token 黑名单） |
| `/api/refresh` | POST | 刷新 Token |
| `/api/user` | GET | 当前用户信息 + 权限 |
| `/api/menus/tree` | GET | 菜单树 |
| `/api/menus` | GET/POST | 菜单 CRUD |
| `/api/config` | GET/POST | 系统配置 CRUD |
| `/api/job` | GET/POST | 定时任务 CRUD |
| `/api/operlog` | GET | 操作日志 |
| `/api/tickets` | GET/POST | 工单 CRUD |
| `/api/stats/dashboard` | GET | Dashboard 统计 |
| `/api/system/info` | GET | JVM 系统信息 |
| `/api/sse/dashboard` | GET | SSE 实时监控流 |
| `/oauth2/authorize` | GET/POST | OAuth2 授权端点 |

### 支付服务 (payment) — 端口 8083

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/payment/orders` | GET/POST | 支付订单列表/创建 |
| `/api/payment/orders/{id}` | GET | 订单详情 |
| `/api/payment/orders/{id}/pay` | POST | 发起支付 |
| `/api/payment/orders/{id}/refund` | POST | 退款 |
| `/api/payment/callback/{channel}` | POST | 异步支付回调 |

### 订单服务 (order) — 端口 8084

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/products` | GET/POST | 商品 CRUD |
| `/api/products/hot` | GET | 热门商品 |
| `/api/products/new` | GET | 新品 |
| `/api/products/search` | GET | 全文搜索 |
| `/api/cart` | GET/POST/DELETE | 购物车 |
| `/api/orders` | GET/POST | 订单列表/提交 |
| `/api/orders/{id}/pay` | POST | 发起支付 |
| `/api/categories` | GET/POST | 分类 CRUD |
| `/api/coupons` | GET/POST | 优惠券 |
| `/api/addresses` | GET/POST | 收货地址 |
| `/api/banners` | GET | 首页横幅 |

### 库存服务 (inventory) — 端口 8085

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/warehouses` | GET/POST | 仓库 CRUD |
| `/api/inventory` | GET | 库存列表 |
| `/api/inventory/stock-in` | POST | 入库 |
| `/api/inventory/stock-out` | POST | 出库 |
| `/api/inventory/alerts` | GET | 低库存预警 |
| `/api/inventory/alerts/stream` | GET | SSE 实时预警流 |

### 文档地址

- Knife4j: `http://localhost:8080/doc.html`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- Prometheus: `http://localhost:8080/actuator/prometheus`

## 安全特性

- **认证**: JWT 双 Token (access 2h + refresh 7d) + 黑名单登出
- **限流**: Sentinel 全链路流控 (QPS) + IP 级别 10次/分钟
- **熔断**: Sentinel 降级 + Feign fallbackFactory 服务间降级
- **锁定**: 5次失败锁定 30 分钟
- **密码**: 8位 + 大写 + 小写 + 数字 + 特殊字符
- **权限**: RBAC (user_roles 多对多) + @PreAuthorize 方法级控制
- **安全头**: XSS/HSTS/X-Frame/Referrer-Policy
- **审计**: Spring Events 异步审计日志

## 部署

### Docker Compose

```bash
docker-compose up -d  # 9 服务：Nacos + Sentinel + MySQL + Redis + 5 应用服务
```

### Docker Swarm

```bash
docker stack deploy -c docker-compose-swarm.yml yunxingcloud
```

### Kubernetes

```bash
kubectl apply -k k8s/
```

### 阿里云 ECS

```bash
./deploy.sh init      # 初始化服务器环境
./deploy.sh full      # 构建→上传→启动→健康检查
./deploy.sh quick     # 增量构建快速部署
./deploy.sh restart   # 重启全部 6 服务
./deploy.sh status    # 服务状态
```

### 生产 Checklist

1. 修改 `deploy.conf` 中所有密码和密钥
2. 配置 Nginx + HTTPS (参考 `nginx.conf`)
3. 配置 MySQL 数据库（每服务独立 DB）
4. 填入支付商户凭证到 `payment.application.yaml`（可选，默认 mock）
5. 设置 crontab 定时备份: `0 2 * * * /opt/yunxingcloud/backup.sh`
6. 启用 systemd: `systemctl enable yunxingcloud-{core,gateway,usercenter,payment,order,inventory}`

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_URL` | `jdbc:mysql://localhost:3306/sso_yunxingcloud` | 数据库连接 |
| `DB_USERNAME` | `root` | 数据库用户 |
| `DB_PASSWORD` | - | 数据库密码 |
| `JWT_SECRET` | (内置) | JWT 签名密钥 |
| `NACOS_SERVER` | `127.0.0.1:8848` | Nacos 注册中心 |
| `REDIS_HOST` | `redis` | Redis 地址 |
| `SENTINEL_DASHBOARD` | `127.0.0.1:8082` | Sentinel 控制台 |

## 测试

```bash
# 全部测试 (46+ tests)
make test

# 按服务
make test-core         # 21 tests
make test-payment      # 6 tests
make test-order        # 7 tests
make test-inventory    # 8 tests

# E2E (Playwright)
cd frontend && npx playwright test
```

## 许可证

MIT