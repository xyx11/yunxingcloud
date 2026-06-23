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
        // 流控规则
        List<FlowRule> flowRules = new ArrayList<>();
        addFlowRule(flowRules, "registerFlow", 5);          // /api/register QPS=5
        addFlowRule(flowRules, "userServiceFlow", 50);       // /api/users/** QPS=50
        addFlowRule(flowRules, "roleServiceFlow", 30);       // /api/roles/** QPS=30
        addFlowRule(flowRules, "deptServiceFlow", 30);       // /api/departments/** QPS=30
        FlowRuleManager.loadRules(flowRules);
        log.info("Sentinel 流控规则已加载: {} 条", flowRules.size());

        // 降级规则: 异常比例 > 0.3 时降级，恢复窗口 30s
        List<DegradeRule> degradeRules = new ArrayList<>();
        addDegradeRule(degradeRules, "registerFlow", 0.3);
        addDegradeRule(degradeRules, "userServiceFlow", 0.3);
        addDegradeRule(degradeRules, "roleServiceFlow", 0.3);
        addDegradeRule(degradeRules, "deptServiceFlow", 0.3);
        DegradeRuleManager.loadRules(degradeRules);
        log.info("Sentinel 降级规则已加载: {} 条", degradeRules.size());
    }

    private void addFlowRule(List<FlowRule> rules, String resource, int qps) {
        FlowRule rule = new FlowRule(resource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(qps);
        rules.add(rule);
    }

    private void addDegradeRule(List<DegradeRule> rules, String resource, double threshold) {
        DegradeRule rule = new DegradeRule(resource);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        rule.setCount(threshold);
        rule.setTimeWindow(30);
        rule.setMinRequestAmount(5);
        rules.add(rule);
    }
}