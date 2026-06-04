package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {
    List<SysMenu> findByVisibleTrueOrderBySortOrder();
    List<SysMenu> findByMenuTypeInOrderBySortOrder(List<String> types);
}
