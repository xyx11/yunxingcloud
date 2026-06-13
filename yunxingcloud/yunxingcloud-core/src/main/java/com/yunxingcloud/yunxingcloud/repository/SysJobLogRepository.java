package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.SysJobLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SysJobLogRepository extends JpaRepository<SysJobLog, Long> {
    List<SysJobLog> findTop50ByOrderByCreatedAtDesc();
    List<SysJobLog> findByJobNameOrderByCreatedAtDesc(String jobName);
}
