package org.iam.service;

import org.iam.pojo.domain.User;
import org.iam.pojo.dto.UserDTO;
import org.iam.pojo.dto.UserQueryDTO;
import org.iam.pojo.vo.UserPageQueryVO;

public interface UserService {
    void saveUser(User user);
    void deleteUser(Integer id);
    void updateUser(User user);
    User getUserById(Integer id);
    UserPageQueryVO getUserList(UserQueryDTO userQueryDTO);
    User getUserByAccount(String account);
}
