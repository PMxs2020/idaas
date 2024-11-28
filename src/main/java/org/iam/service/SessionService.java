package org.iam.service;

import org.iam.pojo.domain.Application;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface SessionService {
    String createThirdPartySession(String uuid, Application application) ;
    String createIamSession(String uuid);
    String renewalSession(String iamToken);
}
