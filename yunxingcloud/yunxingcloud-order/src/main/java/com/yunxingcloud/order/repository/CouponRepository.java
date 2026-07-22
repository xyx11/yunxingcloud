package com.yunxingcloud.order.repository;
import com.yunxingcloud.order.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Modifying
    @Query("UPDATE Coupon c SET c.usedQty = c.usedQty + 1 WHERE c.id = :id AND c.usedQty < c.totalQty")
    int incrementUsedQty(@Param("id") Long id);
}
