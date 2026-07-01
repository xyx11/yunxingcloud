package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.Banner;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByStatus(String status, Sort sort);
    List<Banner> findAllByOrderBySortOrderAsc();
}
