package org.iam.controller.application;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.iam.convertor.ListStringConverter;
import org.iam.pojo.domain.Application;
import org.iam.pojo.domain.User;
import org.iam.pojo.dto.ApplicationDTO;
import org.iam.pojo.dto.ApplicationQueryDTO;
import org.iam.pojo.dto.UserDTO;
import org.iam.pojo.dto.UserQueryDTO;
import org.iam.pojo.vo.ApplicationPageVO;
import org.iam.pojo.vo.UserPageQueryVO;
import org.iam.service.ApplicationService;
import org.iam.util.Result;
import org.iam.validation.ValidationGroups;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@RequestMapping("/console/application")
@Slf4j
public class ApplicationController {
    @Autowired
    private ApplicationService applicationService;

    /**
     * 应用创建
     * @param applicationDTO
     * @return
     * @throws NoSuchAlgorithmException
     */
    @PostMapping
    public Result addApplication(@Validated(ValidationGroups.Add.class)  @RequestBody ApplicationDTO applicationDTO) throws NoSuchAlgorithmException {
        // 处理文件上传并保存到服务器
        // 保存应用信息
        Application application=new Application();
        BeanUtils.copyProperties(applicationDTO,application);
        String applyTypeIds=ListStringConverter.listToString(applicationDTO.getApplyTypeId());
        application.setApplyTypeId(applyTypeIds);
        applicationService.saveApplication(application);
        return Result.ok().message("应用创建成功");
    }

    /**
     * 应用删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result deleteApplication(@RequestBody List<String> ids){
        log.info("删除应用，ids：{}", ids);
        int deleteCount=applicationService.deleteApplications(ids);
        return Result.ok().message("应用已删除");
    }
    /**
     * 查询应用
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result getApplication(@PathVariable String id) {
        Application application = applicationService.getApplicationById(id);
        return Result.ok().data("application", application);
    }
    /**
     * 应用更新
     * @param applicationDTO
     * @return
     */
    @PutMapping
    public Result saveApplication(@RequestBody @Validated(ValidationGroups.Update.class) ApplicationDTO applicationDTO) {
        Application application = new Application();
        // 将 applicationDTO 的属性赋值给 application
        BeanUtils.copyProperties(applicationDTO, application);
        applicationService.updateApplication(application);
        return Result.ok().message("应用更新成功");
    }

    @GetMapping
    public Result applicationList(ApplicationQueryDTO applicationQueryDTO) {
        ApplicationPageVO pageInfo = applicationService.getApplicationList(applicationQueryDTO);
        return Result.ok().data("pageInfo", pageInfo);
    }
}
