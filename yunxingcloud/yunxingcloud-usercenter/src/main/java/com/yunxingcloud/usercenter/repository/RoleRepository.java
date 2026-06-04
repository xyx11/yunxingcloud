package com.yunxingcloud.usercenter.repository;

import com.yunxingcloud.usercenter.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    List<Role> findByEnabledTrue();

    boolean existsByCode(String code);
}
