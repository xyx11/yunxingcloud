package com.yunxingcloud.yunxingcloud.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "全局搜索", description = "跨模块搜索")
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final JdbcTemplate jdbc;

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    public SearchController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String q) {
        String kw = "%" + q + "%";
        Map<String, Object> results = new LinkedHashMap<>();

        try { results.put("users", jdbc.queryForList(
            "SELECT id, username, nickname, email FROM users WHERE username LIKE ? OR nickname LIKE ? OR email LIKE ? LIMIT 5", kw, kw, kw));
        } catch (Exception e) { log.warn("搜索用户失败: {}", e.getMessage()); }

        try { results.put("menus", jdbc.queryForList(
            "SELECT id, name, path FROM sys_menu WHERE name LIKE ? OR path LIKE ? LIMIT 5", kw, kw));
        } catch (Exception e) { log.warn("搜索菜单失败: {}", e.getMessage()); }

        try { results.put("roles", jdbc.queryForList(
            "SELECT id, name, code FROM role WHERE name LIKE ? OR code LIKE ? LIMIT 5", kw, kw));
        } catch (Exception e) { log.warn("搜索角色失败: {}", e.getMessage()); }

        try { results.put("configs", jdbc.queryForList(
            "SELECT id, name, config_key FROM sys_config WHERE name LIKE ? OR config_key LIKE ? LIMIT 5", kw, kw));
        } catch (Exception e) { log.warn("搜索配置失败: {}", e.getMessage()); }

        try { results.put("dict", jdbc.queryForList(
            "SELECT id, dict_name, dict_type FROM sys_dict_type WHERE dict_name LIKE ? OR dict_type LIKE ? LIMIT 5", kw, kw));
        } catch (Exception e) { log.warn("搜索字典失败: {}", e.getMessage()); }

        try { results.put("notices", jdbc.queryForList(
            "SELECT id, notice_title, notice_type FROM sys_notice WHERE notice_title LIKE ? LIMIT 5", kw));
        } catch (Exception e) { log.warn("搜索公告失败: {}", e.getMessage()); }

        try { results.put("posts", jdbc.queryForList(
            "SELECT id, post_code, post_name FROM sys_post WHERE post_name LIKE ? OR post_code LIKE ? LIMIT 5", kw, kw));
        } catch (Exception e) { log.warn("搜索岗位失败: {}", e.getMessage()); }

        try { results.put("departments", jdbc.queryForList(
            "SELECT id, name FROM department WHERE name LIKE ? LIMIT 5", kw));
        } catch (Exception e) { log.warn("搜索部门失败: {}", e.getMessage()); }

        try { results.put("jobs", jdbc.queryForList(
            "SELECT id, job_name, job_group FROM sys_job WHERE job_name LIKE ? LIMIT 5", kw));
        } catch (Exception e) { log.warn("搜索任务失败: {}", e.getMessage()); }

        // 计算总数
        int total = 0;
        for (Object v : results.values()) {
            if (v instanceof List) total += ((List<?>) v).size();
        }
        results.put("total", total);

        return ResponseEntity.ok(results);
    }
}
