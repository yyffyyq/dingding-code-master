package com.example.backend.controller;

import com.example.backend.annotion.AuthCheck;
import com.example.backend.common.BaseResponse;
import com.example.backend.common.ResultUtils;
import com.example.backend.constant.UserConstant;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ThrowUtils;
import com.example.backend.model.entity.SysUser;
import com.example.backend.model.eum.UserRoleEnum;
import com.example.backend.model.vo.SysUserVO;
import com.example.backend.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.xml.transform.Result;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/dingUser")
public class DingUserController {

    // === 补充缺失的属性注入（确保你能顺利编译） ===
    @Value("${dingtalk.corpId}")
    private String clientId;

    @Value("${dingtalk.ssoSecret}")
    private String clientSecret;

    @Value("${dingtalk.getUserTokenUrl:https://api.dingtalk.com/v1.0/oauth2/userAccessToken}")
    private String getUserTokenUrl;

    @Value("${dingtalk.getUserInfoUrl:https://api.dingtalk.com/v1.0/contact/users/me}")
    private String getUserInfoUrl;

    @Value("${dingtalk.DING_GET_USERID_URL:https://oapi.dingtalk.com/topapi/user/getbyunionid}")
    private String DING_GET_USERID_URL;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private Controller dingTalkController;

    @Resource
    private SysUserService sysUserService;

    /**
     * 用户扫码登录后入库操作
     * @param params 扫码传入的数据
     * @param request http请求
     * @return 返回用户封装后信息
     */
    @PostMapping("/login")
    @Operation(summary = "钉钉扫码/授权登录", description = "获取钉钉用户信息及userId")
    public BaseResponse<SysUserVO> dingLogin(@RequestBody Map<String, String> params, HttpServletRequest request) {

        // 先获取到登录用户扫码后的code，用于api调用获取用户信息，并创建返回对象
        String authCode = params.get("authCode");

        // 判断扫码后获取的code是否为空，为空就抛出异常
        if (authCode == null || authCode.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.SYS_USER_SCAN_LOGIN_PARAMS_ERROR);
        }

        try {

            // 获取用户全部基础信息
            Map<String, Object> userInfo = getDingTalkAccessToken(authCode);

            // 提取关键标识，用户唯一标识id、用户名字
            String unionId = (String) userInfo.get("unionId");
            String nickName = (String) userInfo.get("nick");

            // 获取用户头像图片路径
            String avatarUrl = (String) userInfo.get("avatarUrl");
            String userId = null;

            // 使用 union_id 换取企业内的 user_id
            try {
                // 调用 Controller 获取【应用级】Token
                ResponseEntity<Map<String, Object>> appTokenRes = dingTalkController.getDingTalkAccessToken(request);
                Map<String, Object> appTokenBody = appTokenRes.getBody();

                // 判断token是否为空
                if (appTokenBody != null && (Boolean) appTokenBody.get("success")) {
                    String appAccessToken = (String) appTokenBody.get("accessToken");

                    // 3.2 拼接接口地址 (直接拼到 URL 后面)
                    String getUserIdUrl = DING_GET_USERID_URL + "?access_token=" + appAccessToken;

                    HttpHeaders idHeaders = new HttpHeaders();
                    idHeaders.setContentType(MediaType.APPLICATION_JSON);

                    Map<String, String> idBody = new HashMap<>();
                    idBody.put("unionid", unionId); // 传入扫码拿到的 unionId

                    HttpEntity<Map<String, String>> idEntity = new HttpEntity<>(idBody, idHeaders);
                    ResponseEntity<Map> idResponse = restTemplate.postForEntity(getUserIdUrl, idEntity, Map.class);
                    Map<String, Object> idResBody = idResponse.getBody();

                    // 3.3 解析返回的 userId
                    if (idResBody != null && (Integer) idResBody.get("errcode") == 0) {
                        Map<String, Object> resultObj = (Map<String, Object>) idResBody.get("result");
                        userId = (String) resultObj.get("userid");

                        // 把成功信息放入成功日志文件里
                        log.info("成功换取到企业内部员工 userId: {}", userId);

                        // 为了方便，顺手把 userId 塞回 userInfo 集合里
                        userInfo.put("userId", userId);
                    } else {

                        // 把警告信息放入警告日志文件里
                        log.warn("换取 userId 失败 (该账号可能未加入当前钉钉企业架构): {}", idResBody);
                    }
                }
            } catch (Exception e) {

                // 把报错信息放入报错日志文件
                log.error("根据 unionId 获取 userId 异常", e);
            }

            // 数据转换，将数据拿到数据库判断是否入库，然后更新 request 状态码进入 Session

            // 创建新的 SysUser 对象
            SysUser sysUser = new SysUser();
            sysUser.setUserRole(UserRoleEnum.USER.toString());
            sysUser.setNickName(nickName);
            sysUser.setUserId(userId);
            sysUser.setUnionId(unionId);
            sysUser.setAvarUrl(avatarUrl);

            // 将数据存入数据库，然后返回脱敏后信息
            SysUserVO login_user = sysUserService.userLogin(sysUser,request);

            // 返回给前端封装脱敏后信息
            return ResultUtils.success(login_user);

        } catch (Exception e) {

            // 日志添加报错内容
            log.error("钉钉扫码登录异常", e);
            // 抛出异常，钉钉登录异常报错
            throw new BusinessException(ErrorCode.SYS_USER_SCAN_LOGIN_ERROR,e.toString());
        }
    }

    /**
     * 根据 authcode 获取用户信息等
     * @param authCode 用户扫码后的信息code
     * @return 返回用户全部基础信息
     */
    public Map<String, Object> getDingTalkAccessToken(String authCode) {
        // 使用 authCode 换取用户级 userAccessToken
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_JSON);

        // 创建请求体，放入应用相关信息
        Map<String, String> tokenBody = new HashMap<>();
        tokenBody.put("clientId", clientId);
        tokenBody.put("clientSecret", clientSecret);
        tokenBody.put("code", authCode);
        tokenBody.put("grantType", "authorization_code");

        // 发送请求
        HttpEntity<Map<String, String>> tokenEntity = new HttpEntity<>(tokenBody, tokenHeaders);
        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                getUserTokenUrl,
                HttpMethod.POST,
                tokenEntity,
                Map.class
        );

        // 获取数据
        Map<String, Object> tokenResBody = tokenResponse.getBody();

        // 判断获取的值是否为空，为空就抛出异常
        if (tokenResBody == null || !tokenResBody.containsKey("accessToken")) {
            throw new BusinessException(ErrorCode.SYS_USER_GET_USER_ACESS_TOKEN_FAIL_ERROR);
        }

        // 获取用户通行token
        String userAccessToken = (String) tokenResBody.get("accessToken");

        // 使用 userAccessToken 获取用户基础信息
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setContentType(MediaType.APPLICATION_JSON);
        userHeaders.set("x-acs-dingtalk-access-token", userAccessToken);

        // todo这一块是干嘛的？
        HttpEntity<String> userEntity = new HttpEntity<>(null, userHeaders);
        ResponseEntity<Map> userResponse = restTemplate.exchange(
                getUserInfoUrl,
                HttpMethod.GET,
                userEntity,
                Map.class
        );

        return userResponse.getBody();
    }


    /**
     * 获取当前登录用户信息
     * @param request http请求
     * @return 返回当前用户职位
     * @exception BusinessException 请求数据为空抛出
     */
    @GetMapping("/get/login")
    @Operation(summary = "获取用户登录状态" ,description = "获取用户登录状态")
    public BaseResponse<SysUserVO> health(HttpServletRequest request) {

        // 判断请求是否为空
        ThrowUtils.throwIf(request == null , ErrorCode.PARAMS_ERROR,"请求不可为空");

        // 获取请求，看看登录状态 getLoginUser()
        SysUser sysUser = sysUserService.getLoginUser(request);

        // 返回值
        return ResultUtils.success(sysUserService.getSysUserVO(sysUser));
    }

    /**
     * 用户登出请求
     * @param request http请求
     * @return 返回 String 类型
     * @exception BusinessException http请求为空抛出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登录状态退出" ,description = "用户退出登录，清空sseion")
    public BaseResponse<String> logout(HttpServletRequest request) {

        // 判断请求是否为空
        ThrowUtils.throwIf(request == null,ErrorCode.PARAMS_ERROR);

        // 更新 session 状态返回 result 值返回前端
        String result = sysUserService.logoutUser(request);

        // 返回给前端作为判断并返回
        return ResultUtils.success(result);
    }

    /**
     * 测试权限校验
     */
    @GetMapping("/test")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public String test(HttpServletRequest request) {
        return "通过";
    }

}