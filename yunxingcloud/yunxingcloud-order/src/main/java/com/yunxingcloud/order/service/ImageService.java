package com.yunxingcloud.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);
    private final Path uploadDir;

    public ImageService(@Value("${app.upload.dir:uploads}") String dir) {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
        try { Files.createDirectories(uploadDir); } catch (IOException e) {
            log.error("无法创建上传目录: {}", e.getMessage());
        }
    }

    public String upload(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("文件为空");
        String ext = getExtension(file.getOriginalFilename());
        if (!ext.matches("jpg|jpeg|png|gif|webp"))
            throw new IllegalArgumentException("仅支持 JPG/PNG/GIF/WEBP 格式");

        String name = UUID.randomUUID() + "." + ext;
        Path target = uploadDir.resolve(name);
        try {
            Files.copy(file.getInputStream(), target);
            log.info("图片上传成功: {}", name);
            return "/uploads/" + name;
        } catch (IOException e) {
            log.error("图片上传失败: {}", e.getMessage());
            throw new RuntimeException("图片上传失败", e);
        }
    }

    private String getExtension(String name) {
        if (name == null) return "jpg";
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(i + 1).toLowerCase() : "jpg";
    }
}