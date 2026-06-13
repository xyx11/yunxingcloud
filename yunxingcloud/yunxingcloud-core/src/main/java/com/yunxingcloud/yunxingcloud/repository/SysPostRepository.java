package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.SysPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SysPostRepository extends JpaRepository<SysPost, Long> {
    boolean existsByPostCode(String postCode);
    List<SysPost> findByStatusOrderBySortOrder(String status);
}
