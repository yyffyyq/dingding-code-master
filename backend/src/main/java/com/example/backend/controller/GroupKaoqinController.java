package com.example.backend.controller;

import com.example.backend.annotion.AuthCheck;
import com.example.backend.common.BaseResponse;
import com.example.backend.common.ResultUtils;
import com.example.backend.constant.UserConstant;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ThrowUtils;
import com.example.backend.model.dto.groupKaoqin.GroupKaoqinDTO;
import com.example.backend.model.dto.groupKaoqin.GroupKaoqinQuertRequest;
import com.example.backend.model.entity.GroupKaoqin;
import com.example.backend.model.entity.SysUser;
import com.example.backend.model.vo.groupKaovo.GroupKaoqinVO;
import com.example.backend.model.vo.sysUservo.SysUserVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.backend.service.GroupKaoqinService;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.backend.constant.CommonConstant.ACCSEE_TOKEN;

/**
 *  考勤组功能部分-控制层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@RestController
@RequestMapping("/groupKaoqin")
//@Tag(name = "考勤组功能部分")
public class GroupKaoqinController {

    @Autowired
    // todo 这个resttemplate是干嘛的？
    private RestTemplate restTemplate;

    @Autowired
    private GroupKaoqinService groupKaoqinService;

    @Value("${dingtalk.getSimpleGroupInfo}")
    private String getSimpleGroupInfo;
    @Qualifier("jacksonObjectMapper")
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 调用接口之后通过钉钉 api 访问所有 group 的信息，处理一下，根据 groupid 判断否存入数据库过，然后决定是否存入
     * @param request http请求用户获取其中token
     * @return 返回更新插入的数量
     * @throws JsonProcessingException request请求为空抛出/钉钉返回code不为 0 抛出官方提示报错/ 未知报错，抛出通用报错
     */
    // todo 这里之后可以做一个省api调用的优化，
    //  判断是否有更新再决定是否需要更新数据库的考勤组
    @GetMapping("/get/simplegroup")
    @Operation(summary = "更新最新的考勤组情况")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> getSimpleGroup(HttpServletRequest request) throws JsonProcessingException {

        // 1. 判断http请求是否为空
        ThrowUtils.throwIf(request == null , ErrorCode.PARAMS_ERROR);

        // 2. 通过http请求的session拿到钉钉的access_token，为了之后获取考勤组做准备
        Object accessTokenobj =  request.getSession().getAttribute(ACCSEE_TOKEN);;
        String accessToken = (String) accessTokenobj;

        // 3. 拼接 钉钉API 请求，并调用请求获取数据进行处理
        String dingAPI = getSimpleGroupInfo+"?access_token="+accessToken;
        // 构建请求钉钉 api 请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("offset", 0); // 从第 0 条开始
        requestBody.put("size", 10);  // 最大取 10 条

        try {
            // 4. 调用 钉钉API 请求并存入数据库
            // 拿到原始 json 数据，为后续处理数据做准备
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(dingAPI, requestBody, String.class);
            String rawJsonResult = responseEntity.getBody();

            // 解析Json 拿到需要的字段，并准备接收数据放入数据库中
            JsonNode rootNode = objectMapper.readTree(rawJsonResult);
            ThrowUtils.throwIf(rootNode.path("errcode").asInt() != 0, ErrorCode.PARAMS_ERROR,rootNode.path("sub_msg").asText());
            List<GroupKaoqinDTO> resultList = new ArrayList<>();

            // 判断接口是否调用成功
            Integer suNumber = 0;
            if (rootNode.path("errcode").asInt() == 0) {

                // 拿到 groups 数组为遍历查询做准备
                JsonNode groupsArray = rootNode.path("result").path("groups");

                if (groupsArray.isArray()) {
                    for (JsonNode grouNode : groupsArray) {
                        GroupKaoqinDTO dto = new GroupKaoqinDTO();
                        dto.setGroupId(grouNode.path("group_id").asText());
                        dto.setGroupName(grouNode.path("group_name").asText());

                        resultList.add(dto);
                    }
                }

                // 将获取到的值存入数据表中
                suNumber = groupKaoqinService.insertGroupList(resultList);
            }
            return ResultUtils.success(suNumber);
        }catch (BusinessException e){
            throw e;
        }catch (Exception e){
            throw  new BusinessException(ErrorCode.GROUP_KAOQIN_COMMONG_ERROR,e.toString());
        }
    }

    /**
     * 考勤组分页查询
     * @param groupKaoqinQuertRequest 分页查询请求
     * @param request http请求
     * @return 分页信息
     * @throws BusinessException http请求为空抛出/分页查询请求页码或页数为空抛出
     */
    @PostMapping("/get/list/groups")
    @Operation(summary = "考勤组分页查询")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<GroupKaoqinVO>> getGroupList(@RequestBody GroupKaoqinQuertRequest groupKaoqinQuertRequest
            , HttpServletRequest request) throws JsonProcessingException {

        // 1. 判断考勤组分页查询请求是否为空
        ThrowUtils.throwIf(request == null , ErrorCode.PARAMS_ERROR);

        // 2. 获取页码信息，并判断是否为空
        long pageNum = groupKaoqinQuertRequest.getPageNum();
        long pageSize = groupKaoqinQuertRequest.getPageSize();
        if(pageSize <= 0 || pageNum <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"考勤组分页查询，分页或页码为空");
        }

        // 3. 调用方法获取未封装的查询到的信息
        Page<GroupKaoqin> groupKaoqinPage = groupKaoqinService.page(Page.of(pageNum, pageSize),
                groupKaoqinService.getQueryWrapper(groupKaoqinQuertRequest));

        // 4. 数据封装脱敏
        Page<GroupKaoqinVO> groupKaoqinVOPage = new Page<>(pageNum, pageSize, groupKaoqinPage.getTotalRow());
        List<GroupKaoqinVO> groupKaoqinVOList = groupKaoqinService.getGrouKaoqinList(groupKaoqinPage.getRecords());

        groupKaoqinVOPage.setRecords(groupKaoqinVOList);

        // 5. 返回分页信息给前端
        return ResultUtils.success(groupKaoqinVOPage);
    }

}
