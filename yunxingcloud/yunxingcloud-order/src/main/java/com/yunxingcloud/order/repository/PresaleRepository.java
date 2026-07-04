package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.Presale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresaleRepository extends JpaRepository<Presale, Long> {
    Page<Presale> findByStatus(String status, Pageable pageable);
    Presale findByProductId(Long productId);
}
