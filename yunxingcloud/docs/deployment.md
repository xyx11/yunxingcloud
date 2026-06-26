# yunxingcloud 部署指南

## 生产架构

```
Browser → Nginx:443 → Gateway:8090 ─┬─→ Core:8080 (SSO + 管理 + SPA)
                                     └─→ Usercenter:8081 (注册 + 用户)
                          MySQL:3306 ←── Core + Usercenter
                          Redis:6379 ←── Core
```

## 前置条件

- 阿里云 ECS（建议 4GB+ 内存，当前运行在 2GB）
- 域名 DNS 解析到服务器 IP
- Java 17、MySQL 5.7+、Redis、Nginx

## 首次部署

### 1. 配置密钥

```bash
cp deploy.conf.example deploy.conf
# 编辑 deploy.conf，填入服务器信息和密钥
# 生成强密钥: openssl rand -base64 48
```

### 2. 初始化服务器

```bash
ssh root@<server-ip> "
  yum install -y java-17-openjdk nginx mysql-server redis
  mkdir -p /opt/yunxingcloud/{app,logs,backup}
"
```

### 3. 构建并部署

```bash
# 构建
./mvnw package -pl yunxingcloud-core -am -DskipTests
cd frontend && npm run build && cd ..

# 上传
scp yunxingcloud-core/target/*.jar root@<server>:/opt/yunxingcloud/app/
```

### 4. SSL 证书

```bash
ssh root@<server> "certbot --nginx -d www.yunxingcloud.com"
```

### 5. 配置 systemd

将服务文件复制到 `/etc/systemd/system/`，启用开机自启：

```bash
systemctl enable yunxingcloud-core yunxingcloud-gateway yunxingcloud-usercenter
systemctl start yunxingcloud-core yunxingcloud-gateway yunxingcloud-usercenter
```

## 服务管理

| 命令 | 说明 |
|------|------|
| `systemctl status yunxingcloud-*` | 查看服务状态 |
| `systemctl restart yunxingcloud-*` | 重启服务 |
| `journalctl -u yunxingcloud-core -f` | 查看日志 |
| `/opt/yunxingcloud/app/status.sh` | 运行状态概览 |

## 数据库备份

自动每日 2:00 AM 备份，保留 7 天。

```bash
# 手动备份
bash /opt/yunxingcloud/app/backup.sh

# 查看备份
ls /opt/yunxingcloud/backup/
```

## 回滚

```bash
# 1. 停止服务
systemctl stop yunxingcloud-core yunxingcloud-gateway yunxingcloud-usercenter

# 2. 恢复数据库
gunzip < /opt/yunxingcloud/backup/yunxingcloud_*.sql.gz | mysql -u root -p

# 3. 恢复旧 JAR
cp /opt/yunxingcloud/backup/old.jar /opt/yunxingcloud/app/yunxingcloud-core.jar

# 4. 启动
systemctl start yunxingcloud-core yunxingcloud-gateway yunxingcloud-usercenter
```

## CI/CD

push main 分支自动触发部署（需配置 GitHub Secrets）：

- `ECS_HOST` - 服务器 IP
- `ECS_USER` - SSH 用户
- `ECS_SSH_KEY` - SSH 私钥
