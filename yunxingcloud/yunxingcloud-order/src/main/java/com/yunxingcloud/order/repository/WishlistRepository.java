package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUsername(String username);
    boolean existsByUsernameAndProductId(String username, Long productId);
    void deleteByUsernameAndProductId(String username, Long productId);
}