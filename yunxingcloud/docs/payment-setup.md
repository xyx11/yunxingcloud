# 支付渠道对接指南

## 微信支付

### 1. 开通微信支付商户号
- 前往 https://pay.weixin.qq.com 注册商户号
- 获取: 商户号(mchid)、APIv3密钥、商户证书序列号

### 2. 配置 application.yaml
```yaml
payment:
  wechat:
    merchant-id: "1234567890"           # 商户号
    merchant-serial-number: "ABC123..." # 证书序列号
    api-v3-key: "your-32-char-key"      # APIv3密钥
    private-key-path: /app/certs/apiclient_key.pem
    app-id: "wx1234567890"              # 小程序/公众号 AppID
```

### 3. 上传证书
```bash
scp apiclient_key.pem root@ECS:/opt/yunxingcloud/certs/
```

### 4. 配置支付回调
- 微信支付商户平台 → 产品中心 → 开发配置 → 支付回调URL
- 回调地址: `https://api.yunxingcloud.com/api/payment/callback/wechat`

---

## 支付宝

### 1. 开通支付宝商户
- 前往 https://open.alipay.com 创建应用
- 获取: AppID、应用私钥、支付宝公钥

### 2. 配置 application.yaml
```yaml
payment:
  alipay:
    app-id: "2021001234567890"
    private-key: "MIIEvQIBADANBgkqhkiG9w0BAQ..."
    alipay-public-key: "MIIBIjANBgkqhkiG9w0BAQ..."
    gateway-url: https://openapi.alipay.com/gateway.do
```

### 3. 配置支付回调
- 支付宝开放平台 → 应用详情 → 回调地址
- 回调地址: `https://api.yunxingcloud.com/api/payment/callback/alipay`

---

## 验证

```bash
# 测试微信支付
curl -X POST https://api.yunxingcloud.com/api/payment/orders \
  -H 'Content-Type: application/json' -H 'Authorization: Bearer TOKEN' \
  -d '{"title":"测试","amount":1,"channel":"wechat"}'

# 测试支付宝
curl -X POST https://api.yunxingcloud.com/api/payment/orders \
  -H 'Content-Type: application/json' -H 'Authorization: Bearer TOKEN' \
  -d '{"title":"测试","amount":1,"channel":"alipay"}'
```

## Mock 模式

留空所有凭证 = 自动使用 Mock 模式，支付立即成功，无需真实商户号。
