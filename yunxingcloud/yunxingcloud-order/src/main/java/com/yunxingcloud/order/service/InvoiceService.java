package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Invoice;
import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.repository.InvoiceRepository;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final OrderHeadRepository orderRepo;

    public InvoiceService(InvoiceRepository invoiceRepo, OrderHeadRepository orderRepo) {
        this.invoiceRepo = invoiceRepo;
        this.orderRepo = orderRepo;
    }

    @Transactional
    public Invoice apply(Long orderId, String username, String type, String title, String taxNo, String email) {
        OrderHead order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        if (!order.getUsername().equals(username)) throw new IllegalStateException("无权操作");
        if (!"3".equals(order.getStatus())) throw new IllegalStateException("仅已完成订单可申请发票");
        if (!"personal".equals(type) && !"company".equals(type))
            throw new IllegalArgumentException("类型: personal/company");
        if ("company".equals(type) && (taxNo == null || taxNo.isBlank()))
            throw new IllegalArgumentException("企业发票需填写税号");

        Invoice inv = new Invoice();
        inv.setOrderId(orderId);
        inv.setOrderNo(order.getOrderNo());
        inv.setUsername(username);
        inv.setType(type);
        inv.setTitle(title);
        inv.setTaxNo(taxNo);
        inv.setEmail(email);
        inv.setStatus("0");
        return invoiceRepo.save(inv);
    }

    @Transactional
    public Invoice issue(Long invoiceId, String invoiceNo, String invoiceUrl) {
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        inv.setStatus("1");
        inv.setInvoiceNo(invoiceNo);
        inv.setInvoiceUrl(invoiceUrl);
        return invoiceRepo.save(inv);
    }
}