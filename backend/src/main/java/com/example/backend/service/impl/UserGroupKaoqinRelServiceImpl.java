package com.example.backend.service.impl;

import com.example.backend.model.entity.GroupKaoqin;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.example.backend.model.entity.UserGroupKaoqinRel;
import com.example.backend.mapper.UserGroupKaoqinRelMapper;
import com.example.backend.service.UserGroupKaoqinRelService;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  服务层实现。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@Service
public class UserGroupKaoqinRelServiceImpl extends ServiceImpl<UserGroupKaoqinRelMapper, UserGroupKaoqinRel>  implements UserGroupKaoqinRelService{


    @Autowired
    private UserGroupKaoqinRelMapper userGroupKaoqinRelMapper;

    /**
     * 获取考勤组id列表
     * @param groupId 考勤组id
     * @return
     */
    @Override
    public List<String> getIdListByGroupId(String groupId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(UserGroupKaoqinRel::getUserId)
                .eq(UserGroupKaoqinRel::getGroupId, groupId);
        return userGroupKaoqinRelMapper.selectObjectListByQueryAs(queryWrapper, String.class);
    }
}
