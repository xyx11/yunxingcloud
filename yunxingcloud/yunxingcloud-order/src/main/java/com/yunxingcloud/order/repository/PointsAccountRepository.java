package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.PointsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PointsAccountRepository extends JpaRepository<PointsAccount, Long> {
    Optional<PointsAccount> findByUsername(String username);
}