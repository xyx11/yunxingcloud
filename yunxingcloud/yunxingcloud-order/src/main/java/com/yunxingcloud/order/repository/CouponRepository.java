package com.yunxingcloud.order.repository;
import com.yunxingcloud.order.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CouponRepository extends JpaRepository<Coupon, Long> {}
