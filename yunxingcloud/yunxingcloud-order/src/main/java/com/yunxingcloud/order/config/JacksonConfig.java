package com.yunxingcloud.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    private static final Logger log = LoggerFactory.getLogger(JacksonConfig.class);

    @Autowired(required = false)
    private ObjectMapper jackson2Mapper;

    @PostConstruct
    public void registerModule() {
        if (jackson2Mapper != null) {
            jackson2Mapper.registerModule(new JavaTimeModule());
            log.info("Registered JavaTimeModule on Jackson2 ObjectMapper");
        }
    }
}
