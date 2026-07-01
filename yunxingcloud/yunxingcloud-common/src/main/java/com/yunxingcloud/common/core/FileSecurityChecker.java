package com.yunxingcloud.common.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public final class FileSecurityChecker {

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    // 魔数映射 (文件头字节 → 类型)
    private static final Map<String, Set<String>> MAGIC_BYTES = Map.of(
        "FFD8FF",              Set.of("jpg", "jpeg"),
        "89504E47",            Set.of("png"),
        "47494638",            Set.of("gif"),
        "52494646",            Set.of("webp"),
        "25504446",            Set.of("pdf"),
        "504B0304",            Set.of("xlsx", "docx")
    );

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "jpg", "jpeg", "png", "gif", "webp", "pdf", "xlsx", "docx"
    );

    private FileSecurityChecker() {}

    public static void check(String originalName, InputStream in, long size) throws IOException {
        // 大小限制
        if (size > MAX_SIZE) throw new IllegalArgumentException("文件大小不能超过 10MB");

        // 扩展名检查
        String ext = getExtension(originalName);
        if (!ALLOWED_TYPES.contains(ext))
            throw new IllegalArgumentException("不支持的文件类型: ." + ext);

        // 魔数校验
        byte[] header = new byte[6];
        int read = in.read(header, 0, 6);
        if (read < 4) throw new IllegalArgumentException("文件内容为空");

        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < read && hex.length() < 8; i++)
            hex.append(String.format("%02X", header[i]));

        boolean matched = false;
        for (var entry : MAGIC_BYTES.entrySet()) {
            if (hex.toString().startsWith(entry.getKey()) && entry.getValue().contains(ext)) {
                matched = true;
                break;
            }
        }
        if (!matched) throw new IllegalArgumentException("文件内容与扩展名不匹配 (魔数校验失败)");
    }

    private static String getExtension(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(i + 1).toLowerCase() : "";
    }
}