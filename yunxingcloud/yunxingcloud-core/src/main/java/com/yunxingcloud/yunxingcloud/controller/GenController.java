package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.service.GenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "代码生成", description = "数据库表代码生成器")
@RestController
@RequestMapping("/api/generator")
public class GenController {

    private final GenService genService;

    public GenController(GenService genService) {
        this.genService = genService;
    }

    @GetMapping("/tables")
    public ResponseEntity<List<Map<String, Object>>> tables() {
        return ResponseEntity.ok(genService.listTables());
    }

    @GetMapping("/table/{tableName}")
    public ResponseEntity<Map<String, Object>> tableColumns(@PathVariable String tableName) {
        return ResponseEntity.ok(genService.getTableColumns(tableName));
    }

    @PostMapping("/generate/{tableName}")
    public ResponseEntity<Map<String, Object>> generate(@PathVariable String tableName,
                                                         @RequestBody Map<String, String> body) {
        String packageName = body.getOrDefault("packageName", "com.yunxingcloud");
        String author = body.getOrDefault("author", "generator");
        return ResponseEntity.ok(genService.generate(tableName, packageName, author));
    }
}
