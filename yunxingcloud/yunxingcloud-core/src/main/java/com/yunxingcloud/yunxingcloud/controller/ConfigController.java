package com.yunxingcloud.yunxingcloud.controller;
import org.springframework.security.access.prepost.PreAuthorize;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.yunxingcloud.entity.SysConfig;
import com.yunxingcloud.yunxingcloud.repository.SysConfigRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final SysConfigRepository configRepository;

    public ConfigController(SysConfigRepository configRepository) { this.configRepository = configRepository; }

    @GetMapping
    public ResponseEntity<List<SysConfig>> list() { return ResponseEntity.ok(configRepository.findAll()); }

    @GetMapping("/key/{key}")
    public ResponseEntity<SysConfig> getByKey(@PathVariable String key) {
        return configRepository.findByConfigKey(key).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('config:write')")
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody SysConfig config) {
        if (configRepository.existsByConfigKey(config.getConfigKey())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "参数键名已存在"));
        }
        return ResponseEntity.ok(configRepository.save(config));
    }

    @PreAuthorize("hasAuthority('config:write')")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<SysConfig> update(@PathVariable Long id, @RequestBody SysConfig body) {
        return configRepository.findById(id).map(c -> {
            c.setName(body.getName()); c.setConfigKey(body.getConfigKey());
            c.setConfigValue(body.getConfigValue()); c.setConfigType(body.getConfigType());
            return ResponseEntity.ok(configRepository.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('config:write')")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        configRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
