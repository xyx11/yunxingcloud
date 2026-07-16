package com.yunxingcloud.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
public class ElasticsearchConfig {

    @Configuration
    @ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
    @EnableElasticsearchRepositories(basePackages = "com.yunxingcloud.order.repository.es")
    static class ElasticsearchRepositoryConfig {
    }

    @Configuration
    @ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
    static class ElasticsearchClientConfig extends ElasticsearchConfiguration {
        @Value("${spring.elasticsearch.uris:localhost:9200}")
        private String esUris;

        @Override
        public ClientConfiguration clientConfiguration() {
            return ClientConfiguration.builder()
                    .connectedTo(esUris)
                    .build();
        }
    }
}
