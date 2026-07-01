# 成本优化指南

## 云资源推荐配置

| 环境 | 配置 | 月费(估算) |
|------|------|-----------|
| 开发 | 1台 2C4G ECS | ¥200 |
| 预发 | 1台 4C8G ECS (all-in-one) | ¥400 |
| 生产(低) | 1台 4C8G ECS + RDS 2C4G | ¥800 |
| 生产(中) | 2台 4C8G + RDS 4C8G + Redis 2G | ¥1,500 |
| 生产(高) | 3台 8C16G + RDS 8C16G(主从) + Redis 4G(集群) | ¥5,000 |

## JVM 内存优化

```bash
# 每个服务建议 (按实际负载调整)
- core:       -Xms256m -Xmx512m   (管理后台+SPA托管)
- gateway:    -Xms128m -Xmx256m   (纯转发, 轻量)
- usercenter: -Xms192m -Xmx384m   (用户注册)
- payment:    -Xms192m -Xmx384m   (支付SDK较重)
- order:      -Xms256m -Xmx512m   (核心业务, 缓存多)
- inventory:  -Xms192m -Xmx384m   (库存, 连接池小)

# 总计: ~1.5GB RSS (6 服务)
```

## 数据库连接池

```yaml
# HikariCP — 按负载调整
core:       max 15, min 5   # 管理后台, 并发低
gateway:    max 5,  min 2   # 无数据库连接
usercenter: max 5,  min 2
payment:    max 5,  min 2
order:      max 15, min 5   # 核心业务, 并发高
inventory:  max 5,  min 2

# 总连接: ~50 (MySQL max_connections 建议 200)
```

## Redis 内存

```
# 预估
缓存数据:    50MB (商品/分类/品牌 + Session)
幂等Key:     10MB (5s TTL)
限流Key:     5MB
分布式锁:    1MB (临时)
─────────────
合计:       ~70MB → 分配 256MB 充足
```

## 带宽 & 存储

| 项目 | 日流量 | 月费用 |
|------|--------|--------|
| 静态资源 | 500MB (CDN命中80%) | ¥10 |
| API流量 | 2GB | ¥0 (内网) |
| 日志 | 500MB × 7天 = 3.5GB | ¥0 |
| 数据库 | 1GB + 备份 7GB | ¥20 |

## 省钱技巧

1. **CDN**: 静态资源 CDN (`/assets/`, `/mall/`)
2. **Gzip/Brotli**: 压缩率 70%+, 节省带宽
3. **缓存**: Redis 缓存减少 DB 查询 60%+
4. **Sentinel**: 限流防止恶意请求消耗资源
5. **按需扩容**: K8s HPA 根据 CPU/内存自动伸缩
6. **Spot实例**: 非核心服务使用抢占式实例
7. **预留实例**: 包年包月比按量便宜 40%
8. **日志**: 生产环境 WARN 级别, 减少 IO