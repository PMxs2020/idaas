package org.iam.service;

import org.iam.pojo.domain.Application;
import org.iam.pojo.dto.ApplicationQueryDTO;
import org.iam.pojo.vo.ApplicationPageVO;

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
     *
     * @param ids
     * @return
     */
    int deleteApplications(List<String> ids);

    void updateApplication(Application application);

    ApplicationPageVO getApplicationList(ApplicationQueryDTO applicationQueryDTO);
}