package org.iam.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApplicationVO {
    private String applyUuid;    // 应用uuid
    private String applyName;    // 应用名称
    private String createUserId;  // 创建人uuid
    private String applyIcon;      // 应用图标URL
    private List<Long> applyTypeId;   // 应用类型
    private Long status;      // 是否启用（0-禁用，1-启用）
    private LocalDateTime createTime; // 创建时间
} 