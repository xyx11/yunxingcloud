package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.LiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "直播管理", description = "直播带货管理")
@RestController
@RequestMapping("/api/live")
public class LiveController {

    private final LiveService liveService;

    public LiveController(LiveService liveService) { this.liveService = liveService; }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping("/rooms")
    public ResponseEntity<Map<String, Object>> rooms() {
        return ResponseEntity.ok(Map.of("rooms", liveService.rooms()));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<Map<String, Object>> roomDetail(@PathVariable Long id) {
        Map<String, Object> result = liveService.roomDetail(id);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/rooms/{id}/view")
    public ResponseEntity<Map<String, Object>> addView(@PathVariable Long id) {
        int count = liveService.addView(id);
        if (count < 0) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("success", true, "viewerCount", count));
    }

    @PostMapping("/{roomId}/remind")
    public ResponseEntity<Map<String, Object>> setRemind(@PathVariable Long roomId) {
        liveService.setRemind(user(), roomId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/rooms/active")
    public ResponseEntity<Map<String, Object>> activeRooms() {
        return ResponseEntity.ok(Map.of("rooms", liveService.rooms()));
    }
}
