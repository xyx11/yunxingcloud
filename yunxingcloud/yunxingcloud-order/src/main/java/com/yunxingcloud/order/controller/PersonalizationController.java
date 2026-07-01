package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.PersonalizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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