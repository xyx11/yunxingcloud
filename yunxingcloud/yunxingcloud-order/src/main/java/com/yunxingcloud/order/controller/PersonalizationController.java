package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.PersonalizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "个性化推荐", description = "个性化商品推荐")
@RestController
@RequestMapping("/api/personalized")
public class PersonalizationController {

    private final PersonalizationService service;

    public PersonalizationController(PersonalizationService service) { this.service = service; }

    @GetMapping("/home")
    public ResponseEntity<?> home() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(service.personalizedHome(username));
    }
}