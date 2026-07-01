package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.AfterSale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AfterSaleRepository extends JpaRepository<AfterSale, Long> {
    List<AfterSale> findByUsernameOrderByCreatedAtDesc(String username);
    List<AfterSale> findByStatusOrderByCreatedAtDesc(String status);
}