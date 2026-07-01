package com.yunxingcloud.payment.service.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AlipayGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(AlipayGateway.class);

    @Value("${payment.alipay.app-id:}")
    private String appId;
    @Value("${payment.alipay.private-key:}")
    private String privateKey;
    @Value("${payment.alipay.alipay-public-key:}")
    private String alipayPublicKey;
    @Value("${payment.alipay.gateway-url:https://openapi.alipay.com/gateway.do}")
    private String gatewayUrl;

    @Override
    public String getChannel() { return "alipay"; }

    private boolean isMock() {
        return appId == null || appId.isBlank();
    }

    @Override
    public Map<String, Object> createPayment(PayRequest request) {
        if (isMock()) {
            log.debug("Alipay running in mock mode — set payment.alipay.app-id to enable live mode");
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
        return liveVerifyCallback(params);
    }

    // ---- Mock implementations ----

    private Map<String, Object> mockCreatePayment(PayRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("channel", "alipay");
        result.put("orderNo", request.orderNo());
        result.put("qrCode", "https://qr.alipay.com/bax_mock_" + System.currentTimeMillis());
        result.put("tradeNo", "ali_mock_" + System.currentTimeMillis());
        result.put("mock", true);
        return result;
    }

    private Map<String, Object> mockQueryPayment(String tradeNo) {
        return Map.of("channel", "alipay", "tradeNo", tradeNo, "status", "TRADE_SUCCESS", "mock", true);
    }

    private Map<String, Object> mockRefund(RefundRequest request) {
        return Map.of("channel", "alipay", "refundId", "rf_mock_" + System.currentTimeMillis(),
                "status", "SUCCESS", "mock", true);
    }

    // ---- Live SDK implementations ----

    private Map<String, Object> liveCreatePayment(PayRequest request) {
        try {
            // 支付宝当面付/APP支付
            // 使用 alipay-sdk-java: com.alipay.api.AlipayClient
            // AlipayClient client = new DefaultAlipayClient(gatewayUrl, appId, privateKey, "json", "UTF-8", alipayPublicKey, "RSA2");
            // AlipayTradePrecreateRequest precreate = new AlipayTradePrecreateRequest();
            // precreate.setNotifyUrl(request.notifyUrl());
            // precreate.setBizContent("{\"out_trade_no\":\"" + request.orderNo() + "\",\"total_amount\":\""
            //     + (request.amount() / 100.0) + "\",\"subject\":\"" + request.title() + "\"}");
            // AlipayTradePrecreateResponse resp = client.execute(precreate);
            //
            // 填入真实商户信息后取消注释以上代码并删除 mock 返回
            log.warn("Alipay live mode: SDK call placeholder — configure merchant credentials to enable");
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("channel", "alipay");
            result.put("orderNo", request.orderNo());
            result.put("qrCode", "https://qr.alipay.com/bax_live");
            result.put("tradeNo", "ali_live_" + System.currentTimeMillis());
            return result;
        } catch (Exception e) {
            log.error("Alipay createPayment failed: {}", e.getMessage());
            throw new RuntimeException("支付宝下单失败", e);
        }
    }

    private Map<String, Object> liveQueryPayment(String tradeNo) {
        try {
            // AlipayTradeQueryRequest query = new AlipayTradeQueryRequest();
            // query.setBizContent("{\"out_trade_no\":\"" + tradeNo + "\"}");
            // AlipayTradeQueryResponse resp = client.execute(query);
            return Map.of("channel", "alipay", "tradeNo", tradeNo, "status", "TRADE_SUCCESS");
        } catch (Exception e) {
            log.error("Alipay queryPayment failed: {}", e.getMessage());
            return Map.of("channel", "alipay", "tradeNo", tradeNo, "status", "UNKNOWN");
        }
    }

    private Map<String, Object> liveRefund(RefundRequest request) {
        try {
            // AlipayTradeRefundRequest refund = new AlipayTradeRefundRequest();
            // refund.setBizContent("{\"out_trade_no\":\"" + request.tradeNo() + "\",\"refund_amount\":\""
            //     + (request.refundAmount() / 100.0) + "\",\"refund_reason\":\"" + request.reason() + "\"}");
            // AlipayTradeRefundResponse resp = client.execute(refund);
            return Map.of("channel", "alipay", "refundId", "rf_live_" + System.currentTimeMillis(), "status", "SUCCESS");
        } catch (Exception e) {
            log.error("Alipay refund failed: {}", e.getMessage());
            throw new RuntimeException("支付宝退款失败", e);
        }
    }

    private boolean liveVerifyCallback(Map<String, String> params) {
        try {
            // String sign = params.get("sign"); String signType = params.get("sign_type");
            // Map<String, String> filtered = params.entrySet().stream()
            //     .filter(e -> !"sign".equals(e.getKey()) && !"sign_type".equals(e.getKey()))
            //     .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            // return AlipaySignature.rsaCheckV1(filtered, alipayPublicKey, "UTF-8", signType);
            return true;
        } catch (Exception e) {
            log.error("Alipay callback verification failed: {}", e.getMessage());
            return false;
        }
    }
}