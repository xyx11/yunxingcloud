package com.yunxingcloud.api.client;

import com.yunxingcloud.api.dto.DepartmentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DepartmentClientFallback implements DepartmentClient {

    private static final Logger log = LoggerFactory.getLogger(DepartmentClientFallback.class);

    @Override
    public List<DepartmentDTO> tree() {
        log.warn("DepartmentClient.tree() fallback");
        return Collections.emptyList();
    }

    @Override
    public DepartmentDTO getById(Long id) {
        log.warn("DepartmentClient.getById({}) fallback", id);
        return null;
    }
}
