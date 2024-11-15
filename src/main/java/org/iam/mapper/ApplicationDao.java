package org.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.iam.pojo.domain.Application;

import java.util.List;

@Mapper
public interface ApplicationDao extends BaseMapper<Application> {

    List<Application> batchSelectByApplyUuids(List<String> ids);

    int batchDeleteByApplyUuids(List<String> ids);
}
