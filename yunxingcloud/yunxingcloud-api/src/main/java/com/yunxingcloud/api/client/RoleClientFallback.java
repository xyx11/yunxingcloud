package com.yunxingcloud.api.client;

import com.yunxingcloud.api.dto.RoleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RoleClientFallback implements RoleClient {

    private static final Logger log = LoggerFactory.getLogger(RoleClientFallback.class);

    @Override
    public List<RoleDTO> list() {
        log.warn("RoleClient.list() fallback triggered");
        return Collections.emptyList();
    }

    @Override
    public RoleDTO getById(Long id) {
        log.warn("RoleClient.getById({}) fallback triggered", id);
        return null;
    }
}
