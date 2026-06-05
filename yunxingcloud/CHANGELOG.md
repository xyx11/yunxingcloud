# Changelog

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
