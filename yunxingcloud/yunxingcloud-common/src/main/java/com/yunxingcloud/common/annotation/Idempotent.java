package com.yunxingcloud.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    String prefix() default "idempotent";
    String key() default "";            // SpEL expression, e.g. "#request.orderNo"
    long ttl() default 5;              // 幂等窗口
    TimeUnit unit() default TimeUnit.SECONDS;
    String message() default "请勿重复提交";
}