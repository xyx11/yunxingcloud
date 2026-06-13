package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.SysDictType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SysDictTypeRepository extends JpaRepository<SysDictType, Long> {
    Optional<SysDictType> findByDictType(String dictType);
    boolean existsByDictType(String dictType);
}
