package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findByUsernameAndNotifiedFalse(String username);
    boolean existsByUsernameAndProductId(String username, Long productId);
}