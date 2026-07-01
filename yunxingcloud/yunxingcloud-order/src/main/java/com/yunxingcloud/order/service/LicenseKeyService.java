package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.LicenseKey;
import com.yunxingcloud.order.repository.LicenseKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
public class LicenseKeyService {

    private final LicenseKeyRepository repo;

    public LicenseKeyService(LicenseKeyRepository repo) { this.repo = repo; }

    /** 批量生成激活码 */
    public List<LicenseKey> generate(Long productId, int count, Long orderId) {
        List<LicenseKey> keys = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LicenseKey k = new LicenseKey();
            k.setProductId(productId);
            k.setOrderId(orderId);
            k.setKeyCode(generateKey());
            k.setStatus("0");
            keys.add(repo.save(k));
        }
        return keys;
    }

    /** 激活 */
    @Transactional
    public LicenseKey activate(String keyCode, String username) {
        LicenseKey k = repo.findByKeyCode(keyCode)
                .orElseThrow(() -> new IllegalArgumentException("激活码不存在"));
        if (!"0".equals(k.getStatus())) throw new IllegalStateException("激活码已使用或已过期");
        if (k.getExpireAt() != null && k.getExpireAt().isBefore(java.time.LocalDateTime.now()))
            throw new IllegalStateException("激活码已过期");
        k.setStatus("1"); k.setActivatedBy(username);
        k.setActivateAt(java.time.LocalDateTime.now());
        return repo.save(k);
    }

    private String generateKey() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        SecureRandom r = new SecureRandom();
        for (int i = 0; i < 4; i++) {
            if (i > 0) sb.append('-');
            for (int j = 0; j < 4; j++) sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }
}