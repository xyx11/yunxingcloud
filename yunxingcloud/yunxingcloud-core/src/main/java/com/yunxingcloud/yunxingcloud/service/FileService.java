package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.common.core.I18nService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileService {

    private final Path uploadDir;
    private final I18nService i18n;

    public FileService(@Value("${app.upload-dir:uploads}") String dir, I18nService i18n) {
        this.uploadDir = Paths.get(dir).toAbsolutePath();
        this.i18n = i18n;
        try { Files.createDirectories(uploadDir); } catch (IOException ignored) {}
    }

    public Map<String, Object> upload(MultipartFile file) {
        if (file.isEmpty()) {
            return Map.of("success", false, "message", i18n.msg("file.empty"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Map.of("success", false, "message", i18n.msg("file.image_only"));
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            return Map.of("success", false, "message", i18n.msg("file.size_exceed"));
        }

        try {
            String ext = getExtension(file.getOriginalFilename());
            String name = UUID.randomUUID().toString().substring(0, 8) + ext;
            file.transferTo(uploadDir.resolve(name).toFile());
            return Map.of(
                    "success", true,
                    "url", "/uploads/" + name,
                    "filename", name
            );
        } catch (IOException e) {
            return Map.of("success", false, "message", i18n.msg("file.upload_failed"));
        }
    }

    public List<Map<String, Object>> list() {
        try (Stream<Path> stream = Files.list(uploadDir)) {
            return stream.filter(Files::isRegularFile).map(p -> {
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
            ).collect(Collectors.toList());
        } catch (Exception e) { return List.of(); }
    }

    public Map<String, Object> delete(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return Map.of("success", false, "message", i18n.msg("common.bad_request"));
        }
        try {
            Files.deleteIfExists(uploadDir.resolve(filename));
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".png";
        String ext = filename.substring(filename.lastIndexOf("."));
        return switch (ext.toLowerCase()) {
            case ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp" -> ext;
            default -> ".png";
        };
    }
}
