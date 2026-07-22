# YXCLOUD 部署指南

## 架构概览

```
Gateway (:8082) ─┬─ Core (:8080) ─── MySQL
                 ├─ Order (:8084) ── MySQL
                 ├─ Payment (:8083) ─ MySQL
                 ├─ Inventory (:8085) ─ MySQL
                 ├─ UserCenter (:8081) ─ MySQL
                 └─ Frontend (:5174) ─ Nginx (:80)
```

## 快速启动 (Docker Compose)

```bash
# 1. 配置环境变量
cp .env.example .env
# 编辑 .env: 设置 JWT_SECRET, DB_PASSWORD 等

# 2. 启动全部服务
docker-compose up -d

# 3. 检查健康状态
curl http://localhost:8084/actuator/health/liveness
curl http://localhost:8084/actuator/health/readiness

# 4. 访问
# 商城前端: http://localhost
# 管理后台: http://localhost/admin
# API文档: http://localhost:8084/doc.html
```

## K8s 部署

```bash
# 使用 overlay 部署到阿里云 ACK
kubectl apply -k k8s/overlays/ack

# 检查 Pod 状态
kubectl get pods -n yunxingcloud

# 查看日志
kubectl logs -f deployment/yunxingcloud-order -n yunxingcloud
```

## 健康检查

| 端点 | 用途 |
|------|------|
| `/actuator/health/liveness` | K8s Liveness Probe |
| `/actuator/health/readiness` | K8s Readiness Probe |
| `/actuator/metrics` | Prometheus Metrics |
| `/api/health` | 综合健康检查 |
| `/api/ping` | 简单连通性检查 |

## 必备环境变量

| 变量 | 说明 | 必填 |
|------|------|------|
| `jwt.secret` | JWT 签名密钥 (≥256bit) | **是** |
| `spring.datasource.url` | 数据库连接 | **是** |
| `spring.datasource.password` | 数据库密码 | **是** |
| `spring.redis.host` | Redis 地址 | 否 |
| `app.data-init.enabled` | 启用数据初始化 | 否 (dev only) |
