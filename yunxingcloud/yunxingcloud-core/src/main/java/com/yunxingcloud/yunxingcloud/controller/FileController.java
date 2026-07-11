package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "文件管理", description = "文件上传/列表/删除")
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PreAuthorize("hasAuthority('file:write')")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = fileService.upload(file);
        if (Boolean.FALSE.equals(result.get("success"))) {
            String msg = String.valueOf(result.getOrDefault("message", ""));
            if (msg.contains("empty") || msg.contains("image_only") || msg.contains("size_exceed")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('file:read')")
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(fileService.list());
    }

    @PreAuthorize("hasAuthority('file:write')")
    @DeleteMapping("/{filename}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String filename) {
        Map<String, Object> result = fileService.delete(filename);
        if (Boolean.FALSE.equals(result.get("success"))) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
