package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.SysNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SysNoticeRepository extends JpaRepository<SysNotice, Long> {
    List<SysNotice> findByStatusOrderByCreatedAtDesc(String status);
    List<SysNotice> findTop5ByStatusAndNoticeTypeOrderByCreatedAtDesc(String status, String noticeType);
}
