package com.yunxingcloud.common.annotation;

import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.common.enums.OperatorType;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    String title() default "";
    BusinessType businessType() default BusinessType.OTHER;
    OperatorType operatorType() default OperatorType.MANAGE;
    boolean saveRequestData() default true;
    boolean saveResponseData() default true;
}
