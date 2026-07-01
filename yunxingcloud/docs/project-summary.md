# yunxingcloud 项目总结

## 规模

| 维度 | 数量 |
|------|------|
| Java 模块 | 8 (common/api/core/gateway/usercenter/payment/order/inventory) |
| Java 源码 | 350+ 文件, 18,000+ 行 |
| 前端 SPA | 2 (Admin 31页 + Mall 16页) |
| TypeScript/Vue | 140+ 文件 |
| JPA 实体 | 65+ |
| REST 端点 | 110+ |
| Flyway 迁移 | 34 (V1-V14) |
| 数据库 | 6 独立 DB, 45+ 表 |
| 测试 | 55+ 文件 (集成+单元+E2E+Pact) |
| 运维脚本 | 32 |
| 配置编排 | 4套 (Compose/Swarm/K8s/ELK) |
| CI/CD 工作流 | 4 |
| 文档 | 13 篇 |

## 技术栈

| 层级 | 技术 |
|------|------|
| 运行时 | Java 17 · Spring Boot 4.0.6 · Spring Cloud |
| 认证 | JWT (HS384) · OAuth2/OIDC · Argon2id · TOTP |
| 数据库 | MySQL 8 · H2 · Flyway · HikariCP |
| 缓存 | Redis · Caffeine · Hibernate L2 |
| 前端 | Vue 3 · Vite 5 · Naive UI · Pinia · ECharts |
| 网关 | Spring Cloud Gateway · Sentinel · Resilience4j |
| 服务调用 | OpenFeign · RestTemplate · WebClient |
| 消息 | Spring Events · Kafka (可选) · WebSocket STOMP |
| 监控 | Prometheus · Grafana · ELK · Zipkin · OTel |
| 部署 | Docker · K8s · Swarm · systemd · Nginx |

## 安全

- JWT 双Token + 轮转防窃取
- Argon2id + BCrypt 兼容升级
- RBAC + 数据权限隔离
- XSS过滤 + CSP + HSTS
- 分布式锁(Redisson) + 幂等(Redis SETNX)
- 敏感数据脱敏 + 配置加密
- TOTP双因素认证
- OWASP Top10 逐项覆盖

## 业务功能

- 商品管理: CRUD/标签/组合套餐/搜索/对比/导入导出
- 订单系统: 购物车/下单/支付/退款/售后/发票
- 营销引擎: 优惠券/满减/折扣/拼团/秒杀/赠品
- 会员体系: 积分/等级/权益/降价提醒
- 内容运营: CMS文章/通知推送/客户反馈
- 社交裂变: 心愿单/分享追踪
- 物流追踪: 快递轨迹/包裹查询
- 个性化: 千人千面推荐/协同过滤

## 运维

- 一键部署: deploy.sh (12命令)
- 灰度发布: 金丝雀+蓝绿+Nginx加权
- 灾备恢复: RPO<1h RTO<15min
- 健康检查: 6服务每分钟自动检测重启
- 定时任务: 订单超时/秒杀启停/拼团过期/日报
- 日志聚合: Filebeat→Logstash→ES→Kibana
- 链路追踪: Gateway→Order→Payment/Inventory
