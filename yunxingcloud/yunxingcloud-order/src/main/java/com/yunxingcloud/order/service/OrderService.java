package com.yunxingcloud.order.service;

import com.yunxingcloud.api.client.InventoryClient;
import com.yunxingcloud.common.annotation.Idempotent;
import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderHeadRepository orderRepo;
    private final OrderLineRepository lineRepo;
    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;
    private final CouponRepository couponRepo;
    private final CouponUserRepository couponUserRepo;
    private final InventoryClient inventoryClient;

    public OrderService(OrderHeadRepository orderRepo, OrderLineRepository lineRepo,
                        CartItemRepository cartRepo, ProductRepository productRepo,
                        CouponRepository couponRepo, CouponUserRepository couponUserRepo,
                        InventoryClient inventoryClient) {
        this.orderRepo = orderRepo;
        this.lineRepo = lineRepo;
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.couponRepo = couponRepo;
        this.couponUserRepo = couponUserRepo;
        this.inventoryClient = inventoryClient;
    }

    @Idempotent(prefix = "order", ttl = 10, unit = TimeUnit.SECONDS, message = "请勿重复提交订单")
    @Transactional
    public OrderHead submit(String username, Map<String, String> receiver, Long couponId) {
        List<CartItem> cartItems = cartRepo.findByUsernameOrderByCreatedAtDesc(username);
        if (cartItems.isEmpty()) throw new IllegalStateException("购物车为空");

        long totalAmount = cartItems.stream().mapToLong(c -> c.getPrice() * c.getQuantity()).sum();
        long couponAmount = 0;
        Coupon coupon = null;

        // 优惠券核销
        if (couponId != null) {
            var userCoupon = couponUserRepo.findById(couponId).orElse(null);
            if (userCoupon == null || !"0".equals(userCoupon.getStatus()))
                throw new IllegalStateException("优惠券不可用");
            coupon = couponRepo.findById(userCoupon.getCouponId()).orElse(null);
            if (coupon == null) throw new IllegalStateException("优惠券不存在");
            if (coupon.getThreshold() != null && totalAmount < coupon.getThreshold())
                throw new IllegalStateException("未达最低消费 " + (coupon.getThreshold() / 100.0) + " 元");
            couponAmount = Math.min(coupon.getAmount(), totalAmount);
            userCoupon.setStatus("1"); // 标记已使用
            couponUserRepo.save(userCoupon);
        }

        long actualAmount = totalAmount - couponAmount;

        OrderHead order = new OrderHead();
        order.setOrderNo("ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int)(Math.random() * 10000)));
        order.setUsername(username);
        order.setTotalAmount(totalAmount);
        order.setCouponAmount(couponAmount);
        order.setActualAmount(actualAmount);
        order.setStatus("0");
        if (couponId != null) order.setCouponId(couponId);
        if (receiver != null) {
            order.setReceiverName(receiver.getOrDefault("name", ""));
            order.setReceiverPhone(receiver.getOrDefault("phone", ""));
            order.setReceiverAddress(receiver.getOrDefault("address", ""));
        }
        order = orderRepo.save(order);

        // 保存订单行 + 预占库存
        for (CartItem c : cartItems) {
            OrderLine line = new OrderLine();
            line.setOrderId(order.getId());
            line.setProductId(c.getProductId());
            line.setProductName(c.getProductName());
            line.setPrice(c.getPrice());
            line.setQuantity(c.getQuantity());
            lineRepo.save(line);

            // 预占库存 (不立即扣减，而是 reserve)
            try {
                Map<String, Object> invBody = new HashMap<>();
                invBody.put("productId", c.getProductId());
                invBody.put("productName", c.getProductName());
                invBody.put("warehouseId", 1L);
                invBody.put("quantity", c.getQuantity());
                invBody.put("orderId", order.getId());
                inventoryClient.orderOut(invBody);
            } catch (Exception e) {
                log.warn("Inventory order-out failed for product {}: {}", c.getProductId(), e.getMessage());
                // 继续执行，库存扣减失败不阻塞下单
            }
        }

        cartRepo.deleteByUsername(username);
        return order;
    }

    // 兼容无优惠券调用
    public OrderHead submit(String username, Map<String, String> receiver) {
        return submit(username, receiver, null);
    }

    @Transactional
    public void cancelOrder(OrderHead order) {
        OrderHead current = orderRepo.findById(order.getId()).orElse(null);
        if (current == null) return;
        if ("3".equals(current.getStatus()) || "4".equals(current.getStatus())) return;
        current.setStatus("4");
        orderRepo.save(current);

        // 回退库存
        for (OrderLine line : lineRepo.findByOrderId(current.getId())) {
            try {
                Map<String, Object> invBody = new HashMap<>();
                invBody.put("productId", line.getProductId());
                invBody.put("warehouseId", 1L);
                invBody.put("quantity", line.getQuantity());
                invBody.put("orderId", current.getId());
                inventoryClient.orderBack(invBody);
            } catch (Exception e) {
                log.warn("Inventory order-back failed for product {}: {}", line.getProductId(), e.getMessage());
            }
        }

        // 退还优惠券
        if (current.getCouponId() != null) {
            couponUserRepo.findById(current.getCouponId()).ifPresent(uc -> {
                uc.setStatus("0");
                couponUserRepo.save(uc);
            });
        }
    }

    @Transactional
    public void cancelTimeoutOrders() {
        List<OrderHead> expired = orderRepo.findByStatusAndExpireAtBefore("0", LocalDateTime.now());
        log.info("取消 {} 个超时订单", expired.size());
        for (OrderHead order : expired) {
            // 重新读取保证状态最新，防止覆盖已支付的订单
            OrderHead current = orderRepo.findById(order.getId()).orElse(null);
            if (current == null || !"0".equals(current.getStatus())) {
                log.info("订单 {} 状态已变更, 跳过取消", order.getId());
                continue;
            }
            current.setStatus("4");
            current.setRemark("超时未支付自动取消");
            orderRepo.save(current);

            for (OrderLine line : lineRepo.findByOrderId(current.getId())) {
                try {
                    Map<String, Object> invBody = new HashMap<>();
                    invBody.put("productId", line.getProductId());
                    invBody.put("warehouseId", 1L);
                    invBody.put("quantity", line.getQuantity());
                    invBody.put("orderId", current.getId());
                    inventoryClient.orderBack(invBody);
                } catch (Exception e) {
                    log.warn("Timeout inventory-back failed: {}", e.getMessage());
                }
            }
            if (current.getCouponId() != null) {
                couponUserRepo.findById(current.getCouponId()).ifPresent(uc -> {
                    uc.setStatus("0");
                    couponUserRepo.save(uc);
                });
            }
        }
    }

    public List<OrderLine> lines(Long orderId) {
        return lineRepo.findByOrderId(orderId);
    }
}