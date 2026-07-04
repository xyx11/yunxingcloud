package com.yunxingcloud.api.client;

import com.yunxingcloud.api.dto.DepartmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "yunxingcloud-usercenter", contextId = "departmentClient", path = "/api/departments", fallback = DepartmentClientFallback.class)
public interface DepartmentClient {

    @GetMapping
    List<DepartmentDTO> tree();

    @GetMapping("/{id}")
    DepartmentDTO getById(@PathVariable("id") Long id);
}
