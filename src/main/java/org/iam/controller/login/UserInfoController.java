package org.iam.controller.login;

import org.iam.util.JwtUtil;
import org.iam.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/info")
public class UserInfoController {
    @Autowired
    private JwtUtil jwtUtil;
    @GetMapping
    public Result currentUserInfo(@RequestHeader(value = "${jwt.token-name}", required = false) String token){
        if (token == null ) {
            return Result.error().message("无token");
        }
        String account = jwtUtil.getUsernameFromToken(token);
        return Result.ok().data("account",account);
    }
}
