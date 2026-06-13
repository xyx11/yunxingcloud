package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {
    @Query("SELECT m FROM SysMenu m WHERE m.visible = true ORDER BY m.sortOrder")
    List<SysMenu> findByVisibleTrueOrderBySortOrder();
    List<SysMenu> findByMenuTypeInOrderBySortOrder(List<String> types);
    List<SysMenu> findByParentId(Long parentId);
}
