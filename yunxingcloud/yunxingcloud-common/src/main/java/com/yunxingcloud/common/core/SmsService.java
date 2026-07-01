package com.yunxingcloud.common.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 短信服务 — 支持阿里云短信 API (mock 模式开箱即用)
 */
@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);
    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${sms.access-key-id:}")
    private String accessKeyId;

    @Value("${sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${sms.sign-name:yunxingcloud}")
    private String signName;

    @Value("${sms.template-code:}")
    private String templateCode;

    /** 发送验证码短信 */
    public boolean sendVerifyCode(String phone, String code) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        return send(phone, templateCode, params);
    }

    /** 发送通知短信 */
    public boolean sendNotification(String phone, String templateId, Map<String, String> params) {
        return send(phone, templateId, params);
    }

    private boolean send(String phone, String templateId, Map<String, String> params) {
        if (accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
            log.info("[SMS MOCK] 发送短信到 {}: template={} params={}", phone, templateId, params);
            return true;
        }

        try {
            Map<String, String> query = new TreeMap<>();
            query.put("PhoneNumbers", phone);
            query.put("SignName", signName);
            query.put("TemplateCode", templateId);
            query.put("TemplateParam", toJson(params));
            query.put("AccessKeyId", accessKeyId);
            query.put("Action", "SendSms");
            query.put("Version", "2017-05-25");
            query.put("Timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date()));
            query.put("SignatureMethod", "HMAC-SHA1");
            query.put("SignatureVersion", "1.0");
            query.put("SignatureNonce", UUID.randomUUID().toString());

            String signature = sign(query, accessKeySecret + "&");
            query.put("Signature", signature);

            String url = "https://dysmsapi.aliyuncs.com/?" + toQueryString(query);
            var req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            var resp = client.send(req, HttpResponse.BodyHandlers.ofString());

            log.info("SMS sent to {}: {}", phone, resp.body());
            return resp.statusCode() == 200 && resp.body().contains("\"OK\"");
        } catch (Exception e) {
            log.error("SMS send failed: {}", e.getMessage());
            return false;
        }
    }

    private String sign(Map<String, String> params, String keySecret) throws Exception {
        String sortedQuery = toQueryString(params);
        String stringToSign = "GET&" + percentEncode("/") + "&" + percentEncode(sortedQuery);
        var mac = javax.crypto.Mac.getInstance("HmacSHA1");
        mac.init(new javax.crypto.spec.SecretKeySpec(keySecret.getBytes("UTF-8"), "HmacSHA1"));
        return Base64.getEncoder().encodeToString(mac.doFinal(stringToSign.getBytes("UTF-8")));
    }

    private String toQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (var entry : params.entrySet()) {
            if (!sb.isEmpty()) sb.append("&");
            sb.append(percentEncode(entry.getKey())).append("=").append(percentEncode(entry.getValue()));
        }
        return sb.toString();
    }

    private String percentEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8")
                    .replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (Exception e) { return value; }
    }

    private String toJson(Map<String, String> params) {
        StringBuilder sb = new StringBuilder("{");
        for (var entry : params.entrySet()) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }
}