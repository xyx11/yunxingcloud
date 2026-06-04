package com.yunxingcloud.usercenter.config;

import com.yunxingcloud.usercenter.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SocialOAuth2Config {

    private final CustomOAuth2UserService customOAuth2UserService;

    public SocialOAuth2Config(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    @Order(0)
    public SecurityFilterChain socialLoginFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/oauth2/**", "/login/oauth2/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService))
                .defaultSuccessUrl("/", true)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")));

        return http.build();
    }
}
