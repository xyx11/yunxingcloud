package com.yunxingcloud.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IdCardValidator implements ConstraintValidator<IdCard, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isEmpty()) return true;
        if (!value.matches("\\d{17}[0-9Xx]")) return false;
        // 校验码验证
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkCodes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int sum = 0;
        for (int i = 0; i < 17; i++) sum += (value.charAt(i) - '0') * weights[i];
        return checkCodes[sum % 11] == Character.toUpperCase(value.charAt(17));
    }
}
