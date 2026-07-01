# Nacos 动态配置模板

将以下 JSON 配置导入 Nacos Console (`http://localhost:8848/nacos`)，实现运行时动态调整。
6 服务各有 flow（流控）和 degrade（降级）规则。

## 用法

1. 启动 Nacos
2. 在 Nacos Console 中为每个服务创建配置：
   - Data ID: `{service-name}-sentinel-flow`，Group: `DEFAULT_GROUP`，格式: JSON
   - Data ID: `{service-name}-sentinel-degrade`，Group: `DEFAULT_GROUP`，格式: JSON
3. 粘贴对应的 JSON 文件内容（文件名为 `{service-name}-sentinel-{type}.json`）
4. 规则实时生效，无需重启

## 配置文件清单

| 文件 | 说明 |
|------|------|
| `yunxingcloud-core-sentinel-flow.json` | Core 流控规则（登录/注册/工单等） |
| `yunxingcloud-core-sentinel-degrade.json` | Core 降级规则 |
| `yunxingcloud-usercenter-sentinel-flow.json` | UserCenter 流控规则（注册/OAuth2等） |
| `yunxingcloud-usercenter-sentinel-degrade.json` | UserCenter 降级规则 |
| `yunxingcloud-payment-sentinel-flow.json` | Payment 流控规则（支付/退款/回调） |
| `yunxingcloud-order-sentinel-flow.json` | Order 流控规则（商品/购物车/订单） |
| `yunxingcloud-inventory-sentinel-flow.json` | Inventory 流控规则（出入库/仓库） |
| `yunxingcloud-gateway-sentinel-flow.json` | Gateway 网关流控规则 |
| `yunxingcloud-gateway-sentinel-degrade.json` | Gateway 网关降级规则 |
| `yunxingcloud-feature-flags.properties` | Core 特性开关配置 |