package org.iam.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.iam.pojo.domain.User;

@Mapper
public interface UserDao extends BaseMapper<User> {

}
