package org.iam.pojo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    /**
     * 账户名
     */
    @NotBlank(message = "账户名不能为空")
    private String account;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 应用ID
     */
    @NotBlank(message = "应用ID不能为空")
    private String appId;
}