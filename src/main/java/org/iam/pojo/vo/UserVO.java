package org.iam.pojo.vo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private String userName;     // 用户名
    private String account;      // 账号
    private String idCard;       // 证件号
    private String tel;          // 手机号
    private Long departmentId; // 所属部门
    private Long isDisabled;   // 锁定状态
}
