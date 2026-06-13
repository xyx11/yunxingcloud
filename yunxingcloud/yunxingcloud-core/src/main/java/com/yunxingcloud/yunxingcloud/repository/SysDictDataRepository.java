package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.SysDictData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SysDictDataRepository extends JpaRepository<SysDictData, Long> {
    List<SysDictData> findByDictTypeOrderBySortOrder(String dictType);
    void deleteByDictType(String dictType);
}
