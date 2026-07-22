package com.yunxingcloud.order.service;

import com.yunxingcloud.api.client.InventoryClient;
import com.yunxingcloud.common.annotation.Idempotent;
import com.yunxingcloud.common.core.I18nService;
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
    private final I18nService i18n;

    public OrderService(OrderHeadRepository orderRepo, OrderLineRepository lineRepo,
                        CartItemRepository cartRepo, ProductRepository productRepo,
                        CouponRepository couponRepo, CouponUserRepository couponUserRepo,
                        InventoryClient inventoryClient, I18nService i18n) {
        this.orderRepo = orderRepo;
        this.lineRepo = lineRepo;
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.couponRepo = couponRepo;
        this.couponUserRepo = couponUserRepo;
        this.inventoryClient = inventoryClient;
        this.i18n = i18n;
    }

    @Idempotent(prefix = "order", ttl = 10, unit = TimeUnit.SECONDS, message = "order.duplicate_submit")
    @Transactional
    public OrderHead submit(String username, Map<String, String> receiver, Long couponId) {
        List<CartItem> cartItems = cartRepo.findByUsernameOrderByCreatedAtDesc(username);
        if (cartItems.isEmpty()) throw new IllegalStateException(i18n.msg("cart.empty"));

        // 库存校验 + 乐观扣减
        List<Product> productsToUpdate = new ArrayList<>();
        for (CartItem c : cartItems) {
            Product product = productRepo.findById(c.getProductId()).orElse(null);
            if (product != null && product.getStock() != null) {
                if (c.getQuantity() > product.getStock()) {
                    throw new IllegalStateException(i18n.msg("order.stock_insufficient",
                        product.getName(), c.getQuantity(), product.getStock()));
                }
                product.setStock(product.getStock() - c.getQuantity());
                productsToUpdate.add(product);
            }
        }
        productRepo.saveAll(productsToUpdate);

        long totalAmount = cartItems.stream().mapToLong(c -> c.getPrice() * c.getQuantity()).sum();
        long couponAmount = validateAndApplyCoupon(couponId, totalAmount);
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

            // 预占库存
            try {
                Map<String, Object> invBody = new HashMap<>();
                invBody.put("productId", c.getProductId());
                invBody.put("productName", c.getProductName());
                invBody.put("warehouseId", 1L);
                invBody.put("quantity", c.getQuantity());
                invBody.put("orderId", order.getId());
                inventoryClient.orderOut(invBody);
            } catch (Exception e) {
                log.error("订单 {} 库存预占失败 productId={} qty={}: {}",
                    order.getOrderNo(), c.getProductId(), c.getQuantity(), e.getMessage());
            }
        }

        cartRepo.deleteByUsername(username);
        return order;
    }

    public OrderHead submit(String username, Map<String, String> receiver) {
        return submit(username, receiver, null);
    }

    private long validateAndApplyCoupon(Long couponId, long totalAmount) {
        if (couponId == null) return 0;
        var userCoupon = couponUserRepo.findById(couponId).orElse(null);
        if (userCoupon == null || !"0".equals(userCoupon.getStatus()))
            throw new IllegalStateException(i18n.msg("coupon.unavailable"));
        Coupon coupon = couponRepo.findById(userCoupon.getCouponId()).orElse(null);
        if (coupon == null) throw new IllegalStateException(i18n.msg("coupon.not_found"));
        if (coupon.getThreshold() != null && totalAmount < coupon.getThreshold())
            throw new IllegalStateException(i18n.msg("coupon.below_threshold", coupon.getThreshold() / 100.0));
        long couponAmount = Math.min(coupon.getAmount(), totalAmount);
        userCoupon.setStatus("1");
        couponUserRepo.save(userCoupon);
        return couponAmount;
    }

    private void rollbackInventory(Long orderId) {
        for (OrderLine line : lineRepo.findByOrderId(orderId)) {
            try {
                Map<String, Object> invBody = new HashMap<>();
                invBody.put("productId", line.getProductId());
                invBody.put("warehouseId", 1L);
                invBody.put("quantity", line.getQuantity());
                invBody.put("orderId", orderId);
                inventoryClient.orderBack(invBody);
            } catch (Exception e) {
                log.warn("库存回退失败 orderId={} productId={}: {}", orderId, line.getProductId(), e.getMessage());
            }
        }
    }

    private void refundCoupon(Long couponId) {
        if (couponId != null) {
            couponUserRepo.findById(couponId).ifPresent(uc -> {
                uc.setStatus("0");
                couponUserRepo.save(uc);
            });
        }
    }

    @Transactional
    public void cancelOrder(OrderHead order) {
        OrderHead current = orderRepo.findById(order.getId()).orElse(null);
        if (current == null) return;
        if ("3".equals(current.getStatus()) || "4".equals(current.getStatus())) return;
        current.setStatus("4");
        orderRepo.save(current);
        restoreProductStock(current.getId());
        rollbackInventory(current.getId());
        refundCoupon(current.getCouponId());
    }

    @Transactional
    public void cancelTimeoutOrders() {
        List<OrderHead> expired = orderRepo.findByStatusAndExpireAtBefore("0", LocalDateTime.now());
        log.info("Cancelling {} expired orders", expired.size());
        for (OrderHead order : expired) {
            OrderHead current = orderRepo.findById(order.getId()).orElse(null);
            if (current == null || !"0".equals(current.getStatus())) {
                log.info("Order {} status changed, skipping cancellation", order.getId());
                continue;
            }
            current.setStatus("4");
            current.setRemark(i18n.msg("order.timeout_cancel_remark"));
            orderRepo.save(current);
            restoreProductStock(current.getId());
            rollbackInventory(current.getId());
            refundCoupon(current.getCouponId());
        }

    }

    private void restoreProductStock(Long orderId) {
        for (OrderLine line : lineRepo.findByOrderId(orderId)) {
            productRepo.findById(line.getProductId()).ifPresent(product -> {
                product.setStock((product.getStock() != null ? product.getStock() : 0) + line.getQuantity());
                productRepo.save(product);
            });
        }
    }

    public List<OrderLine> lines(Long orderId) {
        return lineRepo.findByOrderId(orderId);
    }
}
