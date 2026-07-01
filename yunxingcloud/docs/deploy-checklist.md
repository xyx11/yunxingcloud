# 生产部署 Pre-flight Checklist

## 部署前检查

- [ ] 所有测试通过: `./mvnw test`
- [ ] 前端构建成功: `cd frontend && npm run build`
- [ ] Mall 构建成功: `cd frontend-mall && npm run build`
- [ ] JAR 打包成功: `./mvnw package -DskipTests`
- [ ] deploy.conf 已配置 (SERVER_HOST/USER/PORT/PASSWORD)
- [ ] JWT_SECRET 已生成: `openssl rand -base64 48`
- [ ] DB_PASSWORD 已更新
- [ ] SSL 证书有效: `certbot certificates`
- [ ] 防火墙规则: `ufw status` (仅 22/80/443)
- [ ] 备份已执行: `./backup-all.sh`

## 部署命令

```bash
# 首次部署
./deploy.sh init      # 初始化服务器环境
./deploy.sh full      # 完整部署

# 增量部署 (生产推荐)
./deploy.sh quick     # 跳过 clean + 并行编译

# 金丝雀发布 (高风险变更)
./deploy-canary.sh start core v2.1.0   # 10% 流量
./deploy-canary.sh promote core         # 全量切换

# 蓝绿部署 (零停机)
./deploy-blue-green.sh auto
```

## 部署后验证

```bash
# 1. 冒烟测试
./smoke-test.sh

# 2. 健康检查
for p in 8080 8081 8083 8084 8085 8090; do
  curl -s http://server:$p/actuator/health
done

# 3. 登录测试
curl -X POST https://www.yunxingcloud.com/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 4. 商品查询
curl https://www.yunxingcloud.com/api/products/hot
```

## 回滚预案

```bash
# 金丝雀回滚
./deploy-canary.sh abort core

# 完整回滚
./deploy.sh rollback

# 数据库回滚
./backup-all.sh restore <timestamp>
```

## 监控告警

- Prometheus: `https://www.yunxingcloud.com/actuator/prometheus`
- Grafana: `http://localhost:3000` (内网)
- Kibana: `http://localhost:5601` (内网)
- Sentinel: `http://localhost:8082`
- 告警通道: 钉钉/飞书 Webhook