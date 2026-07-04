package com.yunxingcloud.api.client;

import com.yunxingcloud.api.dto.RoleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "yunxingcloud-usercenter", contextId = "roleClient", path = "/api/roles", fallback = RoleClientFallback.class)
public interface RoleClient {

    @GetMapping
    List<RoleDTO> list();

    @GetMapping("/{id}")
    RoleDTO getById(@PathVariable("id") Long id);
}
