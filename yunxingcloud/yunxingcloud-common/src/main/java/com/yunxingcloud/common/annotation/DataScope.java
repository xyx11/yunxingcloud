package com.yunxingcloud.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    String deptAlias() default "d";
    String userAlias() default "u";
    String deptField() default "dept_id";
    boolean filterDept() default true;
    boolean filterUser() default false;
}