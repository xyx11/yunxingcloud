package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    List<UserFavorite> findByUsernameOrderByCreatedAtDesc(String username);
    Optional<UserFavorite> findByUsernameAndProductId(String username, Long productId);
    boolean existsByUsernameAndProductId(String username, Long productId);
    void deleteByUsernameAndProductId(String username, Long productId);
}
