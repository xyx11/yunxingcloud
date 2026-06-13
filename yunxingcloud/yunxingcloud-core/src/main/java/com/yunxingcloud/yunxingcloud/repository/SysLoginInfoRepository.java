package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.SysLoginInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SysLoginInfoRepository extends JpaRepository<SysLoginInfo, Long> {
    Page<SysLoginInfo> findByUserNameContaining(String userName, Pageable pageable);
    Page<SysLoginInfo> findByStatus(String status, Pageable pageable);
    Page<SysLoginInfo> findByUserNameContainingAndStatus(String userName, String status, Pageable pageable);
    Page<SysLoginInfo> findByLoginTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    void deleteByLoginTimeBefore(LocalDateTime cutoff);
}
