package org.iam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.iam.annotation.AutoFill;
import org.iam.enumeration.OperationType;
import org.iam.pojo.domain.User;
import org.iam.mapper.UserDao;
import org.iam.pojo.dto.UserDTO;
import org.iam.pojo.dto.UserQueryDTO;
import org.iam.pojo.vo.UserPageQueryVO;
import org.iam.pojo.vo.UserVO;
import org.iam.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Override
    @AutoFill(value = OperationType.INSERT )
    public void saveUser(User user) {
        user.setUserUuid(UUID.randomUUID().toString()); // 生成唯一的 UUID
        userDao.insert(user);
    }

    @Override
    public void deleteUser(Integer id) {
        userDao.deleteById(id);
    }

    /**
     * 更新用户信息
     * @param user
     */
    @Override
    @AutoFill(value = OperationType.UPDATE )
    public void updateUser(User user) {
        userDao.updateById(user);
    }

    @Override
    public User getUserById(Integer id) {
        return userDao.selectById(id);
    }

    @Override
    public UserPageQueryVO getUserList(UserQueryDTO userQueryDTO) {
        Page<User> page = new Page<>(userQueryDTO.getPage(), userQueryDTO.getSize());
        // 添加模糊查询条件
        IPage<User> userPage = userDao.selectPage(page, new QueryWrapper<User>()
                .like(StringUtils.isNotBlank(userQueryDTO.getUserName()), "user_name", userQueryDTO.getUserName())
                .like(StringUtils.isNotBlank(userQueryDTO.getAccount()), "account", userQueryDTO.getAccount())
                .like(StringUtils.isNotBlank(userQueryDTO.getStatus()), "status", userQueryDTO.getStatus())
                .like(StringUtils.isNotBlank(userQueryDTO.getIdCard()), "id_card", userQueryDTO.getIdCard())
                .like(StringUtils.isNotBlank(userQueryDTO.getTel()), "tel", userQueryDTO.getTel())
                .eq(userQueryDTO.getDepartmentId() != null, "department_id", userQueryDTO.getDepartmentId())
                .eq(userQueryDTO.getIsDisabled() != null, "is_disabled", userQueryDTO.getIsDisabled())
        );
        List<UserVO> pageVOList = new ArrayList<>();
        for (User user : userPage.getRecords()) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            pageVOList.add(userVO);
        }
        UserPageQueryVO userPageQueryVO = new UserPageQueryVO();
        userPageQueryVO.setTotal(userPage.getTotal());
        userPageQueryVO.setRecords(pageVOList);
        return userPageQueryVO;
    }

    @Override
    public User getUserByAccount(String account) {
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, account);
        // 查询并返回用户 
        return userDao.selectOne(queryWrapper);
    }

}
