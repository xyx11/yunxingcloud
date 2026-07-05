package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.entity.Presale;
import com.yunxingcloud.order.entity.UserAddress;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import com.yunxingcloud.order.repository.PresaleRepository;
import com.yunxingcloud.order.repository.UserAddressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PresaleService {

    private final PresaleRepository presaleRepo;
    private final OrderHeadRepository orderRepo;
    private final UserAddressRepository addressRepo;
    private static final Random RANDOM = new Random();

    public PresaleService(PresaleRepository presaleRepo, OrderHeadRepository orderRepo,
                          UserAddressRepository addressRepo) {
        this.presaleRepo = presaleRepo;
        this.orderRepo = orderRepo;
        this.addressRepo = addressRepo;
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", RANDOM.nextInt(10000));
    }

    public Map<String, Object> list(int page, int size) {
        Page<Presale> result = presaleRepo.findAll(PageRequest.of(page, size));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("content", result.getContent());
        map.put("totalPages", result.getTotalPages());
        map.put("totalElements", result.getTotalElements());
        map.put("currentPage", result.getNumber());
        return map;
    }

    public Optional<Presale> detail(Long id) {
        return presaleRepo.findById(id);
    }

    @Transactional
    public OrderHead deposit(Long id, String username) {
        Presale presale = presaleRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("预售活动不存在"));
        if (!"1".equals(presale.getStatus()))
            throw new IllegalStateException("当前不在定金阶段");
        if (presale.getSold() >= presale.getStock())
            throw new IllegalStateException("库存不足");

        List<UserAddress> addresses = addressRepo.findByUsernameOrderByIsDefaultDesc(username);
        UserAddress addr = addresses.isEmpty() ? null : addresses.get(0);
        if (addr == null) throw new IllegalStateException("请先添加收货地址");

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

        presale.setSold(presale.getSold() + 1);
        presaleRepo.save(presale);
        return order;
    }

    @Transactional
    public Map<String, Object> finalPay(Long presaleId, Long orderId, String username) {
        Presale presale = presaleRepo.findById(presaleId)
                .orElseThrow(() -> new IllegalArgumentException("预售活动不存在"));
        if (!"2".equals(presale.getStatus()))
            throw new IllegalStateException("当前不在尾款阶段");

        OrderHead order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        if (!"presale_deposit".equals(order.getStatus()))
            throw new IllegalStateException("订单状态异常");
        if (!order.getUsername().equals(username))
            throw new SecurityException("无权操作此订单");

        long finalAmount = presale.getFullPrice() - presale.getDepositPrice();
        order.setTotalAmount(presale.getFullPrice());
        order.setActualAmount(finalAmount);
        order.setStatus("presale_paid");
        orderRepo.save(order);

        return Map.of("orderId", order.getId(), "finalAmount", finalAmount);
    }
}
