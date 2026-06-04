package com.yunxingcloud.yunxingcloud.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

@RestController
public class CaptchaController {

    private static final String SESSION_KEY = "CAPTCHA_CODE";
    private static final Random RANDOM = new Random();
    private static KeyPair rsaKeyPair;

    static {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(1024);
            rsaKeyPair = gen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/api/captcha")
    public ResponseEntity<Map<String, Object>> captcha(HttpSession session) {
        String code = generateCode(4);
        session.setAttribute(SESSION_KEY, code);

        String base64 = generateCaptchaImage(code);
        return ResponseEntity.ok(Map.of(
                "captchaEnabled", true,
                "img", base64
        ));
    }

    @GetMapping("/api/publicKey")
    public ResponseEntity<Map<String, String>> publicKey() {
        String publicKey = Base64.getEncoder().encodeToString(rsaKeyPair.getPublic().getEncoded());
        return ResponseEntity.ok(Map.of("publicKey", publicKey));
    }

    public static String decryptRSA(String encryptedText) {
        try {
            byte[] data = Base64.getDecoder().decode(encryptedText);
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA");
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            return new String(cipher.doFinal(data));
        } catch (Exception e) {
            return encryptedText;
        }
    }

    public static String getCaptcha(HttpSession session) {
        String code = (String) session.getAttribute(SESSION_KEY);
        if (code != null) {
            session.removeAttribute(SESSION_KEY);
        }
        return code;
    }

    private String generateCode(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        return sb.toString();
    }

    private String generateCaptchaImage(String code) {
        int w = 130, h = 48;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, w, h);

        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(20 + RANDOM.nextInt(100), 20 + RANDOM.nextInt(100), 20 + RANDOM.nextInt(100)));
            int x = 15 + i * 27 + RANDOM.nextInt(5);
            int y = 33 + RANDOM.nextInt(6);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }

        // noise lines
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(150 + RANDOM.nextInt(80), 150 + RANDOM.nextInt(80), 150 + RANDOM.nextInt(80)));
            g.drawLine(RANDOM.nextInt(w), RANDOM.nextInt(h), RANDOM.nextInt(w), RANDOM.nextInt(h));
        }

        g.dispose();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "PNG", bos);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }
}
