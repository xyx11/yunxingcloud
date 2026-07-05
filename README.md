# yunxingcloud

分布式微服务电商平台。

## 架构

```
yunxingcloud/
├── yunxingcloud-common/       # 公共模块 (BaseException, I18n, 工具类)
├── yunxingcloud-core/         # 核心服务 (用户/角色/菜单/配置/工单) :8080
├── yunxingcloud-usercenter/   # 用户中心 (OAuth2/认证/授权)     :8081
├── yunxingcloud-gateway/      # API 网关 (路由/限流)           :8090
├── yunxingcloud-order/        # 订单服务 (商品/订单/预售/直播)  :8084
├── yunxingcloud-payment/      # 支付服务 (支付/退款/流水)      :8083
├── yunxingcloud-inventory/    # 库存服务 (库存/仓库/盘点)      :8085
├── yunxingcloud-api/          # Feign 客户端接口
├── frontend/                  # 管理后台 (Vue 3 + Naive UI)
└── frontend-mall/             # 商城前台 (Vue 3)
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 框架 | Spring Boot 4.x + Spring Cloud |
| 注册中心 | Nacos |
| 数据库 | MySQL 8.0 + Flyway 迁移 |
| 缓存 | Redis |
| 搜索 | Elasticsearch 8.x |
| 认证 | JWT + OAuth2 |
| 前端 | Vue 3 + Vite + TypeScript + Naive UI |
| 监控 | Prometheus + Grafana + Sentinel |
| 部署 | Docker + Docker Compose |

## 快速启动

### 基础设施

```bash
docker compose up -d nacos mysql redis elasticsearch
```

### 后端

```bash
./mvnw clean package -DskipTests
docker compose up -d --build
```

### 前端

```bash
cd frontend && npm install && npm run dev      # 管理后台 :5173
cd frontend-mall && npm install && npm run dev  # 商城前台 :5174
```

## 测试

```bash
./mvnw test -pl yunxingcloud-core,yunxingcloud-common  # 核心测试
./mvnw test -pl yunxingcloud-order -Dtest=OrderApplicationTest  # 订单测试(需 ES)
cd frontend && npx vue-tsc --noEmit  # 前端类型检查
```

## CI/CD

GitHub Actions 自动运行: `mvn test` → `vue-tsc` → `npm build` → Docker 构建推送