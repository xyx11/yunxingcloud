package com.yunxingcloud.yunxingcloud.controller;
import org.springframework.security.access.prepost.PreAuthorize;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.yunxingcloud.config.FeatureFlags;
import com.yunxingcloud.yunxingcloud.config.I18nService;
import com.yunxingcloud.yunxingcloud.entity.SysConfig;
import com.yunxingcloud.yunxingcloud.repository.SysConfigRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@Tag(name = "参数配置", description = "系统参数的增删改查")
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final SysConfigRepository configRepository;
    private final FeatureFlags featureFlags;
    private final I18nService i18n;

    public ConfigController(SysConfigRepository configRepository, FeatureFlags featureFlags, I18nService i18n) {
        this.configRepository = configRepository;
        this.featureFlags = featureFlags;
        this.i18n = i18n;
    }

    private void refreshIfFeatureConfig(SysConfig c) {
        if (c.getConfigKey() != null && c.getConfigKey().startsWith("feature.")) {
            featureFlags.refresh();
        }
    }

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
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("config.key_exists")));
        }
        SysConfig saved = configRepository.save(config);
        refreshIfFeatureConfig(saved);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasAuthority('config:write')")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<SysConfig> update(@PathVariable Long id, @RequestBody SysConfig body) {
        return configRepository.findById(id).map(c -> {
            c.setName(body.getName()); c.setConfigKey(body.getConfigKey());
            c.setConfigValue(body.getConfigValue()); c.setConfigType(body.getConfigType());
            SysConfig updated = configRepository.save(c);
            refreshIfFeatureConfig(updated);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('config:write')")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        SysConfig c = configRepository.findById(id).orElse(null);
        configRepository.deleteById(id);
        if (c != null) refreshIfFeatureConfig(c);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('config:write')")
    @PostMapping("/refresh-flags")
    public ResponseEntity<Map<String, Object>> refreshFlags() {
        featureFlags.refresh();
        return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("config.flags_refreshed")));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        StringBuilder sb = new StringBuilder("名称,键名,键值,系统内置,创建时间\n");
        configRepository.findAll().forEach(c -> sb.append(String.format("%s,%s,%s,%s,%s\n",
            c.getName(), c.getConfigKey(), c.getConfigValue(), "Y".equals(c.getConfigType()) ? "是" : "否", c.getCreatedAt())));
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=configs.csv").header("Content-Type", "text/csv; charset=UTF-8").body(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
