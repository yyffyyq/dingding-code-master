package com.example.backend.service;

import com.example.backend.model.dto.UserKaoqinDTO;
import com.mybatisflex.core.service.IService;
import com.example.backend.model.entity.UserKaoqin;

import java.util.List;

/**
 *  服务层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
public interface UserKaoqinService extends IService<UserKaoqin> {


    /**
     * 获取用户信息通过考勤组id并存入数据库
     * @param resultList 需要插入的考勤人员请求列表
     * @param group_id 考勤组id
     * @return 插入后的考勤人员id列表
     */
    List<String> insertGroupList(List<UserKaoqinDTO> resultList,String group_id);

    /**
     * 通过考勤组id获取用户id列表通用方法
     * @param groupId 考勤组id
     * @param accessToken 通行token
     * @param userId 操作员id
     * @return 返回需要插入的考勤人员列表
     */
    List<UserKaoqinDTO> getMemeberListId(String groupId, String accessToken, String userId);

    /**
     * 通过用户id列表查询所有用户名字并存入
     * @param idList
     * @return
     */
    String insertUserName(List<String> idList,String accessToken);

}
