package com.yunxingcloud.common.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    @Test
    void validPasswordShouldPass() {
        assertTrue(PasswordValidator.isValid("Abc123!@#"));
        assertEquals(0, PasswordValidator.validate("Abc123!@#").size());
    }

    @Test
    void nullPasswordShouldFail() {
        List<String> errors = PasswordValidator.validate(null);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("长度"));
    }

    @Test
    void tooShortPasswordShouldFail() {
        assertFalse(PasswordValidator.isValid("Ab1!"));
        List<String> errors = PasswordValidator.validate("Ab1!");
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("长度")));
    }

    @Test
    void missingUpperCaseShouldFail() {
        List<String> errors = PasswordValidator.validate("abcdef1!");
        assertTrue(errors.stream().anyMatch(e -> e.contains("大写")));
    }

    @Test
    void missingLowerCaseShouldFail() {
        List<String> errors = PasswordValidator.validate("ABCDEF1!");
        assertTrue(errors.stream().anyMatch(e -> e.contains("小写")));
    }

    @Test
    void missingDigitShouldFail() {
        List<String> errors = PasswordValidator.validate("Abcdef!@");
        assertTrue(errors.stream().anyMatch(e -> e.contains("数字")));
    }

    @Test
    void missingSpecialCharShouldFail() {
        List<String> errors = PasswordValidator.validate("Abcdef12");
        assertTrue(errors.stream().anyMatch(e -> e.contains("特殊字符")));
    }

    @Test
    void allErrorsShouldBeReported() {
        List<String> errors = PasswordValidator.validate("ab");
        assertEquals(4, errors.size());
    }

    @Test
    void onlyMissingOneTypeShouldReportOneError() {
        List<String> errors = PasswordValidator.validate("Abcdef1!");
        assertEquals(0, errors.size());
    }

    @Test
    void exactlyEightCharsShouldPass() {
        assertTrue(PasswordValidator.isValid("Ab1!5678"));
    }
}