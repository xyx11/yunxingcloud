package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/generator")
public class GenController {

    private final JdbcTemplate jdbcTemplate;

    public GenController(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @GetMapping("/tables")
    public ResponseEntity<List<Map<String, Object>>> tables() {
        List<Map<String, Object>> tables = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE()");
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/table/{tableName}")
    public ResponseEntity<Map<String, Object>> tableColumns(@PathVariable String tableName) {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT, IS_NULLABLE, COLUMN_KEY " +
            "FROM information_schema.COLUMNS WHERE TABLE_NAME = ? AND TABLE_SCHEMA = DATABASE() " +
            "ORDER BY ORDINAL_POSITION", tableName);

        return ResponseEntity.ok(Map.of("tableName", tableName, "columns", columns));
    }

    @PostMapping("/generate/{tableName}")
    public ResponseEntity<Map<String, Object>> generate(@PathVariable String tableName,
                                                         @RequestBody Map<String, String> body) {
        String packageName = body.getOrDefault("packageName", "com.yunxingcloud");
        String className = toCamelCase(tableName, true);
        String author = body.getOrDefault("author", "generator");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entity", generateEntity(tableName, className, packageName, author));
        result.put("controller", generateController(tableName, className, packageName, author));
        result.put("repository", generateRepository(className, packageName));
        result.put("service", generateService(className, packageName));

        return ResponseEntity.ok(result);
    }

    private String generateEntity(String tableName, String className, String pkg, String author) {
        List<Map<String, Object>> cols = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS " +
            "WHERE TABLE_NAME = ? AND TABLE_SCHEMA = DATABASE() ORDER BY ORDINAL_POSITION", tableName);

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(".entity;\n\n");
        sb.append("import jakarta.persistence.*;\n\n");
        sb.append("@Entity\n@Table(name = \"").append(tableName).append("\")\n");
        sb.append("public class ").append(className).append(" {\n\n");

        for (var col : cols) {
            String colName = (String) col.get("COLUMN_NAME");
            String type = (String) col.get("DATA_TYPE");
            String comment = (String) col.get("COLUMN_COMMENT");
            sb.append("    @Column(name = \"").append(colName).append("\")\n");
            if (comment != null && !comment.isEmpty()) sb.append("    // ").append(comment).append("\n");
            sb.append("    private ").append(mapJavaType(type)).append(" ").append(toCamelCase(colName, false)).append(";\n\n");
        }
        sb.append("    // getters and setters\n");
        sb.append("}");
        return sb.toString();
    }

    private String generateController(String tableName, String className, String pkg, String author) {
        return "package " + pkg + ".controller;\n\n" +
               "import org.springframework.web.bind.annotation.*;\n\n" +
               "@RestController\n@RequestMapping(\"/api/" + tableName + "\")\n" +
               "public class " + className + "Controller {\n" +
               "    // TODO: CRUD endpoints\n" +
               "}";
    }

    private String generateRepository(String className, String pkg) {
        return "package " + pkg + ".repository;\n\n" +
               "import " + pkg + ".entity." + className + ";\n" +
               "import org.springframework.data.jpa.repository.JpaRepository;\n\n" +
               "public interface " + className + "Repository extends JpaRepository<" + className + ", Long> {\n" +
               "}";
    }

    private String generateService(String className, String pkg) {
        return "package " + pkg + ".service;\n\n" +
               "import org.springframework.stereotype.Service;\n\n" +
               "@Service\npublic class " + className + "Service {\n" +
               "}";
    }

    private String toCamelCase(String name, boolean capitalize) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i == 0 && !capitalize) { sb.append(parts[i]); }
            else { sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1)); }
        }
        return sb.toString();
    }

    private String mapJavaType(String sqlType) {
        return switch (sqlType.toLowerCase()) {
            case "int", "tinyint", "smallint" -> "Integer";
            case "bigint" -> "Long";
            case "varchar", "char", "text", "longtext", "enum" -> "String";
            case "datetime", "timestamp" -> "java.time.LocalDateTime";
            case "decimal", "double", "float" -> "java.math.BigDecimal";
            case "boolean", "bit" -> "Boolean";
            default -> "String";
        };
    }
}
