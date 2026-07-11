package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "系统日志", description = "系统日志查询")
@RestController
@RequestMapping("/api/system/logs")
@PreAuthorize("hasAuthority('config:write')")
public class SysLogController {

    private static final String LOG_DIR = "logs";

    @GetMapping("/files")
    public ResponseEntity<List<Map<String, Object>>> files() {
        try (Stream<Path> stream = Files.list(Paths.get(LOG_DIR))) {
            return ResponseEntity.ok(stream.filter(p -> p.toString().endsWith(".log"))
                .map(p -> Map.<String, Object>of("name", p.getFileName().toString(), "size", p.toFile().length()))
                .collect(Collectors.toList()));
        } catch (Exception e) { return ResponseEntity.ok(List.of()); }
    }

    @GetMapping("/view/{filename}")
    public ResponseEntity<Map<String, Object>> view(@PathVariable String filename,
                                                      @RequestParam(defaultValue = "100") int lines) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid filename"));
        }
        try {
            Path file = Paths.get(LOG_DIR, filename);
            List<String> allLines = Files.readAllLines(file);
            int from = Math.max(0, allLines.size() - lines);
            List<String> tail = allLines.subList(from, allLines.size());
            return ResponseEntity.ok(Map.of("filename", filename, "totalLines", allLines.size(), "content", String.join("\n", tail)));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", e.getMessage()));
        }
    }
}
