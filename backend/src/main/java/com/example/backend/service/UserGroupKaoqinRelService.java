package com.example.backend.service;

import com.mybatisflex.core.service.IService;
import com.example.backend.model.entity.UserGroupKaoqinRel;

import java.util.List;

/**
 *  服务层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
public interface UserGroupKaoqinRelService extends IService<UserGroupKaoqinRel> {

    /**
     * 获取考勤人员id列表
     * @param groupId 考勤组id
     * @return
     */
    List<String> getIdListByGroupId(String groupId);
}
