package org.iam.controller.user;

import jakarta.validation.Valid;
import org.iam.pojo.domain.User;
import org.iam.pojo.dto.UserDTO;
import org.iam.pojo.dto.UserQueryDTO;
import org.iam.pojo.vo.UserPageQueryVO;
import org.iam.service.UserService;
import org.iam.util.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/console/user")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping
    public Result addUser(@RequestBody @Valid UserDTO userDTO) {
        User user = new User();
        // 将 UserDTO 的属性赋值给 User
        BeanUtils.copyProperties(userDTO, user);
        userService.saveUser(user);
        return Result.ok().message("新增用户成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return Result.ok().message("删除用户成功");
    }

    @GetMapping("/{id}")
    public Result getUser(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        return Result.ok().data("user", user);
    }
    @PutMapping
    public Result saveUser(@RequestBody @Valid UserDTO userDTO) {
        User user = new User();
        // 将 UserDTO 的属性赋值给 User
        BeanUtils.copyProperties(userDTO, user);
        userService.updateUser(user);
        return Result.ok().message("更新用户成功");
    }

    @GetMapping
    public Result userList(UserQueryDTO userQueryDTO) {
        UserPageQueryVO userPageInfo = userService.getUserList(userQueryDTO);
        return Result.ok().data("userPageInfo",userPageInfo);
    }
}
