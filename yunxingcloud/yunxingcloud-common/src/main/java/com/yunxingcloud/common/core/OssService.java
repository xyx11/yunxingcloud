package com.yunxingcloud.common.core;

import io.minio.*;
import io.minio.http.Method;
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
import java.util.concurrent.TimeUnit;

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
    private MinioClient minioClient;

    public OssService() {
        try { Files.createDirectories(localDir); } catch (IOException ignored) {}
    }

    private MinioClient getClient() {
        if (minioClient == null && !isMockMode()) {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        }
        return minioClient;
    }

    public String upload(MultipartFile file) {
        if (isMockMode()) return uploadLocal(file);
        try {
            String key = generateKey(file.getOriginalFilename());
            MinioClient client = getClient();
            boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            client.putObject(PutObjectArgs.builder().bucket(bucket).object(key)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType()).build());
            log.info("OSS upload: {} -> {}/{}", file.getOriginalFilename(), bucket, key);
            return "/api/files/" + key;
        } catch (Exception e) {
            log.error("OSS upload failed, falling back to local: {}", e.getMessage());
            return uploadLocal(file);
        }
    }

    public boolean delete(String objectKey) {
        if (isMockMode()) {
            try { Files.deleteIfExists(localDir.resolve(objectKey)); return true; }
            catch (IOException e) { return false; }
        }
        try {
            getClient().removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
            return true;
        } catch (Exception e) {
            log.error("OSS delete failed: {}", e.getMessage());
            return false;
        }
    }

    public String presignedUrl(String objectKey, int expireSeconds) {
        if (isMockMode()) return "/uploads/oss/" + objectKey;
        try {
            return getClient().getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket).object(objectKey).method(Method.GET)
                    .expiry(expireSeconds, TimeUnit.SECONDS).build());
        } catch (Exception e) {
            log.error("OSS presigned URL failed: {}", e.getMessage());
            return "/uploads/oss/" + objectKey;
        }
    }

    private String uploadLocal(MultipartFile file) {
        try {
            String key = generateKey(file.getOriginalFilename());
            Path target = localDir.resolve(key);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("OSS local upload: {} -> {}", file.getOriginalFilename(), key);
            return "/uploads/oss/" + key;
        } catch (IOException e) {
            log.error("OSS upload failed: {}", e.getMessage());
            return null;
        }
    }

    private String generateKey(String filename) {
        String ext = getExtension(filename);
        return UUID.randomUUID().toString().substring(0, 8) + ext;
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
