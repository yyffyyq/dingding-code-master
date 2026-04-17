package com.example.backend.controller;

import com.example.backend.common.BaseResponse;
import com.example.backend.common.ResultUtils;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ThrowUtils;
import com.example.backend.model.dto.UserKaoqinByGroupIdQuertRequest;
import com.example.backend.model.dto.UserKaoqinDTO;
import com.example.backend.model.dto.groupKaoqin.GroupKaoqinQuertRequest;
import com.example.backend.model.entity.SysUser;
import com.example.backend.model.entity.UserKaoqin;
import com.example.backend.model.vo.UserKaoqinVO;
import com.example.backend.model.vo.groupKaovo.GroupKaoqinVO;
import com.example.backend.service.UserGroupKaoqinRelService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.backend.service.UserKaoqinService;

import java.util.ArrayList;
import java.util.List;

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
    private UserKaoqinService userKaoqinService;
    @Autowired
    private UserGroupKaoqinRelService userGroupKaoqinRelService;

    /**
     * 获取考勤组人员，并添加到数据库内
     * @param group_id 考勤组id
     * @param request http请求，用于获取token和op_us_id
     * @return 返回成功信息
     * @exception
     */
    @PostMapping("/get/userId")
    @Operation(summary = "获取考勤人员信息并存入数据库中" ,description = "通过考勤组id获取考勤人员信息并存入数据库")
    public BaseResponse<String> getUserkaoqin(@RequestParam String group_id, HttpServletRequest request) {

        // 1. 判断http请求是否为空
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);

        // 2. 构建返回获取需要插入的考勤人员列表
        List<UserKaoqinDTO> resultList = new ArrayList<>();

        // 判断接口是否调用成功
        String result = "无操作";

        // 调用钉钉API，然后存入数据库中
        List<String> idList = null;
        try {

            // 3. 获取access_token，操作人员op_user_id
            Object accessTokenobj = request.getSession().getAttribute(ACCSEE_TOKEN);
            String accessToken = (String) accessTokenobj;

            Object userVO = request.getSession().getAttribute(USER_LOGIN_STATE);
            SysUser opUser = (SysUser) userVO;


            // 4. 调用方法获取考勤组考勤人员user_id列表
            resultList = userKaoqinService.getMemeberListId(group_id, accessToken, opUser.getUserId());

            // 5. 通过考勤人员user_id列表批量插入
            idList = userKaoqinService.insertGroupList(resultList, group_id);

            // 6. 通过用户user_idList(idList) 获取用户namelist
            result = userKaoqinService.insertUserName(idList,accessToken);

        } catch (Exception e) {
            System.out.println("报错了，报错：" + e);
            e.printStackTrace();
        }
        return ResultUtils.success(result + idList);
    }

    // 查询，根据考勤组groupId获取考勤人员列表，
    // 分页查询
    @PostMapping("/get/list/userkaoqins")
    public BaseResponse<Page<UserKaoqinVO>> getGroupList(@RequestBody UserKaoqinByGroupIdQuertRequest userKaoqinByGroupIdQuertRequest
            , HttpServletRequest request) throws JsonProcessingException {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        long pageNum = userKaoqinByGroupIdQuertRequest.getPageNum();
        long pageSize = userKaoqinByGroupIdQuertRequest.getPageSize();

        // 先通过 userKaoqinByGroupIdQuertRequest 中的group_id 获取到userIdList组
        ThrowUtils.throwIf(userKaoqinByGroupIdQuertRequest.getGroupId() == null, ErrorCode.PARAMS_ERROR,"查询考勤组id为空");
        List<String> idList = userGroupKaoqinRelService.getIdListByGroupId(userKaoqinByGroupIdQuertRequest.getGroupId());

        // 判断查询到的信息为空，返回空值
        Page<UserKaoqinVO> emptyPage = new Page<>(pageNum, pageSize, 0);
        if (idList == null || idList.isEmpty()) {
            emptyPage.setRecords(new ArrayList<>());
            return ResultUtils.success(emptyPage);
        }

        // 2. 根据 userIdList 分页查询 UserKaoqin
        Page<UserKaoqin> userKaoqinPage = userKaoqinService.page(
                Page.of(pageNum, pageSize),
                userKaoqinService.getQueryWrapperByUserIdList(idList, userKaoqinByGroupIdQuertRequest)
        );

        Page<UserKaoqinVO> userKaoqinVOPage = new Page<>(pageNum, pageSize, userKaoqinPage.getTotalRow());
        List<UserKaoqinVO> userKaoqinVOList = userKaoqinService.getuserKaoqinList(userKaoqinPage.getRecords());

        userKaoqinVOPage.setRecords(userKaoqinVOList);

        return ResultUtils.success(userKaoqinVOPage);
    }
}
