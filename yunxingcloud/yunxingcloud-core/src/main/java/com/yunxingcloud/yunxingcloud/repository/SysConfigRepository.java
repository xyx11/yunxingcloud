package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SysConfigRepository extends JpaRepository<SysConfig, Long> {
    Optional<SysConfig> findByConfigKey(String configKey);
    boolean existsByConfigKey(String configKey);
}
