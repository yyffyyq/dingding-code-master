package com.example.backend.controller;

import com.example.backend.service.DingUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import com.aliyun.dingtalkoauth2_1_0.models.GetSsoAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetSsoAccessTokenResponse;
import com.aliyun.dingtalkoauth2_1_0.models.GetSsoUserInfoHeaders;
import com.aliyun.dingtalkoauth2_1_0.models.GetSsoUserInfoRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetSsoUserInfoResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.example.backend.common.ApiResponse;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static com.example.backend.constant.CommonConstant.ACCSEE_TOKEN;
import static com.example.backend.constant.UserConstant.USER_LOGIN_STATE;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api")
public class Controller {
    @Value("${dingtalk.corpId}")
    private String appKey;
    @Value("${dingtalk.ssoSecret}")
    private String appSecret;
    // 钉钉新版获取AccessToken接口地址
    @Value("${dingtalk.getToken}")
    private String DING_TALK_TOKEN_URL;

    @Resource
    private DingUserService dingUserService;

    // Spring自带的HTTP请求工具
    @Resource
    private RestTemplate restTemplate;

    // ====================== 获取钉钉AccessToken（带缓存，有效期2小时） ======================
    /**
     * 获取钉钉应用级accessToken
     * 缓存有效期：7000秒（小于钉钉官方7200秒，避免过期）
     */
    @GetMapping("/dingtalk/token")
    @Cacheable(value = "dingTalkTokenCache", key = "'dingTalkToken'", cacheManager = "cacheManager")
    @Operation(summary = "获取钉钉token" ,description = "获取钉钉token用于后续获取其他信息")
    public ResponseEntity<Map<String, Object>> getDingTalkAccessToken(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            resultMap = dingUserService.getDingTalkUserInfo();

            // 获取access_token用于存入session中
            String accessToken = resultMap.get("accessToken").toString();

            // 清理 request 里的 access_token,再存入最新
            request.getSession().removeAttribute(ACCSEE_TOKEN);
            request.getSession().setAttribute(ACCSEE_TOKEN, accessToken);

            return ResponseEntity.ok(resultMap);

        } catch (Exception e) {
            log.error("调用钉钉获取Token接口异常", e);
            resultMap.put("success", false);
            resultMap.put("msg", "系统异常：" + e.getMessage());
            return ResponseEntity.ok(resultMap);
        }
    }

}
