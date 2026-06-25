package com.yunxingcloud.usercenter.service;

import com.yunxingcloud.usercenter.entity.SocialAccount;
import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.repository.SocialAccountRepository;
import com.yunxingcloud.usercenter.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository,
                                    SocialAccountRepository socialAccountRepository,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String providerUserId = extractProviderUserId(provider, attributes);
        String nickname = extractNickname(provider, attributes);
        String avatarUrl = extractAvatarUrl(provider, attributes);

        User user;
        Optional<SocialAccount> existingAccount = socialAccountRepository
                .findByProviderAndProviderUserId(provider, providerUserId);

        if (existingAccount.isPresent()) {
            SocialAccount account = existingAccount.get();
            account.setNickname(nickname);
            account.setAvatarUrl(avatarUrl);
            socialAccountRepository.save(account);

            user = userRepository.findById(account.getUserId())
                    .orElseThrow(() -> new OAuth2AuthenticationException("关联用户不存在"));
            user.setNickname(nickname);
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
        } else {
            user = new User();
            user.setUsername(generateUsername(provider, providerUserId, nickname));
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setNickname(nickname);
            user.setAvatarUrl(avatarUrl);
            user.setRegisterSource(provider);
            user.setEnabled(true);
            user = userRepository.save(user);

            SocialAccount account = new SocialAccount(user.getId(), provider, providerUserId);
            account.setNickname(nickname);
            account.setAvatarUrl(avatarUrl);
            socialAccountRepository.save(account);
        }

        return new DefaultOAuth2User(
                Collections.emptyList(),
                Map.of("id", user.getId(), "username", user.getUsername(),
                       "nickname", user.getNickname(), "avatarUrl", user.getAvatarUrl()),
                "username"
        );
    }

    private String extractProviderUserId(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "wechat", "qq" -> (String) attributes.get("openid");
            case "alipay" -> (String) attributes.get("user_id");
            default -> {
                Object sub = attributes.get("sub");
                yield sub != null ? sub.toString() : "";
            }
        };
    }

    private String extractNickname(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "wechat" -> {
                String nick = (String) attributes.get("nickname");
                yield nick != null ? nick : "";
            }
            case "qq" -> {
                String nick = (String) attributes.get("nickname");
                yield nick != null ? nick : "";
            }
            case "alipay" -> {
                String nick = (String) attributes.get("nick_name");
                yield nick != null ? nick : "";
            }
            default -> {
                String name = (String) attributes.get("name");
                yield name != null ? name : "";
            }
        };
    }

    private String extractAvatarUrl(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "wechat" -> {
                String url = (String) attributes.get("headimgurl");
                yield url != null ? url : "";
            }
            case "qq" -> {
                String url = (String) attributes.get("figureurl_qq_2");
                if (url == null) url = (String) attributes.get("figureurl_qq_1");
                yield url != null ? url : "";
            }
            case "alipay" -> {
                String url = (String) attributes.get("avatar");
                yield url != null ? url : "";
            }
            default -> {
                String url = (String) attributes.get("picture");
                yield url != null ? url : "";
            }
        };
    }

    private String generateUsername(String provider, String providerUserId, String nickname) {
        if (nickname != null && !nickname.isBlank() && !userRepository.existsByUsername(nickname)) {
            return nickname;
        }
        String username = provider + "_" + providerUserId;
        if (username.length() > 50) {
            username = username.substring(0, 50);
        }
        if (!userRepository.existsByUsername(username)) {
            return username;
        }
        return username + "_" + System.nanoTime() % 10000;
    }
}
