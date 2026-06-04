package com.yunxingcloud.usercenter.repository;

import com.yunxingcloud.usercenter.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByParentIdOrderBySortOrder(Long parentId);

    List<Department> findByParentIdIsNullOrderBySortOrder();

    List<Department> findByEnabledTrueOrderBySortOrder();
}
