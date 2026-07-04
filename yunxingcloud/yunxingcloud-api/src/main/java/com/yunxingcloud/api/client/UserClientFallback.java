package com.yunxingcloud.api.client;

import com.yunxingcloud.api.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class UserClientFallback implements UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClientFallback.class);

    @Override
    public Map<String, Object> currentUser() {
        log.warn("UserClient.currentUser() fallback");
        return Collections.emptyMap();
    }

    @Override
    public UserDTO getByUsername(String username) {
        log.warn("UserClient.getByUsername({}) fallback", username);
        return null;
    }

    @Override
    public List<String> permissions() {
        log.warn("UserClient.permissions() fallback");
        return Collections.emptyList();
    }
}
