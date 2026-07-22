package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findByUsernameAndNotifiedFalse(String username);
    List<PriceAlert> findByUsernameOrderByCreatedAtDesc(String username);
    boolean existsByUsernameAndProductId(String username, Long productId);
    void deleteByIdAndUsername(Long id, String username);
}