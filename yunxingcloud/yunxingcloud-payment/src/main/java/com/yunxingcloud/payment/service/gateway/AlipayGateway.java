package com.yunxingcloud.payment.service.gateway;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    /** 构建 SDK 客户端（沙箱环境配置沙箱 gateway-url 即可） */
    private AlipayClient client() {
        return new DefaultAlipayClient(gatewayUrl, appId, privateKey, "json", "UTF-8", alipayPublicKey, "RSA2");
    }

    private static String yuan(long fen) {
        return String.format("%.2f", fen / 100.0);
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
        if (isMock()) { log.warn("Mock mode: callback verification always fails for security"); return false; }
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

    // ---- Live SDK implementations（支付宝当面付） ----

    private Map<String, Object> liveCreatePayment(PayRequest request) {
        try {
            AlipayTradePrecreateRequest precreate = new AlipayTradePrecreateRequest();
            precreate.setNotifyUrl(request.notifyUrl());
            precreate.setBizContent("{\"out_trade_no\":\"" + request.orderNo() + "\","
                    + "\"total_amount\":\"" + yuan(request.amount()) + "\","
                    + "\"subject\":\"" + request.title() + "\"}");
            AlipayTradePrecreateResponse resp = client().execute(precreate);
            if (!resp.isSuccess()) {
                log.error("Alipay precreate failed: {} {}", resp.getCode(), resp.getSubMsg());
                throw new RuntimeException("支付宝下单失败: " + resp.getSubMsg());
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("channel", "alipay");
            result.put("orderNo", request.orderNo());
            result.put("qrCode", resp.getQrCode());
            result.put("tradeNo", resp.getOutTradeNo());
            result.put("mock", false);
            return result;
        } catch (AlipayApiException e) {
            log.error("Alipay createPayment failed: {}", e.getErrMsg());
            throw new RuntimeException("支付宝下单失败", e);
        }
    }

    private Map<String, Object> liveQueryPayment(String tradeNo) {
        try {
            AlipayTradeQueryRequest query = new AlipayTradeQueryRequest();
            query.setBizContent("{\"out_trade_no\":\"" + tradeNo + "\"}");
            AlipayTradeQueryResponse resp = client().execute(query);
            String status = resp.isSuccess() ? resp.getTradeStatus() : "UNKNOWN";
            return Map.of("channel", "alipay", "tradeNo", tradeNo, "status", status);
        } catch (AlipayApiException e) {
            log.error("Alipay queryPayment failed: {}", e.getErrMsg());
            return Map.of("channel", "alipay", "tradeNo", tradeNo, "status", "UNKNOWN");
        }
    }

    private Map<String, Object> liveRefund(RefundRequest request) {
        try {
            AlipayTradeRefundRequest refund = new AlipayTradeRefundRequest();
            refund.setBizContent("{\"out_trade_no\":\"" + request.tradeNo() + "\","
                    + "\"refund_amount\":\"" + yuan(request.refundAmount()) + "\","
                    + "\"refund_reason\":\"" + request.reason() + "\"}");
            AlipayTradeRefundResponse resp = client().execute(refund);
            if (!resp.isSuccess()) {
                throw new RuntimeException("支付宝退款失败: " + resp.getSubMsg());
            }
            return Map.of("channel", "alipay", "refundId", resp.getTradeNo(), "status", "SUCCESS");
        } catch (AlipayApiException e) {
            log.error("Alipay refund failed: {}", e.getErrMsg());
            throw new RuntimeException("支付宝退款失败", e);
        }
    }

    private boolean liveVerifyCallback(Map<String, String> params) {
        try {
            String signType = params.getOrDefault("sign_type", "RSA2");
            Map<String, String> filtered = params.entrySet().stream()
                    .filter(e -> !"sign".equals(e.getKey()) && !"sign_type".equals(e.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return AlipaySignature.rsaCheckV1(filtered, alipayPublicKey, "UTF-8", signType);
        } catch (Exception e) {
            log.error("Alipay callback verification failed: {}", e.getMessage());
            return false;
        }
    }
}
