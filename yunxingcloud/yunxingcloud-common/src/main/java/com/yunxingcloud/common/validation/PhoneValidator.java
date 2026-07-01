package com.yunxingcloud.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<Phone, String> {
    private static final String PATTERN = "^1[3-9]\\d{9}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isEmpty()) return true; // @NotBlank handles required
        return value.matches(PATTERN);
    }
}
