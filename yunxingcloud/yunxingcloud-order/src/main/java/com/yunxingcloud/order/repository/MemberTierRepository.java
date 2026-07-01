package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.MemberTier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemberTierRepository extends JpaRepository<MemberTier, Long> {
    List<MemberTier> findAllByOrderByMinPointsAsc();
    List<MemberTier> findAllByOrderByMinPointsDesc();
}