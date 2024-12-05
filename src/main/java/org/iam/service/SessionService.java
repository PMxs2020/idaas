package org.iam.service;

import org.iam.pojo.domain.Application;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface SessionService {
    /**
     * 创建第三方会话
     * @param uuid
     * @param application
     * @return
     */
    String createThirdPartySession(String iamToken,String uuid, Application application) ;

    /**
     * 创建系统本土会话
     * @param uuid
     * @return
     */
    String createIamSession(String uuid,String ip);

    /**
     * token续期
     * @param iamToken
     * @return
     */
    String renewalSession(String iamToken);

    /**
     * iam主会话删除
     * @param iamToken
     */
    void deleteIamSession(String iamToken);

    /**
     * 应用会话删除
     * @param applyUuid
     * @param applyToken
     */
    void deleteThirdPartySession(String applyToken,String applyUuid);
}
