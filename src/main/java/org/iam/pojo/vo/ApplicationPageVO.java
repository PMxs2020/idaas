package org.iam.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApplicationPageVO {
    private Long total;      // 总记录数
    private List<ApplicationVO> records;  // 当前页数据列表
}

