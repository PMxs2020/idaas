package org.iam.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iam.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {
    @NotBlank(message = "应用名称不能为空")
    private String applyName;
    @NotNull(message = "模式ID不能为空")
    private Long modeId;
    private String applyIcon;
    private String backgroundImageUrl;
    private String backgroundVideoUrl;
    //更新时必填，添加时不必填
    @NotBlank(message = "应用ID不能为空", groups = {ValidationGroups.Update.class})
    private String applyUuid;
    @NotNull(message = "是否禁用状态不能为空")
    private Long isDisabled;
    @NotBlank(message = "重定向URL不能为空")
    private String redirectUrl;
    @NotBlank(message = "登出URL不能为空")
    private String logoutUrl;
    @NotNull(message = "应用类型不能为空")
    private List<Long> applyTypeId;
    @NotNull(message = "站点ID不能为空")
    private Long siteId;
    @NotNull(message = "token有效期不能为空")
    private Long tokenValidity;
    private String targetUrl;
    @NotBlank(message = "请求方法不能为空")
    private String requestMethod;
    @NotNull(message = "二次认证状态不能为空")
    private Long secondAuth;
    @NotNull(message = "匹配类型不能为空")
    private Long patternType;
    @NotNull(message = "是否显示系统名称不能为空")
    private Long showSystemName;
}