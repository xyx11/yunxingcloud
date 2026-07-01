package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.PointsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointsRecordRepository extends JpaRepository<PointsRecord, Long> {
    List<PointsRecord> findByUsernameOrderByCreatedAtDesc(String username);
}