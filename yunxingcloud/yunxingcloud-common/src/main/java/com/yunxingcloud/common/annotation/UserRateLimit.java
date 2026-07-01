package com.yunxingcloud.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserRateLimit {
    String key() default "";           // SpEL, e.g. "#username"
    int permits() default 10;         // 令牌数
    int window() default 60;          // 时间窗口
    TimeUnit unit() default TimeUnit.SECONDS;
    String message() default "操作过于频繁，请稍后重试";
}