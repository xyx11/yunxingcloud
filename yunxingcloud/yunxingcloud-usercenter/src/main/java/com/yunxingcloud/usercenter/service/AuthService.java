package com.yunxingcloud.usercenter.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private static final String SAVED_REQUEST_ATTR = "SPRING_SECURITY_SAVED_REQUEST";

    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public Map<String, Object> login(String username, String password,
                                      HttpServletRequest request, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authenticationManager.authenticate(token);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        String redirectUrl = getSavedRequestUrl(request);
        return Map.of(
                "success", true,
                "username", auth.getName(),
                "redirectUrl", redirectUrl != null ? redirectUrl : "/"
        );
    }

    private String getSavedRequestUrl(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            SavedRequest savedRequest = (SavedRequest) session.getAttribute(SAVED_REQUEST_ATTR);
            if (savedRequest != null) {
                return savedRequest.getRedirectUrl();
            }
        }
        return null;
    }
}
