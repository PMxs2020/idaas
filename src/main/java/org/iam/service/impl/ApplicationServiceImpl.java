package org.iam.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.iam.annotation.AutoFill;
import org.iam.constant.ApplicationConstant;
import org.iam.enumeration.OperationType;
import org.iam.exception.ApplicationNotExistException;
import org.iam.exception.BaseException;
import org.iam.mapper.ApplicationDao;
import org.iam.pojo.domain.Application;
import org.iam.pojo.domain.KeyAddress;
import org.iam.pojo.dto.ApplicationQueryDTO;
import org.iam.pojo.vo.ApplicationPageVO;
import org.iam.pojo.vo.ApplicationVO;
import org.iam.pojo.vo.LoginSettingVO;
import org.iam.service.ApplicationService;
import org.iam.util.SafeKeyUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.iam.exception.IdNullException;
import org.iam.util.AppKeyGenerator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 应用服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {
    @Autowired
    private ApplicationDao applicationDao;
    @Autowired
    private SafeKeyUtil safeKeyUtil;
    @Autowired
    private AppKeyGenerator appKeyGenerator;


    /**
     * 根据应用ID查询应用信息
     * @param id 应用UUID
     */
    @Override
    public Application getApplicationById(String id) {
        if (id == null) {
            throw new IdNullException();
        }
        
        // 判断是否是控制台应用
        if (ApplicationConstant.CONSOLE_APP_UUID.equals(id)) {
            return buildConsoleApplication();
        }

        // 判断是否是用户中心应用
        if (ApplicationConstant.USER_CENTER_APP_UUID.equals(id)) {
            return buildUserCenterApplication();
        }
        
        // 查询其他应用
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Application::getApplyUuid, id);
        //判断应用是否查询到
        Application application=applicationDao.selectOne(queryWrapper);
        if(application==null){
            throw new ApplicationNotExistException("查询的应用不存在");
        }
        return application;
    }

    @Override
    @AutoFill(value = OperationType.INSERT )
    public void saveApplication(Application application) {
        //生成应用的uuid
        UUID applicationUuid=UUID.randomUUID();
        application.setApplyUuid(applicationUuid.toString());
        //生成pkcs1，pkcs8的公私密钥,并返回存储地址
        KeyAddress keyAddress=safeKeyUtil.generateAppSafeKey(applicationUuid);
        application.setPrivateKeyPkcs1(keyAddress.getPkcs1PrivatePath());
        application.setPublicKeyPkcs1(keyAddress.getPkcs1PublicPath());
        application.setPrivateKeyPkcs8(keyAddress.getPkcs8PrivatePath());
        application.setPublicKeyPkcs8(keyAddress.getPkcs8PublicPath());
        //生成appKey和appSecret
        AppKeyGenerator.KeyPair keyPair = appKeyGenerator.generateKeyPair();
        application.setAppKey(keyPair.getAppKey());
        application.setAppSecret(keyPair.getAppSecret());
        //将数据插入到数据库
        applicationDao.insert(application);
    }

    /**
     * 删除应用（支持单个和批量）
     *
     * @param ids 应用ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteApplications(List<String> ids) {
        //验证参数是否为空
        if (CollectionUtils.isEmpty(ids)) {
            throw new IdNullException("删除的应用ID不能为空");
        }
        //应用信息的验证和数据层删除
        try {
            //要删除的应用是否存在
            List<Application> applications = applicationDao.batchSelectByApplyUuids(ids);
            if (applications.isEmpty()) {
                throw new ApplicationNotExistException("要删除的应用不存在");
            }
            //执行删除操作
            int deleteCount=applicationDao.batchDeleteByApplyUuids(ids);
            log.info("删除应用成功，ids：{}", ids);
            return deleteCount;
        } catch (Exception e) {
            log.error("删除应用失败，ids：{}", ids, e);
            throw e;
        }
    }
    /**
     * 构建控制台应用信息
     */
    private Application buildConsoleApplication() {
        Application app = new Application();
        app.setApplyUuid(ApplicationConstant.CONSOLE_APP_UUID);
        app.setApplyName("控制台应用");
        return app;
    }
    
    /**
     * 构建用户中心应用信息
     */
    private Application buildUserCenterApplication() {
        Application app = new Application();
        app.setApplyUuid(ApplicationConstant.USER_CENTER_APP_UUID);
        app.setApplyName("用户中心应用");
        return app;
    }

    @Override
    @AutoFill(value = OperationType.UPDATE)
    public void updateApplication(Application application) {
        if (application == null || application.getApplyUuid() == null) {
            throw new IdNullException("应用ID不能为空");
        }
        // 检查应用是否存在
        Application existingApp = getApplicationById(application.getApplyUuid());
        if (existingApp == null) {
            throw new ApplicationNotExistException("应用不存在");
        }
        try {
            UpdateWrapper<Application> wrapper=new UpdateWrapper<>();
            wrapper.eq("apply_uuid", application.getApplyUuid());
            applicationDao.update(application,wrapper);
            log.info("更新应用成功，id：{}", application.getApplyUuid());
        } catch (Exception e) {
            log.error("更新应用失败，id：{}", application.getApplyUuid(), e);
            throw new BaseException("更新应用失败：" + e.getMessage());
        }
    }

    @Override
    public ApplicationPageVO getApplicationList(ApplicationQueryDTO applicationQueryDTO) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
            // 应用uuid精确查询
            if (StringUtils.hasText(applicationQueryDTO.getApplyUuid())) {
                queryWrapper.eq(Application::getApplyUuid, applicationQueryDTO.getApplyUuid());
            }
            // 应用名称模糊查询
            if (StringUtils.hasText(applicationQueryDTO.getApplyName())) {
                queryWrapper.like(Application::getApplyName, applicationQueryDTO.getApplyName());
            }

            // 创建人uuid匹配
            if (StringUtils.hasText(applicationQueryDTO.getCreateUserId())) {
                queryWrapper.eq(Application::getCreateUserId, applicationQueryDTO.getCreateUserId());
            }
            // 应用类型
            if (!CollectionUtils.isEmpty(applicationQueryDTO.getApplyTypeId())) {
                queryWrapper.and(wrapper -> {
                    applicationQueryDTO.getApplyTypeId().forEach(typeId -> {
                        wrapper.or().apply("FIND_IN_SET({0}, apply_type_id)", typeId);
                    });
                });
            }
            // 启用状态
            if (applicationQueryDTO.getIsDisabled() != null) {
                queryWrapper.eq(Application::getIsDisabled, applicationQueryDTO.getIsDisabled());
            }

            // 创建时间范围
            if (applicationQueryDTO.getStartTime() != null) {
                queryWrapper.ge(Application::getCreateTime, applicationQueryDTO.getStartTime());
            }
            if (applicationQueryDTO.getEndTime() != null) {
                queryWrapper.le(Application::getCreateTime, applicationQueryDTO.getEndTime());
            }

            // 按创建时间降序排序
            queryWrapper.orderByDesc(Application::getCreateTime);

            // 执行分页查询
            Page<Application> page = new Page<>(applicationQueryDTO.getPageNum(), applicationQueryDTO.getPageSize());
            Page<Application> pageResult = applicationDao.selectPage(page, queryWrapper);

            // 转换为VO对象
            ApplicationPageVO pageVO = new ApplicationPageVO();
            pageVO.setTotal(pageResult.getTotal());
            List<ApplicationVO> pageVOList = new ArrayList<>();
            for (Application application : pageResult.getRecords()) {
                ApplicationVO applicationVO = new ApplicationVO();
                BeanUtils.copyProperties(application, applicationVO);
                pageVOList.add(applicationVO);
            }
            pageVO.setRecords(pageVOList);
            return pageVO;
        } catch (Exception e) {
            log.error("查询应用列表失败", e);
            throw new BaseException("查询应用列表失败：" + e.getMessage());
        }
    }

    /**
     * 判断是否是第三方应用
     * @param id
     * @return
     */
    @Override
    public boolean isTrirdPartyApply(String id) {
        if (id == null) {
            throw new IdNullException();
        }
        return !(ApplicationConstant.CONSOLE_APP_UUID.equals(id)||ApplicationConstant.USER_CENTER_APP_UUID.equals(id));
    }

    @Override
    public LoginSettingVO getLoginSetting(String appId) {
        if(appId==null){
            throw new IdNullException();
        }
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Application::getApplyUuid, appId);
        Application application = applicationDao.selectOne(queryWrapper);
        if(application==null){
            throw new ApplicationNotExistException("查询的应用不存在");
        }
        LoginSettingVO loginSettingVO = new LoginSettingVO();
        BeanUtils.copyProperties(application, loginSettingVO);
        return loginSettingVO;
    }
} 