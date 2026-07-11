package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "首页", description = "系统首页")
@Controller
public class IndexController {

    // SPA fallback: forward all non-file paths to index.html
    @GetMapping("/{path:[^\\.]+}")
    public String forward() {
        return "forward:/index.html";
    }

    // Also handle root explicitly
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}