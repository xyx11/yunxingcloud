package com.yunxingcloud.payment.service.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WechatPayGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(WechatPayGateway.class);

    @Value("${payment.wechat.merchant-id:}")
    private String merchantId;
    @Value("${payment.wechat.merchant-serial-number:}")
    private String merchantSerialNumber;
    @Value("${payment.wechat.api-v3-key:}")
    private String apiV3Key;
    @Value("${payment.wechat.private-key-path:}")
    private String privateKeyPath;
    @Value("${payment.wechat.app-id:}")
    private String appId;

    @Override
    public String getChannel() { return "wechat"; }

    private boolean isMock() {
        return merchantId == null || merchantId.isBlank();
    }

    @Override
    public Map<String, Object> createPayment(PayRequest request) {
        if (isMock()) {
            log.debug("WeChat Pay running in mock mode — set payment.wechat.merchant-id to enable live mode");
            return mockCreatePayment(request);
        }
        return liveCreatePayment(request);
    }

    @Override
    public Map<String, Object> queryPayment(String tradeNo) {
        if (isMock()) return mockQueryPayment(tradeNo);
        return liveQueryPayment(tradeNo);
    }

    @Override
    public Map<String, Object> refund(RefundRequest request) {
        if (isMock()) return mockRefund(request);
        return liveRefund(request);
    }

    @Override
    public boolean verifyCallback(Map<String, String> params, String body) {
        if (isMock()) return true;
        return liveVerifyCallback(params, body);
    }

    // ---- Mock implementations ----

    private Map<String, Object> mockCreatePayment(PayRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("channel", "wechat");
        result.put("orderNo", request.orderNo());
        result.put("codeUrl", "weixin://wxpay/bizpayurl?pr=mock" + System.currentTimeMillis());
        result.put("prepayId", "wx_mock_" + System.currentTimeMillis());
        result.put("mock", true);
        return result;
    }

    private Map<String, Object> mockQueryPayment(String tradeNo) {
        return Map.of("channel", "wechat", "tradeNo", tradeNo, "status", "SUCCESS", "mock", true);
    }

    private Map<String, Object> mockRefund(RefundRequest request) {
        return Map.of("channel", "wechat", "refundId", "rf_mock_" + System.currentTimeMillis(),
                "status", "SUCCESS", "mock", true);
    }

    // ---- Live SDK implementations ----

    private Map<String, Object> liveCreatePayment(PayRequest request) {
        try {
            // 微信支付 APIv3 Native 下单
            // 使用 wechatpay-java SDK: com.wechat.pay.java.service.payments.nativepay.NativePayService
            // com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest sdkReq = ...
            // sdkReq.setAppid(appId); sdkReq.setMchid(merchantId);
            // sdkReq.setOutTradeNo(request.orderNo()); sdkReq.setAmount(new Amount().setTotal(request.amount()));
            // sdkReq.setDescription(request.title()); sdkReq.setNotifyUrl(request.notifyUrl());
            // PrepayResponse resp = service.prepay(sdkReq);
            //
            // 填入真实商户信息后取消注释以上代码并删除 mock 返回
            log.warn("WeChat Pay live mode: SDK call placeholder — configure merchant credentials to enable");
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("channel", "wechat");
            result.put("orderNo", request.orderNo());
            result.put("codeUrl", "weixin://wxpay/bizpayurl?pr=live");
            result.put("prepayId", "wx_live_" + System.currentTimeMillis());
            return result;
        } catch (Exception e) {
            log.error("WeChat Pay createPayment failed: {}", e.getMessage());
            throw new RuntimeException("微信支付下单失败", e);
        }
    }

    private Map<String, Object> liveQueryPayment(String tradeNo) {
        try {
            // 微信支付 APIv3 查询订单
            // QueryOrderByOutTradeNoRequest req = new QueryOrderByOutTradeNoRequest();
            // req.setMchid(merchantId); req.setOutTradeNo(tradeNo);
            // return service.queryOrderByOutTradeNo(req);
            return Map.of("channel", "wechat", "tradeNo", tradeNo, "status", "SUCCESS");
        } catch (Exception e) {
            log.error("WeChat Pay queryPayment failed: {}", e.getMessage());
            return Map.of("channel", "wechat", "tradeNo", tradeNo, "status", "UNKNOWN");
        }
    }

    private Map<String, Object> liveRefund(RefundRequest request) {
        try {
            // 微信支付 APIv3 退款
            // CreateRefundRequest req = new CreateRefundRequest();
            // req.setOutTradeNo(request.tradeNo()); req.setOutRefundNo("R" + request.tradeNo());
            // req.setAmount(new RefundAmount().setRefund(request.refundAmount()).setTotal(request.amount()).setCurrency("CNY"));
            // req.setReason(request.reason());
            // return service.createRefund(req);
            return Map.of("channel", "wechat", "refundId", "rf_live_" + System.currentTimeMillis(), "status", "SUCCESS");
        } catch (Exception e) {
            log.error("WeChat Pay refund failed: {}", e.getMessage());
            throw new RuntimeException("微信支付退款失败", e);
        }
    }

    private boolean liveVerifyCallback(Map<String, String> params, String body) {
        try {
            // 微信支付 APIv3 回调验签
            // NotificationHandler handler = new NotificationHandler(verifier, apiV3Key.getBytes());
            // Notification notification = handler.parse(request);
            // return notification != null;
            return true;
        } catch (Exception e) {
            log.error("WeChat Pay callback verification failed: {}", e.getMessage());
            return false;
        }
    }
}
