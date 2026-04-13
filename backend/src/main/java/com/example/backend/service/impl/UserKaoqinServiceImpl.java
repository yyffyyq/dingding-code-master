package com.example.backend.service.impl;

import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ThrowUtils;
import com.example.backend.mapper.UserGroupKaoqinRelMapper;
import com.example.backend.model.dto.UserKaoqinDTO;
import com.example.backend.model.dto.groupKaoqin.GroupKaoqinDTO;
import com.example.backend.model.entity.GroupKaoqin;
import com.example.backend.model.entity.SysUser;
import com.example.backend.model.entity.UserGroupKaoqinRel;
import com.example.backend.service.UserGroupKaoqinRelService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.example.backend.model.entity.UserKaoqin;
import com.example.backend.mapper.UserKaoqinMapper;
import com.example.backend.service.UserKaoqinService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  服务层实现。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@Service
public class UserKaoqinServiceImpl extends ServiceImpl<UserKaoqinMapper, UserKaoqin>  implements UserKaoqinService {

    @Resource
    private UserKaoqinMapper userKaoqinMapper;

    @Resource
    private UserGroupKaoqinRelService userGroupKaoqinRel;

    @Value("${dingtalk.getMemberIdList}")
    private String getMemberIdList;

    @Autowired
    // todo 这个resttemplate是干嘛的？
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserGroupKaoqinRelMapper userGroupKaoqinRelMapper;


    @Override
    public String insertGroupList(List<UserKaoqinDTO> resultList,String group_id) {
        // todo 这里需要修改一下，一个是把人员加入到user_kaoqin表中
        //  ，不要做删除操作
        //  ，然后把这个人的id加group_id去查询另一个表做存储判断

        ThrowUtils.throwIf(resultList == null, ErrorCode.PARAMS_ERROR);

        String result = "";

        // 根据 group_id 获取到对应的考勤组成员信息
        // user_group_kaoqin_rel表里的信息
        Set<String>  user_group_kaoqin_ids = getCurrentUserIdSet(group_id);

        // user_kaoqin表里的信息
        Set<String> user_kaoqin_ids = getCurrentUserIdSet();

        // 最新钉钉考勤组更新人员信息
        Set<String> currentUserIds = resultList.stream()
                .map(UserKaoqinDTO::getUserId)
                .collect(Collectors.toSet());

        // todo 这里需要修改成查询 user_group_kaoqin_revl 表，
        //  查询 group_id + user_id,
        // 数据库内所有人员的人员，还未被减
        Set<String> needDeleteUserIds = new HashSet<>(user_group_kaoqin_ids);

        // 减去最新考勤组人员获得多余人员信息
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

            result = "成功删除用户"+needDeleteUserIds;
            System.out.println("成功删除用户"+needDeleteUserIds);
        }

        // todo 这里做添加用户需要两个地方的添加，一个是添加到user_kaoqin表一个是user_group_kaoqin_rel
        // 这里的员工信息是一直作为增加
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
                insertList.add(userKaoqin);
            }else{
                // 这里做一个跳过的日志
            }
        }
        // 插入列表中所有数据
        if (insertList.size() > 0) {
            userKaoqinMapper.insertBatch(insertList);
        }

        result = result + "成功添加用户"+insertList;
        System.out.println("成功添加用户"+insertList);

        // todo 这里做一个user_group_kaoqin_rel表的插入语句

        Set<String> addUserIds = currentUserIds;

        // 发过来的idList - 数据库查询到的
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
        }

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

        List<UserKaoqinDTO> resultList = new ArrayList<>();

        // 拼接 钉钉API 请求，并调用请求获取数据进行处理
        String dingAPI = getMemberIdList+"?access_token="+accessToken;

        // 创建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("op_user_id", userId);
        requestBody.put("group_id", group_id);
        requestBody.put("cursor", 0);

        boolean has_more=true;

        //调用钉钉API
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
}
