package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.CompareList;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompareListRepository extends JpaRepository<CompareList, Long> {
    List<CompareList> findByUsernameOrderByCreatedAtDesc(String username);
    void deleteByUsernameAndProductId(String username, Long productId);
    void deleteByUsername(String username);
}