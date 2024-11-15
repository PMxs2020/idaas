package org.iam.pojo.dto;
import lombok.Data;

@Data
public class UserQueryDTO {
    private String userName; // 模糊检索用户姓名
    private String account; // 模糊检索用户名
    private String status; // 模糊检索状态
    private String idCard; // 模糊检索证件号
    private String tel; // 模糊检索手机号
    private Long departmentId; // 精确检索所属部门
    private Long isDisabled; // 精确检索用户状态
    private int page=1; // 页码
    private int size=10; // 每页大小
}
