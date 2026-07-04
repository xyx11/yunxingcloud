package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.AfterSale;
import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.repository.AfterSaleRepository;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AfterSaleService {

    private final AfterSaleRepository afterSaleRepo;
    private final OrderHeadRepository orderRepo;

    public AfterSaleService(AfterSaleRepository afterSaleRepo, OrderHeadRepository orderRepo) {
        this.afterSaleRepo = afterSaleRepo;
        this.orderRepo = orderRepo;
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
        // 退款类型标记退款中 (实际退款由支付服务异步回调触发)
        if ("refund".equals(as.getType()) && as.getRefundAmount() != null && as.getRefundAmount() > 0) {
            as.setStatus("3");
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
}