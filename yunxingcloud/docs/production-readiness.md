# yunxingcloud 生产部署就绪检查清单

## 部署前检查

### 1. 安全配置
- [ ] 修改 `deploy.conf` 中所有密码和密钥 (DB_PASSWORD / JWT_SECRET / OAUTH2_CLIENT_SECRET)
- [ ] K8s Secret 替换占位符 (`change-me` → 真实密钥)
- [ ] 生产域名 + TLS 证书配置 (Let's Encrypt 或购买)
- [ ] 防火墙开放端口: 80/443 (Nginx), 3306 (MySQL 仅内网)
- [ ] SSH 密钥登录, 禁用密码登录
- [ ] 数据库密码强度 ≥ 16 位随机字符

### 2. 基础设施
- [ ] MySQL 8 安装 + 5 数据库创建 (core/usercenter/payment/order/inventory)
- [ ] Redis 7 安装 + 密码设置
- [ ] Nacos 2.4 安装 + 命名空间配置
- [ ] JDK 17 安装 (生产推荐 Eclipse Temurin)
- [ ] Nginx 安装 + 生产配置 (nginx-production.conf)
- [ ] 防火墙/安全组规则确认

### 3. 应用部署
- [ ] 构建前端: `cd frontend && npm run build`
- [ ] 构建后端: `./mvnw package -DskipTests`
- [ ] systemd 服务创建: `./setup-prod.sh`
- [ ] Nacos 配置导入: 10 个 Sentinel 规则 JSON
- [ ] 健康检查: 6 服务 `/actuator/health` 全部 UP

### 4. 支付配置 (可选)
- [ ] 微信支付商户号/APIv3密钥/证书
- [ ] 支付宝 AppID/私钥/公钥
- [ ] 留空 = Mock 模式 (返回模拟数据)

### 5. 短信/OSS (可选)
- [ ] 阿里云短信 AccessKey + 签名 + 模板
- [ ] 阿里云 OSS Endpoint/Bucket/AccessKey
- [ ] 留空 = Mock 模式

### 6. 监控告警
- [ ] Prometheus 配置 6 服务抓取
- [ ] Grafana Dashboard 部署 + 数据源配置
- [ ] 告警规则启用: ServiceDown/HighCPU/HighErrorRate/DBPool/LowStock
- [ ] ELK 日志聚合 (可选)
- [ ] Zipkin 链路追踪 (可选)

### 7. 备份策略
- [ ] crontab 定时备份: `0 2 * * * /opt/yunxingcloud/backup-all.sh`
- [ ] 异地备份目标配置: `REMOTE_BACKUP=user@host:/path`
- [ ] 备份保留天数: 默认 7 天 (可调 RETENTION_DAYS)
- [ ] 验证恢复流程: `./backup-all.sh restore <timestamp>`

### 8. 部署方式选择
- [ ] **Docker Compose**: `docker-compose up -d` (开发/测试)
- [ ] **Docker Swarm**: `./swarm-deploy.sh deploy` (中小规模)
- [ ] **Kubernetes**: `kubectl apply -k k8s/` (大规模)
- [ ] **裸机 systemd**: `./deploy.sh full` (传统部署)
- [ ] **蓝绿部署**: `./deploy-blue-green.sh auto` (零停机)
- [ ] **金丝雀发布**: `./deploy-canary.sh start <svc> <ver>` (灰度)

### 9. 启动顺序 (依赖关系)
```
1. MySQL + Redis + Nacos + Sentinel (基础设施)
2. yunxingcloud-usercenter (8081)
3. yunxingcloud-payment (8083)
4. yunxingcloud-order (8084)
5. yunxingcloud-inventory (8085)
6. yunxingcloud-core (8080)
7. yunxingcloud-gateway (8090)
8. Nginx (80/443 → Gateway :8090)
```

### 10. 冒烟验证
```bash
./smoke-test.sh                    # 冒烟测试 (公开+认证+写操作)
./health-check.sh                  # 健康检查 (6 服务)
k6 run k6-smoke-test.js            # k6 冒烟压测
curl https://your-domain/actuator/health  # 公网可达性
```

## 快速命令参考

```bash
# 服务管理
systemctl start yunxingcloud-gateway
systemctl status yunxingcloud-core
journalctl -u yunxingcloud-order -f

# 部署
./deploy.sh full                   # 一键全量部署
./deploy.sh quick                  # 增量快速部署
./deploy.sh rollback               # 回滚到上次备份

# 监控
curl localhost:8090/actuator/health     # 网关健康
curl localhost:8090/actuator/prometheus # 指标
```