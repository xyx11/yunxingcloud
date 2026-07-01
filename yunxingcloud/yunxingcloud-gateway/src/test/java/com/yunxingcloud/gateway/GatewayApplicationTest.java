package com.yunxingcloud.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
@ActiveProfiles("test")
@Import(GatewayApplicationTest.TestConfig.class)
class GatewayApplicationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        WebClient.Builder webClientBuilder() {
            return WebClient.builder();
        }
    }

    @Test
    void shouldLoadContext() {
    }
}