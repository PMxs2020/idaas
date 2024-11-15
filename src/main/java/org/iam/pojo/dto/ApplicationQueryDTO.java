package org.iam.pojo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApplicationQueryDTO {
    private String applyUuid;    // 应用uuid，支持精确查询
    private String applyName;    // 应用名称，支持模糊查询
    private String createUserId;  // 创建人uuid，支持模糊查询
    private List<Long> applyTypeId;   // 应用类型
    private Long status;      // 是否启用
    private LocalDateTime startTime; // 创建时间范围-开始
    private LocalDateTime endTime;   // 创建时间范围-结束
    private Integer pageNum = 1;     // 当前页码
    private Integer pageSize = 10;   // 每页数量
}