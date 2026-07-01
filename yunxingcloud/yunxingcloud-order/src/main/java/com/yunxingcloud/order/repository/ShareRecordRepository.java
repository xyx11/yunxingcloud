package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.ShareRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShareRecordRepository extends JpaRepository<ShareRecord, Long> {
    List<ShareRecord> findBySharerOrderByCreatedAtDesc(String sharer);
}