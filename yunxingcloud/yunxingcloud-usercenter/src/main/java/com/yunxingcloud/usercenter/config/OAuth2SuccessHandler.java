package com.yunxingcloud.usercenter.config;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2SuccessHandler.class);
    private static final long TOKEN_EXPIRY = 2 * 60 * 60 * 1000; // 2 hours

    @Value("${jwt.secret:yunxingcloud-oauth2-default-secret-key-min-256-bits}")
    private String jwtSecret;

    @Value("${app.frontend-url:https://www.yunxingcloud.com}")
    private String frontendUrl;

    public OAuth2SuccessHandler() {
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication) throws IOException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attrs = oAuth2User.getAttributes();

        String username = (String) attrs.get("username");
        String nickname = (String) attrs.get("nickname");
        if (nickname == null) nickname = username;

        // Generate JWT token
        String token = Jwts.builder()
                .subject(username)
                .claim("nickname", nickname != null ? nickname : "")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRY))
                .signWith(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8),
                        "HmacSHA256"))
                .compact();

        log.info("OAuth2 login success: {} -> redirect with token", username);

        // Redirect to frontend with token
        String redirectUrl = frontendUrl + "/#/oauth2/callback?token=" + token
                + "&username=" + username
                + "&nickname=" + (nickname != null ? nickname : "");
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
