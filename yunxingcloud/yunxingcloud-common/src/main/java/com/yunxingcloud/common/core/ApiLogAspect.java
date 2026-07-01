package com.yunxingcloud.common.core;

import com.yunxingcloud.common.annotation.ApiLog;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class ApiLogAspect {

    private static final Logger log = LoggerFactory.getLogger("API_LOG");

    @Around("@annotation(com.yunxingcloud.common.annotation.ApiLog)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        ApiLog ann = method.getAnnotation(ApiLog.class);
        long start = System.currentTimeMillis();

        HttpServletRequest req = null;
        try {
            req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (Exception ignored) {}

        String methodName = ann.value().isEmpty() ? method.getName() : ann.value();

        // 记录请求入参
        if (ann.logRequest() && log.isInfoEnabled()) {
            String params = pjp.getArgs().length > 0 ? toStr(pjp.getArgs()) : "-";
            log.info("[{}] {} {} | ip={} | params={}",
                    methodName, req != null ? req.getMethod() : "-",
                    req != null ? req.getRequestURI() : "-",
                    req != null ? getIp(req) : "-", params);
        }

        Object result;
        try {
            result = pjp.proceed();
        } catch (Throwable e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[{}] FAILED in {}ms | error={}", methodName, elapsed, e.getMessage());
            throw e;
        }

        long elapsed = System.currentTimeMillis() - start;
        if (ann.logTime() && log.isInfoEnabled()) {
            String resp = ann.logResponse() ? toStr(result) : "-";
            log.info("[{}] OK in {}ms | resp={}", methodName, elapsed, resp);
        }

        return result;
    }

    private String toStr(Object obj) {
        if (obj == null) return "null";
        if (obj.getClass().isArray()) return Arrays.toString((Object[]) obj);
        return obj.toString();
    }

    private String getIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = req.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = req.getRemoteAddr();
        return ip;
    }
}