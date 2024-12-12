package org.iam.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionQueryDTO {
    private String userName;//用户姓名
    private String account;//用户名
    private String ipAddress;//ip地址
    private LocalDateTime startTime; // 登录时间范围-开始
    private LocalDateTime endTime;   // 登录时间范围-结束
    private int page=1; // 页码
    private int size=10; // 每页大小
}
