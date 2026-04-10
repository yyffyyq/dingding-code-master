package com.example.backend.controller;

import com.example.backend.common.BaseResponse;
import com.example.backend.common.ResultUtils;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ThrowUtils;
import com.example.backend.model.dto.UserKaoqinDTO;
import com.example.backend.model.dto.groupKaoqin.GroupKaoqinDTO;
import com.example.backend.model.entity.SysUser;
import com.example.backend.model.vo.sysUservo.SysUserVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.backend.model.entity.UserKaoqin;
import com.example.backend.service.UserKaoqinService;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.backend.constant.CommonConstant.ACCSEE_TOKEN;
import static com.example.backend.constant.UserConstant.USER_LOGIN_STATE;

/**
 *  控制层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@RestController
@RequestMapping("/userKaoqin")
public class UserKaoqinController {

    @Autowired
    // todo 这个resttemplate是干嘛的？
    private RestTemplate restTemplate;
    @Autowired
    private UserKaoqinService userKaoqinService;

    @Value("${dingtalk.getMemberIdList}")
    private String getMemberIdList;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/get/userId")
    public BaseResponse<Integer> getUserkaoqin(@RequestParam String group_id, HttpServletRequest request) {

        ThrowUtils.throwIf(request == null , ErrorCode.PARAMS_ERROR);

        // 拿到token请求，为了之后获取考勤组做准备
        Object accessTokenobj =  request.getSession().getAttribute(ACCSEE_TOKEN);;
        String accessToken = (String) accessTokenobj;
        // 拼接 钉钉API 请求，并调用请求获取数据进行处理
        String dingAPI = getMemberIdList+"?access_token="+accessToken;

        // 获取操作者id
        Object userVO = request.getSession().getAttribute(USER_LOGIN_STATE);
        SysUser opUser = (SysUser) userVO;

        // 创建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("op_user_id", opUser.getUserId());
        requestBody.put("group_id", group_id);
        requestBody.put("cursor", 0);

        boolean has_more=true;

        // 判断接口是否调用成功
        Integer suNumber = 0;

        List<UserKaoqinDTO> resultList = new ArrayList<>();

        // 调用钉钉API，然后存入数据库中
        try{

            // todo 这里需要改造一下，使用 while 获取所有用户id

            // todo 上来继续测试
            while(has_more){
                // 获取原始数据
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(dingAPI, requestBody, String.class);
                String rawJsonResult = responseEntity.getBody();

                // 解析Json 拿到需要的字段，并准备接收数据放入数据库中
                JsonNode rootNode = objectMapper.readTree(rawJsonResult);
                ThrowUtils.throwIf(rootNode.path("errcode").asInt() != 0, ErrorCode.PARAMS_ERROR,rootNode.path("sub_msg").asText());

                // 获取这两个值作为判断是否需要继续向后查询
                requestBody.put("cursor", rootNode.path("cursor").asInt());
                has_more = rootNode.path("has_more").asBoolean();

                if (rootNode.path("errcode").asInt() == 0) {
                    // 拿到 groups 数组为遍历查询做准备
                    JsonNode usersArray = rootNode.path("result").path("result");
                    if (usersArray.isArray()) {
                        for (JsonNode user : usersArray) {
                            UserKaoqinDTO u = new UserKaoqinDTO();
                            u.setUserId(user.asText());
                            resultList.add(u);
                        }
                    }
                }
            }
            //获取后批量插入
            suNumber = userKaoqinService.insertGroupList(resultList);

        }catch (Exception e){
            e.printStackTrace();
        }
        return ResultUtils.success(suNumber);
    }

}
