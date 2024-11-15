package org.iam;

import org.iam.pojo.domain.KeyAddress;
import org.iam.util.SafeKeyUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

@SpringBootTest
class IamApplicationTests {
    @Autowired
    SafeKeyUtil safeKeyUtil;
    @Test
    public void jiami() throws NoSuchAlgorithmException, IOException {
        UUID uuid=UUID.nameUUIDFromBytes("001".getBytes(StandardCharsets.UTF_8));

        KeyAddress keyAddress=safeKeyUtil.generateAppSafeKey(uuid);

        // 读取PEM文件并生成JWT token
        String jwtToken = generateTokenFromPEM(keyAddress.getPkcs1PrivatePath());
        System.out.println("Generated JWT Token: " + jwtToken);

        // 解密JWT token并打印结果
        String decodedPayload = decodeToken(jwtToken, keyAddress.getPkcs8PublicPath());
        System.out.println("Decoded JWT Payload: " + decodedPayload);
    }


    private String generateTokenFromPEM(String privateKeyPath) throws IOException {
        // 从PEM文件读取私钥
        PrivateKey privateKey = readPrivateKeyFromPEM(privateKeyPath);

        // 创建JWT token
        return Jwts.builder()
                .setSubject("user")
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

    private String decodeToken(String token, String publicKeyPath) throws IOException {
        // 从PEM文件读取公钥
        PublicKey publicKey = readPublicKeyFromPEM(publicKeyPath);

        // 解密JWT token
        Claims claims = Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    private PrivateKey readPrivateKeyFromPEM(String filePath) throws IOException {
        Security.addProvider(new BouncyCastleProvider());
        try (Reader reader = new FileReader(filePath);
             PemReader pemReader = new PemReader(reader)) {
            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(content);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IOException("Failed to read private key from PEM file", e);
        }
    }

    private PublicKey readPublicKeyFromPEM(String filePath) throws IOException {
        Security.addProvider(new BouncyCastleProvider());
        try (Reader reader = new FileReader(filePath);
             PemReader pemReader = new PemReader(reader)) {
            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(content);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new IOException("Failed to read public key from PEM file", e);
        }
    }
}
