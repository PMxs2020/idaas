package org.iam.util;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Component
public class AppKeyGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int SECRET_LENGTH = 16;

    /**
     * 生成AppKey和AppSecret
     */
    public KeyPair generateKeyPair() {
        String appKey = generateAppKey();
        String appSecret = generateAppSecret();
        return new KeyPair(appKey, appSecret);
    }

    /**
     * 生成AppKey
     * 使用UUID去除横线
     */
    private String generateAppKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成AppSecret
     * 使用SecureRandom生成随机字节，然后Base64编码
     */
    private String generateAppSecret() {
        byte[] randomBytes = new byte[SECRET_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Data
    @AllArgsConstructor
    public static class KeyPair {
        private String appKey;
        private String appSecret;
    }
}