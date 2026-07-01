package com.yunxingcloud.common.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 对象存储服务 — 本地文件系统 (开发) / 阿里云 OSS / MinIO
 * 留空配置 = 本地模式 (开箱即用)
 */
@Service
public class OssService {

    private static final Logger log = LoggerFactory.getLogger(OssService.class);

    @Value("${oss.endpoint:}")
    private String endpoint;

    @Value("${oss.bucket:yunxingcloud}")
    private String bucket;

    @Value("${oss.access-key:}")
    private String accessKey;

    @Value("${oss.secret-key:}")
    private String secretKey;

    private final Path localDir = Paths.get("uploads/oss");

    public OssService() {
        try { Files.createDirectories(localDir); } catch (IOException ignored) {}
    }

    /** 上传文件，返回访问 URL */
    public String upload(MultipartFile file) {
        if (isMockMode()) return uploadLocal(file);
        // TODO: 接入真实 OSS/MinIO
        log.warn("OSS endpoint configured but SDK not integrated, using local fallback");
        return uploadLocal(file);
    }

    /** 删除文件 */
    public boolean delete(String objectKey) {
        if (isMockMode()) {
            try { Files.deleteIfExists(localDir.resolve(objectKey)); return true; }
            catch (IOException e) { return false; }
        }
        // TODO: 接入真实 OSS/MinIO
        return true;
    }

    /** 生成预签名 URL (临时访问) */
    public String presignedUrl(String objectKey, int expireSeconds) {
        // Mock: 直接返回本地 URL
        return "/uploads/oss/" + objectKey;
    }

    private String uploadLocal(MultipartFile file) {
        try {
            String ext = getExtension(file.getOriginalFilename());
            String key = UUID.randomUUID().toString().substring(0, 8) + ext;
            Path target = localDir.resolve(key);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("OSS local upload: {} -> {}", file.getOriginalFilename(), key);
            return "/uploads/oss/" + key;
        } catch (IOException e) {
            log.error("OSS upload failed: {}", e.getMessage());
            return null;
        }
    }

    public boolean isMockMode() {
        return endpoint.isEmpty() || accessKey.isEmpty();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".png";
        String ext = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        return switch (ext) {
            case ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".pdf", ".zip" -> ext;
            default -> ".png";
        };
    }
}