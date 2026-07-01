package com.yunxingcloud.common.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErrorPageConfig {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> errorPages() {
        return factory -> factory.addErrorPages(
                new ErrorPage(org.springframework.http.HttpStatus.NOT_FOUND, "/index.html"),
                new ErrorPage(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "/index.html"));
    }
}