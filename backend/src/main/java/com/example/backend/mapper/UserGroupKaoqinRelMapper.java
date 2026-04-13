package com.example.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.example.backend.model.entity.UserGroupKaoqinRel;

import java.util.List;

/**
 *  映射层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
public interface UserGroupKaoqinRelMapper extends BaseMapper<UserGroupKaoqinRel> {

    /**
     * 删除用户信息根据Id列表物理删除
     * @param relIdList id列表
     */
    void deleteBatchByids(List<Long> relIdList);
}
