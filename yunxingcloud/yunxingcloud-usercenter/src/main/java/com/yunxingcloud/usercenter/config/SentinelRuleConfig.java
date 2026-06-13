package com.yunxingcloud.usercenter.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SentinelRuleConfig {

    private static final Logger log = LoggerFactory.getLogger(SentinelRuleConfig.class);

    @PostConstruct
    public void initRules() {
        // 流控规则: /api/register QPS=5, 防止批量注册
        List<FlowRule> flowRules = new ArrayList<>();
        FlowRule registerRule = new FlowRule("registerFlow");
        registerRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        registerRule.setCount(5);
        flowRules.add(registerRule);
        FlowRuleManager.loadRules(flowRules);
        log.info("Sentinel 流控规则已加载: {} 条", flowRules.size());

        // 降级规则: 异常比例 > 0.3 时降级，恢复窗口 30s
        List<DegradeRule> degradeRules = new ArrayList<>();
        DegradeRule registerDegrade = new DegradeRule("registerFlow");
        registerDegrade.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        registerDegrade.setCount(0.3);
        registerDegrade.setTimeWindow(30);
        registerDegrade.setMinRequestAmount(5);
        degradeRules.add(registerDegrade);
        DegradeRuleManager.loadRules(degradeRules);
        log.info("Sentinel 降级规则已加载: {} 条", degradeRules.size());
    }
}