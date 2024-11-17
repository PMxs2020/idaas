package org.iam.pojo.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用管理实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("iam_apply")
public class Application {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * uuid 应用id
     */
    @TableField("apply_uuid")
    private String applyUuid;
    
    /**
     * 应用名称
     */
    @TableField("apply_name")
    private String applyName;
    
    /**
     * pkc1格式令牌私钥文件地址
     */
    @TableField("private_key_pkcs1")
    private String privateKeyPkcs1;
    
    /**
     * pkc1格式令牌公钥文件地址
     */
    @TableField("public_key_pkcs1")
    private String publicKeyPkcs1;
    
    /**
     * pkc8格式令牌私钥文件地址
     */
    @TableField("private_key_pkcs8")
    private String privateKeyPkcs8;
    
    /**
     * pkc8格式令牌公钥文件地址
     */
    @TableField("public_key_pkcs8")
    private String publicKeyPkcs8;
    
    /**
     * 对接协议 1 JWT, 2 Oath2.0,3插件代填
     */
    @TableField("mode_id")
    private Long modeId;
    
    /**
     * 应用图标访问地址
     */
    @TableField("apply_icon")
    private String applyIcon;
    
    /**
     * 应用登录页面的背景图片地址
     */
    @TableField("background_image_url")
    private String backgroundImageUrl;
    
    /**
     * 应用登录页面的背景视频地址
     */
    @TableField("background_video_url")
    private String backgroundVideoUrl;
    
    /**
     * 是否禁用，1已禁用，0正常
     */
    @TableField("is_disabled")
    private Long isDisabled;
    
    /**
     * 应用的退出登录接口
     */
    @TableField("logout_url")
    private String logoutUrl;
    
    /**
     * 应用的单点登录地址
     */
    @TableField("redirect_url")
    private String redirectUrl;
    
    /**
     * 应用类型，1web应用，2移动应用
     */
    @TableField("apply_type_id")
    private String applyTypeId;

    
    /**
     * 登录框位子，1左，2中，3右
     */
    @TableField("site_id")
    private Long siteId;
    
    /**
     * token有效期，单��秒，默认600秒
     */
    @TableField("token_validity")
    private Long tokenValidity;
    
    /**
     * 单点登录后回调地址
     */
    @TableField("target_url")
    private String targetUrl;
    
    /**
     * 应用app_key
     */
    @TableField("app_key")
    private String appKey;
    
    /**
     * 应用app_secret
     */
    @TableField("app_secret")
    private String appSecret;
    
    /**
     * 单点登录请求方式
     */
    @TableField("request_method")
    private String requestMethod;
    
    /**
     * 创建人 iam_user表id
     */
    @TableField("create_user_id")
    private String createUserId;


    /**
     * 最后更新人 iam_user表id
     */
    @TableField("update_user_id")
    private String updateUserId;


    /**
     * 是否显示系统名称 1 显示 0不显示 默认1 显示
     */
    @TableField("show_system_name")
    private Long showSystemName;
    
    /**
     * 用户名输入框
     */
    @TableField("username_input_box")
    private String usernameInputBox;
    
    /**
     * 密码输入框
     */
    @TableField("password_input_box")
    private String passwordInputBox;
    
    /**
     * 登录按钮
     */
    @TableField("submit_box")
    private String submitBox;
    
    /**
     * 是否二次认证，1是，0否
     */
    @TableField("second_auth")
    private Long secondAuth;
    
    /**
     * 对接模式，1标准模式，2接口后置
     */
    @TableField("pattern_type")
    private Long patternType;
    
    /**
     * 是否删除，1已删除，0正常
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Long isDeleted;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
} 