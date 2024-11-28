package org.iam.controller.identify;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.iam.constant.LoginConstant;
import org.iam.pojo.domain.Application;
import org.iam.pojo.vo.LoginSuccessVO;
import org.iam.service.ApplicationService;
import org.iam.service.SessionService;
import org.iam.util.JwtUtil;
import org.iam.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/identify")
@Slf4j
public class IdentifyController {
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private SessionService sessionService;
    @GetMapping("/{appId}")
    public Result identify(@PathVariable String appId, @RequestHeader(value = "iam_token", required = false) String iamToken) {
        //验证token是否存在
        if (iamToken == null || iamToken.trim().isEmpty()) {
            return Result.error().message("无token传入");
        }
        //验证应用是否存在
        Application application=applicationService.getApplicationById(appId);
        //验证token是否有效
        Claims claims=jwtUtil.validateIamToken(iamToken);
        // 验证token是否在Redis中存在
        String redisToken = redisTemplate.opsForValue().get(iamToken);
        if (redisToken == null) {
            return Result.error().message("无效token");
        }
        //新增第三方用户会话，生成应用token
        String applyToken=sessionService.createThirdPartySession(claims.get("uuid",String.class),application);
        //iam_token续期生成新的iamtoken
        String newIamToken=sessionService.renewalSession(iamToken);
        //返回认证成功结果
        LoginSuccessVO loginSuccessVO= LoginSuccessVO.builder().
                loginType(LoginConstant.THIRD_PARTY_APPLICATION).
                iamToken(newIamToken).
                applyToken(applyToken).
                redirect_url(application.getRedirectUrl()).
                target_url(application.getTargetUrl()).build();
        return Result.ok().data("identify",loginSuccessVO);
    }
}
