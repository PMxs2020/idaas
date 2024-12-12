package org.iam.util;

import lombok.extern.slf4j.Slf4j;
import org.iam.exception.KeyCreateException;
import org.iam.exception.KeyFileReadException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.crypto.Cipher;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
@Slf4j
public class EncryptionUtil {
    public PrivateKey getPrivateKey(String privateKeyPath) {
        // 读取PEM文件内容
        // 使用ClassPathResource读取资源文件
        Resource resource = new ClassPathResource(privateKeyPath);
        String privateKeyContent = null;
        try {
            privateKeyContent = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            log.info("失败：" + e.getMessage());
            throw new KeyFileReadException("公钥文件读取失败");
        }
        // 提取公钥内容（保留格式化的Base64内容）
        String[] lines = privateKeyContent.split("\n");
        StringBuilder key = new StringBuilder();
        for (String line : lines) {
            if (!line.contains("BEGIN PRIVATE KEY") && !line.contains("END PRIVATE KEY")) {  // 修改这里
                key.append(line.trim());
            }
        }
        // 将Base64编码的公钥转换为PublicKey对象
        byte[] keyBytes = Base64.getDecoder().decode(key.toString());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = null;
        PrivateKey privateKey = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new KeyCreateException("私钥对象生成失败或加密算法错误");
        }
        return privateKey;
    }
    // 私钥加密
    public  String encryptWithPrivateKey(String data, PrivateKey privateKey) throws Exception {
        // 使用 SHA-256 作为哈希算法并结合 RSA 加密
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }
    // 使用公钥解密
    public  String decryptWithPublicKey(String encryptedData, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData);
    }
}
