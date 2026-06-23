package com.yunxingcloud.yunxingcloud.config;

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
        initFlowRules();
        initDegradeRules();
    }

    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        add(rules, "loginFlow", 10, RuleConstant.FLOW_GRADE_QPS);
        add(rules, "refreshFlow", 20, RuleConstant.FLOW_GRADE_QPS);
        add(rules, "passwordForgotFlow", 5, RuleConstant.FLOW_GRADE_QPS);
        add(rules, "passwordResetFlow", 10, RuleConstant.FLOW_GRADE_QPS);
        add(rules, "searchFlow", 20, RuleConstant.FLOW_GRADE_QPS);      // 全局搜索
        add(rules, "fileUploadFlow", 10, RuleConstant.FLOW_GRADE_QPS);   // 文件上传
        add(rules, "exportFlow", 5, RuleConstant.FLOW_GRADE_QPS);        // 数据导出
        FlowRuleManager.loadRules(rules);
        log.info("Sentinel 流控规则已加载: {} 条", rules.size());
    }

    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();
        addDegrade(rules, "loginFlow", 2000, RuleConstant.DEGRADE_GRADE_RT, 10);
        addDegrade(rules, "passwordForgotFlow", 0.3, RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO, 30);
        addDegrade(rules, "refreshFlow", 0.2, RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO, 30);
        addDegrade(rules, "searchFlow", 1000, RuleConstant.DEGRADE_GRADE_RT, 10); // 搜索超500ms降级
        addDegrade(rules, "fileUploadFlow", 0.3, RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO, 60);
        DegradeRuleManager.loadRules(rules);
        log.info("Sentinel 降级规则已加载: {} 条", rules.size());
    }

    private void add(List<FlowRule> rules, String r, int qps, int grade) {
        FlowRule rule = new FlowRule(r); rule.setGrade(grade); rule.setCount(qps); rules.add(rule);
    }
    private void addDegrade(List<DegradeRule> rules, String r, double threshold, int grade, int window) {
        DegradeRule rule = new DegradeRule(r);
        rule.setGrade(grade); rule.setCount(threshold); rule.setTimeWindow(window); rule.setMinRequestAmount(5);
        if (grade == RuleConstant.DEGRADE_GRADE_RT) rule.setSlowRatioThreshold(0.2);
        rules.add(rule);
    }
}
