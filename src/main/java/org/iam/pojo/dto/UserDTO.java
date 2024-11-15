package org.iam.pojo.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String userName;

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotNull(message = "密码安全强度不能为空")
    private Long passwordSecurityStrength;

    private String tel;

    @NotNull(message = "用户必须有所属部门")
    private Long departmentId;

    @NotBlank(message = "证件号不能为空")
    private String idCard;

    private Date overTime;

    private String remarks;
}
