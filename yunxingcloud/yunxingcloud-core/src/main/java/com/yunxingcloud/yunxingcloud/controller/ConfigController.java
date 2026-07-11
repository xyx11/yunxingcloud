package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.yunxingcloud.config.FeatureFlags;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.SysConfig;
import com.yunxingcloud.yunxingcloud.service.ConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@Tag(name = "参数配置", description = "系统参数的增删改查")
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigService configService;
    private final FeatureFlags featureFlags;
    private final I18nService i18n;

    public ConfigController(ConfigService configService, FeatureFlags featureFlags, I18nService i18n) {
        this.configService = configService;
        this.featureFlags = featureFlags;
        this.i18n = i18n;
    }

    @Operation(summary = "查询参数配置列表")
    @GetMapping
    public ResponseEntity<List<SysConfig>> list() {
        return ResponseEntity.ok(configService.list());
    }

    @Operation(summary = "按Key查询配置")
    @GetMapping("/key/{key}")
    public ResponseEntity<SysConfig> getByKey(@PathVariable String key) {
        return configService.getByKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "新增参数配置")
    @PreAuthorize("hasAuthority('config:write')")
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody SysConfig config) {
        try {
            return ResponseEntity.ok(configService.create(config));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg(e.getMessage())));
        }
    }

    @Operation(summary = "修改参数配置")
    @PreAuthorize("hasAuthority('config:write')")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<SysConfig> update(@PathVariable Long id, @RequestBody SysConfig body) {
        try {
            return ResponseEntity.ok(configService.update(id, body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "删除参数配置")
    @PreAuthorize("hasAuthority('config:write')")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        configService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @Operation(summary = "刷新特性开关")
    @PreAuthorize("hasAuthority('config:write')")
    @PostMapping("/refresh-flags")
    public ResponseEntity<Map<String, Object>> refreshFlags() {
        featureFlags.refresh();
        return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("config.flags_refreshed")));
    }

    @Operation(summary = "导出配置CSV")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        String csv = configService.exportCsv();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=configs.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
