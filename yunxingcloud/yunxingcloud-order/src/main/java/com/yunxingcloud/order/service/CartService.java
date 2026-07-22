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
        var recs = items.isEmpty() ? productRepo.findByStatus("0", PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "sales"))) : Collections.emptyList();
        return Map.of("items", (Object) items, "recommended", recs);
    }

    @Transactional
    public CartItem add(String username, Long productId, int quantity) {
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        if (p.getStock() < quantity)
            throw new IllegalArgumentException("库存不足");
        // Check if user already has this product in cart
        List<CartItem> existing = cartRepo.findByUsernameOrderByCreatedAtDesc(username);
        for (CartItem ci : existing) {
            if (ci.getProductId().equals(productId)) {
                ci.setQuantity(ci.getQuantity() + quantity);
                return cartRepo.save(ci);
            }
        }
        CartItem item = new CartItem();
        item.setUsername(username);
        item.setProductId(productId);
        item.setProductName(p.getName());
        item.setPrice(p.getPrice());
        item.setImageUrl(p.getImageUrl());
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

    @Transactional
    public CartItem updateQuantity(Long id, int quantity, String username) {
        CartItem item = cartRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("购物车项不存在"));
        if (!item.getUsername().equals(username))
            throw new IllegalArgumentException("无权操作");
        if (quantity <= 0) {
            cartRepo.delete(item);
            return null;
        }
        item.setQuantity(quantity);
        return cartRepo.save(item);
    }
}
