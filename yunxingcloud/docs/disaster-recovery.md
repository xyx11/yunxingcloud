# 灾备恢复方案

## RPO/RTO 目标

| 指标 | 目标 | 说明 |
|------|------|------|
| RPO | < 1小时 | 数据库每小时增量备份 |
| RTO | < 15分钟 | 自动故障转移 + 快速回滚 |

## 备份策略

| 类型 | 频率 | 保留 | 工具 |
|------|------|------|------|
| 全量DB | 每日 2:00 | 7天 | backup-all.sh |
| 增量DB | 每小时 | 24小时 | mysqlbinlog |
| 配置 | 每次变更 | 永久 | git + deploy.conf |
| 静态资源 | 每次部署 | 3版本 | uploads/ tar |

## 故障场景 & 恢复

### 单服务宕机
```bash
systemctl restart yunxingcloud-core
# Gateway 自动剔除不健康节点
```

### 数据库损坏
```bash
# 1. 停止所有服务
systemctl stop yunxingcloud-*
# 2. 恢复最新备份
./backup-all.sh restore LATEST
# 3. 应用 binlog (如有)
mysqlbinlog /var/log/mysql/mysql-bin.* | mysql
# 4. 启动服务
systemctl start yunxingcloud-*
```

### 服务器完全故障
```bash
# 1. 新服务器初始化
./deploy.sh init
# 2. 从异地备份恢复
rsync -avz backup-server:/backups/ ./backups/
./backup-all.sh restore LATEST
# 3. 部署应用
./deploy.sh full
# 4. DNS 切换
# 修改 A 记录指向新服务器 IP
```

### 全站宕机
```bash
# 检查顺序: 网络 → Nginx → Gateway → 各服务 → DB → Redis
curl localhost:80          # Nginx
curl localhost:8090/health # Gateway
for p in 8080 8081 8083 8084 8085; do curl localhost:$p/health; done
mysqladmin ping            # DB
redis-cli ping             # Redis
```

## 灾备演练

```bash
#!/bin/bash
# dr-drill.sh — 灾备演练脚本
echo "=== 灾备演练 ==="
echo "1. 验证备份完整性"
ls -lh /opt/yunxingcloud/backup/latest/
echo "2. 测试恢复 (staging)"
mysql -e "CREATE DATABASE test_restore"
gunzip < backup/latest/yunxingcloud_core.sql.gz | mysql test_restore
echo "3. 验证数据一致性"
mysql test_restore -e "SELECT COUNT(*) FROM sys_user"
echo "4. 清理"
mysql -e "DROP DATABASE test_restore"
echo "=== 演练完成 ==="
```

## 异地容灾

| 层级 | 方案 |
|------|------|
| DNS | 多A记录 + 健康检查 |
| 数据库 | 主从复制 + 延迟从库(30min) |
| 文件 | rsync / S3 跨区域复制 |
| 配置 | Git 多remote 推送 |