package org.iam.controller.login;

import jakarta.servlet.http.HttpServletRequest;
import org.iam.constant.LoginConstant;
import org.iam.pojo.domain.Application;
import org.iam.pojo.domain.User;
import org.iam.pojo.dto.LoginRequestDTO;
import org.iam.pojo.vo.LoginSettingVO;
import org.iam.pojo.vo.LoginSuccessVO;
import org.iam.service.SessionService;
import org.iam.service.UserService;
import org.iam.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private ApplicationService applicationService;

    @Autowired
    private SessionService sessionService;

    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequestDTO loginRequestDTO,HttpServletRequest request)  {
        // 获取请求的IP地址
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
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
        //检查用户是否有异常情况（是否锁定、是否禁用等）
        //检查用户是否拥有此应用的登录权限
        //都通过后，将token加入到redis中，将并且返回用户token
        //创建会话，返回iam_token和apply_token,iam_token由前端存储，apply_token转给第三方应用
        //新增用户会话，生成iamToken和applyToken
        String iamToken=sessionService.createIamSession(user.getUserUuid(),ip);
        String applyToken=null;
        if(applicationService.isTrirdPartyApply(application.getApplyUuid())){
            applyToken=sessionService.createThirdPartySession(iamToken,user.getUserUuid(),application);
        }
        //返回登录成功结果
        LoginSuccessVO loginSuccessVO= LoginSuccessVO.builder().
                loginType(applicationService.isTrirdPartyApply(application.getApplyUuid())?LoginConstant.THIRD_PARTY_APPLICATION:LoginConstant.INTERNAL_SYSTEM).
                iamToken(iamToken).
                applyToken(applyToken).
                redirect_url(application.getRedirectUrl()).
                target_url(application.getTargetUrl()).build();
        return Result.ok().data("login", loginSuccessVO);
    }

    /**
     * 在用户中心退出或在控制台退出
     * @param iamToken
     * @return
     */
    @PostMapping("/logout")
    public Result logout(@RequestHeader("${jwt.token-name}") String iamToken) {
        //验证token是否存在
        if (iamToken == null || iamToken.trim().isEmpty()) {
            return Result.error().message("无token传入");
        }
        sessionService.deleteIamSession(iamToken);
        return Result.ok().message("用户登出成功");
    }
    @PostMapping("/applylogout")
    public Result applyLogout(@RequestHeader("${jwt.token-name}") String iamToken,@RequestBody String applyUuid) {
        //验证token是否存在
        if (iamToken == null || iamToken.trim().isEmpty()) {
            return Result.error().message("无token传入");
        }
        //删除主会话下的属于该应用的应用会话
        sessionService.deleteThirdPartySession(iamToken,applyUuid);
        //删除主会话（后续会实现单应用退出，所以将删除应用会话与删除主会话会分离
        sessionService.deleteIamSession(iamToken);
        return Result.ok().message("用户登出成功");
    }
    /**
     * 查询应用登录信息
     */
    @GetMapping("/loginSetting")
    public Result loginSetting(String appId){
      LoginSettingVO loginSettingVO=applicationService.getLoginSetting(appId);
      return Result.ok().data("loginSetting", loginSettingVO);
    }
}
