package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.service.OAuth2ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "OAuth2客户端", description = "OAuth2客户端管理")
@RestController
@RequestMapping("/api/oauth2/clients")
public class OAuth2ClientController {

    private final OAuth2ClientService oAuth2ClientService;

    public OAuth2ClientController(OAuth2ClientService oAuth2ClientService) {
        this.oAuth2ClientService = oAuth2ClientService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(oAuth2ClientService.list());
    }

    @PreAuthorize("hasAuthority('config:write')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, String> body) {
        Map<String, String> result = oAuth2ClientService.create(body);
        return ResponseEntity.ok(Map.of("success", true, "clientSecret", result.get("clientSecret")));
    }

    @PreAuthorize("hasAuthority('config:write')")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id, @RequestBody Map<String, String> body) {
        if (!oAuth2ClientService.update(id, body))
            return ResponseEntity.badRequest().body(Map.of("message", "no fields to update"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('config:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String id) {
        oAuth2ClientService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
