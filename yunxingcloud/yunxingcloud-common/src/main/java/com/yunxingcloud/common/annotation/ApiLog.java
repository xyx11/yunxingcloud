package com.yunxingcloud.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiLog {
    String value() default "";
    boolean logRequest() default true;
    boolean logResponse() default false;
    boolean logTime() default true;
}