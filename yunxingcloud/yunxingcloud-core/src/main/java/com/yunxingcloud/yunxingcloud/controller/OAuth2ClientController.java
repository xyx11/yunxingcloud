package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/oauth2/clients")
public class OAuth2ClientController {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    public OAuth2ClientController(JdbcTemplate jdbc, PasswordEncoder encoder) { this.jdbc = jdbc; this.encoder = encoder; }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(jdbc.queryForList("SELECT id, client_id, client_name, client_secret, redirect_uris, scopes, client_id_issued_at as created_at FROM oauth2_registered_client ORDER BY client_id_issued_at DESC"));
    }

    @PreAuthorize("hasAuthority('config:write')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, String> body) {
        String id = UUID.randomUUID().toString();
        String secret = body.getOrDefault("secret", UUID.randomUUID().toString().substring(0, 16));
        jdbc.update("INSERT INTO oauth2_registered_client (id, client_id, client_name, client_secret, client_authentication_methods, authorization_grant_types, redirect_uris, scopes, client_settings, token_settings) VALUES (?,?,?,?,?,?,?,?,?,?)",
            id, body.get("clientId"), body.getOrDefault("clientName", body.get("clientId")),
            encoder.encode(secret), "client_secret_basic", "authorization_code,refresh_token,client_credentials",
            body.getOrDefault("redirectUris", "http://127.0.0.1:9090/callback"),
            body.getOrDefault("scopes", "openid,profile"), "{}", "{}");
        return ResponseEntity.ok(Map.of("success", true, "clientSecret", secret));
    }

    @PreAuthorize("hasAuthority('config:write')")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id, @RequestBody Map<String, String> body) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("UPDATE oauth2_registered_client SET ");
        if (body.containsKey("clientName")) { sql.append("client_name=?,"); params.add(body.get("clientName")); }
        if (body.containsKey("redirectUris")) { sql.append("redirect_uris=?,"); params.add(body.get("redirectUris")); }
        if (body.containsKey("scopes")) { sql.append("scopes=?,"); params.add(body.get("scopes")); }
        if (params.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "no fields to update"));
        sql.setLength(sql.length() - 1); sql.append(" WHERE id=?"); params.add(id);
        jdbc.update(sql.toString(), params.toArray());
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('config:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String id) {
        jdbc.update("DELETE FROM oauth2_registered_client WHERE id=?", id);
        return ResponseEntity.ok(Map.of("success", (Object) true));
    }
}
