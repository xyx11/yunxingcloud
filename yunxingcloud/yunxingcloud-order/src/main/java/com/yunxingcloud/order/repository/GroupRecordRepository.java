package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.GroupRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupRecordRepository extends JpaRepository<GroupRecord, Long> {
    List<GroupRecord> findByGroupId(Long groupId);
    List<GroupRecord> findByGroupBuyIdAndStatus(Long groupBuyId, String status);
    long countByGroupIdAndStatus(Long groupId, String status);
}