package com.yunxingcloud.yunxingcloud;

import com.yunxingcloud.yunxingcloud.config.JwtTokenService;
import com.yunxingcloud.yunxingcloud.config.FeatureFlags;
import com.yunxingcloud.yunxingcloud.controller.*;
import com.yunxingcloud.yunxingcloud.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.flyway.enabled=false"})
class YunxingcloudApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void controllersAreLoaded() {
        assertThat(context.getBean(UserController.class)).isNotNull();
        assertThat(context.getBean(MenuController.class)).isNotNull();
        assertThat(context.getBean(ConfigController.class)).isNotNull();
    }

    @Test
    void servicesAreLoaded() {
        assertThat(context.getBean(JwtTokenService.class)).isNotNull();
        assertThat(context.getBean(FeatureFlags.class)).isNotNull();
    }

    @Test
    void repositoriesAreLoaded() {
        assertThat(context.getBean(UserRepository.class)).isNotNull();
        assertThat(context.getBean(SysMenuRepository.class)).isNotNull();
        assertThat(context.getBean(SysOperLogRepository.class)).isNotNull();
    }
}
