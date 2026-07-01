# Nacos 动态配置模板

将以下 JSON 配置导入 Nacos Console (`http://localhost:8848/nacos`)，实现运行时动态调整。
6 服务各有一套 flow（流控）和 degrade（降级）规则。

## 用法

1. 启动 Nacos
2. 在 Nacos Console 中为每个服务创建配置：
   - Data ID: `{service-name}-sentinel-flow`，Group: `DEFAULT_GROUP`，格式: JSON
   - Data ID: `{service-name}-sentinel-degrade`，Group: `DEFAULT_GROUP`，格式: JSON
3. 粘贴下方对应的 JSON 内容
4. 规则实时生效，无需重启

## 服务列表

| 服务 | Data ID prefix |
|------|---------------|
| yunxingcloud-core | yunxingcloud-core |
| yunxingcloud-usercenter | yunxingcloud-usercenter |
| yunxingcloud-payment | yunxingcloud-payment |
| yunxingcloud-order | yunxingcloud-order |
| yunxingcloud-inventory | yunxingcloud-inventory |
| yunxingcloud-gateway | yunxingcloud-gateway |