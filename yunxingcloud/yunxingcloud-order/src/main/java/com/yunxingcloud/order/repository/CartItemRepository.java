package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUsernameOrderByCreatedAtDesc(String username);
    void deleteByUsername(String username);
}
