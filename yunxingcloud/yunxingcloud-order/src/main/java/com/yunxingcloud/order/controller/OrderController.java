package com.yunxingcloud.order.controller;

import com.yunxingcloud.api.client.PaymentClient;
import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import com.yunxingcloud.order.service.OrderFulfillmentService;
import com.yunxingcloud.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Tag(name = "订单管理", description = "订单CRUD与状态流转")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderHeadRepository orderRepo;
    private final OrderService orderService;
    private final PaymentClient paymentClient;
    private final OrderFulfillmentService fulfillmentService;

    public OrderController(OrderHeadRepository orderRepo, OrderService orderService,
                           PaymentClient paymentClient, OrderFulfillmentService fulfillmentService) {
        this.orderRepo = orderRepo;
        this.orderService = orderService;
        this.paymentClient = paymentClient;
        this.fulfillmentService = fulfillmentService;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
    private boolean isAdmin() { return SecurityContextHolder.getContext().getAuthentication()
            .getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("admin")); }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (isAdmin()) return ResponseEntity.ok(orderRepo.findAll(pageable));
        return ResponseEntity.ok(orderRepo.findByUsernameOrderByCreatedAtDesc(user(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return orderRepo.findById(id).map(order -> {
            if (!order.getUsername().equals(user()) && !isAdmin())
                return ResponseEntity.status(403).<Map<String, Object>>build();
            var lines = orderService.lines(id);
            return ResponseEntity.ok(Map.of("order", order, "lines", lines));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody(required = false) Map<String, Object> body) {
        try {
            Long couponId = null;
            if (body != null && body.containsKey("couponId")) {
                try { couponId = Long.valueOf(body.get("couponId").toString()); } catch (NumberFormatException ignored) {}
            }
            @SuppressWarnings("unchecked")
            Map<String, String> receiver = (Map<String, String>) (Map<?, ?>) body;
            OrderHead order = couponId != null
                    ? orderService.submit(user(), receiver, couponId)
                    : orderService.submit(user(), receiver);
            return ResponseEntity.ok(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Order submit failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage() != null ? e.getMessage() : "下单失败"));
        }
    }

    @Transactional
    @PostMapping("/{id}/pay")
    public ResponseEntity<?> pay(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        return orderRepo.findByIdForUpdate(id).map(order -> {
            if (!order.getUsername().equals(user()) && !isAdmin())
                return ResponseEntity.status(403).build();
            if (!"0".equals(order.getStatus()))
                return ResponseEntity.badRequest().body(Map.of("message", "当前状态不可支付"));
            // Idempotency: if already has a paymentOrderId, return it to prevent duplicate charges
            if (order.getPaymentOrderId() != null) {
                log.warn("Order {} already has paymentOrderId {}, skipping duplicate pay", order.getOrderNo(), order.getPaymentOrderId());
                return ResponseEntity.ok(Map.of("id", order.getPaymentOrderId(), "duplicate", true));
            }
            String channel = body != null && body.containsKey("channel") ? body.get("channel").toString() : "wechat";
            Map<String, Object> payOrder = Map.of(
                    "title", "订单" + order.getOrderNo(),
                    "amount", order.getTotalAmount(),
                    "channel", channel);
            try {
                Map<String, Object> result = paymentClient.createOrder(payOrder);
                if (result != null && result.get("id") != null) {
                    order.setPaymentOrderId(Long.valueOf(result.get("id").toString()));
                    orderRepo.saveAndFlush(order);
                    paymentClient.pay(Long.valueOf(result.get("id").toString()), Map.of());
                    order.setStatus("1");
                    orderRepo.save(order);
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

    @PutMapping("/internal/by-order-no/{orderNo}/status")
    public ResponseEntity<?> updateStatusByOrderNo(@PathVariable String orderNo, @RequestParam String status,
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey) {
        if (!"yunxingcloud-internal".equals(internalKey))
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
        return orderRepo.findByOrderNo(orderNo).map(order -> {
            order.setStatus(status);
            orderRepo.save(order);
            log.info("订单 {} 状态由回调更新为 {}", orderNo, status);
            return ResponseEntity.ok(Map.of("success", true));
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
            if ("2".equals(order.getStatus()) || "3".equals(order.getStatus()) || "4".equals(order.getStatus()))
                return ResponseEntity.badRequest().body(Map.of("message", "订单已发货/已完成/已取消，不可取消"));
            orderService.cancelOrder(order);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/confirm-receive")
    public ResponseEntity<?> confirmReceive(@PathVariable Long id) {
        return orderRepo.findById(id).map(order -> {
            if (!order.getUsername().equals(user()) && !isAdmin())
                return ResponseEntity.status(403).<Map<String, Object>>build();
            try {
                fulfillmentService.confirmReceive(id);
                return ResponseEntity.ok(Map.of("success", true));
            } catch (IllegalStateException e) {
                return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        long pending = orderRepo.countByUsernameAndStatus(user(), "0");
        long paid = orderRepo.countByUsernameAndStatus(user(), "1");
        long shipped = orderRepo.countByUsernameAndStatus(user(), "2");
        long done = orderRepo.countByUsernameAndStatus(user(), "3");
        long total = pending + paid + shipped + done;
        return ResponseEntity.ok(Map.of(
            "pending", pending, "paid", paid, "shipped", shipped, "done", done,
            "total", total, "totalSpent", orderRepo.totalSpentByUser(user())
        ));
    }

    @GetMapping("/export")
    public ResponseEntity<?> export() {
        var orders = isAdmin() ? orderRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                : orderRepo.findByUsernameOrderByCreatedAtDesc(user());
        StringBuilder sb = new StringBuilder();
        sb.append("订单号,用户,金额(元),状态,创建时间\n");
        for (var o : orders) {
            sb.append(String.format("%s,%s,%.2f,%s,%s\n",
                o.getOrderNo(), o.getUsername(),
                (o.getTotalAmount() != null ? o.getTotalAmount() : 0) / 100.0,
                switch (o.getStatus()) {
                    case "0" -> "待支付"; case "1" -> "已支付"; case "2" -> "已发货";
                    case "3" -> "已完成"; case "4" -> "已取消"; default -> o.getStatus();
                },
                o.getCreatedAt() != null ? o.getCreatedAt().toString().substring(0, 16) : ""));
        }
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=orders.csv")
                .body(sb.toString());
    }

    @GetMapping("/recent")
    public ResponseEntity<?> recent() {
        var orders = orderRepo.findByUsernameOrderByCreatedAtDesc(user());
        return ResponseEntity.ok(orders.stream().limit(5).toList());
    }
}
