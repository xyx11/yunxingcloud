package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.OrderHead;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface OrderHeadRepository extends JpaRepository<OrderHead, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrderHead o WHERE o.id = :id")
    Optional<OrderHead> findByIdForUpdate(Long id);
    List<OrderHead> findByUsernameOrderByCreatedAtDesc(String username);
    Page<OrderHead> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
    List<OrderHead> findByStatusOrderByCreatedAtDesc(String status);
    List<OrderHead> findByStatusAndExpireAtBefore(String status, java.time.LocalDateTime time);
    Optional<OrderHead> findByOrderNo(String orderNo);

    @Query("SELECT COALESCE(SUM(CASE WHEN o.status <> '4' THEN COALESCE(o.actualAmount, o.totalAmount) ELSE 0 END), 0) FROM OrderHead o")
    long totalRevenue();

    @Query("SELECT COALESCE(SUM(CASE WHEN o.status IN ('1','2','3') THEN COALESCE(o.actualAmount, o.totalAmount) ELSE 0 END), 0) FROM OrderHead o")
    long paidRevenue();

    @Query("SELECT COUNT(o) FROM OrderHead o")
    long totalOrderCount();

    @Query("SELECT COUNT(o) FROM OrderHead o WHERE o.status <> '0' AND o.status <> '4'")
    long paidOrderCount();
}
