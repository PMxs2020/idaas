package org.iam.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionVO {
    private String userName;//用户姓名
    private String account;//用户名
    private String userUuid;//用户uuid
    private String userIamToken;//用户主会话token
    private String ipAddress;//ip地址
    private LocalDateTime loginTime; // 创建时间

    public SessionVO(String userName,String account){
        this.account=account;
        this.userName=userName;
    }
}
