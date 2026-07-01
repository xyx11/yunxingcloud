# 安全清单 (OWASP Top 10)

## 1. 访问控制失效
- [x] JWT 双 Token 认证
- [x] @PreAuthorize 方法级 RBAC
- [x] @DataScope 数据权限隔离
- [x] Refresh Token Rotation 防窃取
- [ ] 定期审计权限分配

## 2. 加密失效
- [x] TLS 1.3 + HSTS
- [x] Argon2id 密码哈希
- [x] Jasypt 配置加密 (ENC)
- [x] JWT HS256+ 密钥
- [ ] 密钥定期轮换

## 3. 注入攻击
- [x] JPA 参数化查询 (防 SQL 注入)
- [x] XSS Filter (HTML 转义)
- [x] CSP 安全响应头
- [ ] 正则表达式 ReDoS 防护

## 4. 不安全设计
- [x] 限流: IP + 用户级 (Redis Token Bucket)
- [x] 账号锁定: 5次/30分钟
- [x] 密码策略: 8位+大小写+数字+特殊字符
- [ ] 敏感操作二次确认 (退款/删除)

## 5. 安全配置错误
- [x] 生产配置分离 (dev/staging/prod)
- [x] 默认密码强制修改
- [x] 防火墙: 仅 22/80/443
- [x] 服务绑定 127.0.0.1
- [ ] 定期依赖漏洞扫描 (OWASP Dependency Check)

## 6. 易受攻击组件
- [x] Spring Boot 4.0.6 (最新稳定版)
- [x] Nginx 最新版
- [ ] 自动化 CVE 监控 (Dependabot)

## 7. 认证失败
- [x] JWT 黑名单登出
- [x] 登录审计 (IP/UA/OS)
- [x] 登录限流 5次/分钟
- [x] TOTP 双因素认证
- [ ] 异常登录检测 (异地/新设备)

## 8. 软件和数据完整性
- [x] 文件上传魔数校验
- [x] 类型白名单 + 大小限制
- [x] CI/CD 自动化构建
- [ ] 构建产物签名验证

## 9. 日志和监控
- [x] 操作审计日志 (Spring Events)
- [x] API 请求日志 (@ApiLog AOP)
- [x] 登录审计 (成功/失败)
- [x] ELK 日志聚合
- [x] Prometheus 告警 8条规则
- [ ] 安全事件告警 (暴力破解/扫描)

## 10. SSRF
- [x] Gateway 限制上游仅 localhost
- [ ] URL 白名单校验 (回调地址)

## 额外防护

- [x] 幂等性: @Idempotent 防重复提交
- [x] 分布式锁: Redisson 防超卖
- [x] 敏感数据脱敏: @Sensitive 注解
- [x] CORS: 开发按需/生产关闭
- [x] 多租户: X-Tenant-Id 隔离
- [ ] CSP 策略细化
- [ ] Cookie Secure/HttpOnly/SameSite

## 合规基线

- [ ] GDPR: 数据删除/导出 API
- [ ] PCI-DSS: 支付数据不落库 (仅存 token)
- [ ] 等保2.0: 三级等保测评