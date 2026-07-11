package com.yunxingcloud.usercenter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "用户首页", description = "用户中心首页")
@Controller
public class IndexController {

    @Operation(summary = "首页")
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @Operation(summary = "登录页")
    @GetMapping("/login")
    public String login() {
        return "forward:/index.html";
    }

    @Operation(summary = "注册页")
    @GetMapping("/register")
    public String register() {
        return "forward:/index.html";
    }
}
