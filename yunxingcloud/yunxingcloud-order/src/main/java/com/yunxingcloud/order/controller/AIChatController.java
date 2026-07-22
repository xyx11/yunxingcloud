package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "AI客服", description = "AI智能客服聊天")
@RestController
@RequestMapping("/api/chat")
public class AIChatController {

    private final ChatService chatService;

    public AIChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "发送消息")
    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        String message = (String) body.getOrDefault("message", "");
        return ResponseEntity.ok(chatService.chat(message));
    }

    @Operation(summary = "AI服务状态")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(chatService.health());
    }

    @Operation(summary = "清空对话")
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clear() {
        return ResponseEntity.ok(Map.of("success", true, "message", "对话已清空"));
    }

    @Operation(summary = "服务状态")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() { return ResponseEntity.ok(Map.of("ai", "ready")); }
}
