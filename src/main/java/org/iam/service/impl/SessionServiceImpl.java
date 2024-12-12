package org.iam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.iam.exception.*;
import org.iam.mapper.UserDao;
import org.iam.pojo.domain.Application;
import org.iam.pojo.domain.User;
import org.iam.pojo.dto.SessionQueryDTO;
import org.iam.pojo.vo.ApplicationPageVO;
import org.iam.pojo.vo.SessionPageVO;
import org.iam.pojo.vo.SessionVO;
import org.iam.properties.JwtProperties;
import org.iam.service.ApplicationService;
import org.iam.service.SessionService;
import org.iam.service.UserService;
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
import java.util.stream.Collectors;

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

    /**
     *删除主会话
     * @param iamToken
     */
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

    /**
     * 条件查询系统当前会话信息
     * @param sessionQueryDTO
     * @return
     */
    @Override
    public SessionPageVO getSessionList(SessionQueryDTO sessionQueryDTO) {
        if (sessionQueryDTO.getPage() <= 0) {
            throw new IllegalArgumentException("分页查询-页数错误");
        }
        if (sessionQueryDTO.getSize() <= 0) {
            throw new IllegalArgumentException("分页查询-分页大小错误");
        }
        //查询用户名和用户姓名符合条件的用户列表
        List<User> targetUserList=null;
        if((sessionQueryDTO.getUserName()!=null&& !sessionQueryDTO.getUserName().isEmpty())||(sessionQueryDTO.getAccount()!=null&& !sessionQueryDTO.getAccount().isEmpty())){
            LambdaQueryWrapper<User> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.like(StringUtils.isNotBlank(sessionQueryDTO.getUserName()),User::getUserName,sessionQueryDTO.getUserName());
            lambdaQueryWrapper.like(StringUtils.isNotBlank(sessionQueryDTO.getAccount()),User::getAccount,sessionQueryDTO.getAccount());
            targetUserList=userDao.selectList(lambdaQueryWrapper);
        }
        Map<String,SessionVO> userMap=new HashMap<>();
        if (targetUserList != null) {
            for (User user : targetUserList) {
                userMap.put(user.getUserUuid(),new SessionVO(user.getUserName(),user.getAccount()));
            }
        }
        //获取redis中的全部iam会话key
        String prefix="iam_token:";
        Set<String> keys=redisTemplate.keys("*");
        // 遍历所有的key，筛选出满足指定前缀的key
        List<SessionVO> sessionVOList=new ArrayList<>();
        for (String key : keys) {
            if (key.startsWith(prefix)) {
               //组装会话数据
              Map<Object,Object> map=redisTemplate.opsForHash().entries(key);
              if(sessionQueryDTO.getIpAddress()!=null && !sessionQueryDTO.getIpAddress().isEmpty() &&map.get("login_ip").equals(sessionQueryDTO.getIpAddress()))
                  continue;
              if(sessionQueryDTO.getEndTime()!=null&&sessionQueryDTO.getEndTime().isBefore((LocalDateTime) map.get("login_time")))
                  continue;
              if(sessionQueryDTO.getStartTime()!=null&&sessionQueryDTO.getStartTime().isAfter((LocalDateTime) map.get("login_time")))
                  continue;
              String token=key.split(":")[1];
                sessionVOList.add(new SessionVO("","",(String)map.get("user_uuid"),token,(String)map.get("login_ip"),(LocalDateTime)map.get("login_time")));
            }
        }
        //按照登录时间降序,然后去除掉uuid不在targetUserList中的会话
        List<SessionVO> sortedSessions = sessionVOList.stream()
                .sorted((e1, e2) -> e2.getLoginTime().compareTo(e1.getLoginTime()))  // 降序排序
                .toList();
        if(targetUserList!=null){
            // 使用 Iterator 遍历并更新/删除
            Iterator<SessionVO> iterator = sortedSessions.iterator();
            while (iterator.hasNext()) {
                SessionVO sessionVO = iterator.next();
                String uuid = sessionVO.getUserUuid();

                // 如果 Map 中找不到该 uuid，移除该元素
                if (!userMap.containsKey(uuid)) {
                    iterator.remove();
                } else {
                    // 如果 Map 中找到该 uuid，更新该元素
                    SessionVO updatedEntity = userMap.get(uuid);
                    sessionVO.setAccount(updatedEntity.getAccount());  // 更新用户名
                    sessionVO.setUserName(updatedEntity.getUserName());  // 更新用户姓名
                }
            }
        }
        //计算分页参数
        long total = sortedSessions.size();
        // 计算起始和结束索引
        long startIndex = (long) (sessionQueryDTO.getPage() - 1) * sessionQueryDTO.getSize();
        long endIndex = Math.min(startIndex + sessionQueryDTO.getSize(), total);  // 防止越界
        SessionPageVO sessionPageVO = new SessionPageVO();
        sessionPageVO.setTotal(total);
        // 如果起始索引超出范围，返回空数据
        if (startIndex >= total) {
            sessionPageVO.setRecords(new ArrayList<>());
        }else{
            List<SessionVO> sessionVOPageList = sortedSessions.subList((int) startIndex, (int) endIndex);
            List<String> sessionUuids = sessionVOPageList.stream()
                    .map(SessionVO::getUserUuid) // 提取 userUuid 字段
                    .toList(); // 收集到 List 中
            LambdaQueryWrapper<User> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(User::getUserUuid, sessionUuids);
            List<User> userList = userDao.selectList(lambdaQueryWrapper);
            Map<String,SessionVO> sessionVOMap=new HashMap<>();
            if (userList != null) {
                for (User user : userList) {
                    sessionVOMap.put(user.getUserUuid(),new SessionVO(user.getUserName(),user.getAccount()));
                }
            }
            for (SessionVO sessionVO : sessionVOPageList) {
                sessionVO.setUserName(sessionVOMap.get(sessionVO.getUserUuid()).getUserName());
                sessionVO.setAccount(sessionVOMap.get(sessionVO.getUserUuid()).getAccount());
            }
            sessionPageVO.setRecords(sessionVOPageList);
        }

        return sessionPageVO;
    }
}
