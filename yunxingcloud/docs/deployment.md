# yunxingcloud 部署指南

## 生产架构

```
                    ┌──────────────────────────────────────────┐
                    │           Nginx :443 (HTTPS)              │
                    │     反向代理 + 静态文件 + Gzip            │
                    └────────────┬─────────────────────────────┘
                                 │
                    ┌────────────▼─────────────────────────────┐
                    │        Gateway :8090 (内网)               │
                    │     路由分发 · Sentinel 限流/熔断         │
                    └──┬──────┬──────┬──────┬──────┬──────────┘
                       │      │      │      │      │
              ┌────────▼┐ ┌───▼──┐ ┌─▼───┐ ┌▼───┐ ┌▼──────┐
              │ Core    │ │ UC   │ │Pay  │ │Ord │ │ Inv   │
              │ :8080   │ │:8081 │ │:8083│ │:8084│ │:8085 │
              │ SSO+管理│ │用户  │ │支付 │ │订单 │ │库存  │
              └────┬────┘ └──┬───┘ └──┬──┘ └──┬──┘ └──┬────┘
                   │         │        │       │       │
              ┌────▼─────────▼────────▼───────▼───────▼────┐
              │              MySQL 8 (6 DB)                │
              │  sso / usercenter / payment / order / inv  │
              └────────────────────────────────────────────┘
              ┌────────────────────┐
              │  Redis · Nacos     │
              │  Sentinel Dashboard│
              └────────────────────┘
```

## 前置条件

- 阿里云 ECS（建议 4GB+ 内存）
- 域名 DNS 解析到服务器 IP
- Java 17、MySQL 8、Redis、Nginx

## 首次部署

### 1. 配置密钥

```bash
cp deploy.conf.example deploy.conf
# 编辑 deploy.conf，填入：
#   SERVER_HOST=你的服务器IP
#   SERVER_USER=root
#   DB_PASSWORD=强密码
#   JWT_SECRET=$(openssl rand -base64 48)
```

### 2. 初始化服务器

```bash
./deploy.sh init
# 自动安装 Java 17、MySQL、Redis
# 创建目录结构、配置防火墙
```

### 3. 一键部署

```bash
./deploy.sh full      # 完整部署：构建→上传→启动→健康检查
./deploy.sh quick     # 增量部署：跳过 clean + 并行编译
```

### 4. SSL 证书

```bash
ssh root@<server> "certbot --nginx -d www.yunxingcloud.com"
# 自动续期: systemctl enable certbot-renew.timer
```

### 5. 配置 systemd（可选，deploy.sh 已处理）

```bash
systemctl enable yunxingcloud-{core,gateway,usercenter,payment,order,inventory}
systemctl start yunxingcloud-{core,gateway,usercenter,payment,order,inventory}
```

## 支付配置（可选）

支付服务默认使用 mock 模式。启用真实支付需在服务器配置文件中填入商户凭证：

```yaml
# /opt/yunxingcloud/app/payment-application.yaml
payment:
  wechat:
    merchant-id: "1234567890"
    merchant-serial-number: "ABC123..."
    api-v3-key: "your-api-v3-key"
    private-key-path: "/etc/yunxingcloud/apiclient_key.pem"
    app-id: "wxabc123..."
  alipay:
    app-id: "2021..."
    private-key: "MIIEvQIBADAN..."
    alipay-public-key: "MIIBIjAN..."
```

配置后自动切换为 live 模式，调用微信/支付宝真实 API。

## 部署命令参考

| 命令 | 说明 |
|------|------|
| `./deploy.sh full` | 完整部署（构建→上传→启动→检查） |
| `./deploy.sh quick` | 增量构建快速部署 |
| `./deploy.sh build` | 仅构建（前端 + 后端 6 模块） |
| `./deploy.sh build-backend` | 仅构建后端 |
| `./deploy.sh build-frontend` | 仅构建前端（Admin + Mall） |
| `./deploy.sh restart` | 远程重启全部 6 服务 |
| `./deploy.sh stop` | 远程停止全部服务 |
| `./deploy.sh status` | 查看服务健康状态 |
| `./deploy.sh logs 100` | 查看最近 100 行日志 |
| `./deploy.sh migrate` | 执行数据库迁移 |
| `./deploy.sh rollback` | 回滚到上一个备份 |

## 服务管理

| 命令 | 说明 |
|------|------|
| `systemctl status yunxingcloud-*` | 查看所有服务状态 |
| `systemctl restart yunxingcloud-core` | 重启单个服务 |
| `journalctl -u yunxingcloud-payment -f` | 查看支付服务日志 |
| `curl localhost:8080/actuator/health` | Core 健康检查 |
| `curl localhost:8083/actuator/prometheus` | Payment 指标 |

## 数据库备份

自动每日 2:00 AM 备份，保留 7 天。

```bash
# 手动备份全部数据库
bash /opt/yunxingcloud/app/backup.sh

# 查看备份
ls /opt/yunxingcloud/backup/
```

## 回滚

```bash
./deploy.sh rollback
# 自动：停止 → 恢复上个备份 → 重启 → 健康检查
```

## CI/CD

push main 分支自动触发部署（需配置 GitHub Secrets）：

- `ECS_HOST` - 服务器 IP
- `ECS_USER` - SSH 用户
- `ECS_SSH_KEY` - SSH 私钥

## Kubernetes 部署

```bash
kubectl apply -k k8s/
# 6 Deployment + 6 Service + HPA + Ingress
```

## Docker Swarm 部署

```bash
docker stack deploy -c docker-compose-swarm.yml yunxingcloud
# 9 服务: 3 基础设施 + 6 应用
```

## 监控地址

| 服务 | URL |
|------|-----|
| Core Prometheus | `https://www.yunxingcloud.com/actuator/prometheus` |
| Gateway Health | `http://localhost:8090/actuator/health` |
| Sentinel Dashboard | `http://localhost:8082` (admin/admin) |
| Nacos Console | `http://localhost:8848/nacos` |