package org.iam.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class HttpRequestUtil {
    @Autowired
    private RestTemplate restTemplate;

    @Async
    public CompletableFuture<Void> sendHttpRequestAsync(String logoutUrl, String token) {
        // 创建 HTTP 请求头，带上用户的 token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> request = new HttpEntity<>(headers);
        try {
            // 发送 HTTP 请求到目标应用的登出接口
            restTemplate.exchange(logoutUrl, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("应用登出{}地址,请求失败。{}",logoutUrl,e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
}
