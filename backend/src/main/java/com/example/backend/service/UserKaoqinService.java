package com.example.backend.service;

import com.example.backend.model.dto.UserKaoqinDTO;
import com.example.backend.model.entity.SysUser;
import com.mybatisflex.core.service.IService;
import com.example.backend.model.entity.UserKaoqin;

import java.util.List;

/**
 *  服务层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
public interface UserKaoqinService extends IService<UserKaoqin> {


    String insertGroupList(List<UserKaoqinDTO> resultList,String group_id);

    /**
     * 通过考勤组id获取用户id列表通用方法
     * @param groupId
     * @param accessToken
     * @param userId
     * @return
     */
    List<UserKaoqinDTO> getMemeberListId(String groupId, String accessToken, String userId);
}
