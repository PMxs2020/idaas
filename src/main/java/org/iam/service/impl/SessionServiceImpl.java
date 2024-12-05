package org.iam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.iam.exception.*;
import org.iam.mapper.UserDao;
import org.iam.pojo.domain.Application;
import org.iam.pojo.domain.User;
import org.iam.properties.JwtProperties;
import org.iam.service.ApplicationService;
import org.iam.service.SessionService;
import org.iam.util.EncryptionUtil;
import org.iam.util.HttpRequestUtil;
import org.iam.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.util.*;
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
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private HttpRequestUtil httpRequestUtil;
    @Autowired
    private EncryptionUtil encryptionUtil;
    @Override
    public String createThirdPartySession(String iamToken,String uuid, Application application)  {
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
        String privateKeyPath = application.getPrivateKeyPkcs8();
        if (privateKeyPath.startsWith("src/main/resources/")) {
            privateKeyPath = privateKeyPath.substring("src/main/resources/".length());
        }
        PrivateKey privateKey = encryptionUtil.getPrivateKey(privateKeyPath);
        String applyToken=jwtUtil.generateToken(user,privateKey);
        //向主会话注册应用
        // 验证token是否在Redis中存在
        if(Boolean.FALSE.equals(redisTemplate.hasKey("iam_token:" + iamToken))){
            throw new SessionNotExistException("主会话不存在");
        }
        redisTemplate.opsForHash().put("iam_token:" + iamToken,"apply_uuid:"+application.getApplyUuid()+":"+applyToken, applyToken);
        //创建应用会话
        redisTemplate.opsForValue().set("apply_token:"+applyToken, iamToken, application.getTokenValidity()*2, TimeUnit.SECONDS);
        return applyToken;
    }



    @Override
    public String createIamSession(String uuid,String ip) {
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
        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("user_uuid", uuid);
        sessionMap.put("login_time", LocalDateTime.now());
        sessionMap.put("login_ip", ip);
        // 创建hash类型的会话键值对
        redisTemplate.opsForHash().putAll("iam_token:"+token, sessionMap);
        // 设置过期时间，例如设置为 60 秒
        redisTemplate.expire("iam_token:"+token, jwtProperties.getTtl()*2, TimeUnit.SECONDS);
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
        Map<Object, Object> tokenData = redisTemplate.opsForHash().entries("iam_token:" + iamToken);
        redisTemplate.opsForHash().putAll("iam_token:" + token, tokenData);
        // 设置过期时间
        redisTemplate.expire("iam_token:"+token, jwtProperties.getTtl()*2, TimeUnit.SECONDS);
        // 删除原始 token 键
        redisTemplate.delete("iam_token:"+iamToken);
        return token;
    }

    @Override
    public void deleteIamSession(String iamToken) {
        //验证token是否在Redis中存在
        if(Boolean.FALSE.equals(redisTemplate.hasKey("iam_token:" + iamToken))){
           throw new TokenInvalidException("无效token");
        }
        //清除主会话下的第三方应用session
        Map<Object,Object> iamSessionData = redisTemplate.opsForHash().entries("iam_token:" + iamToken);
        Map<String, Set<String>> applyTokens=new HashMap<>();
        // 遍历 iamSessionData
        for (Map.Entry<Object, Object> entry : iamSessionData.entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith("apply_uuid:")) { // 判断key是否以 "apply_uuid:" 开头
                // 提取UUID部分
                String[] parts = key.split(":");
                if (parts.length > 1) {
                    String applyUuid = parts[1]; // 提取 "apply_uuid:" 后面的 UUID 部分
                    // 获取 applyTokens 中对应 UUID 的 Set，并将该 value 加入该 Set
                    applyTokens.computeIfAbsent(applyUuid, k -> new HashSet<>()).add(entry.getValue().toString());
                }
            }
        }
        deleteThirdPartySession(iamToken,applyTokens);
        //删除主会话token
        redisTemplate.delete("iam_token:"+iamToken);
    }

    /**
     * 批量删除一个主会话下的第三方会话
     * @param applyTokens
     */
    public void deleteThirdPartySession(String iamToken,Map<String, Set<String>> applyTokens) {
        for (Map.Entry<String, Set<String>> entry : applyTokens.entrySet()) {
            String appUuid = entry.getKey();
            Set<String> tokens = entry.getValue();
            Application application = applicationService.getApplicationById(appUuid);
            String logoutUrl = application.getLogoutUrl();
            for (String token : tokens) {
                httpRequestUtil.sendHttpRequestAsync(logoutUrl, token);
                redisTemplate.delete("apply_token:" + token);
                redisTemplate.opsForHash().delete("iam_token:" + iamToken,"apply_token:" +appUuid+":"+ token);
            }
        }
    }

    /**
     * 删除住会话下的单个应用会话
     * @param iamToken
     * @param applyUuid
     */
    public void deleteThirdPartySession(String iamToken,String applyUuid) {
        if (applyUuid == null || applyUuid.isEmpty()) {
            throw new ApplicationNotExistException("应用uuid为空");
        }
        Application application = applicationService.getApplicationById(applyUuid);
        if (application == null) {
            throw new ApplicationNotExistException("应用不存在");
        }
        if(Boolean.FALSE.equals(redisTemplate.hasKey("iam_token:" + iamToken))){
            throw new TokenInvalidException("无效token");
        }
        Map<Object,Object> iamSessionData = redisTemplate.opsForHash().entries("iam_token:" + iamToken);
        // 遍历 iamSessionData
        for (Map.Entry<Object, Object> entry : iamSessionData.entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith("apply_uuid:" + applyUuid)) {
                // 提取UUID部分
                String[] parts = key.split(":");
                if (parts.length > 1) {
                    String applyToken = parts[2];
                    redisTemplate.delete("apply_token:" + applyToken);
                    redisTemplate.opsForHash().delete("iam_token:" + iamToken,"apply_uuid:"+applyUuid +":"+applyToken);
                }
            }
        }
    }
}
