package com.yunxingcloud.order.repository;
import com.yunxingcloud.order.entity.CouponUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CouponUserRepository extends JpaRepository<CouponUser, Long> {
    List<CouponUser> findByUsernameAndStatus(String username, String status);
    List<CouponUser> findByUsername(String username);
    boolean existsByCouponIdAndUsername(Long couponId, String username);
}
