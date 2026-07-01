package com.yunxingcloud.payment.service.gateway;

import java.util.Map;

public interface PaymentGateway {
    String getChannel();
    Map<String, Object> createPayment(PayRequest request);
    Map<String, Object> queryPayment(String tradeNo);
    Map<String, Object> refund(RefundRequest request);
    boolean verifyCallback(Map<String, String> params, String body);

    record PayRequest(String orderNo, String title, Long amount, String notifyUrl) {}
    record RefundRequest(String tradeNo, Long amount, Long refundAmount, String reason) {}
}
