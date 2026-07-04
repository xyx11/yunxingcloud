

# yunxingcloud 架构文档

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Nginx :443                             │
│              HTTPS · Brotli · 限流 · 静态资源                │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                 Gateway :8090 (WebFlux)                      │
│         路由分发 · Sentinel限流 · 请求日志 · CORS            │
└──┬──────┬──────┬──────┬──────┬──────┬──────────────────────┘
   │      │      │      │      │      │
┌──▼──┐ ┌▼───┐ ┌▼───┐ ┌▼───┐ ┌▼───┐ ┌▼──────────┐
│Core │ │UC  │ │Pay │ │Ord │ │Inv │ │Aggregation│
│8080 │ │8081│ │8083│ │8084│ │8085│ │   :8090   │
│SSO  │ │用户│ │支付│ │订单│ │库存│ │  Gateway   │
│管理 │ │注册│ │网关│ │商城│ │仓库│ │  聚合API   │
│工单 │ │OAuth│ │回调│ │秒杀│ │预警│ │           │
└──┬──┘ └──┬─┘ └──┬─┘ └──┬─┘ └──┬─┘ └───────────┘
   │       │      │      │      │
┌──▼───────▼──────▼──────▼──────▼──────────────────┐
│              MySQL 8 (6 独立 DB)                  │
│  sso / usercenter / payment / order / inventory  │
└──────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────┐
│        Redis · Nacos · Sentinel Dashboard        │
│    缓存/限流  注册中心  流控降级控制台            │
└──────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────┐
│    Prometheus · Grafana · ELK · Zipkin · OTel    │
│       指标采集    可视化   日志   链路追踪         │
└──────────────────────────────────────────────────┘
```

## 服务间调用

```
Order ──Feign──> Payment    (创建支付 → 查询状态)
Order ──Feign──> Inventory  (下单扣库存 → 取消回退)
Payment <──回调── 微信/支付宝 (异步通知)
Inventory ──SSE──> 前端     (库存预警实时推送)
Core ──RestTemplate──> Order (数据大屏聚合)
```

## 分层架构 (每服务)

```
controller/   REST API · SSE · WebSocket
    │
service/      业务逻辑 · SAGA · 缓存 · 锁
    │
repository/   JPA Repository · 自定义查询
    │
entity/       JPA Entity · 45+ 实体
    │
config/       安全 · 缓存 · CORS · Redisson · Kafka
```

## 安全架构

```
Nginx       TLS1.3 · HSTS · CSP · 限流zone
    │
Gateway     Sentinel 流控 · IP黑名单
    │
JWT Filter  双Token(access 2h + refresh 7d) · 轮转
    │
@PreAuthorize  RBAC 方法级 · 数据权限 AOP
    │
Business    分布式锁(Redisson) · 幂等(Redis SETNX) · 脱敏
```

## 数据流

```
用户请求 → Nginx → Gateway → JWT认证 → Controller
    → Service(缓存检查 → DB查询 → 缓存回填)
    → 领域事件(Spring/Kafka) → 异步监听器(邮件/WS/指标)
    → 响应 → Gateway → Nginx → 用户

日志: Logback JSON → Filebeat → Logstash → ES → Kibana
追踪: Micrometer Tracing → Zipkin/OTel
指标: Micrometer → Prometheus → Grafana
```

## 部署架构

```
开发:    java -jar (H2) + vite dev
测试:    mvn test (H2 + @SpringBootTest)
预发:    docker-compose (staging profile)
生产:    K8s / Swarm / systemd (prod profile)
```

## 技术债务 & 演进方向

- [ ] gRPC 服务间通信 (替代 Feign 提升性能)
- [ ] 分库分表 (ShardingSphere)
- [ ] 读写分离 (MySQL 主从)
- [ ] 多活容灾 (异地多活)
- [ ] 全链路压测平台化
- [ ] 自动化安全扫描 (OWASP ZAP)
