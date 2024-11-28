package org.iam.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginSettingVO {
    private String applyUuid;
    private String applyName;
    private String applyIcon;
    private String backgroundImageUrl;
    private String backgroundVideoUrl;
    private Long showSystemName;
    private String usernameInputBox;
    private String passwordInputBox;
    private String submitBox;
    private Long secondAuth;
    private String patterType;
}
