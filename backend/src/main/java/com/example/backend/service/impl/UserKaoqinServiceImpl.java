package com.example.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ThrowUtils;
import com.example.backend.mapper.UserGroupKaoqinRelMapper;
import com.example.backend.model.dto.UserKaoqinByGroupIdQuertRequest;
import com.example.backend.model.dto.UserKaoqinDTO;
import com.example.backend.model.entity.UserGroupKaoqinRel;
import com.example.backend.model.vo.UserKaoqinVO;
import com.example.backend.service.UserGroupKaoqinRelService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.example.backend.model.entity.UserKaoqin;
import com.example.backend.mapper.UserKaoqinMapper;
import com.example.backend.service.UserKaoqinService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  服务层实现。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@Slf4j
@Service
public class UserKaoqinServiceImpl extends ServiceImpl<UserKaoqinMapper, UserKaoqin>  implements UserKaoqinService {

    @Resource
    private UserKaoqinMapper userKaoqinMapper;

    @Resource
    private UserGroupKaoqinRelService userGroupKaoqinRel;

    @Value("${dingtalk.getMemberIdList}")
    private String getMemberIdList;

    @Value("${dingtalk.getUserKaoqinInfoListUrl}")
    private String getUserKaoqinInfoListUrl;

    @Autowired
    // todo 这个resttemplate是干嘛的？
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserGroupKaoqinRelMapper userGroupKaoqinRelMapper;
    @Autowired
    private ResourceUrlProvider resourceUrlProvider;


    /**
     *
     * @param resultList
     * @param group_id
     * @return
     */
    @Override
    public List<String> insertGroupList(List<UserKaoqinDTO> resultList,String group_id) {

        // 1. 判断输入参数
        ThrowUtils.throwIf(resultList == null || group_id == null , ErrorCode.PARAMS_ERROR , "获取用户信息列表失败，参数缺失");

        // 2. 构建返回结果对象
        List<String> result = new ArrayList<>();

        // 3. 根据 group_id 获取到对应的考勤组成员信息
        // 查询user_group_kaoqin_rel表里的信息
        Set<String>  user_group_kaoqin_ids = getCurrentUserIdSet(group_id);
        // 查询user_kaoqin表里的信息
        Set<String> user_kaoqin_ids = getCurrentUserIdSet();

        // 4. 获取传入需要待更新考勤人员信息
        Set<String> currentUserIds = resultList.stream()
                .map(UserKaoqinDTO::getUserId)
                .collect(Collectors.toSet());

        // 5. 获取需要被删除的考勤人员信息，在user_group_kaoqin_rel表中
        Set<String> needDeleteUserIds = new HashSet<>(user_group_kaoqin_ids);
        // 数据库现有人员信息 - 当前最新更新用户信息 = 数据库中有但新数据没有的考勤人员信息
        needDeleteUserIds.removeAll(currentUserIds);
        // 删除不是最新考勤组人员
        if(!needDeleteUserIds.isEmpty()){
            // 创建sql语句查询对应的 user_id 加 group_id 只获取 id
            QueryWrapper queryWrapper = QueryWrapper.create()
                    // 只查询id
                    .select(UserGroupKaoqinRel::getId)
                    // 筛选当前考勤组
                    .eq(UserGroupKaoqinRel::getGroupId, group_id)
                    // 筛选需要删除的用户ID列表
                    .in(UserGroupKaoqinRel::getUserId, needDeleteUserIds);
            List<Long> relIdList = userGroupKaoqinRelMapper.selectObjectListByQueryAs(queryWrapper, Long.class);
            // 通过获取到的id列表去批量删除
            // 直接物理删除
            userGroupKaoqinRelMapper.deleteBatchByids(relIdList);
            // 加入操作日志
            log.info("成功删除用户"+needDeleteUserIds);
        }

        // 6. 添加考勤人员信息到user_kaoqin表中
        // ps:数据表user_kaoqin表里的人员信息不需要做删除操作一直作为增加
        // 额外用户添加功能
        List<UserKaoqin> insertList = new ArrayList<>();
        for (UserKaoqinDTO userKaoqinDTO : resultList) {
            String userId = userKaoqinDTO.getUserId();
            if (!user_kaoqin_ids.contains(userId)) {
                UserKaoqin userKaoqin = new UserKaoqin();
                userKaoqin.setUserId(userId);
                userKaoqin.setCreateTime(LocalDateTime.now());
                userKaoqin.setUpdateTime(LocalDateTime.now());
                userKaoqin.setIsDeleted(false);
                result.add(userId);
                insertList.add(userKaoqin);
            }else{
                // 这里做一个跳过的日志
                log.info("无需要添加的用户在，user_kaoqin表中");
            }
        }
        // 插入列表中所有数据
        if (insertList.size() > 0) {
            userKaoqinMapper.insertBatch(insertList);
            log.info("添加user_kaoqin表信息成功，添加人员"+insertList);
        }

        // 7. 在user_group_kaoqin_rel表中，加入新加入的考勤人员信息
        Set<String> addUserIds = currentUserIds;

        // 需要加入的考勤人员 - 数据库中已经有的考勤人员 = 需要加入数据库中的考勤人员
        addUserIds.removeAll(user_group_kaoqin_ids);

        List<UserGroupKaoqinRel> updateList = new ArrayList<>();
        for (String userId : addUserIds) {
            UserGroupKaoqinRel userGroupKaoqinRel = new UserGroupKaoqinRel();
            userGroupKaoqinRel.setUserId(userId);
            userGroupKaoqinRel.setGroupId(group_id);
            userGroupKaoqinRel.setCreateTime(LocalDateTime.now());
            userGroupKaoqinRel.setUpdateTime(LocalDateTime.now());
            userGroupKaoqinRel.setIsDeleted(0);
            updateList.add(userGroupKaoqinRel);
        }

        if(updateList!=null&&updateList.size()>0){
            userGroupKaoqinRelMapper.insertBatch(updateList);
            log.info("成功添加表user_grou_kaoqin_rel表，用户id"+updateList);
        }

        // 返回更新后的user_kaoqin表中的用户idlist
        return result;
    }

    /**
     * 通过考勤组id获取用户id列表通用方法
     * @param group_id
     * @param accessToken
     * @param userId
     * @return
     */
    @Override
    public List<UserKaoqinDTO> getMemeberListId(String group_id, String accessToken, String userId) {

        // 1. 判断输入的参数
        ThrowUtils.throwIf(group_id == null || accessToken == null || userId == null,
                ErrorCode.PARAMS_ERROR,"获取考勤组人员失败，参数为空");

        // 2. 构成返回的考勤人员列表信息
        List<UserKaoqinDTO> resultList = new ArrayList<>();

        // 3. 调用钉钉API 请求，并调用请求获取数据进行处理
        String dingAPI = getMemberIdList+"?access_token="+accessToken;

        // 创建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("op_user_id", userId);
        requestBody.put("group_id", group_id);
        requestBody.put("cursor", 0);

        // 判断是否是否查询完所有信息
        boolean has_more=true;

        try{

            // 做判断是否全部读取，是否需要继续调用
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
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultList;
    }

    /**
     * 通过用户id列表查询所有用户名字并存入
     * @param idList 用户id列表
     * @return 返回用户id+用户名字
     */
    @Override
    public String insertUserName(List<String> idList,String accessToken) {

        // 1. 判断传入值是否为空
        ThrowUtils.throwIf(idList == null,ErrorCode.PARAMS_ERROR,"用户id列表参数为空");

        // 2. 判断idList长度，判断需要请求几次api接口，因为一次只能查询50个用户信息
        int count = 0;

        // 3. 循环处理需要获取的用户名字，
        // 并调用函数getUserNameList()调用API获取信息
        Map<String, String> userIdNameMap = new HashMap<>();
        List<String> insertList = new ArrayList<>();
        for(String userid: idList){
            if(count < 3){
                insertList.add(userid);
                count++;
            }else {
                insertList.add(userid);
                // 调用钉钉api接口，并解析存放入hashmap中
                String rawJsonResult = getUserNameList(insertList, "姓名", accessToken);
                parseUserNameResult(rawJsonResult, userIdNameMap);
                // 清空列表
                insertList.clear();
                count = 0;
            }
        }
        // 做一个不足3人的请求判断
        if(insertList.size()>0){
            String rawJsonResult = getUserNameList(insertList, "姓名", accessToken);
            parseUserNameResult(rawJsonResult, userIdNameMap);
            // 清空列表
            insertList.clear();
        }
        // 6. 将hashmap拆解成userKaoqinList ，存入数据库
        System.out.println("userIdNameMap = " + userIdNameMap);
        List<UserKaoqin> userkaoqinList = userIdNameMap.entrySet().stream()
                .map(entry -> {
                    UserKaoqin user = new UserKaoqin();
                    user.setUserId(entry.getKey());
                    user.setUserName(entry.getValue());
                    return user;
                })
                .toList();
        int row = 0;
        if(userkaoqinList.size() > 0){
            row = userKaoqinMapper.batchUpdateUserName(userkaoqinList);

        }

        // 7. 返回存入值
        return "成功修改行数：" + row ;
    }


    /**
     * 钉钉获取的用户信息存入到hashmap中
     * @param rawJsonResult 钉钉获取数据
     * @param userIdNameMap 需要修改的hashmap
     */
    private void parseUserNameResult(String rawJsonResult, Map<String, String> userIdNameMap) {
        // 1. 判断传入值
        ThrowUtils.throwIf(rawJsonResult == null,ErrorCode.PARAMS_ERROR,"钉钉获取用户信息失败，参数缺失");
        try{
            // 2. 处理传入的钉钉接口获取数据
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(rawJsonResult);

            int errcode = rootNode.path("errcode").asInt();
            boolean success = rootNode.path("success").asBoolean();

            if (errcode != 0 || !success) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "钉钉接口调用失败: " + rawJsonResult);
            }

            JsonNode resultNode = rootNode.path("result");

            if (resultNode.isArray()) {
                for (JsonNode userNode : resultNode) {
                    String userId = userNode.path("userid").asText();

                    JsonNode fieldList = userNode.path("field_list");
                    String userName = null;

                    if (fieldList.isArray()) {
                        for (JsonNode fieldNode : fieldList) {
                            String fieldName = fieldNode.path("field_name").asText();
                            String fieldCode = fieldNode.path("field_code").asText();

                            if ("姓名".equals(fieldName) || "sys00-name".equals(fieldCode)) {
                                userName = fieldNode.path("value").asText();
                                break;
                            }
                        }
                    }

                    if (userId != null && !userId.isEmpty() && userName != null && !userName.isEmpty()) {
                        userIdNameMap.put(userId, userName);
                    }
                }
            }

        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析钉钉返回数据失败: " + e.getMessage());
        }


        // 3. 将数据存入到hashmap中

    }

    /**
     * 钉钉获取用户名字通过userIdList
     * @param userIdList 需要查询的用户id列表
     * @param field_filter_list 需要查询的字段
     * @param accessToken 钉钉accees_token
     * @return 返回钉钉查询所有结果 String
     */
    public String getUserNameList(List<String> userIdList, String field_filter_list,String accessToken){

        // 1. 判断输入的值是否为空
        ThrowUtils.throwIf(userIdList == null || field_filter_list == null,
                ErrorCode.PARAMS_ERROR,"钉钉api获取用户名参数异常");
        // 2. 将获取的值处理构建请求体
        // 处理userIdList为["",""]形式
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userid_list", String.join(",", userIdList));
        requestBody.put("field_filter_list", field_filter_list);
        // 3. 拼接钉钉api接口url
        String dingAPI = getUserKaoqinInfoListUrl+"?access_token="+ accessToken;
        // 4. 发送请求获取原始数据
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(dingAPI, requestBody, String.class);
        // 5. 处理原始数据返回String类型
        String rawJsonResult = responseEntity.getBody();
        return rawJsonResult;
    }

    /**
     * 查询获取数据表中 user_id 用于存入数据表前查询避免重复插入
     * @return 返回 hashSet<> 类型
     */
    public Set<String> getCurrentUserIdSet(String group_id) {
        // 查询一次数据库，存入数组中，用于返回
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(UserGroupKaoqinRel::getUserId)
                .eq(UserGroupKaoqinRel::getGroupId, group_id);
        List<String> idList = userGroupKaoqinRelMapper.selectObjectListByQueryAs(queryWrapper, String.class);

        // 返回存入 HashSet
        return new HashSet<>(idList);

    }

    /**
     * 不带参数
     * @return
     */
    public Set<String> getCurrentUserIdSet() {
        // 查询一次数据库，存入数组中，用于返回
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(UserKaoqin::getUserId);
        List<String> idList = userKaoqinMapper.selectObjectListByQueryAs(queryWrapper, String.class);

        // 返回存入 HashSet
        return new HashSet<>(idList);
    }

    /**
     * 分页查询考勤人员信息根据groupid
     * @param idList
     * @param request
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapperByUserIdList(List<String> idList, UserKaoqinByGroupIdQuertRequest request) {

        // 1. 判断查询请求
        ThrowUtils.throwIf(request == null ,ErrorCode.PARAMS_ERROR,"分页查询请求参数为空");
        // 2. 构建查询语句
        QueryWrapper queryWrapper = new QueryWrapper();
        String userName = request.getUserName();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        // 必须条件：只查这个考勤组下的用户
        queryWrapper.in("user_id", idList, idList != null && !idList.isEmpty());
        queryWrapper.like(UserKaoqin::getUserName, userName, StrUtil.isNotBlank(userName));
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }
        // 3. 返回查询语句
        return queryWrapper;
    }

    /**
     * 获取封装后的考勤人员信息
     * @param records
     * @return
     */
    @Override
    public List<UserKaoqinVO> getuserKaoqinList(List<UserKaoqin> records) {
        if(CollUtil.isEmpty(records)){
            return new ArrayList<>();
        }
        //landa表达式
        return records.stream()
                .map(this::getUserKaoqinVO)
                .collect(Collectors.toList());
    }


    /**
     * 封装考勤人员信息
     * @param userKaoqin
     * @return
     */
    @Override
    public UserKaoqinVO getUserKaoqinVO(UserKaoqin userKaoqin) {
        if (userKaoqin == null) {
            return null;
        }

        UserKaoqinVO userKaoqinVO = new UserKaoqinVO();
        BeanUtils.copyProperties(userKaoqin, userKaoqinVO);

        return userKaoqinVO;
    }


}
