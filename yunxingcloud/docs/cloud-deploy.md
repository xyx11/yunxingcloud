# yunxingcloud 云部署指南

## 前置准备

```bash
# 1. 阿里云 ECS (2核4G 推荐, CentOS 8+ / Alibaba Cloud Linux 3)
# 2. 安全组开放端口: 22, 80, 443
# 3. 域名解析: www.yunxingcloud.com → ECS 公网 IP
# 4. 配置 deploy.conf 中的 SERVER_HOST / SERVER_USER / 密钥路径
```

## 方式一: 一键部署 (推荐)

```bash
# 本地执行
./deploy.sh init      # 初始化 ECS 环境 (JDK/MySQL/Nginx/防火墙)
./deploy.sh full      # 构建 → 上传 → 启动 → 健康检查 (6 服务)

# 或直接 SSH 到 ECS 执行
ssh root@your-server
cd /opt/yunxingcloud
./setup-prod.sh       # 一键初始化 + 创建 systemd + 启动

# 验证
./smoke-test.sh       # 冒烟测试
curl localhost:8090/actuator/health
```

## 方式二: Docker Compose

```bash
# 本地构建镜像并推送
./docker-buildx.sh push  # 多架构 (amd64+arm64)

# SCP 到 ECS
scp docker-compose.yml docker-compose-monitoring.yml .env root@your-server:/opt/yunxingcloud/

# ECS 上启动
ssh root@your-server
cd /opt/yunxingcloud
docker-compose up -d                          # 9 服务
docker-compose -f docker-compose-monitoring.yml up -d  # +监控栈
```

## 方式三: Kubernetes

```bash
# 推送镜像到 GHCR
docker buildx build --platform linux/amd64 --push -t ghcr.io/xyx/yunxingcloud-core:latest .

# 部署到 K8s
kubectl apply -k k8s/
kubectl get pods -n yunxingcloud
```

## Nginx + HTTPS

```bash
# 安装证书
yum install -y certbot python3-certbot-nginx
certbot --nginx -d www.yunxingcloud.com

# 使用生产配置
cp nginx-production.conf /etc/nginx/conf.d/yunxingcloud.conf
nginx -t && systemctl restart nginx
```

## 监控接入

```bash
# Prometheus + Grafana
docker-compose -f docker-compose-monitoring.yml up -d
# Grafana: http://your-server:3000 (admin/admin)
# Prometheus: http://your-server:9090

# ELK (可选)
docker-compose -f docker-compose-elk.yml up -d
# Kibana: http://your-server:5601
```

## 备份

```bash
# crontab 定时备份
0 2 * * * /opt/yunxingcloud/backup-all.sh sync

# 异地同步 (配置 REMOTE_BACKUP 环境变量)
REMOTE_BACKUP=backup@backup-server:/backups ./backup-all.sh sync
```

## 验证清单

- [ ] `curl https://www.yunxingcloud.com/actuator/health` → 200
- [ ] `curl https://www.yunxingcloud.com/api/login` → 200
- [ ] Grafana 仪表盘有数据
- [ ] SSL Labs 评级 ≥ A
- [ ] `./smoke-test.sh` 全部通过
---

## 方式三: 阿里云 ACK (推荐生产)

### 前置准备

```bash
# 1. 创建 ACK 集群 (标准托管版, 2 节点 ecs.g7.xlarge 起步)
# 2. 创建 ACR 企业版镜像仓库
#    控制台 → 容器镜像服务 → 创建命名空间 yunxingcloud
# 3. 配置 kubectl 连接 ACK
#    控制台 → 集群 → 连接信息 → 复制 kubeconfig
# 4. Docker 登录 ACR
docker login --username=your-aliyun-account registry.cn-hangzhou.aliyuncs.com

# 5. (可选) 创建 RDS MySQL 8.0 + Redis 6.0
#    或使用 K8s 内部署 (infrastructure.yaml)
# 6. 域名 DNS A 记录 → SLB 公网 IP
```

### 一键部署

```bash
# 构建镜像 + 推送 ACR + Kubectl 部署
bash scripts/deploy-ack.sh registry.cn-hangzhou.aliyuncs.com/yunxingcloud v1.0.0

# 或使用 GitHub Actions
# 配置 Secrets: ACR_USERNAME, ACR_PASSWORD, KUBE_CONFIG
# git push main → 自动触发 deploy-k8s.yml
```

### ACK 资源配置

| 资源 | 规格 | 说明 |
|------|------|------|
| ACK 集群 | 标准托管版 2 节点 | ecs.g7.xlarge (4C16G) |
| SLB | 按量付费 | ACK 自动创建 |
| MySQL | RDS 8.0 2C4G 100GB | 或集群内自建 (infrastructure.yaml) |
| Redis | Redis 6.0 2GB | 或集群内自建 |
| Nacos | MSE 2C4G | 或集群内自建 (infrastructure.yaml) |
| Elasticsearch | 3 节点 2C4G | 可选, 用于商品搜索 |
| OSS | 标准存储 100GB | 文件上传 (OssService) |
| 域名 | 已备案 | api.your-domain.com / admin.your-domain.com |

### 成本估算 (月)

| 组件 | 月费 (¥) |
|------|----------|
| ACK 2 节点 ecs.g7.xlarge | ~1,200 |
| RDS MySQL 2C4G | ~400 |
| Redis 2GB | ~200 |
| MSE Nacos 2C4G | ~400 |
| SLB | ~100 |
| OSS 100GB | ~20 |
| 域名 + DNS | ~10/年 |
| **合计** | **~2,400/月** |

### 部署后验证

```bash
# 检查 Pod
kubectl get pods -n yunxingcloud

# 获取 SLB IP
kubectl get svc -n kube-system nginx-ingress-lb

# API 测试
curl https://api.your-domain.com/actuator/health
curl -X POST https://api.your-domain.com/api/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'

# 管理后台
open https://admin.your-domain.com
