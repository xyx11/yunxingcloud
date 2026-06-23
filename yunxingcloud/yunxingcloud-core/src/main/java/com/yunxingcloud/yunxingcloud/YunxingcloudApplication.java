package com.yunxingcloud.yunxingcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@ComponentScan(basePackages = {"com.yunxingcloud.yunxingcloud", "com.yunxingcloud.common"})
@EnableScheduling
@EnableAsync
@EnableCaching
@EnableMethodSecurity
@EnableFeignClients(basePackages = "com.yunxingcloud.api.client")
public class YunxingcloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(YunxingcloudApplication.class, args);
    }

}
