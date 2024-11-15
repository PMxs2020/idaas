package org.iam.pojo.dto;

import jakarta.validation.Validation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iam.validation.ValidationGroups;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {
    @NotBlank
    private String applyName;
    @NotNull
    private Long modeId;
    private String applyIcon;
    private String backgroundImageUrl;
    private String backgroundVideoUrl;
    //更新时必填，添加时不必填
    @NotBlank(message = "应用ID不能为空", groups = {ValidationGroups.Update.class})
    private String applyUuid;
    @NotNull
    private Long isDisabled;
    @NotBlank
    private String redirectUrl;
    @NotBlank
    private String logoutUrl;
    @NotNull
    private List<Long> applyTypeId;
    @NotNull
    private Long siteId;
    @NotNull
    private Long tokenValidity;
    private String targetUrl;
    @NotBlank
    private String requestMethod;
    @NotNull
    private Long secondAuth;
    @NotNull
    private Long patternType;
    @NotNull
    private Long showSystemName;
}