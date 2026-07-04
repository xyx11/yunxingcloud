package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.entity.Presale;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.entity.UserAddress;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import com.yunxingcloud.order.repository.PresaleRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import com.yunxingcloud.order.repository.UserAddressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/presale")
public class PresaleController {

    private final PresaleRepository presaleRepo;
    private final OrderHeadRepository orderRepo;
    private final ProductRepository productRepo;
    private final UserAddressRepository addressRepo;

    public PresaleController(PresaleRepository presaleRepo, OrderHeadRepository orderRepo,
                             ProductRepository productRepo, UserAddressRepository addressRepo) {
        this.presaleRepo = presaleRepo;
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.addressRepo = addressRepo;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
    private static final Random RANDOM = new Random();

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", RANDOM.nextInt(10000));
    }

    /** 分页查询预售活动 */
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        Page<Presale> result = presaleRepo.findAll(PageRequest.of(page, size));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("content", result.getContent());
        map.put("totalPages", result.getTotalPages());
        map.put("totalElements", result.getTotalElements());
        map.put("currentPage", result.getNumber());
        return ResponseEntity.ok(map);
    }

    /** 预售详情 */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long id) {
        Presale presale = presaleRepo.findById(id).orElse(null);
        if (presale == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("presale", presale));
    }

    /** 支付定金(创建预售订单) */
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Map<String, Object>> deposit(@PathVariable Long id) {
        Presale presale = presaleRepo.findById(id).orElse(null);
        if (presale == null) return ResponseEntity.badRequest().body(Map.of("message", "预售活动不存在"));
        if (!"1".equals(presale.getStatus())) return ResponseEntity.badRequest().body(Map.of("message", "当前不在定金阶段"));
        if (presale.getSold() >= presale.getStock()) return ResponseEntity.badRequest().body(Map.of("message", "库存不足"));

        String username = user();

        // 获取用户默认地址
        List<UserAddress> addresses = addressRepo.findByUsernameOrderByIsDefaultDesc(username);
        UserAddress addr = addresses.isEmpty() ? null : addresses.get(0);
        if (addr == null) return ResponseEntity.badRequest().body(Map.of("message", "请先添加收货地址"));

        // 创建预售订单
        OrderHead order = new OrderHead();
        order.setOrderNo(generateOrderNo());
        order.setUsername(username);
        order.setTotalAmount(presale.getDepositPrice());
        order.setActualAmount(presale.getDepositPrice());
        order.setStatus("presale_deposit");
        order.setReceiverName(addr.getName());
        order.setReceiverPhone(addr.getPhone());
        order.setReceiverAddress(addr.getProvince() + addr.getCity() + addr.getDistrict() + addr.getDetail());
        orderRepo.save(order);

        // 更新已售
        presale.setSold(presale.getSold() + 1);
        presaleRepo.save(presale);

        return ResponseEntity.ok(Map.of("success", true, "orderId", order.getId(), "orderNo", order.getOrderNo()));
    }

    /** 支付尾款(更新订单状态) */
    @PostMapping("/{id}/final-pay")
    public ResponseEntity<Map<String, Object>> finalPay(@PathVariable Long id, @RequestParam Long orderId) {
        Presale presale = presaleRepo.findById(id).orElse(null);
        if (presale == null) return ResponseEntity.badRequest().body(Map.of("message", "预售活动不存在"));
        if (!"2".equals(presale.getStatus())) return ResponseEntity.badRequest().body(Map.of("message", "当前不在尾款阶段"));

        OrderHead order = orderRepo.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.badRequest().body(Map.of("message", "订单不存在"));
        if (!"presale_deposit".equals(order.getStatus())) return ResponseEntity.badRequest().body(Map.of("message", "订单状态异常"));
        if (!order.getUsername().equals(user())) return ResponseEntity.badRequest().body(Map.of("message", "无权操作此订单"));

        // 支付尾款 = 全价 - 定金
        long finalAmount = presale.getFullPrice() - presale.getDepositPrice();
        order.setTotalAmount(presale.getFullPrice());
        order.setActualAmount(finalAmount);
        order.setStatus("presale_paid");
        orderRepo.save(order);

        return ResponseEntity.ok(Map.of("success", true, "orderId", order.getId(), "finalAmount", finalAmount));
    }
}
