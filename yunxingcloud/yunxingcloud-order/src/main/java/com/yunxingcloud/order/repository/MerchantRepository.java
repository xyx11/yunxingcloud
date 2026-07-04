package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    List<Merchant> findByStatus(String status);
    Merchant findByPhone(String phone);
}
