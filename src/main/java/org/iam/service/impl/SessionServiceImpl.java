package org.iam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.iam.constant.ApplicationConstant;
import org.iam.exception.*;
import org.iam.mapper.UserDao;
import org.iam.pojo.domain.Application;
import org.iam.pojo.domain.User;
import org.iam.properties.JwtProperties;
import org.iam.service.SessionService;
import org.iam.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SessionServiceImpl implements SessionService {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDao userDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JwtProperties jwtProperties;
    @Override
    public String createThirdPartySession(String uuid, Application application)  {
        if(uuid == null) {
            throw new IdNullException("无用户uuid");
        }
        if(application == null) {
            throw new ApplicationException("应用为空");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserUuid, uuid);
        User user=userDao.selectOne(queryWrapper);
        if(user==null){
            throw new UserNotExistException("用户不存在");
        }
        String token=null;
        String privateKeyPath = application.getPrivateKeyPkcs8();
        if (privateKeyPath.startsWith("src/main/resources/")) {
            privateKeyPath = privateKeyPath.substring("src/main/resources/".length());
        }
        // 读取PEM文件内容
        // 使用ClassPathResource读取资源文件
        Resource resource = new ClassPathResource(privateKeyPath);
        String privateKeyContent =null;
        try {
            privateKeyContent = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));
        }catch (IOException e){
            e.printStackTrace();
            log.info("失败："+e.getMessage());
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
        token=jwtUtil.generateToken(user,privateKey);
        redisTemplate.opsForValue().set(token, token, application.getTokenValidity()*2, TimeUnit.SECONDS);
        return token;
    }

    @Override
    public String createIamSession(String uuid) {
        if(uuid == null) {
            throw new IdNullException("无用户uuid");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserUuid, uuid);
        User user=userDao.selectOne(queryWrapper);
        if(user==null){
            throw new UserNotExistException("用户不存在");
        }
        String token=jwtUtil.generateIamToken(user);
        redisTemplate.opsForValue().set(token, token, jwtProperties.getTtl()*2, TimeUnit.SECONDS);
        return token;
    }

    @Override
    public String renewalSession(String iamToken) {
        if(iamToken == null) {
            throw new TokenInvalidException("token为空");
        }
        Claims claims=jwtUtil.validateIamToken(iamToken);
        //获取用户id生成新的jwt应用token
        String uuid = claims.get("uuid", String.class);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserUuid, uuid);
        User user=userDao.selectOne(queryWrapper);
        if(user==null){
            throw new UserNotExistException("用户不存在");
        }
        String token=jwtUtil.generateIamToken(user);
        redisTemplate.opsForValue().setIfAbsent(token, token, jwtProperties.getTtl(), TimeUnit.SECONDS);
        // 删除原始 token 键
        redisTemplate.delete(iamToken);
        return token;
    }
}
