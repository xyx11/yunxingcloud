package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.SysOperLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysOperLogRepository extends JpaRepository<SysOperLog, Long> {
    Page<SysOperLog> findByBusinessType(String businessType, Pageable pageable);
    Page<SysOperLog> findByOperNameContaining(String operName, Pageable pageable);
    Page<SysOperLog> findByBusinessTypeAndOperNameContaining(String businessType, String operName, Pageable pageable);
    Page<SysOperLog> findByTitleContaining(String title, Pageable pageable);
}
