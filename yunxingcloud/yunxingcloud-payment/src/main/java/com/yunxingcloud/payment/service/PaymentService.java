package com.yunxingcloud.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunxingcloud.payment.entity.PaymentOrder;
import com.yunxingcloud.payment.entity.PaymentRecord;
import com.yunxingcloud.payment.repository.PaymentOrderRepository;
import com.yunxingcloud.payment.repository.PaymentRecordRepository;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.payment.service.gateway.PaymentGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentOrderRepository orderRepo;
    private final PaymentRecordRepository recordRepo;
    private final Map<String, PaymentGateway> gateways;
    private final I18nService i18n;
    private final ObjectMapper mapper = new ObjectMapper();

    public PaymentService(PaymentOrderRepository orderRepo,
                          PaymentRecordRepository recordRepo,
                          List<PaymentGateway> gatewayList,
                          I18nService i18n) {
        this.orderRepo = orderRepo;
        this.recordRepo = recordRepo;
        this.i18n = i18n;
        this.gateways = new HashMap<>();
        gatewayList.forEach(g -> gateways.put(g.getChannel(), g));
    }

    @Transactional
    public PaymentOrder create(String title, Long amount, String channel) {
        PaymentOrder order = new PaymentOrder();
        order.setTitle(title);
        order.setAmount(amount);
        order.setChannel(channel);
        order.setStatus("0");
        order.setOrderNo(generateOrderNo());
        return orderRepo.save(order);
    }

    @Transactional
    public Map<String, Object> pay(Long orderId, String notifyBaseUrl) {
        PaymentOrder order = orderRepo.findById(orderId).orElseThrow();
        if (!"0".equals(order.getStatus())) throw new IllegalStateException(i18n.msg("payment.status_invalid"));
        PaymentGateway gw = gateways.get(order.getChannel());
        if (gw == null) throw new IllegalArgumentException("Unsupported channel: " + order.getChannel());
        order.setStatus("1");
        orderRepo.save(order);

        String notifyUrl = notifyBaseUrl + "/api/payment/callback/" + order.getChannel();
        var req = new PaymentGateway.PayRequest(order.getOrderNo(), order.getTitle(), order.getAmount(), notifyUrl);
        Map<String, Object> result = gw.createPayment(req);

        PaymentRecord rec = new PaymentRecord();
        rec.setOrderId(order.getId());
        rec.setType("pay");
        rec.setChannel(order.getChannel());
        rec.setRequest(toJson(req));
        rec.setResponse(toJson(result));
        rec.setSuccess(true);
        recordRepo.save(rec);
        return result;
    }

    @Transactional
    public void handleCallback(String channel, Map<String, String> params, String body) {
        PaymentGateway gw = gateways.get(channel);
        if (gw == null) return;

        String orderNo = params.getOrDefault("out_trade_no",
                params.getOrDefault("trade_no", ""));
        PaymentOrder order = orderRepo.findByOrderNo(orderNo).orElse(null);
        if (order == null) return;

        boolean verified = gw.verifyCallback(params, body);
        PaymentRecord rec = new PaymentRecord();
        rec.setOrderId(order.getId());
        rec.setType("callback");
        rec.setChannel(channel);
        rec.setRequest(params.toString());
        rec.setResponse(body);
        rec.setSuccess(verified);
        recordRepo.save(rec);

        if (verified && "1".equals(order.getStatus())) {
            order.setStatus("2");
            order.setPaidAt(LocalDateTime.now());
            String tradeNo = params.getOrDefault("trade_no", params.getOrDefault("transaction_id", ""));
            if (!tradeNo.isEmpty()) order.setTradeNo(tradeNo);
            orderRepo.save(order);
            syncOrderStatus(orderNo, "2");
        }
    }

    @org.springframework.beans.factory.annotation.Value("${payment.order-service-url:http://localhost:8084}")
    private String orderServiceUrl;

    private void syncOrderStatus(String orderNo, String status) {
        try {
            var rest = new org.springframework.web.client.RestTemplate();
            String url = orderServiceUrl + "/api/orders/internal/by-order-no/" + orderNo + "/status?status=" + status;
            var headers = new org.springframework.http.HttpHeaders();
            headers.set("X-Internal-Key", "yunxingcloud-internal");
            rest.put(url, new org.springframework.http.HttpEntity<>(null, headers));
        } catch (Exception e) {
            log.warn("Failed to sync order status for {}: {}", orderNo, e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> refund(Long orderId, Long refundAmount, String reason) {
        PaymentOrder order = orderRepo.findById(orderId).orElseThrow();
        if (!"2".equals(order.getStatus())) throw new IllegalStateException(i18n.msg("payment.refund_only_paid"));
        if (refundAmount > order.getAmount() - order.getRefundAmount())
            throw new IllegalArgumentException(i18n.msg("payment.refund_exceeds"));

        PaymentGateway gw = gateways.get(order.getChannel());
        var req = new PaymentGateway.RefundRequest(order.getTradeNo(), order.getAmount(), refundAmount, reason);
        Map<String, Object> result = gw.refund(req);

        PaymentRecord rec = new PaymentRecord();
        rec.setOrderId(order.getId());
        rec.setType("refund");
        rec.setChannel(order.getChannel());
        rec.setRequest(toJson(req));
        rec.setResponse(toJson(result));
        rec.setSuccess(true);
        recordRepo.save(rec);

        order.setRefundAmount((order.getRefundAmount() != null ? order.getRefundAmount() : 0L) + refundAmount);
        order.setRefundReason(reason);
        order.setRefundAt(LocalDateTime.now());
        if (order.getRefundAmount().equals(order.getAmount())) order.setStatus("3");
        orderRepo.save(order);
        return result;
    }

    public List<PaymentRecord> records(Long orderId) {
        return recordRepo.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    private String generateOrderNo() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int)(Math.random() * 10000));
    }

    private String toJson(Object obj) {
        try { return mapper.writeValueAsString(obj); } catch (Exception e) { return "{}"; }
    }
}
