package com.yunxingcloud.common.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class I18nServiceTest {

    private MessageSource messageSource;
    private I18nService i18nService;

    @BeforeEach
    void setUp() {
        messageSource = mock(MessageSource.class);
        i18nService = new I18nService(messageSource);
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE);
    }

    @Test void shouldResolveMessage() {
        when(messageSource.getMessage("test.key", null, "test.key", Locale.SIMPLIFIED_CHINESE))
                .thenReturn("测试消息");
        assertEquals("测试消息", i18nService.msg("test.key"));
    }

    @Test void shouldFallbackToCodeWhenNotFound() {
        when(messageSource.getMessage(eq("missing.key"), any(), eq("missing.key"), any()))
                .thenReturn("missing.key");
        assertEquals("missing.key", i18nService.msg("missing.key"));
    }

    @Test void shouldResolveMessageWithArgs() {
        when(messageSource.getMessage(eq("greeting"), any(), eq("greeting"), any()))
                .thenReturn("你好, 张三");
        assertEquals("你好, 张三", i18nService.msg("greeting", "张三"));
    }
}
