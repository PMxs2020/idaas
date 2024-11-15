package org.iam.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPageQueryVO {
    //符合条件的用户总数
    private Long total;
    //查询结果集
    private List<UserVO> records;
}
