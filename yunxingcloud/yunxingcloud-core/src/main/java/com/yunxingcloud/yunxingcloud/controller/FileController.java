package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.core.I18nService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final Path uploadDir;
    private final I18nService i18n;

    public FileController(@Value("${app.upload-dir:uploads}") String dir, I18nService i18n) {
        this.uploadDir = Paths.get(dir).toAbsolutePath();
        this.i18n = i18n;
        try { Files.createDirectories(uploadDir); } catch (IOException ignored) {}
    }

    @PreAuthorize("hasAuthority('file:write')")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("file.empty")));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("file.image_only")));
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", i18n.msg("file.size_exceed")));
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
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", i18n.msg("file.upload_failed")));
        }
    }

    @PreAuthorize("hasAuthority('file:read')")
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> list() {
        try (Stream<Path> stream = Files.list(uploadDir)) {
            return ResponseEntity.ok(stream.filter(Files::isRegularFile).map(p -> {
                try {
                    var attr = Files.readAttributes(p, BasicFileAttributes.class);
                    return Map.<String, Object>of(
                        "filename", p.getFileName().toString(),
                        "size", attr.size(),
                        "modified", attr.lastModifiedTime().toString(),
                        "url", "/uploads/" + p.getFileName().toString()
                    );
                } catch (Exception e) { return null; }
            }).filter(Objects::nonNull).sorted((a, b) ->
                b.get("modified").toString().compareTo(a.get("modified").toString())
            ).collect(Collectors.toList()));
        } catch (Exception e) { return ResponseEntity.ok(List.of()); }
    }

    @PreAuthorize("hasAuthority('file:write')")
    @DeleteMapping("/{filename}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String filename) {
        try {
            Files.deleteIfExists(uploadDir.resolve(filename));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".png";
        return filename.substring(filename.lastIndexOf("."));
    }
}
