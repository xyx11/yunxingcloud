package com.yunxingcloud.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IdCardValidator.class)
@Documented
public @interface IdCard {
    String message() default "身份证号格式不正确";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}