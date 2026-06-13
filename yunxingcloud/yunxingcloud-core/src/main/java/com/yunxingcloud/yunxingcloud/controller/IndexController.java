package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/index.html";
    }

    @GetMapping("/oauth2/consent")
    public String consent() {
        return "forward:/index.html";
    }

    @GetMapping("/register")
    public String register() {
        return "forward:/index.html";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forward:/index.html";
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        return "forward:/index.html";
    }

    // SPA 路由转发
    @GetMapping("/dict")
    public String dict() { return "forward:/index.html"; }
    @GetMapping("/notices")
    public String notices() { return "forward:/index.html"; }
    @GetMapping("/posts")
    public String posts() { return "forward:/index.html"; }
    @GetMapping("/loginlog")
    public String loginlog() { return "forward:/index.html"; }
    @GetMapping("/online")
    public String online() { return "forward:/index.html"; }
    @GetMapping("/backup")
    public String backup() { return "forward:/index.html"; }
}
