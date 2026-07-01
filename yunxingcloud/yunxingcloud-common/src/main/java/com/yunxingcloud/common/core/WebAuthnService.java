package com.yunxingcloud.common.core;

import java.security.SecureRandom;
import java.util.*;

/**
 * WebAuthn/Passkey 服务 (简化实现 — 生产建议用 com.yubico:webauthn-server-core)
 * 流程: 注册(生成challenge → 验证assertion) + 认证(生成challenge → 验证assertion)
 */
public final class WebAuthnService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private WebAuthnService() {}

    /** 生成注册/认证 Challenge (Base64URL) */
    public static String generateChallenge() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** 构建 PublicKeyCredentialCreationOptions (注册) */
    public static Map<String, Object> registrationOptions(String username, String challenge,
                                                           String rpId, String rpName) {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("challenge", challenge);
        options.put("rp", Map.of("id", rpId, "name", rpName));
        options.put("user", Map.of(
                "id", Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(username.getBytes()),
                "name", username,
                "displayName", username));
        options.put("pubKeyCredParams", List.of(
                Map.of("type", "public-key", "alg", -7),    // ES256
                Map.of("type", "public-key", "alg", -257)   // RS256
        ));
        options.put("timeout", 60000);
        options.put("attestation", "none");
        options.put("authenticatorSelection", Map.of(
                "userVerification", "preferred",
                "residentKey", "preferred"));
        return options;
    }

    /** 构建 PublicKeyCredentialRequestOptions (认证) */
    public static Map<String, Object> authenticationOptions(String challenge, String rpId) {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("challenge", challenge);
        options.put("rpId", rpId);
        options.put("timeout", 60000);
        options.put("userVerification", "preferred");
        return options;
    }

    /** 验证注册响应中的 challenge 是否匹配 */
    public static boolean verifyChallenge(String expected, String actual) {
        return expected != null && expected.equals(actual);
    }

    /** 存储 Credential 信息 (rpId + credentialId + publicKey + signCount) */
    public static Map<String, Object> credentialRecord(String credentialId, String publicKey,
                                                        String userId, String rpId) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("credentialId", credentialId);
        record.put("publicKey", publicKey);
        record.put("userId", userId);
        record.put("rpId", rpId);
        record.put("signCount", 0);
        record.put("createdAt", System.currentTimeMillis());
        return record;
    }
}