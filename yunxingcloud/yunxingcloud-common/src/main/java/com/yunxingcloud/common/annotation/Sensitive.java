package com.yunxingcloud.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {
    Type value() default Type.AUTO;

    enum Type { PHONE, EMAIL, ID_CARD, NAME, ADDRESS, AUTO }
}