package org.iam.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginSuccessVO {
    private String loginType;
    private String iamToken;
    private String applyToken;
    private String redirect_url;
    private String target_url;
}
