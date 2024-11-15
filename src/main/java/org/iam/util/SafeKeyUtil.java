package org.iam.util;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.util.io.pem.PemObject;
import org.iam.exception.ApplicationDirExistException;
import org.iam.exception.ApplicationException;
import org.iam.pojo.domain.KeyAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.UUID;

@Component
public class SafeKeyUtil {

    private static final Logger log = LoggerFactory.getLogger(SafeKeyUtil.class);

    public  KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        // 创建 KeyPairGenerator 对象，指定算法为 RSA
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥长度为 2048 位
        keyGen.initialize(2048);
        // 生成密钥对
        return keyGen.generateKeyPair();
    }

    public  KeyAddress generateAppSafeKey(UUID applicationUuid) throws ApplicationException {
        try {
            //创建应用的key文件夹
            File appKeyDir = new File("src/main/resources/keys/", applicationUuid.toString());
            if (!appKeyDir.exists()) {
                appKeyDir.mkdirs();
                File appKeyDirPkcs1 = new File(appKeyDir, "pkcs1");
                File appKeyDirPkcs8 = new File(appKeyDir, "pkcs8");
                appKeyDirPkcs1.mkdirs();
                appKeyDirPkcs8.mkdirs();
            } else {
                throw new ApplicationDirExistException("应用的密钥文件夹已存在,创建失败");
            }

            //生成KeyPair和秘钥地址
            KeyPair keyPair = generateKeyPair();
            KeyAddress appKeyAddress = new KeyAddress(applicationUuid);

            //生成秘钥文件并写入文件
            writePkcs1PEMfile(keyPair, appKeyAddress.getPkcs1PrivatePath(), appKeyAddress.getPkcs1PublicPath());
            writePkcs8PEMfile(keyPair, appKeyAddress.getPkcs8PrivatePath(), appKeyAddress.getPkcs8PublicPath());

            return appKeyAddress;

        } catch (IOException | NoSuchAlgorithmException e) {
            // 记录详细错误日志
            log.error("生成应用密钥失败, applicationUuid: {}, 错误信息: {}", applicationUuid, e.getMessage(), e);
            
            // 删除已创建的文件夹（清理）
            cleanupKeyDirectory(applicationUuid);
            
            // 转换为应用异常并抛出
            throw new ApplicationException("生成应用密钥文件失败: " );
        }
    }

    // 清理密钥目录的辅助方法
    private void cleanupKeyDirectory(UUID applicationUuid) {
        try {
            File appKeyDir = new File("src/main/resources/keys/", applicationUuid.toString());
            if (appKeyDir.exists()) {
                // 递归删除目录及其内容
                deleteDirectory(appKeyDir);
            }
        } catch (Exception e) {
            log.error("清理密钥目录失败, applicationUuid: {}, 错误信息: {}", applicationUuid, e.getMessage(), e);
        }
    }

    // 递归删除目录及其内容
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    private  void writePkcs8PEMfile(KeyPair keyPair, String pkcs8PrivatePath, String pkcs8PublicPath) throws IOException {
        PrivateKey privateKey=keyPair.getPrivate();
        PublicKey publicKey=keyPair.getPublic();
        // 写入PKCS8格式的私钥
        try (Writer writer = new FileWriter(pkcs8PrivatePath);
             JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
            JcaPKCS8Generator pkcs8Generator = new JcaPKCS8Generator(privateKey, null);
            pemWriter.writeObject(pkcs8Generator.generate());
        }

        // 写入PKCS8格式的公钥
        try (Writer writer = new FileWriter(pkcs8PublicPath);
             JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
            pemWriter.writeObject(publicKey);
        }
    }

    private  void writePkcs1PEMfile(KeyPair keyPair, String pkcs1PrivatePath, String pkcs1PublicPath) throws IOException {
       PrivateKey privateKey=keyPair.getPrivate();
       PublicKey publicKey=keyPair.getPublic();
        // 写入PKCS1格式的私钥
        try (Writer writer = new FileWriter(pkcs1PrivatePath);
             JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
            RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey) privateKey;
            // 创建ASN.1格式的RSA私钥
            org.bouncycastle.asn1.pkcs.RSAPrivateKey rsaPrivateKeyStruct = new org.bouncycastle.asn1.pkcs.RSAPrivateKey(
                    rsaPrivateKey.getModulus(),
                    rsaPrivateKey.getPublicExponent(),
                    rsaPrivateKey.getPrivateExponent(),
                    rsaPrivateKey.getPrimeP(),
                    rsaPrivateKey.getPrimeQ(),
                    rsaPrivateKey.getPrimeExponentP(),
                    rsaPrivateKey.getPrimeExponentQ(),
                    rsaPrivateKey.getCrtCoefficient());
            // 将ASN.1格式的私钥转换为PEM对象
            PemObject pemObject = new PemObject("RSA PRIVATE KEY", rsaPrivateKeyStruct.getEncoded());
            pemWriter.writeObject(pemObject);
        }
        // 写入PKCS1格式的公钥
        try (Writer writer = new FileWriter(pkcs1PublicPath);
             JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
            SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
            pemWriter.writeObject(spki);
        }
    }
}
