package org.iam.pojo.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("iam_user")
public class User {
    @TableId(type = IdType.AUTO) // 自增ID
    private Long id;

    @TableField("user_name")
    private String userName;

    @TableField("account")
    private String account;

    @JsonIgnore // json返回时永不返回密码
    @TableField("password")
    private String password;

    @TableField("password_security_strength")
    private Long passwordSecurityStrength;

    @TableField("tel")
    private String tel;

    @TableField("department_id")
    private Long departmentId;

    @TableField("last_login_time")
    private Date lastLoginTime;

    @TableField("last_login_ip")
    private Long lastLoginIp;

    @TableField("login_total")
    private Long loginTotal;

    @TableField("id_card")
    private String idCard;

    @TableField("bind_ip")
    private String bindIp;

    @TableField("create_user_id")
    private String createUserId;

    @TableField("update_user_id")
    private String updateUserId;

    @TableField("user_uuid")
    private String userUuid;

    @TableField("is_disabled")
    private Long isDisabled;

    @TableField("over_time")
    private Date overTime;

    @TableField("is_unlock")
    private Long isUnlock;

    @TableField("unlock_time")
    private Date unlockTime;

    @TableField("remarks")
    private String remarks;

    @TableField("is_change_password_first_time")
    private Integer isChangePasswordFirstTime;

    @TableField("password_update_time")
    private Date passwordUpdateTime;

    @TableField("is_white")
    private Long isWhite;

    @TableField("is_head")
    private Long isHead;

    @TableField("face_pic")
    private String facePic;

    @TableField("otp_key")
    private String otpKey;

    @TableField("otp_code")
    private String otpCode;

    @TableField("is_deleted")
    @TableLogic
    private Long isDeleted;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
