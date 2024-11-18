package org.iam.controller.login;

import org.iam.pojo.domain.Application;
import org.iam.pojo.domain.User;
import org.iam.pojo.dto.LoginRequestDTO;
import org.iam.properties.JwtProperties;
import org.iam.service.UserService;
import org.iam.util.JwtUtil;
import org.iam.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.TimeUnit;
import jakarta.validation.Valid;
import org.iam.service.ApplicationService;

/**
 * 登录控制器
 * 接收全部的登录请求，登录后返回给前端token
 * 
 */
@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;   // 注入 JwtUtil

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        // 检查应用UUID是否存在
        Application application = applicationService.getApplicationById(loginRequestDTO.getAppId());
        if (application == null) {
            return Result.error().message("未查询到应用");
        }
        // 检查用户名密码是否正确
        User user =userService.getUserByAccount(loginRequestDTO.getAccount());
        if (user==null||!user.getPassword().equals(loginRequestDTO.getPassword())) {
            return Result.error().message("用户名或密码错误");
        }
        // 检查用户是否有异常情况（是否锁定、是否禁用等）
        //检查用户是否拥有此应用的登录权限
        //都通过后，将token加入到redis中，将并且返回用户token
        String token = jwtUtil.generateToken(user);
        redisTemplate.opsForValue().set(token, token, jwtProperties.getTtl()*2, TimeUnit.SECONDS);
        return Result.ok().data("iam_token", token).data("redirect_url",application.getRedirectUrl());
    }

    public Result logout(@RequestHeader("${jwt.token-name}") String iamToken) {
        //Redis清除token
        redisTemplate.delete(iamToken);
        //记录日志到数据库中
        return Result.ok().message("用户注销成功");
    }
}
