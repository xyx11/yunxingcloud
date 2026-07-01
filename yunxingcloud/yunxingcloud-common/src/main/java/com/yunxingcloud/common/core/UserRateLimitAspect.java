package com.yunxingcloud.common.core;

import com.yunxingcloud.common.annotation.UserRateLimit;
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
public class UserRateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(UserRateLimitAspect.class);
    private final StringRedisTemplate redis;

    public UserRateLimitAspect(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Around("@annotation(com.yunxingcloud.common.annotation.UserRateLimit)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        UserRateLimit limit = method.getAnnotation(UserRateLimit.class);

        String key = buildKey(limit, method, pjp.getArgs());
        String redisKey = "ratelimit:user:" + key;
        long windowSec = limit.unit().toSeconds(limit.window());

        // Token Bucket: INCR + EXPIRE
        Long count = redis.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            redis.expire(redisKey, windowSec, TimeUnit.SECONDS);
        }

        if (count != null && count > limit.permits()) {
            log.warn("用户限流触发: key={} count={}/{}/{}s", key, count, limit.permits(), windowSec);
            throw new IllegalStateException(limit.message());
        }

        return pjp.proceed();
    }

    private String buildKey(UserRateLimit limit, Method method, Object[] args) {
        if (!limit.key().isEmpty()) return limit.key();
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }
}