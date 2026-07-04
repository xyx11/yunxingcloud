package com.yunxingcloud.order.controller;

import com.yunxingcloud.api.client.PaymentClient;
import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import com.yunxingcloud.order.service.OrderService;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderHeadRepository orderRepo;
    private final OrderService orderService;
    private final PaymentClient paymentClient;

    public OrderController(OrderHeadRepository orderRepo, OrderService orderService,
                           PaymentClient paymentClient) {
        this.orderRepo = orderRepo;
        this.orderService = orderService;
        this.paymentClient = paymentClient;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
    private boolean isAdmin() { return SecurityContextHolder.getContext().getAuthentication()
            .getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("admin")); }

    @GetMapping
    public ResponseEntity<?> list() {
        if (isAdmin()) return ResponseEntity.ok(orderRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(orderRepo.findByUsernameOrderByCreatedAtDesc(user()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return orderRepo.findById(id).map(order -> {
            var lines = orderService.lines(id);
            return ResponseEntity.ok(Map.of("order", order, "lines", lines));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody(required = false) Map<String, String> receiver) {
        try {
            OrderHead order = orderService.submit(user(), receiver);
            return ResponseEntity.ok(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> pay(@PathVariable Long id) {
        return orderRepo.findById(id).map(order -> {
            if (!order.getUsername().equals(user()) && !isAdmin())
                return ResponseEntity.status(403).build();
            if (!"0".equals(order.getStatus()))
                return ResponseEntity.badRequest().body(Map.of("message", "当前状态不可支付"));
            // Call Payment service via Feign
            Map<String, Object> payOrder = Map.of(
                    "title", "订单" + order.getOrderNo(),
                    "amount", order.getTotalAmount(),
                    "channel", "wechat");
            try {
                Map<String, Object> result = paymentClient.createOrder(payOrder);
                if (result != null && result.get("id") != null) {
                    order.setPaymentOrderId(Long.valueOf(result.get("id").toString()));
                    order.setStatus("1");
                    orderRepo.save(order);
                    paymentClient.pay(Long.valueOf(result.get("id").toString()), null);
                    return ResponseEntity.ok(result);
                } else {
                    return ResponseEntity.status(502).body(Map.of("message", "创建支付订单失败"));
                }
            } catch (Exception e) {
                log.warn("Payment service unavailable for order {}: {}", order.getOrderNo(), e.getMessage());
                return ResponseEntity.status(503).body(Map.of("message", "支付服务暂不可用，请稍后重试"));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return orderRepo.findById(id).map(order -> {
            String newStatus = body.get("status");
            if (!isValidTransition(order.getStatus(), newStatus))
                return ResponseEntity.badRequest().body(Map.of("message","无效的状态变更: "+order.getStatus()+"→"+newStatus));
            order.setStatus(newStatus);
            return ResponseEntity.ok(orderRepo.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }
    private boolean isValidTransition(String from, String to) {
        return switch (from) {
            case "0" -> "1".equals(to) || "4".equals(to);
            case "1" -> "2".equals(to) || "4".equals(to);
            case "2" -> "3".equals(to);
            default -> false;
        };
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        return orderRepo.findById(id).map(order -> {
            if (!order.getUsername().equals(user()) && !isAdmin())
                return ResponseEntity.status(403).build();
            if ("3".equals(order.getStatus()) || "4".equals(order.getStatus()))
                return ResponseEntity.badRequest().body(Map.of("message", "订单已完成或已取消"));
            orderService.cancelOrder(order);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }
}
