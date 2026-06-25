package com.yunxingcloud.common.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    @Test void shouldAcceptValidPassword() {
        assertTrue(PasswordValidator.isValid("Test1234!"));
    }

    @Test void shouldRejectTooShort() {
        assertFalse(PasswordValidator.isValid("Ab1!"));
    }

    @Test void shouldRejectNoUppercase() {
        assertFalse(PasswordValidator.isValid("test1234!"));
    }

    @Test void shouldRejectNoLowercase() {
        assertFalse(PasswordValidator.isValid("TEST1234!"));
    }

    @Test void shouldRejectNoDigit() {
        assertFalse(PasswordValidator.isValid("TestTest!"));
    }

    @Test void shouldRejectNoSpecialChar() {
        assertFalse(PasswordValidator.isValid("Test12345"));
    }

    @Test void shouldRejectEmpty() {
        assertFalse(PasswordValidator.isValid(""));
    }
}
