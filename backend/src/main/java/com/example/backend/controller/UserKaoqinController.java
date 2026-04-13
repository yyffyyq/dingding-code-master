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

    /**
     * 获取考勤组人员，并添加到数据库内
     * @param group_id 考勤组id
     * @param request http请求，用于获取token和op_us_id
     * @return 返回成功信息
     * @exception
     */
    @PostMapping("/get/userId")
    public BaseResponse<String> getUserkaoqin(@RequestParam String group_id, HttpServletRequest request) {

        ThrowUtils.throwIf(request == null , ErrorCode.PARAMS_ERROR);

        List<UserKaoqinDTO> resultList = new ArrayList<>();

        // 判断接口是否调用成功
        String result = "无操作";

        // 调用钉钉API，然后存入数据库中
        try{

            // 获取access_token
            Object accessTokenobj =  request.getSession().getAttribute(ACCSEE_TOKEN);;
            String accessToken = (String) accessTokenobj;

            // 获取操作者id
            Object userVO = request.getSession().getAttribute(USER_LOGIN_STATE);
            SysUser opUser = (SysUser) userVO;

            // 调用方法获取成员
            resultList = userKaoqinService.getMemeberListId(group_id,accessToken,opUser.getUserId());

            //获取后批量插入
            result = userKaoqinService.insertGroupList(resultList,group_id);

        }catch (Exception e){
            System.out.println("报错了，报错："+e);
            e.printStackTrace();
        }
        return ResultUtils.success(result);
    }
}
