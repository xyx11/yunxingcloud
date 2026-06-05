package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final Path uploadDir;

    public FileController(@Value("${app.upload-dir:uploads}") String dir) {
        this.uploadDir = Paths.get(dir).toAbsolutePath();
        try { Files.createDirectories(uploadDir); } catch (IOException ignored) {}
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "文件为空"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "仅支持图片上传"));
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "文件大小不能超过2MB"));
        }

        try {
            String ext = getExtension(file.getOriginalFilename());
            String name = UUID.randomUUID().toString().substring(0, 8) + ext;
            file.transferTo(uploadDir.resolve(name).toFile());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "url", "/uploads/" + name,
                    "filename", name
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "上传失败"));
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".png";
        return filename.substring(filename.lastIndexOf("."));
    }
}
