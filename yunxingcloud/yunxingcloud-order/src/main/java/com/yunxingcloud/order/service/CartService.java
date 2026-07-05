package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.CartItem;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.CartItemRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CartService {

    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;

    public CartService(CartItemRepository cartRepo, ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    public Map<String, Object> list(String username) {
        var items = cartRepo.findByUsernameOrderByCreatedAtDesc(username);
        if (items.isEmpty()) {
            var recs = productRepo.findByStatus("0", PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "sales")));
            return Map.of("items", items, "recommended", recs);
        }
        return Map.of("items", (Object) items);
    }

    @Transactional
    public CartItem add(String username, Long productId, int quantity) {
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        if (p.getStock() < quantity)
            throw new IllegalArgumentException("库存不足");
        CartItem item = new CartItem();
        item.setUsername(username);
        item.setProductId(productId);
        item.setProductName(p.getName());
        item.setPrice(p.getPrice());
        item.setQuantity(quantity);
        return cartRepo.save(item);
    }

    @Transactional
    public void remove(Long id, String username) {
        cartRepo.findById(id).ifPresent(c -> {
            if (c.getUsername().equals(username)) cartRepo.delete(c);
        });
    }

    @Transactional
    public void clear(String username) {
        cartRepo.deleteByUsername(username);
    }
}
