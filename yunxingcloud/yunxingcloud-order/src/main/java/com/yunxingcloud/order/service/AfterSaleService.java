package com.yunxingcloud.order.service;

import com.yunxingcloud.api.client.PaymentClient;
import com.yunxingcloud.order.entity.AfterSale;
import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.repository.AfterSaleRepository;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AfterSaleService {

    private static final Logger log = LoggerFactory.getLogger(AfterSaleService.class);

    private final AfterSaleRepository afterSaleRepo;
    private final OrderHeadRepository orderRepo;
    private final PaymentClient paymentClient;

    public AfterSaleService(AfterSaleRepository afterSaleRepo, OrderHeadRepository orderRepo,
                            PaymentClient paymentClient) {
        this.afterSaleRepo = afterSaleRepo;
        this.orderRepo = orderRepo;
        this.paymentClient = paymentClient;
    }

    @Transactional
    public AfterSale apply(Long orderId, String username, String type, String reason,
                           Long refundAmount, String evidenceUrls) {
        OrderHead order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        if (!order.getUsername().equals(username)) throw new IllegalStateException("无权操作");
        if (!"3".equals(order.getStatus()) && !"1".equals(order.getStatus()))
            throw new IllegalStateException("仅已支付/已完成的订单可申请售后");
        if (!List.of("refund", "return", "exchange").contains(type))
            throw new IllegalArgumentException("售后类型: refund/return/exchange");

        AfterSale as = new AfterSale();
        as.setOrderId(orderId);
        as.setOrderNo(order.getOrderNo());
        as.setUsername(username);
        as.setType(type);
        as.setReason(reason);
        as.setRefundAmount(refundAmount != null ? refundAmount : order.getActualAmount());
        as.setEvidenceUrls(evidenceUrls);
        as.setStatus("0");
        return afterSaleRepo.save(as);
    }

    @Transactional
    public AfterSale approve(Long afterSaleId, String remark) {
        AfterSale as = afterSaleRepo.findById(afterSaleId).orElseThrow();
        if (!"0".equals(as.getStatus())) throw new IllegalStateException("售后单状态异常");
        as.setStatus("1");
        as.setRemark(remark);
        if ("refund".equals(as.getType()) && as.getRefundAmount() != null && as.getRefundAmount() > 0) {
            as.setStatus("3");
            try {
                Map<String, Object> refundBody = new java.util.HashMap<>();
                refundBody.put("refundAmount", as.getRefundAmount());
                refundBody.put("reason", remark != null ? remark : "售后退款");
                paymentClient.refund(as.getOrderId(), refundBody);
                log.info("Refund initiated for after-sale {}, orderId={}, amount={}", afterSaleId, as.getOrderId(), as.getRefundAmount());
            } catch (Exception e) {
                log.warn("Refund call failed for after-sale {}: {}", afterSaleId, e.getMessage());
            }
        }
        return afterSaleRepo.save(as);
    }

    @Transactional
    public AfterSale reject(Long afterSaleId, String remark) {
        AfterSale as = afterSaleRepo.findById(afterSaleId).orElseThrow();
        as.setStatus("2");
        as.setRemark(remark);
        return afterSaleRepo.save(as);
    }

    @Transactional
    public AfterSale complete(Long afterSaleId) {
        AfterSale as = afterSaleRepo.findById(afterSaleId).orElseThrow();
        as.setStatus("4");
        return afterSaleRepo.save(as);
    }

    public long count() { return afterSaleRepo.count(); }
}