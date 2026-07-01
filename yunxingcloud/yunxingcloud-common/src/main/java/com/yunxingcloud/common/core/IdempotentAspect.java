package com.yunxingcloud.common.core;

import com.yunxingcloud.common.annotation.Idempotent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnBean(StringRedisTemplate.class)
public class IdempotentAspect {

    private static final Logger log = LoggerFactory.getLogger(IdempotentAspect.class);
    private final StringRedisTemplate redis;

    public IdempotentAspect(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Around("@annotation(com.yunxingcloud.common.annotation.Idempotent)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Idempotent idem = method.getAnnotation(Idempotent.class);

        String key = buildKey(idem, method, pjp.getArgs());
        Boolean acquired = redis.opsForValue().setIfAbsent(key, "1", idem.ttl(), idem.unit());

        if (Boolean.TRUE.equals(acquired)) {
            try {
                return pjp.proceed();
            } catch (Throwable e) {
                redis.delete(key); // 异常时释放锁，允许重试
                throw e;
            }
        } else {
            log.warn("幂等拦截: {}", key);
            throw new IllegalStateException(idem.message());
        }
    }

    private String buildKey(Idempotent idem, Method method, Object[] args) {
        if (!idem.key().isEmpty()) {
            return "idempotent:" + idem.prefix() + ":" + idem.key();
        }
        // 默认：前缀 + 类名 + 方法名 + 参数hash
        StringBuilder sb = new StringBuilder("idempotent:")
                .append(idem.prefix()).append(":")
                .append(method.getDeclaringClass().getSimpleName())
                .append(".").append(method.getName());
        for (Object arg : args) {
            if (arg != null) sb.append(":").append(arg.toString());
        }
        return sb.toString();
    }
}