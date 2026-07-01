package com.yunxingcloud.common.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    private PasswordValidator() {}

    public static List<String> validate(String password) {
        List<String> errors = new ArrayList<>();
        if (password == null || password.length() < MIN_LENGTH) {
            errors.add("密码长度至少" + MIN_LENGTH + "位");
        }
        if (password == null) {
            return errors;
        }
        if (!UPPER.matcher(password).find()) {
            errors.add("需包含大写字母");
        }
        if (!LOWER.matcher(password).find()) {
            errors.add("需包含小写字母");
        }
        if (!DIGIT.matcher(password).find()) {
            errors.add("需包含数字");
        }
        if (!SPECIAL.matcher(password).find()) {
            errors.add("需包含特殊字符(!@#$%等)");
        }
        return errors;
    }

    public static boolean isValid(String password) {
        return validate(password).isEmpty();
    }
}
