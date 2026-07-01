package com.yunxingcloud.common.core;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class CsvExporter {

    private static final Logger log = LoggerFactory.getLogger(CsvExporter.class);

    private CsvExporter() {}

    public static <T> void export(HttpServletResponse resp, String filename,
                                   List<T> data, String[] headers, String[] fields) {
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition",
                "attachment; filename=" + filename + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("BOM", "﻿"); // Excel 兼容 BOM

        try (PrintWriter w = new PrintWriter(
                new OutputStreamWriter(resp.getOutputStream(), StandardCharsets.UTF_8))) {
            w.write('﻿'); // UTF-8 BOM
            w.println(String.join(",", quoteHeaders(headers)));

            for (T row : data) {
                List<String> values = new ArrayList<>();
                for (String f : fields) {
                    values.add(quote(getFieldValue(row, f)));
                }
                w.println(String.join(",", values));
            }
        } catch (Exception e) {
            log.error("CSV 导出失败: {}", e.getMessage());
            throw new RuntimeException("CSV 导出失败", e);
        }
    }

    private static String[] quoteHeaders(String[] headers) {
        return Arrays.stream(headers).map(h -> "\"" + h.replace("\"", "\"\"") + "\"")
                .toArray(String[]::new);
    }

    private static String quote(String val) {
        return "\"" + (val != null ? val.replace("\"", "\"\"") : "") + "\"";
    }

    private static String getFieldValue(Object obj, String fieldName) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field == null) return "";
            field.setAccessible(true);
            Object val = field.get(obj);
            return val != null ? val.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try { return clazz.getDeclaredField(name); }
            catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
        }
        return null;
    }
}