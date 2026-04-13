package com.example.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.example.backend.model.entity.UserKaoqin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  映射层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
public interface UserKaoqinMapper extends BaseMapper<UserKaoqin> {

    /**
     * 批量物理删除考勤组人员
     * @param userIds
     */
    void deleteBatchByUserIds(@Param("userIds")List<String> userIds);

}
