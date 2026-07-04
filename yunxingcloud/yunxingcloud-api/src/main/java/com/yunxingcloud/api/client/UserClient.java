package com.yunxingcloud.api.client;

import com.yunxingcloud.api.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "yunxingcloud-usercenter", contextId = "userClient", path = "/api", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/user")
    Map<String, Object> currentUser();

    @GetMapping("/users/by-username")
    UserDTO getByUsername(@RequestParam("username") String username);

    @GetMapping("/users/permissions")
    List<String> permissions();
}
