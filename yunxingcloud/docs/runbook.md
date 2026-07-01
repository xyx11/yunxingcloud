# yunxingcloud 生产运维 Runbook

## 服务端口

| 服务 | 端口 | Health | Prometheus |
|------|------|--------|------------|
| core | 8080 | :8080/actuator/health | :8080/actuator/prometheus |
| usercenter | 8081 | :8081/actuator/health | :8081/actuator/prometheus |
| payment | 8083 | :8083/actuator/health | :8083/actuator/prometheus |
| order | 8084 | :8084/actuator/health | :8084/actuator/prometheus |
| inventory | 8085 | :8085/actuator/health | :8085/actuator/prometheus |
| gateway | 8090 | :8090/actuator/health | - |

## 常见故障排查

### 服务宕机

```bash
# 1. 检查服务状态
systemctl status yunxingcloud-*

# 2. 查看最近日志
journalctl -u yunxingcloud-core --since "5 min ago" -f

# 3. 检查端口
ss -tlnp | grep -E "808[0-5]|8090"

# 4. 重启单个服务
systemctl restart yunxingcloud-core
```

### 内存溢出 (OOM)

```bash
# 查看堆 dump
ls -la /app/logs/*.hprof

# 分析 (本地)
jhat /app/logs/java_pid.hprof

# 临时措施：增大内存重启
sed -i 's/-Xmx512m/-Xmx768m/' /etc/systemd/system/yunxingcloud-*.service
systemctl daemon-reload && systemctl restart yunxingcloud-core
```

### 数据库连接池耗尽

```bash
# 查看活跃连接
mysql -e "SHOW PROCESSLIST;" | wc -l

# 检查慢查询 (>1s)
mysql -e "SELECT * FROM information_schema.processlist WHERE time > 1;"

# 临时增加连接池
# 编辑 application-prod.yaml → hikari.maximum-pool-size
```

### Redis 不可用

```bash
# 检查 Redis
redis-cli ping

# 重启
systemctl restart redis

# 服务会自动重连（Lettuce 自适应刷新）
```

### Nacos 不可用

```bash
# 服务使用本地缓存继续运行，恢复后自动重连
curl http://localhost:8848/nacos/v1/console/health/readiness
```

### 磁盘满

```bash
df -h /
# 清理日志
find /app/logs -name "*.log" -mtime +7 -delete
# 清理 Docker
docker system prune -af --volumes
```

### 高 CPU

```bash
# 找到高 CPU 线程
top -H -p $(pgrep -f yunxingcloud-core)

# 线程 dump
jstack $(pgrep -f yunxingcloud-core) > /tmp/threads.txt

# GC 频繁 → 检查堆
jstat -gcutil $(pgrep -f yunxingcloud-core) 1000
```

## 性能诊断

### 慢接口定位

```bash
# 查看 Sentinel Dashboard
open http://localhost:8082

# 查看 P95 延迟
curl http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:/api/orders

# 实时日志
tail -f /app/logs/yunxingcloud-order.log | grep "OK in [0-9]*ms" | awk '{print $NF}'
```

### 数据库慢查询

```sql
-- 当前慢查询
SELECT * FROM performance_schema.events_statements_summary_by_digest
WHERE AVG_TIMER_WAIT > 1000000000 ORDER BY AVG_TIMER_WAIT DESC LIMIT 10;

-- 锁等待
SELECT * FROM information_schema.innodb_lock_waits;
```

## 应急响应

### 回滚部署

```bash
./deploy.sh rollback
# 或手动
systemctl stop yunxingcloud-*
cp /opt/yunxingcloud/backup/latest/*.jar /opt/yunxingcloud/app/
systemctl start yunxingcloud-*
```

### 灰度回滚

```bash
./deploy-canary.sh abort core
```

### 数据库恢复

```bash
./backup-all.sh restore 20260701_020000
```

### 紧急限流

```bash
# Sentinel Dashboard: 将 API QPS 降到 50
# 或直接修改 Nacos 配置
curl -X POST http://localhost:8848/nacos/v1/cs/configs \
  -d 'dataId=yunxingcloud-core-sentinel-flow&group=DEFAULT_GROUP&content=[...]'
```

### 封禁恶意 IP

```bash
# Nginx 层
echo 'deny 1.2.3.4;' >> /etc/nginx/conf.d/block.conf && nginx -s reload

# 应用层: 通过 IP 黑名单管理页面添加
```

## 日志排查

```bash
# 错误日志 (最近 100 行)
grep -rn "ERROR" /app/logs/ | tail -100

# 某个用户的请求链路 (X-Request-Id)
grep "X-Request-Id: abc123" /app/logs/*.log

# 支付回调日志
tail -f /app/logs/yunxingcloud-payment.log | grep "callback"

# Filebeat → ES → Kibana
open http://localhost:5601
```

## 告警处理

| 告警 | 严重度 | 处理 |
|------|--------|------|
| ServiceDown | critical | 立即重启 + 检查日志 |
| HighErrorRate | critical | 检查依赖服务 + 回滚 |
| HighCPUUsage | warning | 检查 GC + 限流 |
| HighHeapMemory | warning | 检查内存泄漏 |
| DBConnectionPool | warning | 检查慢查询 + 增加连接池 |
| LowStock | info | 通知补货 |
