package com.example.backend.service.impl;

import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ThrowUtils;
import com.example.backend.model.dto.UserKaoqinDTO;
import com.example.backend.model.dto.groupKaoqin.GroupKaoqinDTO;
import com.example.backend.model.entity.GroupKaoqin;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.example.backend.model.entity.UserKaoqin;
import com.example.backend.mapper.UserKaoqinMapper;
import com.example.backend.service.UserKaoqinService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *  服务层实现。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@Service
public class UserKaoqinServiceImpl extends ServiceImpl<UserKaoqinMapper, UserKaoqin>  implements UserKaoqinService {

    @Resource
    private UserKaoqinMapper userKaoqinMapper;

    @Override
    public Integer insertGroupList(List<UserKaoqinDTO> resultList) {

        ThrowUtils.throwIf(resultList == null, ErrorCode.PARAMS_ERROR);

        Set<String> existingIds = getExistingUserIdSet();
        List<UserKaoqin> insertList = new ArrayList<>();
        for (UserKaoqinDTO userKaoqinDTO : resultList) {
            String userId = userKaoqinDTO.getUserId();
            if (!existingIds.contains(userId)) {
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
        Integer result = 0;
        // 插入列表中所有数据
        if (insertList.size() > 0) {
            result = userKaoqinMapper.insertBatch(insertList);
        }
        return result;
    }
    /**
     * 查询获取数据表中 user_id 用于存入数据表前查询避免重复插入
     * @return 返回 hashSet<> 类型
     */
    public Set<String> getExistingUserIdSet() {
        // 查询一次数据库，存入数组中，用于返回
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(UserKaoqin::getUserId);
        List<String> idList = userKaoqinMapper.selectObjectListByQueryAs(queryWrapper, String.class);

        // 返回存入 HashSet
        return new HashSet<>(idList);

    }
}
