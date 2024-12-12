package org.iam.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class SessionPageVO {
    private Long total;      // 总记录数
    private List<SessionVO> records;  // 当前页数据列表
}
