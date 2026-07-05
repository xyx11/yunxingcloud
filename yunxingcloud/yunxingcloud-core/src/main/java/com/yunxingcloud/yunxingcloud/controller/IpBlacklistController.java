package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.IpBlacklist;
import com.yunxingcloud.yunxingcloud.service.IpBlacklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ip-blacklist")
public class IpBlacklistController {

    private final IpBlacklistService ipBlacklistService;
    private final I18nService i18n;

    public IpBlacklistController(IpBlacklistService ipBlacklistService, I18nService i18n) {
        this.ipBlacklistService = ipBlacklistService;
        this.i18n = i18n;
    }

    @GetMapping
    public ResponseEntity<List<IpBlacklist>> list() { return ResponseEntity.ok(ipBlacklistService.list()); }

    @PreAuthorize("hasAuthority('config:write')")
    @Log(title = "IP黑名单", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<?> add(@RequestBody IpBlacklist item) {
        try {
            return ResponseEntity.ok(ipBlacklistService.add(item));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", i18n.msg(e.getMessage())));
        }
    }

    @PreAuthorize("hasAuthority('config:write')")
    @Log(title = "IP黑名单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> del(@PathVariable Long id) {
        ipBlacklistService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
