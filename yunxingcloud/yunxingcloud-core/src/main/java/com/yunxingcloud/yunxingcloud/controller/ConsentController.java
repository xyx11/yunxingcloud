package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "用户同意", description = "用户隐私同意记录")
@RestController
@RequestMapping("/api/oauth2/consent")
public class ConsentController {

    private final RegisteredClientRepository registeredClientRepository;

    public ConsentController(RegisteredClientRepository registeredClientRepository) {
        this.registeredClientRepository = registeredClientRepository;
    }

    @GetMapping("/client")
    public ResponseEntity<Map<String, Object>> clientInfo(@RequestParam("client_id") String clientId) {
        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        if (client == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "client not found"));
        }
        return ResponseEntity.ok(Map.of(
                "clientId", client.getClientId(),
                "clientName", client.getClientName() != null
                        ? client.getClientName() : client.getClientId(),
                "scopes", client.getScopes().stream()
                        .map(s -> Map.of("scope", s, "description", scopeDescription(s)))
                        .toList()
        ));
    }

    private String scopeDescription(String scope) {
        return scope; // i18n handled by frontend ConsentView
    }
}
