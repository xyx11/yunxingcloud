# Changelog

## v2.4.0 (2026-07-01)

### 服务发现与安全
- Gateway/Aggregation 路由改用 Nacos `lb://` 服务发现 (替换全部 localhost 硬编码)
- 全部 6 服务密码/URL 环境变量化 (DB_URL/DB_PASSWORD/JWT_SECRET)
- 所有服务 ddl-auto 统一为 validate/none (生产安全)
- K8s Secret/ConfigMap 补全 + Deployment 添加独立 DB_URL
- CSP 安全头 + X-Frame SAMEORIGIN + Permissions-Policy
- 文件上传白名单 (扩展名+MIME) + 路径遍历防护

### 代码健壮性
- PaymentService 全部写方法添加 @Transactional
- OrderService 订单取消竞态条件修复 (re-read status before cancel)
- CampaignService/FlashSaleService 库存扣减改为 JPQL 原子 UPDATE
- OrderMailService @Autowired(required=false) + null-safe
- PasswordValidator null 输入 NPE 修复
- IdempotentAspect/UserRateLimitAspect 添加 @ConditionalOnClass+@ConditionalOnBean
- EventBus 线程池 daemon 化 + @PreDestroy 优雅关闭
- @Async 默认线程池切换为 taskExecutor (4/8 核 + 100 队列)

### 依赖与兼容
- JaCoCo 0.8.12 → 0.8.15 (JDK 26 官方支持)
- Redisson 3.37.0 → 4.6.0 (Spring Boot 4.0 兼容)
- Gateway 添加 micrometer-registry-prometheus
- Payment/Usercenter 添加 spring-boot-starter-data-redis
- Inventory 添加 yunxingcloud-api 依赖

### 测试 (43 新增 + 预存修复)
- common: R/Snowflake/Password/DataSanitizer (37 tests)
- api: PaymentClient/InventoryClient Fallback (6 tests)
- frontend-mall: useToast/stores (8 tests)
- PaymentControllerTest: JWT 权限修复 (ticket:read,ticket:write)
- ProductControllerTest: 认证期望修复
- InventoryControllerTest: Redisson 兼容修复
- FullStackIntegrationTest: 重写为 Core 模块纯集成测试
- BannerRepository: findAllByOrderBySortOrderAsc 方法名修复

### DevOps
- Docker Compose payment/order/inventory 添加 healthcheck + REDIS_HOST
- CI 添加 ESLint + vue-tsc + npm test 质量门禁
- deploy.sh 健康检查升级为 6 服务逐一验证
- swarm-deploy.sh update 端口映射修复
- Makefile 新增 dev-all/test-all/lint/type-check/k6-* (9 targets)
- prometheus.yml 抓取配置 (6 服务) + prometheus-alerts.yml (8 规则)
- deploy-blue-green.sh bash 数组语法修复

### 国际化
- payment/order/inventory 模块添加 i18n (80+ 条中英文消息)

### 高级特性
- InventoryService 集成 RedissonLockService 分布式锁
- OrderService.submit 添加 @Idempotent 防重复下单
- WarehouseService 新建 (仓库 CRUD 专项服务)
- InventoryService transfer 原子化调拨
- 全部 6 服务 graceful shutdown 30s 统一配置
- 旧数据库名 sso_yunxingcloud → yunxingcloud_core 全面清理 (14 文件)

## v2.0.0 (2026-06-30)

### 新增微服务模块
- 工单管理 (sys_ticket) — 创建/指派/处理/关闭工单，自动编号 TK+日期+序号
- 支付系统 (yunxingcloud-payment:8083) — 微信支付/支付宝 SDK 集成 + 异步回调
- 订单系统 (yunxingcloud-order:8084) — 商品管理 + 购物车 + 下单 + Feign→Payment
- 库存系统 (yunxingcloud-inventory:8085) — 仓库管理 + 入库/出库 + 预警 + Feign↔Order

### 架构演进
- 6 微服务: Core + Gateway + Usercenter + Payment + Order + Inventory
- Feign 服务间调用: Order→Payment 支付, Order↔Inventory 库存扣减/回退
- Gateway 路由分发到 6 个服务

### 生产优化
- JVM 内存调优 (6 服务共 ~1.1GB RSS)
- 防火墙 (仅 22/80/443) + 服务 127.0.0.1 绑定
- Nginx: Cache-Control、SW 不缓存、API 故障转移
- Service Worker v3 (全缓存清除 + 强制重载)
- SSL 自动续期 (certbot-renew.timer)
- Swap 1GB + MySQL 慢查询监控
- deploy.sh 支持全服务一键部署

## v2.1.0 (2026-07-01)

### 安全加固
- 分布式锁防超卖 (Redisson) — 库存扣减/订单提交关键路径保护
- XSS 防护过滤器 — 请求参数 HTML 转义 + 安全响应头
- 文件上传安全校验 — 魔数验证 + 类型白名单 + 大小限制
- 敏感数据脱敏 (@Sensitive) — 手机号/邮箱/身份证/地址自动掩码
- 幂等性处理 (@Idempotent) — Redis SETNX 防重复下单/支付
- 数据权限隔离 (@DataScope) — 部门级数据权限 AOP
- 统一错误码 (ErrorCode) — 30+ 错误码 + BusinessException + 标准 JSON 响应
- 配置加密 (Jasypt) — ENC() 密文存储敏感配置
- 登录审计增强 — 异步记录 IP/UserAgent/OS/设备类型

### 业务增强
- 优惠券下单核销 — 自动匹配可用优惠券 + 门槛校验 + 金额扣减
- 库存预留机制 — 下单预占库存 + 超时 15 分钟自动释放
- 订单超时取消 (Quartz) — 每 30s 扫描过期订单自动取消退券
- 订单邮件通知 — 下单/支付/发货/退款 4 生命周期异步邮件
- WebSocket 实时推送 — 管理员新订单通知 + 库存预警
- 商城 i18n — 6 个核心页面中英切换 + 参数插值
- 商品图片上传 — 多图上传 + JPG/PNG/GIF/WEBP + UUID 命名

### 韧性架构
- Feign Sentinel 降级 — PaymentClient/InventoryClient fallbackFactory
- Resilience4j 重试 — Feign 调用自动重试 3 次间隔 500ms
- 支付网关 mock/live 双模 — 空配置→mock，填凭证→真实 API
- 库存预警 SSE 实时推送 — 30s 间隔推送低库存数据
- 服务优雅关闭 — graceful shutdown 30s + K8s 就绪探针
- 健康检查聚合 — DB/磁盘/JVM 内存综合健康状态

### 可观测性
- Prometheus 告警规则 — 8 条规则 (CPU/内存/QPS/错误率/连接池/库存)
- ELK 日志聚合 — ES + Logstash + Kibana + Filebeat + JSON 日志格式
- Zipkin 链路追踪 — Gateway→Order→Payment/Inventory 全链路可视化
- OpenTelemetry 配置 — OTLP gRPC + Prometheus 导出 + 10% 采样
- Micrometer 业务指标 — 订单/支付/库存 Counter + Timer
- API 请求日志 (@ApiLog) — AOP 自动记录入参/出参/IP/耗时

### 基础设施
- K8s 完整清单 — 6 服务 Deployment + Service + HPA + 金丝雀模板
- Docker Swarm 编排 — 6 应用 + MySQL + Redis 生产级部署
- frontend-mall 独立 SPA — 13 视图 + API 模块化 + 构建集成
- 全链路压测 (k6) — 8 步全流程 + 尖峰 50VU 脚本
- 灰度发布 — deploy-canary.sh (启动/全量/回滚) + Nginx 加权分流
- Nacos 动态配置 — 6 服务流控/降级/功能开关模板
- 全库备份脚本 — 6 DB mysqldump + rsync 异地同步
- 多环境配置 — dev/staging/prod 三套 profile
- Nginx 生产增强 — TLS 1.3 + Brotli + 限流 zone + 安全头
- Makefile 全服覆盖 — dev/package/test/docker 全部 6 服务
- 冒烟测试脚本 — 6 服务健康检查 + 关键 API 一键验证
- Git pre-commit hook — ESLint + TypeScript + Maven 编译检查

### 开发体验
- 统一分页 (PageRequest/PageResponse) — 标准化列表接口
- 自定义校验 (@Phone/@IdCard) — 手机号/身份证 Bean Validation
- 通用 CSV 导出 — 反射 + 注解自动导出 JPA 实体
- CORS 自动配置 — 开发环境一键放行
- 领域事件 — OrderCreated/Paid/Shipped/Canceled 异步解耦
- 多租户上下文 — TenantContext + X-Tenant-Id 请求头隔离

### 测试
- 新增 21 个集成测试 (payment 6 + order 7 + inventory 8)
- E2E 新增 10 个用例 (工单/支付/订单/商品/库存/仓库)

### 文档
- README.md 全面刷新 — 6 服务架构图 + API 端点表 + 部署方式
- docs/README.md — 微服务表格 + 服务间调用关系
- docs/deployment.md — 全 6 服务架构图 + 支付配置 + K8s/Swarm

## v2.3.0 (2026-07-01)

### 营销引擎
- 营销活动 — 满减/折扣/赠品 + Redis限购 + 库存锁定
- 拼团完善 — GroupBuyService 开团/参团/成团/超时退款
- 秒杀完善 — FlashSaleService Redis原子库存+预热+限购
- 售后系统 — AfterSaleService 退款/退货/换货审核流程
- 发票系统 — InvoiceService 个人/企业发票申请开具
- 积分体系 — PointsService 注册赠送/购物返利/积分抵扣
- 礼品卡 — GiftCardService 16位卡号/激活绑定/余额支付
- 推荐引擎 — RecommendationService 协同过滤/个性化/热门

### 内容运营
- CMS 文章管理 — Article 实体+Controller+管理页
- 通知中心 — Notification 系统/订单/促销消息推送
- SEO 优化 — SeoMetaService OG标签/sitemap生成
- A/B 测试 — ABTestService 一致性哈希+加权分流

### 物流与对比
- 物流追踪 — LogisticsTrace 轨迹查询+快递单号
- 商品对比 — CompareList 最多4商品属性对比

### 基础设施
- 数据看板 — AnalyticsService 销售概览/订单趋势/商品排行
- 实时大屏 — RealtimeDashboard SSE每秒推送JVM指标
- 迁移 V5-V10 — 15张新表
- 管理后台 10+ 新页面 — 拼团/秒杀/售后/内容/通知/礼品卡/营销
- Gateway 路由 20+ 新路径

### 开源社区
- MIT License + README 徽章
- PR/Issue 模板 + CONTRIBUTING.md
- Dependabot + CodeQL + Release Drafter
- JaCoCo 代码覆盖率

### 文档
- 系统架构文档 + 灾备方案 + 安全清单(OWASP)
- 成本优化指南 + 数据库迁移索引
- 部署检查清单 + 生产部署 Checklist

## v1.0.0 (2026-06-05)

### 核心架构
- Spring Boot 4.0.6 多模块 Maven 项目 (common/core/gateway/usercenter)
- Vue 3 + Vite + TypeScript + Naive UI 2 前端 SPA
- JWT 双 Token 认证 (access 2h + refresh 7d)
- OAuth2 / OIDC 授权服务器
- RBAC 权限模型 (user_roles 多对多 + @PreAuthorize)
- API 网关 (限流 + 熔断 + 请求日志)

### 前端特性
- PWA 支持 (manifest + Service Worker)
- 暗色/亮色主题切换
- 中/English 国际化 (vue-i18n)
- ECharts 仪表盘 (柱状图 + 饼图)
- Ctrl+K 命令面板 (Spotlight 风格)
- 路由懒加载 (40+ chunks)
- 表格分页/搜索/排序/CSV导出
- 404 美化页面 + 公告横幅
- 键盘快捷键 + 响应式布局
- Playwright E2E 测试 (4 tests)

### 后端特性
- JWT 认证 + Token 黑名单 + 活跃会话管理
- IP 限流 (10次/分钟) + 账号锁定 (5次/30分钟)
- 密码强度策略 (8位+大小写+数字+特殊字符)
- Bean Validation 参数校验 + 全局异常处理
- 统一错误码体系 (ErrorCode 枚举 20+ 错误码)
- 安全响应头 (XSS/HSTS/X-Frame)
- Caffeine 本地缓存 (菜单/配置 30min)
- Flyway 数据库版本迁移 (4 个版本)
- Spring Mail 邮件服务
- SSE 实时推送 (Server-Sent Events)
- 功能开关 + 系统公告
- 系统维护工具 (日志清理/Token清理/数据库统计)
- 文件上传 + 用户头像
- CSV 批量导入用户
- 全局搜索 (跨实体)
- Spring Events 异步审计

### DevOps
- Dockerfile + docker-compose.yml
- Kubernetes 部署清单 (Deployment/Service/Ingress/ConfigMap)
- GitHub Actions CI/CD
- deploy.sh (12 命令一键部署)
- backup.sh (数据库定时备份)
- demo.sh (一键演示)
- Nginx HTTPS 反向代理模板
- Logback 生产日志配置 (按天滚动/异步/错误分离)
- 自定义启动 Banner (ASCII 艺术字)

### 监控
- Spring Boot Actuator (health/info/metrics)
- 自定义健康检查 (数据库/磁盘)
- 请求 ID 追踪 (X-Request-Id + MDC)
- 性能基准端点 (/api/system/benchmark)

### 测试
- 24 tests: core 13 + usercenter 7 + E2E 4

### 文档
- README.md (架构图/快速开始/API文档/部署指南)
- CHANGELOG.md

## v1.1.0 (2026-06-13)

### 新增模块
- 字典管理 (sys_dict_type + sys_dict_data, 分屏CRUD)
- 通知公告 (sys_notice, 横幅弹窗, 首页公告)
- 岗位管理 (sys_post, 用户岗位分配)
- 登录日志 (sys_logininfor, 统计卡片, 设备/Browser记录)
- 在线用户管理 (实时会话列表, 强踢下线)
- 数据备份恢复 (mysqldump, 列表/删除/恢复)
- 定时任务增强 (暂停/恢复/执行日志, Cron快捷预设)
- 代码生成器增强 (完整CRUD + Flyway迁移 + 菜单SQL + Vue组件)
- IP黑名单管理 (封禁/解除, 自动检测)
- 站内信系统 (收件箱/已发送, 未读红点)
- 审批流程 (申请/通过/驳回)
- OAuth2客户端管理 (可视化CRUD)
- 数据大屏 (全屏深色, 实时刷新, 6统计卡片+3图表)
- 系统日志查看 (实时查看日志文件)
- 邮件测试工具

### 体验增强
- Redis 集成 (替换Caffeine, Token/限流/黑名单迁移)
- Prometheus 监控 (/actuator/prometheus, 33个JVM指标)
- WebSocket (SockJS STOMP, 实时推送)
- 服务端分页 (UserManageView + OperLogView)
- 表单校验 (用户名/密码/邮箱格式)
- 导出增强 (用户/字典/公告/岗位/部门/配置 CSV)
- 搜索增强 (9种类型, 结果总数)
- 登录页美化 (渐变动画, 毛玻璃, 卡片入场)
- 菜单排序 (↑↓ 上移下移)
- 部门排序 (↑↓ 上移下移)
- 角色权限树 (可视化勾选, 全选/取消)
- 头像上传 (默认渐变头像)
- 中文语言包补全
- ESLint 配置
- 标签页持久化 (sessionStorage)
- 个人设置 (主题色, 每页条数)
- 操作日志图表 (类型分布柱状图)
- 通知公告弹窗 (首次登录自动弹出)

### 基础设施
- 多阶段 Docker 构建 (JDK编译→JRE运行, 非root用户)
- K8s HPA (2-10 pod 自动扩缩)
- CI/CD 增强 (Redis服务, Lint, GHCR推送)
- Nginx CSP头 + WebSocket代理
- 优雅关闭 (30s)
- Gzip 压缩
- Flyway V7-V16 (10个迁移)
- 数据库 25+ 表

### 安全加固
- Sentinel 全链路流控降级 (core/usercenter/gateway 三层)
- Nacos 动态规则持久化 (flow/degrade 双规则)
- Gateway 路由级限流 + JSON block handler
- @SentinelResource 注解保护登录/注册/密码重置等高危端点
- CSP安全头
- Strict-Transport-Security
- Cross-Origin-Resource-Policy
- X-Permitted-Cross-Domain-Policies
- 数据权限 (部门级隔离, 非admin只看本部门)
- 密码策略 (大小写+数字+特殊字符)
- 账户锁定 (5次/30分钟)
- @PreAuthorize 方法级权限控制

### 测试
- 新增 PasswordControllerTest (5 tests)
- 测试覆盖提升至 28 tests (core 21 + usercenter 7)
