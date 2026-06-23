package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.IpBlacklist;
import com.yunxingcloud.yunxingcloud.repository.IpBlacklistRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ip-blacklist")
public class IpBlacklistController {

    private final IpBlacklistRepository repo;
    private final I18nService i18n;

    public IpBlacklistController(IpBlacklistRepository repo, I18nService i18n) {
        this.repo = repo;
        this.i18n = i18n;
    }

    @GetMapping
    public ResponseEntity<List<IpBlacklist>> list() { return ResponseEntity.ok(repo.findAll()); }

    @PreAuthorize("hasAuthority('config:write')")
    @Log(title = "IP黑名单", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<?> add(@RequestBody IpBlacklist item) {
        if (repo.existsByIp(item.getIp())) return ResponseEntity.badRequest().body(Map.of("message", i18n.msg("ip_blacklist.exists")));
        return ResponseEntity.ok(repo.save(item));
    }

    @PreAuthorize("hasAuthority('config:write')")
    @Log(title = "IP黑名单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> del(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}