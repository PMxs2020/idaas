package org.iam.service;

import org.iam.pojo.domain.Application;
import org.iam.pojo.dto.ApplicationQueryDTO;
import org.iam.pojo.vo.ApplicationPageVO;
import org.iam.pojo.vo.LoginSettingVO;

import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 应用服务接口
 */
public interface ApplicationService {
    /**
     * 根据id查询应用信息
     * @param appId 应用ID
     * @return 应用信息
     */
    Application getApplicationById(String appId);
    /**
     * 保存应用信息
     * @param application 应用信息
     */
    void saveApplication(Application application) throws NoSuchAlgorithmException;

    /**
     * 批量删除应用
     * @param ids
     * @return
     */
    int deleteApplications(List<String> ids);
    /**
     * 更新应用
     * @param application
     */
    void updateApplication(Application application);
    /**
     * 应用分页查询
     * @param applicationQueryDTO
     * @return
     */
    ApplicationPageVO getApplicationList(ApplicationQueryDTO applicationQueryDTO);

    boolean isTrirdPartyApply(String id);

    LoginSettingVO getLoginSetting(String appId);
}