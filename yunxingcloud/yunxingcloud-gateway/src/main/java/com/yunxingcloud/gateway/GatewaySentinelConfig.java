package com.yunxingcloud.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class GatewaySentinelConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewaySentinelConfig.class);

    @PostConstruct
    public void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 认证路由: login/logout/refresh/captcha/csrf/publicKey, QPS=50
        rules.add(new GatewayFlowRule("core-auth")
                .setCount(50)
                .setIntervalSec(1));

        // 注册路由: 防止批量注册, QPS=10
        rules.add(new GatewayFlowRule("usercenter-register")
                .setCount(10)
                .setIntervalSec(1));

        // OAuth2 社交登录路由, QPS=20
        rules.add(new GatewayFlowRule("usercenter-oauth2")
                .setCount(20)
                .setIntervalSec(1));

        // 业务 API 路由, QPS=200
        rules.add(new GatewayFlowRule("core-api")
                .setCount(200)
                .setIntervalSec(1));

        // SPA 前端资源, QPS=500
        rules.add(new GatewayFlowRule("core-spa")
                .setCount(500)
                .setIntervalSec(1));

        GatewayRuleManager.loadRules(rules);
        log.info("Gateway Sentinel 流控规则已加载: {} 条", rules.size());
    }
}