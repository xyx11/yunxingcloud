package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "图片管理", description = "商品图片上传与管理")
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('ticket:write')")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            String url = imageService.upload(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/uploads")
    @PreAuthorize("hasAuthority('ticket:write')")
    public ResponseEntity<?> uploadMulti(@RequestParam("files") MultipartFile[] files) {
        try {
            String[] urls = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                urls[i] = imageService.upload(files[i]);
            }
            return ResponseEntity.ok(Map.of("urls", urls));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}