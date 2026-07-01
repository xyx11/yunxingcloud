package com.yunxingcloud.common.core;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * TOTP 双因素认证 (RFC 6238)
 * 兼容 Google Authenticator / 1Password 等标准 TOTP 客户端
 */
public final class TotpService {

    private static final int SECRET_BYTES = 20;  // 160-bit
    private static final int DIGITS = 6;
    private static final int PERIOD = 30;        // 30s 窗口

    private TotpService() {}

    /** 生成 TOTP 密钥 (Base32 编码，可直接导入 Authenticator) */
    public static String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /** 生成 Google Authenticator URI */
    public static String generateUri(String username, String secret, String issuer) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                issuer, username, secret, issuer, DIGITS, PERIOD);
    }

    /** 校验 TOTP 码 (允许前后 1 个窗口的偏差) */
    public static boolean verify(String secret, String code) {
        if (code == null || code.length() != DIGITS) return false;
        long counter = System.currentTimeMillis() / 1000 / PERIOD;
        byte[] key = Base64.getDecoder().decode(secret);
        // 检查前/当前/后窗口
        return generateCode(key, counter - 1).equals(code)
                || generateCode(key, counter).equals(code)
                || generateCode(key, counter + 1).equals(code);
    }

    private static String generateCode(byte[] key, long counter) {
        try {
            byte[] data = new byte[8];
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (counter & 0xFF);
                counter >>= 8;
            }
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            return String.format("%0" + DIGITS + "d", binary % (int) Math.pow(10, DIGITS));
        } catch (Exception e) {
            throw new RuntimeException("TOTP 码生成失败", e);
        }
    }
}