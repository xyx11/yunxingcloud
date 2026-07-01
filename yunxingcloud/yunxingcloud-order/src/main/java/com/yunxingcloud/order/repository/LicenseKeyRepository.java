package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.LicenseKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LicenseKeyRepository extends JpaRepository<LicenseKey, Long> {
    Optional<LicenseKey> findByKeyCode(String keyCode);
}