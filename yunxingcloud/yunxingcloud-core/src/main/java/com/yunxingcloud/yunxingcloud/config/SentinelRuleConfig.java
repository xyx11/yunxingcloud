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

        FlowRule loginRule = new FlowRule("loginFlow");
        loginRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        loginRule.setCount(10);
        rules.add(loginRule);

        FlowRule refreshRule = new FlowRule("refreshFlow");
        refreshRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        refreshRule.setCount(20);
        rules.add(refreshRule);

        FlowRule forgotRule = new FlowRule("passwordForgotFlow");
        forgotRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        forgotRule.setCount(5);
        rules.add(forgotRule);

        FlowRule resetRule = new FlowRule("passwordResetFlow");
        resetRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        resetRule.setCount(10);
        rules.add(resetRule);

        FlowRuleManager.loadRules(rules);
        log.info("Sentinel 流控规则已加载: {} 条", rules.size());
    }

    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        DegradeRule loginDegrade = new DegradeRule("loginFlow");
        loginDegrade.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        loginDegrade.setCount(2000);
        loginDegrade.setTimeWindow(10);
        loginDegrade.setMinRequestAmount(5);
        loginDegrade.setSlowRatioThreshold(0.2);
        rules.add(loginDegrade);

        DegradeRule forgotDegrade = new DegradeRule("passwordForgotFlow");
        forgotDegrade.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        forgotDegrade.setCount(0.3);
        forgotDegrade.setTimeWindow(30);
        forgotDegrade.setMinRequestAmount(5);
        rules.add(forgotDegrade);

        DegradeRuleManager.loadRules(rules);
        log.info("Sentinel 降级规则已加载: {} 条", rules.size());
    }
}
