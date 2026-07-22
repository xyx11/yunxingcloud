package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUsernameOrderByCreatedAtDesc(String username);
    @Modifying
    @Transactional
    void deleteByUsername(String username);
}
