package com.example.backend.service.impl;

import com.example.backend.service.DingUserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class DingUserServiceImpl implements DingUserService {
    @Value("${dingtalk.corpId}")
    private String appKey;
    @Value("${dingtalk.ssoSecret}")
    private String appSecret;
    // 钉钉新版获取AccessToken接口地址
    @Value("${dingtalk.getToken}")
    private String DING_TALK_TOKEN_URL;

    @Resource
    private RestTemplate restTemplate;

    /**
     * 获取钉钉access_token
     * @return
     */
    @Override
    public Map<String, Object> getDingTalkUserInfo() {
        Map<String, Object> resultMap = new HashMap<>();
        try{
            // 1. 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 2. 构造请求体（自建应用仅需这两个参数！）
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("appKey", appKey);
            requestBody.put("appSecret", appSecret);

            // 3. 发送POST请求
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    DING_TALK_TOKEN_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            // 4. 解析返回结果
            // 这里已经拿到值了
            Map<String, Object> responseBody = responseEntity.getBody();
            if (responseBody == null || !responseBody.containsKey("accessToken")) {
                resultMap.put("success", false);
                resultMap.put("msg", "获取钉钉Token失败");
                return resultMap;
            }
            // 5. 返回成功数据
            String accessToken = (String) responseBody.get("accessToken");
            Integer expireIn = (Integer) responseBody.get("expireIn");

            resultMap.put("success", true);
            resultMap.put("accessToken", accessToken);
            resultMap.put("expireIn", expireIn);

            return resultMap;

        } catch (Exception e) {
            resultMap.put("success", false);
            resultMap.put("msg", "系统异常：" + e.getMessage());
            return resultMap;
        }
    }
}
